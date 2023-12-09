package com.kyang.ftpclient.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.kyang.ftpclient.R;
import com.kyang.ftpclient.activities.AddFTP;
import com.kyang.ftpclient.activities.FileList;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author kyang
 */
public class ActivitiesHelper {
    private static String port;
    private static String ip;
    private static String serverName;
    private static String username;
    private static String password;
    private static String encodeText;
    private static int portNumber;
    private static boolean mode, ftps;
    private final Activity activity;

    public ActivitiesHelper(Activity activity) {
        this.activity = activity;
    }

    public ConstraintLayout createButton(List<CheckBox> checkBoxList, JSONObject serverConfig) {
        JSONArray ipAndPort;
        if (serverConfig != null) {
            ipAndPort = serverConfig.optJSONArray("IPAndPort");
            if (ipAndPort != null && ipAndPort.length() == 5) {
                String ipPart1 = ipAndPort.optString(0);
                String ipPart2 = ipAndPort.optString(1);
                String ipPart3 = ipAndPort.optString(2);
                String ipPart4 = ipAndPort.optString(3);
                port = ipAndPort.optString(4);

                ip = ipPart1 + "." + ipPart2 + "." + ipPart3 + "." + ipPart4;
                portNumber = Integer.parseInt(port);
            }
            serverName = serverConfig.optString("ServerName", "");
            username = serverConfig.optString("Username", "");
            password = serverConfig.optString("Password", "");
            mode = serverConfig.optBoolean("Mode", true);
            ftps = serverConfig.optBoolean("FTPS", true);
            encodeText = serverConfig.optString("Encode", "GBK");
        }

        LayoutInflater inflater = LayoutInflater.from(activity);
        ConstraintLayout layout = (ConstraintLayout) inflater.inflate(R.layout.activity_main_element, null);

        CheckBox checkBox = layout.findViewById(R.id.check_box);
        TextView ipText = layout.findViewById(R.id.ip_text);
        TextView ipAddr = layout.findViewById(R.id.ip_addr);
        ImageButton serverEdit = layout.findViewById(R.id.server_edit);

        serverEdit.setOnClickListener(v -> {
            if (serverConfig != null) {
                activity.startActivity(new Intent(activity, AddFTP.class)
                        .putExtra("serverConfig", serverConfig.toString()));
                activity.finish();
            } else {
                Toast.makeText(activity, "This is a empty config", Toast.LENGTH_SHORT).show();
                Log.i("serverEdit", "createButtonLayout: a empty config");
            }
        });

        if (ip != null) {
            ipAddr.setText(String.format("%s:%s", ip, port));
        }
        if (serverName != null) {
            checkBox.setTag(serverName);
            ipText.setText(serverName);
            layout.setTag(serverName);
        }

        checkBoxList.add(checkBox);

        layout.setOnClickListener(v -> getFtp());

        return layout;
    }

    private void getFtp() {
        authenticationDialog(username, isAuthSuccessful -> {
            if (!isAuthSuccessful) {
                activity.runOnUiThread(() -> Toast.makeText(activity,
                        "Authentication not passed, please try again", Toast.LENGTH_SHORT).show());
                return;
            }

            if (ip == null && portNumber == 0) {
                activity.runOnUiThread(() -> Toast.makeText(activity,
                        "Invalid Configuration, please edit it", Toast.LENGTH_SHORT).show());
                return;
            }

            FtpManager ftpManager = FtpManager.getInstance(ftps);
            ThreadPoolExecutor threadPoolExecutor = ThreadManager.getInstance();
            threadPoolExecutor.execute(() -> {
                boolean isConnected = ftpManager.connect(ip, portNumber, username, password, mode, encodeText);

                activity.runOnUiThread(() -> {
                    if (isConnected) {
                        activity.startActivity(new Intent(activity, FileList.class)
                                .putExtra("serverName", serverName));
                        activity.finish();
                    } else {
                        Toast.makeText(activity, "Connection failed", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });
    }

    private void authenticationDialog(String username, DialogCallback callback) {
        LayoutInflater inflater = activity.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.server_auth, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(dialogView);

        EditText usernameInput = dialogView.findViewById(R.id.auth_username);
        Button confirm = dialogView.findViewById(R.id.auth_confirm);
        Button cancel = dialogView.findViewById(R.id.auth_back);

        AlertDialog dialog = builder.create();
        dialog.setTitle(R.string.auth_title);

        confirm.setOnClickListener(v -> {
            ((InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(usernameInput.getWindowToken(), 0);
            String inputText = usernameInput.getText().toString();
            boolean result = username.equals(inputText);
            callback.onResult(result);
            dialog.dismiss();
        });

        cancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }


    public interface DialogCallback {
        /**
         * give Result from MainActivity.authenticationDialog
         * to MainActivity for create ftp connection
         *
         * @param result result of Authentication
         */
        void onResult(boolean result);
    }
}
