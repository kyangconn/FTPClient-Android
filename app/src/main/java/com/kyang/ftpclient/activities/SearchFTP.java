package com.kyang.ftpclient.activities;

import android.Manifest;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.kyang.ftpclient.R;
import com.kyang.ftpclient.utils.NetworkUtils;
import com.kyang.ftpclient.utils.Permission;
import com.kyang.ftpclient.utils.ThreadManager;

import org.json.JSONArray;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author kyang
 * @noinspection AlibabaClassNamingShouldBeCamel
 */
public class SearchFTP extends AppCompatActivity {
    private LinearLayout container;
    private ThreadPoolExecutor threadPoolManager = ThreadManager.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_ftp);

        container = findViewById(R.id.server_list);

        Permission.requestPermission(this, Manifest.permission.ACCESS_NETWORK_STATE);

        searchFtp();

        ImageButton back = findViewById(R.id.search_back);
        back.setOnClickListener(v -> onBackPressed());
    }

    private void searchFtp() {
        String[] allIps = NetworkUtils.getAllIps(this);
        ProgressBar progressBar = findViewById(R.id.waitingBar);
        View alertMessage = findViewById(R.id.alertMessage);

        if (allIps != null) {
            runOnUiThread(() -> progressBar.setVisibility(View.VISIBLE));
            AtomicInteger ipCheck = new AtomicInteger(0);
            AtomicInteger ftpCheck = new AtomicInteger(0);

            threadPoolManager.execute(() -> NetworkUtils.getIpConcurrent(allIps, ip -> NetworkUtils.getFtpOnIp(ip, new NetworkUtils.FtpConnectionListener() {
                @Override
                public void onFtpConnectionSuccess(String ip) {
                    ftpCheck.incrementAndGet();
                    runOnUiThread(() -> {
                        createTextView(ip);
                        progressBar.setVisibility(View.GONE);
                        alertMessage.setVisibility(View.GONE);
                    });
                }

                @Override
                public void onFtpConnectionFailed(String ip) {
                    if (ipCheck.incrementAndGet() == allIps.length) {
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            if (ftpCheck.get() == 0) {
                                alertMessage.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                }
            })));
        } else {
            alertMessage.setVisibility(View.VISIBLE);
        }
    }

    private void createTextView(String ip) {
        TextView textView = new TextView(SearchFTP.this);
        textView.setText(ip);
        textView.setTextColor(getResources().getColor(R.color.black, null));
        textView.setTextSize(18);
        textView.setClickable(true);
        textView.setPadding(10, 10, 10, 10);
        textView.setBackground(ContextCompat.getDrawable(SearchFTP.this, R.drawable.bg_rounded_button));
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        textParams.setMargins(10, 10, 10, 10);
        textView.setLayoutParams(textParams);

        Drawable drawable = ContextCompat.getDrawable(SearchFTP.this, R.drawable.ic_public_storage);
        int sizeInPixels = (int) (50 * SearchFTP.this.getResources().getDisplayMetrics().density);
        if (drawable != null) {
            drawable.setBounds(0, 0, sizeInPixels, sizeInPixels);
        }

        textView.setCompoundDrawables(drawable, null, null, null);
        textView.setCompoundDrawablePadding(10);

        textView.setOnClickListener(v -> textButton(ip));

        container.addView(textView);
    }

    private void textButton(String ip) {
        JSONArray array = new JSONArray();

        String[] ipParts = ip.split("\\.");
        for (String part : ipParts) {
            array.put(part);
        }
        array.put(String.valueOf(21));

        String result = String.format("{\"IPAndPort\":%s,\"Mode\":true,\"FTPS\":true,\"Encode\":\"GBK\"}", array);
        startActivity(new Intent(this, AddFTP.class)
                .putExtra("serverConfig", result));
        finish();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
