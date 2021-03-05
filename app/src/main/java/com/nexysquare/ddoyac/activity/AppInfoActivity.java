package com.nexysquare.ddoyac.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.nexysquare.ddoyac.R;

public class AppInfoActivity extends AppCompatActivity {
    public static void open(Context context) {
        Intent intent = new Intent(context, AppInfoActivity.class);


        context.startActivity(intent);
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_info);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // back button pressed
                onBackPressed();
            }
        });



        TextView app_version_txt = findViewById(R.id.app_version_txt);

        try {

            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            app_version_txt.setText(version);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
}
