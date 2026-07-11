package com.encorepay.utilities.helpers;

/**
 * Centralized utility for text sanitization and normalization operations.
 * Removes duplicate text processing logic from multiple classes.
 */
public final class TextHelper {

    private TextHelper() {
        // Utility class - prevent instantiation
    }

    /**
     * Normalizes text by removing extra whitespace and trimming.
     * 
     * @param value the text to normalize
     * @return normalized text, or empty string if input is null
     */
    public static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("\\s+", " ").trim();
    }

    /**
     * Sanitizes text by removing common UI artifacts and normalizing whitespace.
     * Removes Material Design icon names and other UI noise.
     * 
     * @param value the text to sanitize
     * @return sanitized text, or empty string if input is null
     */
    public static String sanitize(String value) {
        if (value == null) {
            return "";
        }
        return value
            .replace("do_not_disturb_on", "")
            .replace("content_copy", "")
            .replace("close", "")
            .replaceAll("\\s+", " ")
            .trim();
    }

    /**
     * Sanitizes text for verification details with length limit.
     * 
     * @param value the text to sanitize
     * @return sanitized text truncated to 320 characters if needed
     */
    public static String sanitizeDetail(String value) {
        String s = sanitize(value);
        return s.length() > 320 ? s.substring(0, 317).trim() + "..." : s;
    }

    /**
     * Sanitizes text for use in file names.
     * Removes invalid characters and replaces spaces with underscores.
     * 
     * @param value the text to sanitize for file name
     * @return sanitized file name
     */
    public static String sanitizeFileName(String value) {
        String s = value == null ? "screenshot" : value
            .replaceAll("[\\\\/:*?\"<>|\\r\\n]+", "_")
            .replaceAll("\\s+", "_")
            .trim();
        if (s.isBlank()) {
            s = "screenshot";
        }
        return s.length() > 80 ? s.substring(0, 80).trim() : s;
    }

    /**
     * Checks if a string is blank (null, empty, or whitespace only).
     * 
     * @param value the string to check
     * @return true if blank, false otherwise
     */
    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * Returns the first non-blank value from the provided options.
     * 
     * @param values array of string values to check
     * @return first non-blank value, or empty string if all are blank
     */
    public static String firstNonBlank(String... values) {
        for (String v : values) {
            if (!isBlank(v)) {
                return v;
            }
        }
        return "";
    }
}
