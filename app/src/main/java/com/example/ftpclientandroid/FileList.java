package com.example.ftpclientandroid;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import org.apache.commons.net.ftp.FTPFile;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ThreadPoolExecutor;

enum FileOperation {
    UPLOAD,
    DOWNLOAD,
    COPY,
    MOVE
}

/**
 * @author kyang
 */
public class FileList extends AppCompatActivity {
    private final FTPConnection ftpConnection = FTPConnection.getCurrentInstance();
    private FTPFile selectFile;
    private FileManager fileManager;
    private ThreadPoolExecutor threadPoolExecutor;
    private LinearLayout container;
    private String currentPath = "/";
    private ActivityResultLauncher<Intent> createFileLauncher;
    private FileOperation currentOperation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_management);

        String titleName = getIntent().getStringExtra("serverName");
        TextView title = findViewById(R.id.titleBar);
        title.setText(titleName);

        container = findViewById(R.id.filelist);
        ImageButton back = findViewById(R.id.back);
        back.setOnClickListener(view -> backToPre());
        ImageButton upload = findViewById(R.id.file_add);
        upload.setOnClickListener(v -> uploadFile());
        ImageButton search = findViewById(R.id.search);
        search.setOnClickListener(view -> search());

        threadPoolExecutor = ThreadPoolManager.getInstance();

        fileManager = new FileManager(this, ftpConnection, getContentResolver(), threadPoolExecutor);
        createFileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::handleCreateFileResult
        );

        if (ftpConnection != null) {
            refreshFileList(currentPath);
        }
    }

    @Override
    public void onBackPressed() {
        if ("/".equals(currentPath)) {
            if (ftpConnection != null && ftpConnection.isConnected()) {
                ftpConnection.disconnect();
            }
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            int lastIndex = currentPath.lastIndexOf('/');
            currentPath = (lastIndex > 0) ? currentPath.substring(0, lastIndex) : "/";
            refreshFileList(currentPath);
        }
    }

    private void handleCreateFileResult(ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_OK) {
            Intent data = result.getData();
            if (data != null) {
                Uri uri = data.getData();
                switch (currentOperation) {
                    case UPLOAD:
                        fileManager.uploadFile(uri, currentPath);
                        break;
                    case DOWNLOAD:
                        if (selectFile != null) {
                            String fullPath = currentPath.endsWith("/") ? currentPath + selectFile.getName() : currentPath + "/" + selectFile.getName();
                            fileManager.downloadFile(uri, fullPath);
                        }
                        break;
                    case COPY:
                        break;
                    case MOVE:
                        break;
                    default: {
                    }
                }
            }
        }
    }

    public void startFileCreateIntent(FTPFile file) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/octet-stream");
        intent.putExtra(Intent.EXTRA_TITLE, file.getName());
        createFileLauncher.launch(intent);
    }

    private void refreshFileList(String directoryPath) {
        // 确保路径以 "/" 结尾
        currentPath = currentPath.endsWith("/") ? currentPath : currentPath + "/";
        currentPath += "/".equals(directoryPath) ? "" : directoryPath;

        threadPoolExecutor.execute(() -> {
            try {
                FTPFile[] files = ftpConnection.listFiles(currentPath);
                runOnUiThread(() -> updateFileListView(files));
            } catch (IOException e) {
                Log.e("refreshFileList", "Error: " + e.getMessage(), e);
                runOnUiThread(() -> Toast.makeText(this, "加载文件列表失败: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void backToPre() {
        super.onBackPressed();
    }

    private void uploadFile() {
        currentOperation = FileOperation.UPLOAD;
        // 启动文件选择器
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        createFileLauncher.launch(intent);
    }

    private void search() {
        //TODO in Could of MoSCoW
    }

    private void updateFileListView(FTPFile[] files) {
        LayoutInflater inflater = LayoutInflater.from(this);
        container.removeAllViews();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        for (FTPFile file : files) {
            View fileView = inflater.inflate(R.layout.file_list_element, container, false);
            Calendar lastModified = file.getTimestamp();
            String formattedDate = dateFormat.format(lastModified.getTime());

            ImageView icon = fileView.findViewById(R.id.file_icon);
            TextView name = fileView.findViewById(R.id.file_name);
            TextView time = fileView.findViewById(R.id.file_time);

            icon.setImageResource(getIconBasedOnFileType(file));
            name.setText(file.getName());
            time.setText(String.format("Last Modified: %s", formattedDate));

            fileView.setOnClickListener(v -> clickFile(file));
            fileView.setOnLongClickListener(v -> pressFile(file));

            // 可以在这里为fileView设置点击监听器等
            container.addView(fileView);
        }
    }

    private int getIconBasedOnFileType(FTPFile file) {
        if (file.isDirectory()) {
            return R.drawable.ic_normal_white_grid_folder;
        } else {
            String fileName = file.getName();
            int i = fileName.lastIndexOf('.');
            if (i > 0) {
                String extension = fileName.substring(i + 1).toLowerCase();
                return FileIconManager.getIcon(extension);
            } else {
                return R.drawable.ic_normal_white_grid_unknown;
            }
        }
    }

    private void clickFile(FTPFile file) {
        if (file.isDirectory()) {
            refreshFileList(file.getName());
            try {
                ftpConnection.changeWorkingDirectory(file.getName());
            } catch (IOException e) {
                Log.e("changeDir", "ftp.changeWorkingDir", e);
            }
        } else {
            selectFile = file;
            currentOperation = FileOperation.DOWNLOAD;
            startFileCreateIntent(file);
        }
    }

    private boolean pressFile(FTPFile file) {
        LinearLayout dock = findViewById(R.id.dock);
        ImageButton download = findViewById(R.id.dock_download);
        ImageButton copy = findViewById(R.id.dock_copy);
        ImageButton move = findViewById(R.id.dock_move);
        ImageButton delete = findViewById(R.id.dock_delete);
        selectFile = file;

        download.setOnClickListener(v -> {
            currentOperation = FileOperation.DOWNLOAD;
            startFileCreateIntent(file);
            dock.setVisibility(View.GONE);
        });
        copy.setOnClickListener(v -> {
            dockCopy();
            dock.setVisibility(View.GONE);
        });
        move.setOnClickListener(v -> {
            currentOperation = FileOperation.MOVE;
            dock.setVisibility(View.GONE);
        });
        delete.setOnClickListener(v -> {
            try {
                ftpConnection.deleteFile(file.getName());
            } catch (IOException e) {
                Log.e("delete", "delete file error", e);
                throw new RuntimeException(e);
            }
            dock.setVisibility(View.GONE);
        });

        dock.setVisibility(View.VISIBLE);
        return true;
    }

    private void dockCopy() {
        //TODO in Could of MoSCoW
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (threadPoolExecutor != null && !threadPoolExecutor.isShutdown()) {
            threadPoolExecutor.shutdown();
        }
    }
}
