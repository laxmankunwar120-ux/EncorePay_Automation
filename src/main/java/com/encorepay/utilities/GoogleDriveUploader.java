package com.encorepay.utilities;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.Permission;

public final class GoogleDriveUploader {

    private static final String APPLICATION_NAME = "EncorePay Automation";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private GoogleDriveUploader() {
    }

    /**
     * Uploads a file to Google Drive and returns the shareable URL
     * 
     * @param filePath Path to the file to upload
     * @param folderId Google Drive folder ID where the file should be uploaded (can be empty for root folder)
     * @param credentialsPath Path to the service account JSON credentials file
     * @return Shareable URL of the uploaded file
     * @throws IOException if upload fails
     */
    public static String uploadFileAndGetShareableUrl(
            String filePath,
            String folderId,
            String credentialsPath) throws IOException {

        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("File path must not be blank.");
        }
        if (credentialsPath == null || credentialsPath.isBlank()) {
            throw new IllegalArgumentException("Credentials path must not be blank.");
        }

        java.io.File fileToUpload = new java.io.File(filePath);
        if (!fileToUpload.exists()) {
            throw new IllegalArgumentException("File does not exist: " + filePath);
        }

        // Build Drive service
        Drive driveService = getDriveService(credentialsPath);

        // Create file metadata
        com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
        fileMetadata.setName(fileToUpload.getName());
        
        // Only set parent if folderId is provided
        if (folderId != null && !folderId.isBlank()) {
            fileMetadata.setParents(Collections.singletonList(folderId));
        }

        // Set file content
        String mimeType = "application/pdf";
        FileContent mediaContent = new FileContent(mimeType, fileToUpload);

        try {
            // Upload file to Shared Drive
            com.google.api.services.drive.model.File uploadedFile = driveService.files()
                .create(fileMetadata, mediaContent)
                .setSupportsAllDrives(true)
                .setFields("id")
                .execute();

            String fileId = uploadedFile.getId();
            System.out.println("[DEBUG] Google Drive file uploaded successfully. File ID: " + fileId);

            // Make file publicly readable
            Permission permission = new Permission();
            permission.setType("anyone");
            permission.setRole("reader");

            driveService.permissions()
                .create(fileId, permission)
                .setSupportsAllDrives(true)
                .execute();

            System.out.println("[DEBUG] Google Drive permission set successfully for file ID: " + fileId);

            // Generate shareable URL
            return "https://drive.google.com/file/d/" + fileId + "/view";

        } catch (GoogleJsonResponseException e) {
            System.err.println("[ERROR] Google Drive upload failed. Status Code: " + e.getStatusCode());
            System.err.println("[ERROR] Google Drive upload failed. Content: " + e.getContent());
            System.err.println("[ERROR] Google Drive upload failed. Details: " + (e.getDetails() != null ? e.getDetails().toPrettyString() : "N/A"));
            throw new IOException("Google Drive API error: " + 
                (e.getDetails() != null && e.getDetails().getMessage() != null ? e.getDetails().getMessage() : e.getMessage()), e);
        } catch (Exception e) {
            System.err.println("[ERROR] Google Drive upload failed. Exception: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Google Drive upload failed: " + e.getMessage(), e);
        }
    }

    /**
     * Creates and returns a Drive service authenticated with service account credentials
     */
    private static Drive getDriveService(String credentialsPath) throws IOException {
        try {
            NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

            // Load credentials from JSON file
            InputStream credentialsStream = new FileInputStream(credentialsPath);
            GoogleCredential credential = GoogleCredential.fromStream(credentialsStream)
                .createScoped(Collections.singletonList(DriveScopes.DRIVE));

            return new Drive.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
        } catch (java.security.GeneralSecurityException e) {
            throw new IOException("Failed to create HTTP transport for Google Drive", e);
        }
    }
}
