package com.kyang.ftpclient.commands;

import android.net.Uri;

/**
 * @author kyang
 */
public interface FileCommand {
    /**
     * To implement function of download file from server
     * and upload to server
     *
     * @param uri  of SAF
     * @param path params for Download and Upload
     */
    void execute(Uri uri, String path);
}
