package com.example.ftpclientandroid.activities;

import android.Manifest;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.ftpclientandroid.R;
import com.example.ftpclientandroid.utils.NetworkUtils;
import com.example.ftpclientandroid.utils.Permission;
import com.example.ftpclientandroid.utils.ThreadManager;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author kyang
 */
public class SearchFTP extends AppCompatActivity {
    private ThreadPoolExecutor threadPoolManager;
    private LinearLayout container;
    public int port = 21;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_ftp);

        container = findViewById(R.id.server_list);

        Permission.requestPermission(this, Manifest.permission.ACCESS_NETWORK_STATE);
        threadPoolManager = ThreadManager.getInstance();


        threadPoolManager.execute(() -> {
            String[] allIps = NetworkUtils.getAllIps(this);
            if (allIps != null) {
                NetworkUtils.checkIpsConcurrently(allIps, threadPoolManager, new NetworkUtils.IpCheckListener() {
                    @Override
                    public void onIpReachable(String ip) {
                        runOnUiThread(() -> createTextView(ip));
                    }

                    @Override
                    public void onIpCheckFailed(Exception e) {
                        runOnUiThread(() -> findViewById(R.id.alertMessage)
                                .setVisibility(View.VISIBLE));
                    }
                });
            }
        });

        ImageButton back = findViewById(R.id.search_back);
        back.setOnClickListener(v -> onBackPressed());
    }

    private void createTextView(String ip) {
        TextView textView = new TextView(SearchFTP.this);
        textView.setText(ip);
        textView.setTextColor(getResources().getColor(R.color.black, null));
        textView.setTextSize(18);
        textView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        textView.setPadding(10, 10, 10, 10);

        Drawable drawable = ContextCompat.getDrawable(SearchFTP.this, R.drawable.ic_public_storage);
        int sizeInPixels = (int) (50 * SearchFTP.this.getResources().getDisplayMetrics().density);
        if (drawable != null) {
            drawable.setBounds(0, 0, sizeInPixels, sizeInPixels);
        }

        textView.setCompoundDrawables(drawable, null, null, null);
        textView.setCompoundDrawablePadding(10);

        container.addView(textView);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
