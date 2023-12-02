package com.example.ftpclientandroid;

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

import com.google.android.material.switchmaterial.SwitchMaterial;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author kyang
 */
public class AddFTP extends AppCompatActivity {
    private EditText ip1, ip2, ip3, ip4, serverPort, serverName, username, password;
    private SwitchMaterial passiveSwitcher, ftpSwitcher, encodeSwitcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_ftp);

        ip1 = findViewById(R.id.ip_1);
        ip2 = findViewById(R.id.ip_2);
        ip3 = findViewById(R.id.ip_3);
        ip4 = findViewById(R.id.ip_4);
        serverPort = findViewById(R.id.port);
        serverName = findViewById(R.id.name_edit);
        username = findViewById(R.id.username_edit);
        password = findViewById(R.id.password_edit);
        passiveSwitcher = findViewById(R.id.passiveSwitch);
        ftpSwitcher = findViewById(R.id.ftpswitch);
        encodeSwitcher = findViewById(R.id.coding_switch);
        TextView encodeText = findViewById(R.id.encoding);

        if (getIntent().getStringExtra("serverConfig") != null) {
            loadServerConfig();
        }

        ImageButton backPage = findViewById(R.id.back);
        backPage.setOnClickListener(view -> backToMain());

        ImageButton confirmAdd = findViewById(R.id.confirm_button);
        confirmAdd.setOnClickListener(view -> confirmAdd());

        encodeSwitcher.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                encodeText.setText(R.string.gbk);
            } else {
                encodeText.setText(R.string.utf_8);
            }
        });

        setJumpTo(ip1, ip2);
        setJumpTo(ip2, ip3);
        setJumpTo(ip3, ip4);
        setJumpTo(ip4, serverPort);
    }

    private void backToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void confirmAdd() {
        String ipPart1 = ip1.getText().toString();
        String ipPart2 = ip2.getText().toString();
        String ipPart3 = ip3.getText().toString();
        String ipPart4 = ip4.getText().toString();
        String portStr = serverPort.getText().toString();
        String alias = serverName.getText().toString();
        String userName = username.getText().toString();
        String passWord = password.getText().toString();

        if (!isValidIpPart(ipPart1) || !isValidIpPart(ipPart2) || !isValidIpPart(ipPart3) || !isValidIpPart(ipPart4)) {
            Toast.makeText(this, "invalid ip address, please check", Toast.LENGTH_SHORT).show();
            return;
        }

        if (portStr.isEmpty()) {
            portStr = "21";
        } else if (!isValidPort(portStr)) {
            Toast.makeText(this, "invalid port for ip, please try again", Toast.LENGTH_SHORT).show();
            return;
        }

        if (alias.isEmpty() || userName.isEmpty() || passWord.isEmpty()) {
            Toast.makeText(this, "empty server name, username or password for server.", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean passiveMode = passiveSwitcher.isChecked();
        boolean ftpStat = ftpSwitcher.isChecked();
        String encoding = encodeSwitcher.isChecked() ? "GBK" : "UTF-8";

        saveServerConfig(new String[]{ipPart1, ipPart2, ipPart3, ipPart4, portStr}, alias, userName, passWord, passiveMode, ftpStat, encoding);
        backToMain();
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

    private void loadServerConfig() {
        String config = getIntent().getStringExtra("serverConfig");
        String ipPart1 = null, ipPart2 = null, ipPart3 = null, ipPart4 = null,
                port = null,
                serverNameText = null,
                usernameText = null, passwordText = null,
                encode = null;
        boolean mode = true, ftps = true, encoding = true;

        if (config != null) {
            try {
                JSONObject serverConfig = new JSONObject(config);

                JSONArray ipAndPort = serverConfig.getJSONArray("IPAndPort");
                if (ipAndPort != null && ipAndPort.length() == 5) {
                    ipPart1 = ipAndPort.getString(0);
                    ipPart2 = ipAndPort.getString(1);
                    ipPart3 = ipAndPort.getString(2);
                    ipPart4 = ipAndPort.getString(3);
                    port = ipAndPort.getString(4);
                }
                serverNameText = serverConfig.getString("ServerName");
                usernameText = serverConfig.getString("Username");
                passwordText = serverConfig.getString("Password");
                mode = serverConfig.getBoolean("Mode");
                ftps = serverConfig.getBoolean("FTPS");
                encode = serverConfig.getString("Encode");
            } catch (JSONException e) {
                Log.e("loadConfig", "Error: " + e.getMessage(), e);
            }
        }

        if (encode != null) {
            switch (encode) {
                case "GBK":
                    break;
                case "UTF-8":
                    encoding = false;
                    break;
                default: {
                }
            }
        }

        TextView title = findViewById(R.id.title);
        title.setText(serverNameText);

        ip1.setText(ipPart1);
        ip2.setText(ipPart2);
        ip3.setText(ipPart3);
        ip4.setText(ipPart4);
        serverPort.setText(port);
        serverName.setText(serverNameText);
        username.setText(usernameText);
        password.setText(passwordText);
        passiveSwitcher.setChecked(mode);
        ftpSwitcher.setChecked(ftps);
        encodeSwitcher.setChecked(encoding);
    }

    private void saveServerConfig(String[] ipPort, String name, String username, String password, boolean mode, boolean ftps, String encoding) {
        JSONObject serverConfig = new JSONObject();
        try {
            JSONArray ipPortArray = new JSONArray();
            for (String item : ipPort) {
                ipPortArray.put(item);
            }
            serverConfig.put("IPAndPort", ipPortArray);
            serverConfig.put("ServerName", name);
            serverConfig.put("Username", username);
            serverConfig.put("Password", password);
            serverConfig.put("Mode", mode);
            serverConfig.put("FTPS", ftps);
            serverConfig.put("Encode", encoding);

            SharedPreferences sharedPreferences = getSharedPreferences("ServerConfigs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(name, serverConfig.toString());
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
