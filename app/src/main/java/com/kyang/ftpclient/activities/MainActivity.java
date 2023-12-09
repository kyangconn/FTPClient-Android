package com.kyang.ftpclient.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.kyang.ftpclient.R;
import com.kyang.ftpclient.utils.ActivitiesHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author kyang
 */
public class MainActivity extends AppCompatActivity {
    private final List<CheckBox> checkBoxList = new ArrayList<>();
    private final List<ConstraintLayout> layoutList = new ArrayList<>();
    private LinearLayout container;
    private LinearLayout dock;
    private LinearLayout editDock;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        container = findViewById(R.id.FTPlist);
        sharedPreferences = getSharedPreferences("ServerConfigs", MODE_PRIVATE);
        ActivitiesHelper helper = new ActivitiesHelper(this);
        for (int i = 0; i < getServerConfigCount(); i++) {
            try {
                JSONObject serverConfig = getServerConfigAtIndex(i);
                if (serverConfig != null) {
                    ConstraintLayout layout = helper.createButton(checkBoxList, serverConfig, container);
                    container.addView(layout);
                    layoutList.add(layout);
                }
            } catch (Exception e) {
                Log.e("MainActivity", "Get serve config error: ", e);
                Toast.makeText(this,
                        "Get serve config error: " + ActivitiesHelper.getError(e), Toast.LENGTH_SHORT).show();
                throw new RuntimeException(e);
            }
        }

        ImageButton search = findViewById(R.id.search_server);
        search.setOnClickListener(v -> search());

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

    private void search() {
        startActivity(new Intent(this, SearchFTP.class));
        finish();
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

    private void dockPass() {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        for (CheckBox checkBox : checkBoxList) {
            if (checkBox.isChecked()) {
                editor.remove((String) checkBox.getTag());
                ConstraintLayout layout = isLayout(checkBox.getTag());
                if (layout != null) {
                    container.removeView(layout);
                } else {
                    Log.i("Check dock in MainActivity", "真奇怪，怎么会发生这种不可能发生的错误呢？");
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
        Map<String, ?> allEntries = sharedPreferences.getAll();

        if (index < 0 || index >= allEntries.size()) {
            return null;
        }

        String key = new ArrayList<>(allEntries.keySet()).get(index);
        return getServerConfig(key);
    }

    public int getServerConfigCount() {
        Map<String, ?> allEntries = sharedPreferences.getAll();
        return allEntries.size();
    }

    public JSONObject getServerConfig(String key) {
        String jsonString = sharedPreferences.getString(key, null);

        if (jsonString == null) {
            return null;
        }

        try {
            return new JSONObject(jsonString);
        } catch (JSONException e) {
            Log.e("Get server config", "Error parsing JSON: ", e);
            return null;
        }
    }
}