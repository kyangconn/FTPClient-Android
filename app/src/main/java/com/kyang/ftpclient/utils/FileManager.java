package com.kyang.ftpclient.utils;

import com.kyang.ftpclient.R;

import java.util.HashMap;
import java.util.Map;

/**
 * @author kyang
 */
public class FileManager {
    private static final Map<String, Integer> FILE_TYPE_ICONS = new HashMap<>();
    private static final Map<String, String> FILE_MIME_TYPE = new HashMap<>();

    static {
        FILE_TYPE_ICONS.put("7z", R.drawable.ic_normal_white_grid_7z);
        FILE_TYPE_ICONS.put("amr", R.drawable.ic_normal_white_grid_amr);
        FILE_TYPE_ICONS.put("ape", R.drawable.ic_normal_white_grid_ape);
        FILE_TYPE_ICONS.put("apk", R.drawable.ic_normal_white_grid_applications);
        FILE_TYPE_ICONS.put("hap", R.drawable.ic_normal_white_grid_applications);
        FILE_TYPE_ICONS.put("xapk", R.drawable.ic_normal_white_grid_applications);
        FILE_TYPE_ICONS.put("chm", R.drawable.ic_normal_white_grid_chm);
        FILE_TYPE_ICONS.put("doc", R.drawable.ic_normal_white_grid_doc);
        FILE_TYPE_ICONS.put("flac", R.drawable.ic_normal_white_grid_flac);
        FILE_TYPE_ICONS.put("txt", R.drawable.ic_normal_white_grid_txt);
        FILE_TYPE_ICONS.put("html", R.drawable.ic_normal_white_grid_html);
        FILE_TYPE_ICONS.put("log", R.drawable.ic_normal_white_grid_log);
        FILE_TYPE_ICONS.put("m4a", R.drawable.ic_normal_white_grid_m4a);
        FILE_TYPE_ICONS.put("mp3", R.drawable.ic_normal_white_grid_mp3);
        FILE_TYPE_ICONS.put("pdf", R.drawable.ic_normal_white_grid_pdf);
        FILE_TYPE_ICONS.put("pptx", R.drawable.ic_normal_white_grid_pptx);
        FILE_TYPE_ICONS.put("rar", R.drawable.ic_normal_white_grid_rar);
        FILE_TYPE_ICONS.put("wav", R.drawable.ic_normal_white_grid_wav);
        FILE_TYPE_ICONS.put("wma", R.drawable.ic_normal_white_grid_wma);
        FILE_TYPE_ICONS.put("xls", R.drawable.ic_normal_white_grid_xls);
        FILE_TYPE_ICONS.put("xml", R.drawable.ic_normal_white_grid_xml);
        FILE_TYPE_ICONS.put("zip", R.drawable.ic_normal_white_grid_zip);
        FILE_TYPE_ICONS.put("avi", R.drawable.ic_normal_white_grid_video);
        FILE_TYPE_ICONS.put("flv", R.drawable.ic_normal_white_grid_video);
        FILE_TYPE_ICONS.put("mov", R.drawable.ic_normal_white_grid_video);
        FILE_TYPE_ICONS.put("mp4", R.drawable.ic_normal_white_grid_video);
        FILE_TYPE_ICONS.put("wmv", R.drawable.ic_normal_white_grid_video);
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

    public static int getIcon(String fileType) {
        return FILE_TYPE_ICONS.getOrDefault(fileType, R.drawable.ic_normal_white_grid_unknown);
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
