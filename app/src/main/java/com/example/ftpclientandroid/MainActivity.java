package com.example.ftpclientandroid;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;


/**
 * @author kyang
 */
public class MainActivity extends AppCompatActivity {
    private final List<CheckBox> checkBoxList = new ArrayList<>();
    private final List<ConstraintLayout> layoutList = new ArrayList<>();
    private LinearLayout container;
    private LinearLayout dock;
    private LinearLayout editDock;
    private ThreadPoolExecutor threadPoolExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        threadPoolExecutor = ThreadPoolManager.getInstance();

        container = findViewById(R.id.FTPlist);
        for (int i = 0; i < getServerConfigCount(); i++) {
            try {
                JSONObject serverConfig = getServerConfigAtIndex(i);
                if (serverConfig != null) {
                    ConstraintLayout layout = createButtonLayout(serverConfig);
                    container.addView(layout);
                }
            } catch (Exception e) {
                Log.e("initialPage", "Error: " + e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }

        dock = findViewById(R.id.dock);
        editDock = findViewById(R.id.editdock);

        ImageButton edit = findViewById(R.id.editFtp);
        edit.setOnClickListener(v -> editPage());

        ImageButton addFtp = findViewById(R.id.addFtp);
        addFtp.setOnClickListener(v -> addFtp());

        ImageButton dockPass = findViewById(R.id.editPass);
        dockPass.setOnClickListener(v -> dockPass());

        ImageButton dockBack = findViewById(R.id.editBack);
        dockBack.setOnClickListener(v -> dockBack());

    }

    private ConstraintLayout createButtonLayout(JSONObject serverConfig) {
        String ip1, ip2, ip3, ip4, port = null;
        String ipAddrString = null;
        int portNumber = 0;
        String serverName = null;
        String username = null, password = null;
        boolean mode = false, ftps = false;
        String encode = null;

        try {
            JSONArray ipAndPort = serverConfig.getJSONArray("IPAndPort");
            if (ipAndPort != null && ipAndPort.length() == 5) {
                ip1 = ipAndPort.getString(0);
                ip2 = ipAndPort.getString(1);
                ip3 = ipAndPort.getString(2);
                ip4 = ipAndPort.getString(3);
                port = ipAndPort.getString(4);

                ipAddrString = ip1 + "." + ip2 + "." + ip3 + "." + ip4;
                portNumber = Integer.parseInt(port);
            }
            serverName = serverConfig.getString("ServerName");
            username = serverConfig.getString("Username");
            password = serverConfig.getString("Password");
            mode = serverConfig.getBoolean("Mode");
            ftps = serverConfig.getBoolean("FTPS");
            encode = serverConfig.getString("Encode");
        } catch (JSONException e) {
            Log.e("getServerConfig", "Error: " + e.getMessage(), e);
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        ConstraintLayout layout = (ConstraintLayout) inflater.inflate(R.layout.activity_main_element, null);

        CheckBox checkBox = layout.findViewById(R.id.check_box);
        TextView ipText = layout.findViewById(R.id.ip_text);
        TextView ipAddr = layout.findViewById(R.id.ip_addr);
        ImageButton serverEdit = layout.findViewById(R.id.server_edit);

        serverEdit.setOnClickListener(v -> serverEdit(serverConfig));

        if (ipAddrString != null) {
            ipAddr.setText(String.format("%s:%s", ipAddrString, port));
        }
        if (serverName != null) {
            checkBox.setTag(serverName);
            ipText.setText(serverName);
            layout.setTag(serverName);
        }

        checkBoxList.add(checkBox);
        layoutList.add(layout);

        String finalIpAddrString = ipAddrString;
        int finalPortNumber = portNumber;
        String finalUsername = username;
        String finalPassword = password;
        boolean finalMode = mode;
        boolean finalFtps = ftps;
        String finalEncode = encode;
        String finalServerName = serverName;
        layout.setOnClickListener(v -> showCustomDialog(finalUsername, isAuthSuccessful -> {
            if (!isAuthSuccessful) {
                runOnUiThread(() -> Toast.makeText(this,
                        "Authentication not passed, please try again", Toast.LENGTH_SHORT).show());
                return;
            }

            if (finalIpAddrString == null && finalPortNumber == 0) {
                runOnUiThread(() -> Toast.makeText(this,
                        "Invalid Configuration, please edit it", Toast.LENGTH_SHORT).show());
                return;
            }

            final FTPConnection ftpConnection = FTPConnection.getInstance(finalFtps);
            threadPoolExecutor.execute(() -> {
                boolean isConnected = ftpConnection.connect(
                        finalIpAddrString, finalPortNumber,
                        finalUsername, finalPassword,
                        finalMode, finalEncode);

                runOnUiThread(() -> {
                    if (isConnected) {
                        startActivity(new Intent(this, FileList.class)
                                .putExtra("serverName", finalServerName));
                        finish();
                    } else {
                        Toast.makeText(this, "Connection failed", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }));

        return layout;
    }

    private void showCustomDialog(String username, DialogCallback callback) {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.server_auth, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setView(dialogView);

        EditText input = dialogView.findViewById(R.id.auth_input);
        Button confirm = dialogView.findViewById(R.id.auth_confirm);
        Button cancel = dialogView.findViewById(R.id.auth_back);

        AlertDialog dialog = builder.create();
        dialog.setTitle(R.string.auth_title);

        confirm.setOnClickListener(v -> {
            String inputText = input.getText().toString();
            boolean result = username.equals(inputText);
            callback.onResult(result);
            dialog.dismiss();
        });

        cancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }


    private void addFtp() {
        startActivity(new Intent(this, AddFTP.class));
        finish();
    }

    private void editPage() {
        for (CheckBox checkBox : checkBoxList) {
            checkBox.setVisibility(View.VISIBLE);
        }
        editDock.setVisibility(View.VISIBLE);
        dock.setVisibility(View.INVISIBLE);
    }

    private void serverEdit(JSONObject config) {
        startActivity(new Intent(this, AddFTP.class)
                .putExtra("serverConfig", config.toString()));
    }

    private void dockPass() {
        SharedPreferences sharedPreferences = getSharedPreferences("ServerConfigs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        for (CheckBox checkBox : checkBoxList) {
            if (checkBox.isChecked()) {
                editor.remove((String) checkBox.getTag());
                ConstraintLayout layout = isLayout(checkBox.getTag());
                if (layout != null) {
                    container.removeView(layout);
                } else {
                    Log.i("dockPass.checkLayout", "真奇怪，怎么会发生这种不可能发生的错误呢？");
                }
            }
        }

        editor.apply();
        dockBack();
    }

    private void dockBack() {
        for (CheckBox checkBox : checkBoxList) {
            checkBox.setVisibility(View.GONE);
        }
        editDock.setVisibility(View.INVISIBLE);
        dock.setVisibility(View.VISIBLE);
        container.invalidate();
    }

    private ConstraintLayout isLayout(Object tag) {
        for (ConstraintLayout layout : layoutList) {
            if (layout.getTag() != null && layout.getTag().equals(tag)) {
                return layout;
            }
        }
        return null;
    }

    public JSONObject getServerConfigAtIndex(int index) {
        SharedPreferences sharedPreferences = getSharedPreferences("ServerConfigs", MODE_PRIVATE);
        Map<String, ?> allEntries = sharedPreferences.getAll();

        if (index < 0 || index >= allEntries.size()) {
            return null;
        }

        String key = new ArrayList<>(allEntries.keySet()).get(index);
        return getServerConfig(key);
    }

    public int getServerConfigCount() {
        SharedPreferences sharedPreferences = getSharedPreferences("ServerConfigs", MODE_PRIVATE);
        Map<String, ?> allEntries = sharedPreferences.getAll();
        return allEntries.size();
    }

    public JSONObject getServerConfig(String key) {
        SharedPreferences sharedPreferences = getSharedPreferences("ServerConfigs", MODE_PRIVATE);
        String jsonString = sharedPreferences.getString(key, null);

        if (jsonString == null) {
            return null;
        }

        try {
            return new JSONObject(jsonString);
        } catch (JSONException e) {
            Log.e("getConfig", "Error parsing JSON", e);
            return null;
        }
    }
}

