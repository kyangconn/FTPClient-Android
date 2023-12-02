package com.example.ftpclientandroid;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.documentfile.provider.DocumentFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author kyang
 */
public class FileManager {
    private final Context context;

    private final FTPConnection ftpConnection;
    private final ContentResolver contentResolver;
    private final ThreadPoolExecutor threadPoolExecutor;

    public FileManager(Context context, FTPConnection ftpConnection, ContentResolver contentResolver, ThreadPoolExecutor threadPoolExecutor) {
        this.context = context;
        this.contentResolver = contentResolver;
        this.threadPoolExecutor = threadPoolExecutor;
        this.ftpConnection = ftpConnection;
    }

    public void downloadFile(Uri uri, String fullPath) {
        threadPoolExecutor.execute(() -> {
            try (InputStream inputStream = ftpConnection.retrieveFileStream(fullPath);
                 OutputStream outputStream = context.getContentResolver().openOutputStream(uri)) {
                if (inputStream != null && outputStream != null) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    inputStream.close();
                    if (ftpConnection.completePendingCommand()) {
                        ((Activity) context).runOnUiThread(() -> Toast.makeText(context, "文件下载成功", Toast.LENGTH_SHORT).show());
                    }
                }
            } catch (IOException e) {
                Log.e("DownloadFile", "Error: " + e.getMessage(), e);
                throw new RuntimeException(e);
            }
        });
    }


    public void uploadFile(Uri uri, String currentPath) {
        threadPoolExecutor.execute(() -> {
            try (InputStream inputStream = contentResolver.openInputStream(uri)) {
                if (inputStream != null) {
                    String fileName = getFileNameFromUri(uri);
                    ftpConnection.storeFile(currentPath + fileName, inputStream);
                    ((Activity) context).runOnUiThread(() -> Toast.makeText(context, "文件上传成功", Toast.LENGTH_SHORT).show());
                }
            } catch (IOException e) {
                Log.e("UploadFile", "Error: " + e.getMessage(), e);
                throw new RuntimeException(e);
            }
        });
    }

    private String getFileNameFromUri(Uri uri) {
        if (context == null || uri == null) {
            return null;
        }
        DocumentFile documentFile = DocumentFile.fromSingleUri(context, uri);
        if (documentFile != null) {
            return documentFile.getName();
        }
        return null;
    }
}
