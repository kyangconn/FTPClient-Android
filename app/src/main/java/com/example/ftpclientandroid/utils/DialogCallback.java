package com.example.ftpclientandroid.utils;

/**
 * @author kyang
 */
public interface DialogCallback {
    /**give Result from MainActivity.authenticationDialog
     * to MainActivity for create ftp connection
     *
     * @param result result of Authentication
     *
     */
    void onResult(boolean result);
}
