package com.encorepay.downloader;

import java.io.IOException;
import java.nio.file.Path;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.encorepay.config.ConfigLoader;
import com.encorepay.utilities.DownloadFileUtil;

public class UNDownloader {

    private final ConfigLoader config;
    private final DownloadFileUtil downloadFileUtil;

    public UNDownloader() {
        this.config = ConfigLoader.getInstance();
        this.downloadFileUtil = new DownloadFileUtil();
    }

}