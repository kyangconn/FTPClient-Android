package com.example.ftpclientandroid.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * @author kyang
 */
public class Permission {
    public static void requestPermission(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
            //没有权限，申请权限
            ActivityCompat.requestPermissions((Activity) context, new String[]{permission}, 100);
        }
    }
}
