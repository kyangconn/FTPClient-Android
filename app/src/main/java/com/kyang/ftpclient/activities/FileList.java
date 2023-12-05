package com.kyang.ftpclient.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.kyang.ftpclient.R;
import com.kyang.ftpclient.commands.DownloadCommand;
import com.kyang.ftpclient.commands.FileCommand;
import com.kyang.ftpclient.commands.UploadCommand;
import com.kyang.ftpclient.utils.FileManager;
import com.kyang.ftpclient.utils.FtpManager;
import com.kyang.ftpclient.utils.ThreadManager;

import org.apache.commons.net.ftp.FTPFile;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author kyang
 */
public class FileList extends AppCompatActivity {
    private final FtpManager ftpManager = FtpManager.getCurrentInstance();
    private FTPFile selectFile;
    private ThreadPoolExecutor threadPoolExecutor;
    private ActivityResultLauncher<Intent> createFileLauncher;
    private LinearLayout container;
    private String currentPath = "/";
    private FileCommand currentCommand;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_list);

        threadPoolExecutor = ThreadManager.getInstance();
        createFileLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::handleCreateFileResult);

        String titleName = getIntent().getStringExtra("serverName");
        TextView title = findViewById(R.id.title);
        title.setText(titleName);

        container = findViewById(R.id.filelist);

        ImageButton back = findViewById(R.id.back);
        back.setOnClickListener(view -> onBackPressed());

        ImageButton upload = findViewById(R.id.file_add);
        upload.setOnClickListener(v -> {
            currentCommand = new UploadCommand(this, ftpManager, getContentResolver(), threadPoolExecutor);
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            createFileLauncher.launch(intent);
        });

        LinearLayout textTitle = findViewById(R.id.textTitle);
        LinearLayout searchTitle = findViewById(R.id.searchTitle);

        ImageButton search = findViewById(R.id.search);
        search.setOnClickListener(v -> {
            textTitle.setVisibility(View.GONE);
            searchTitle.setVisibility(View.VISIBLE);
        });

        ImageButton searchBack = findViewById(R.id.search_back);
        searchBack.setOnClickListener(v -> {
            textTitle.setVisibility(View.VISIBLE);
            searchTitle.setVisibility(View.GONE);
        });

        EditText searchInput = findViewById(R.id.search_input);
        ImageButton searchOk = findViewById(R.id.search_ok);
        searchOk.setOnClickListener(v -> threadPoolExecutor.execute(() -> {
            List<FTPFile> searchList = search(searchInput.getText().toString());

            runOnUiThread(() -> updateFileListView(searchList.toArray(new FTPFile[0])));
        }));

        if (ftpManager != null) {
            refreshFileList(currentPath);
        }
    }

    private void refreshFileList(String directoryPath) {
        currentPath = currentPath.endsWith("/") ? currentPath : currentPath + "/";
        currentPath += "/".equals(directoryPath) ? "" : directoryPath;

        threadPoolExecutor.execute(() -> {
            try {
                FTPFile[] files = ftpManager.listFiles(currentPath);
                runOnUiThread(() -> updateFileListView(files));
            } catch (IOException e) {
                Log.e("refreshFileList", "Error: " + e.getMessage(), e);
                runOnUiThread(() -> Toast.makeText(this, "加载文件列表失败: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
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

            container.addView(fileView);
        }
    }

    private List<FTPFile> search(String searchQuery) {
        String searchPath = currentPath;
        List<FTPFile> result = new ArrayList<>();
        try {
            FTPFile[] allFiles = ftpManager.listFiles(searchPath);

            for (FTPFile file : allFiles) {
                if (file.isDirectory()) {
                    result.addAll(search(searchQuery));
                }

                if (file.getName().contains(searchQuery)) {
                    result.add(file);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    private int getIconBasedOnFileType(FTPFile file) {
        if (file.isDirectory()) {
            return R.drawable.ic_normal_white_grid_folder;
        } else {
            String fileName = file.getName();
            int i = fileName.lastIndexOf('.');
            if (i > 0) {
                String extension = fileName.substring(i + 1).toLowerCase();
                return FileManager.getIcon(extension);
            } else {
                return R.drawable.ic_normal_white_grid_unknown;
            }
        }
    }

    private void clickFile(FTPFile file) {
        if (file.isDirectory()) {
            threadPoolExecutor.execute(() -> {
                try {
                    if (ftpManager.changeWorkingDirectory(currentPath + file.getName() + '/')) {
                        refreshFileList(file.getName());
                    }
                } catch (IOException e) {
                    Log.e("changeDir", "ftp.changeWorkingDir", e);
                }
            });

        } else {
            selectFile = file;
            currentCommand = new DownloadCommand(this, ftpManager, threadPoolExecutor);
            startDownload(file);
        }
    }

    private boolean pressFile(FTPFile file) {
        LinearLayout dock = findViewById(R.id.dock);
        ImageButton download = findViewById(R.id.dock_download);
        ImageButton move = findViewById(R.id.dock_move);
        ImageButton delete = findViewById(R.id.dock_delete);

        LinearLayout confirmDock = findViewById(R.id.confirm_dock);

        selectFile = file;

        download.setOnClickListener(v -> {
            currentCommand = new DownloadCommand(this, ftpManager, threadPoolExecutor);
            startDownload(file);
            dock.setVisibility(View.GONE);
        });
        move.setOnClickListener(v -> {
            dockMove(file, confirmDock);
            dock.setVisibility(View.GONE);
        });
        delete.setOnClickListener(v -> dockDel(file, dock));

        dock.setVisibility(View.VISIBLE);
        return true;
    }

    private void dockMove(FTPFile file, LinearLayout dock) {
        dock.setVisibility(View.VISIBLE);
        String fromPath = currentPath + file.getName();
        ImageButton cancel = findViewById(R.id.confirm_cancel);
        ImageButton confirm = findViewById(R.id.confirm_ok);

        cancel.setOnClickListener(v -> dock.setVisibility(View.GONE));
        confirm.setOnClickListener(v -> {
            String toPath = currentPath + file.getName();
            if (!toPath.equals(fromPath)) {
                try {
                    if (ftpManager.rename(fromPath, toPath)) {
                        Toast.makeText(this,
                                "Move operation complete", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    Log.e("dockMove", "dockMove: ftp rename error", e);
                    throw new RuntimeException(e);
                }
            } else {
                Toast.makeText(this,
                        "You have select same location, please try again", Toast.LENGTH_SHORT).show();
            }
            dock.setVisibility(View.GONE);
        });
    }

    private void dockDel(FTPFile file, LinearLayout dock) {
        try {
            if (ftpManager.deleteFile(file.getName())) {
                Toast.makeText(this, "File delete success", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Log.e("delete", "delete file error", e);
            throw new RuntimeException(e);
        }
        dock.setVisibility(View.GONE);
    }

    public void startDownload(FTPFile file) {
        String type = FileManager.getMimeType(file.getName());
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(type);
        intent.putExtra(Intent.EXTRA_TITLE, file.getName());
        createFileLauncher.launch(intent);
    }

    private void handleCreateFileResult(ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_OK) {
            Intent data = result.getData();
            if (data != null && currentCommand != null) {
                Uri uri = data.getData();

                if (currentCommand instanceof UploadCommand) {
                    currentCommand.execute(uri, currentPath);
                } else if (currentCommand instanceof DownloadCommand) {
                    String fullPath = currentPath.endsWith("/") ? currentPath + selectFile.getName() : currentPath + "/" + selectFile.getName();
                    currentCommand.execute(uri, fullPath);
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if ("/".equals(currentPath)) {
            if (ftpManager != null && ftpManager.isConnected()) {
                ftpManager.disconnect();
            }
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            int lastIndex = currentPath.lastIndexOf('/');
            currentPath = (lastIndex > 0) ? currentPath.substring(0, lastIndex) : "/";
            refreshFileList(currentPath);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (threadPoolExecutor != null && !threadPoolExecutor.isShutdown()) {
            threadPoolExecutor.shutdown();
        }
    }
}
