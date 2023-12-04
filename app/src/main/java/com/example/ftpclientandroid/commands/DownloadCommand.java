package com.example.ftpclientandroid.commands;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.example.ftpclientandroid.utils.FtpManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author kyang
 */
public class DownloadCommand implements FileCommand{
    private final Context context;
    private final FtpManager ftpManager;
    private final ContentResolver contentResolver;
    private final ThreadPoolExecutor threadPoolExecutor;

    public DownloadCommand(Context context, FtpManager ftpManager, ContentResolver contentResolver, ThreadPoolExecutor threadPoolExecutor) {
        this.context = context;
        this.ftpManager = ftpManager;
        this.contentResolver = contentResolver;
        this.threadPoolExecutor = threadPoolExecutor;
    }

    @Override
    public void execute(Uri uri, String fullPath) {
        threadPoolExecutor.execute(() -> {
            try (InputStream inputStream = ftpManager.retrieveFileStream(fullPath);
                 OutputStream outputStream = context.getContentResolver().openOutputStream(uri)) {
                if (inputStream != null && outputStream != null) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    inputStream.close();
                    if (ftpManager.completePendingCommand()) {
                        ((Activity) context).runOnUiThread(() -> Toast.makeText(context, "文件下载成功", Toast.LENGTH_SHORT).show());
                    }
                }
            } catch (IOException e) {
                Log.e("DownloadFile", "Error: " + e.getMessage(), e);
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void execute(Uri uri, String fromPath, String toPath) {}
}
