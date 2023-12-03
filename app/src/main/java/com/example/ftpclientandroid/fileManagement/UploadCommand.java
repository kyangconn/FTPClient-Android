package com.example.ftpclientandroid.fileManagement;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.documentfile.provider.DocumentFile;

import com.example.ftpclientandroid.utils.FtpManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ThreadPoolExecutor;

public class UploadCommand implements FileCommand{
    private final Context context;
    private final FtpManager ftpManager;
    private final ContentResolver contentResolver;
    private final ThreadPoolExecutor threadPoolExecutor;

    public UploadCommand(Context context, FtpManager ftpManager, ContentResolver contentResolver, ThreadPoolExecutor threadPoolExecutor) {
        this.context = context;
        this.ftpManager = ftpManager;
        this.contentResolver = contentResolver;
        this.threadPoolExecutor = threadPoolExecutor;
    }

    @Override
    public void execute(Uri uri, String path) {
        threadPoolExecutor.execute(() -> {
            try (InputStream inputStream = contentResolver.openInputStream(uri)) {
                DocumentFile documentFile = DocumentFile.fromSingleUri(context, uri);
                if (inputStream != null && documentFile != null) {
                    String fileName = documentFile.getName();
                    ftpManager.storeFile(path + fileName, inputStream);
                    ((Activity) context).runOnUiThread(() -> Toast.makeText(context, "文件上传成功", Toast.LENGTH_SHORT).show());
                }
            } catch (IOException e) {
                Log.e("UploadFile", "Error: " + e.getMessage(), e);
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void execute(Uri uri, String fromPath, String toPath) {}
}
