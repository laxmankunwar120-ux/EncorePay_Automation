package com.encorepay;

import java.nio.file.Files;
import java.nio.file.Path;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.encorepay.downloader.UNDownloader;

public class UNDownloaderTest {

    @Test(description = "Download the latest UN Security Council consolidated XML")
    public void downloadLatestXml() throws Exception {
        UNDownloader downloader = new UNDownloader();

        Path downloadedFile = downloader.downloadLatestXml();

        Assert.assertTrue(Files.exists(downloadedFile),
                "Downloaded consolidated.xml does not exist at: " + downloadedFile);
        Assert.assertTrue(Files.size(downloadedFile) > 0,
                "Downloaded consolidated.xml is empty: " + downloadedFile);

        System.out.println("Downloaded UN Consolidated XML -> " + downloadedFile.toAbsolutePath());
    }
}
