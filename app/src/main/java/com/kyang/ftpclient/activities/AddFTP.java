package com.kyang.ftpclient.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.kyang.ftpclient.R;
import com.kyang.ftpclient.utils.FtpManager;
import com.kyang.ftpclient.utils.ThreadManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author kyang
 */
public class AddFTP extends AppCompatActivity {
    private EditText ipPart1, ipPart2, ipPart3, ipPart4, serverPort, serverName, username, password;
    private String ipText1, ipText2, ipText3, ipText4, portStr, serverNameText, usernameText, passwordText, encodeText;
    private SwitchMaterial passiveSwitcher, ftpSwitcher, encodeSwitcher;
    private boolean mode, ftps, encoding;
    private JSONObject serverConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_ftp);

        ipPart1 = findViewById(R.id.ip_1);
        ipPart2 = findViewById(R.id.ip_2);
        ipPart3 = findViewById(R.id.ip_3);
        ipPart4 = findViewById(R.id.ip_4);
        serverPort = findViewById(R.id.port);
        serverName = findViewById(R.id.name_edit);
        username = findViewById(R.id.username_edit);
        password = findViewById(R.id.password_edit);
        passiveSwitcher = findViewById(R.id.passiveSwitch);
        ftpSwitcher = findViewById(R.id.ftpswitch);
        encodeSwitcher = findViewById(R.id.coding_switch);

        String configStr = getIntent().getStringExtra("serverConfig");
        if (configStr != null) {
            try {
                serverConfig = new JSONObject(configStr);
            } catch (JSONException e) {
                Log.e("AddFTP", "Error parsing JSON", e);
                serverConfig = new JSONObject();
            }
        } else {
            serverConfig = new JSONObject();
        }
        loadServerConfig();

        ImageButton backPage = findViewById(R.id.back);
        backPage.setOnClickListener(v -> onBackPressed());

        ImageButton confirmAdd = findViewById(R.id.confirm_button);
        confirmAdd.setOnClickListener(v -> confirmAdd());

        FloatingActionButton confirmFtp = findViewById(R.id.confirm_fab);
        confirmFtp.setOnClickListener(v -> confirmFtp());

        TextView encodeTextView = findViewById(R.id.encoding);
        encodeSwitcher.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                encodeTextView.setText(R.string.gbk);
            } else {
                encodeTextView.setText(R.string.utf_8);
            }
        });

        setJumpTo(ipPart1, ipPart2);
        setJumpTo(ipPart2, ipPart3);
        setJumpTo(ipPart3, ipPart4);
        setJumpTo(ipPart4, serverPort);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void confirmAdd() {
        ipText1 = ipPart1.getText().toString();
        ipText2 = ipPart2.getText().toString();
        ipText3 = ipPart3.getText().toString();
        ipText4 = ipPart4.getText().toString();
        portStr = serverPort.getText().toString();
        serverNameText = serverName.getText().toString();
        usernameText = username.getText().toString();
        passwordText = password.getText().toString();

        if (!isValidIpPart(ipText1) || !isValidIpPart(ipText2) || !isValidIpPart(ipText3) || !isValidIpPart(ipText4)) {
            Toast.makeText(this, "invalid ip address, please check", Toast.LENGTH_SHORT).show();
            return;
        }

        if (portStr.isEmpty()) {
            portStr = "21";
        } else if (!isValidPort(portStr)) {
            Toast.makeText(this, "invalid port for ip, please try again", Toast.LENGTH_SHORT).show();
            return;
        }

        if (serverNameText.isEmpty() || usernameText.isEmpty() || passwordText.isEmpty()) {
            Toast.makeText(this, "empty server name, username or password for server.", Toast.LENGTH_SHORT).show();
            return;
        }

        mode = passiveSwitcher.isChecked();
        ftps = ftpSwitcher.isChecked();
        encodeText = encodeSwitcher.isChecked() ? "GBK" : "UTF-8";

        saveServerConfig();
        onBackPressed();
    }

    private boolean isValidIpPart(String part) {
        try {
            int value = Integer.parseInt(part);
            return value >= 0 && value < 255;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isValidPort(String port) {
        try {
            int value = Integer.parseInt(port);
            return value > 0 && value < 65535;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void confirmFtp() {
        String ip = ipText1 + "." + ipText2 + "." + ipText3 + "." + ipText4;
        int port = Integer.parseInt(portStr);

        FtpManager ftpManager = FtpManager.getInstance(ftps);
        ThreadPoolExecutor threadPoolExecutor = ThreadManager.getInstance();
        threadPoolExecutor.execute(() -> {
            boolean isConnected = ftpManager.connect(ip, port, usernameText, passwordText, mode, encodeText);

            runOnUiThread(() -> {
                if (isConnected) {
                    startActivity(new Intent(this, FileList.class)
                            .putExtra("serverName", serverNameText));
                    finish();
                } else {
                    Toast.makeText(this, "Connection failed", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void loadServerConfig() {
        JSONArray ipAndPort = serverConfig.optJSONArray("IPAndPort");
        if (ipAndPort != null && ipAndPort.length() == 5) {
            ipText1 = ipAndPort.optString(0);
            ipText2 = ipAndPort.optString(1);
            ipText3 = ipAndPort.optString(2);
            ipText4 = ipAndPort.optString(3);
            portStr = ipAndPort.optString(4);
        } else {
            ipText1 = ipText2 = ipText3 = ipText4 = portStr = "";
        }
        serverNameText = serverConfig.optString("ServerName", "");
        usernameText = serverConfig.optString("Username", "");
        passwordText = serverConfig.optString("Password", "");
        mode = serverConfig.optBoolean("Mode", true);
        ftps = serverConfig.optBoolean("FTPS", true);
        encodeText = serverConfig.optString("Encode", "GBK");

        switch (encodeText) {
            case "GBK":
                break;
            case "UTF-8":
                encoding = false;
                break;
            default: {
                // No other encoding is ready for application
            }
        }

        TextView title = findViewById(R.id.title);
        title.setText(serverNameText);
        ipPart1.setText(ipText1);
        ipPart2.setText(ipText2);
        ipPart3.setText(ipText3);
        ipPart4.setText(ipText4);
        serverPort.setText(portStr);
        serverName.setText(serverNameText);
        username.setText(usernameText);
        password.setText(passwordText);
        passiveSwitcher.setChecked(mode);
        ftpSwitcher.setChecked(ftps);
        encodeSwitcher.setChecked(encoding);
    }

    private void saveServerConfig() {
        try {
            JSONArray ipPortArray = new JSONArray();
            ipPortArray.put(ipText1);
            ipPortArray.put(ipText2);
            ipPortArray.put(ipText3);
            ipPortArray.put(ipText4);
            ipPortArray.put(portStr);
            serverConfig.put("IPAndPort", ipPortArray);
            serverConfig.put("ServerName", serverNameText);
            serverConfig.put("Username", usernameText);
            serverConfig.put("Password", passwordText);
            serverConfig.put("Mode", mode);
            serverConfig.put("FTPS", ftps);
            serverConfig.put("Encode", encodeText);

            SharedPreferences sharedPreferences = getSharedPreferences("ServerConfigs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(serverNameText, serverConfig.toString());
            editor.apply();
        } catch (Exception e) {
            Log.e("saveConfig", "Error: " + e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void setJumpTo(EditText current, EditText next) {
        current.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 3 || s.toString().contains(".")) {
                    next.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        current.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                next.requestFocus();
                return true;
            }
            return false;
        });
    }
}
