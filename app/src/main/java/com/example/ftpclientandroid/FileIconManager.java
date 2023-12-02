package com.example.ftpclientandroid;

import java.util.HashMap;
import java.util.Map;

public class FileIconManager {
    private static final Map<String, Integer> FILE_TYPE_ICONS = new HashMap<>();

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
    }

    public static int getIcon(String fileType) {
        return FILE_TYPE_ICONS.getOrDefault(fileType, R.drawable.ic_normal_white_grid_unknown);
    }
}
