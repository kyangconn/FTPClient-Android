package com.example.ftpclientandroid;

import java.util.HashMap;
import java.util.Map;

public class FileMimeManager {
    private static final Map<String, String> FILE_MIME_TYPE = new HashMap<>();

    static {
        FILE_MIME_TYPE.put("3gp", "video/3gpp");
        FILE_MIME_TYPE.put("apk", "application/vnd.android.package-archive");
        FILE_MIME_TYPE.put("avi", "video/x-msvideo");
        FILE_MIME_TYPE.put("bmp", "image/bmp");
        FILE_MIME_TYPE.put("css", "text/css");
        FILE_MIME_TYPE.put("dhtml", "text/html");
        FILE_MIME_TYPE.put("doc", "application/msword");
        FILE_MIME_TYPE.put("gif", "image/gif");
        FILE_MIME_TYPE.put("htm", "text/html");
        FILE_MIME_TYPE.put("html", "text/html");
        FILE_MIME_TYPE.put("jpeg", "image/jpeg");
        FILE_MIME_TYPE.put("jpg", "image/jpeg");
        FILE_MIME_TYPE.put("js", "application/x-javascript");
        FILE_MIME_TYPE.put("mov", "video/quicktime");
        FILE_MIME_TYPE.put("movie", "video/x-sgi-movie");
        FILE_MIME_TYPE.put("mp2", "audio/x-mpeg");
        FILE_MIME_TYPE.put("mp3", "audio/x-mpeg");
        FILE_MIME_TYPE.put("mp4", "video/mp4");
        FILE_MIME_TYPE.put("mpe", "video/mpeg");
        FILE_MIME_TYPE.put("mpeg", "video/mpeg");
        FILE_MIME_TYPE.put("mpg", "video/mpeg");
        FILE_MIME_TYPE.put("mpg4", "video/mp4");
        FILE_MIME_TYPE.put("pdf", "application/pdf");
        FILE_MIME_TYPE.put("png", "image/png");
        FILE_MIME_TYPE.put("ppt", "application/vnd.ms-powerpoint");
        FILE_MIME_TYPE.put("qt", "video/quicktime");
        FILE_MIME_TYPE.put("qti", "image/x-quicktime");
        FILE_MIME_TYPE.put("qtif", "image/x-quicktime");
        FILE_MIME_TYPE.put("rar", "application/x-rar-compressed");
        FILE_MIME_TYPE.put("svg", "image/svg-xml");
        FILE_MIME_TYPE.put("tar", "application/x-tar");
        FILE_MIME_TYPE.put("txt", "text/plain");
        FILE_MIME_TYPE.put("wav", "audio/x-wav");
        FILE_MIME_TYPE.put("xhtml", "application/xhtml+xml");
        FILE_MIME_TYPE.put("xls", "application/vnd.ms-excel");
        FILE_MIME_TYPE.put("xml", "text/xml");
        FILE_MIME_TYPE.put("zip", "application/zip");
    }

    public static String getMimeType(String filename) {
        int i = filename.lastIndexOf('.');
        String extension = null;
        if (i > 0) {
            extension = filename.substring(i + 1).toLowerCase();
        }
        return FILE_MIME_TYPE.getOrDefault(extension, "application/octet-stream");
    }
}
