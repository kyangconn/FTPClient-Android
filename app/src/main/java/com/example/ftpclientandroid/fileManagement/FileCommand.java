package com.example.ftpclientandroid.fileManagement;

import android.net.Uri;

/**
 * @author kyang
 */
public interface FileCommand {
    /**To implement function of download file from server
     * and upload to server
     *
     * @param uri of SAF
     * @param path params for Download and Upload
     *
     */
    void execute(Uri uri, String path);

    /**For function Move and Copy
     * They need two paths params to operate
     *
     * @param uri of SAF
     * @param fromPath params of FTP.moveFile
     * @param toPath params of FTP.moveFile
     */
    void execute(Uri uri, String fromPath, String toPath);
}
