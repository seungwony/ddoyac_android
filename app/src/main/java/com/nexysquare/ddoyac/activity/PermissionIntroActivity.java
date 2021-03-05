package com.nexysquare.ddoyac.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.nexysquare.ddoyac.GlobalApp;
import com.nexysquare.ddoyac.R;

import io.realm.Realm;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.nexysquare.ddoyac.Constants.LENS_BACK;

public class PermissionIntroActivity extends AppCompatActivity {

    private final static String TAG = "PermissionIntroActivity";

    public static final int REQ_CODE = 1012;

    View warning_view, progress_view;
    Button getting_started_btn;
    Realm realm;
    public static void open(Context context) {
        Intent intent = new Intent(context, PermissionIntroActivity.class);
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_permission_intro);

        progress_view = findViewById(R.id.progress_view);
        warning_view = findViewById(R.id.warning_view);
        getting_started_btn = findViewById(R.id.getting_started_btn);


        progress_view.setVisibility(View.VISIBLE);
        warning_view.setVisibility(View.GONE);
        getting_started_btn.setVisibility(View.GONE);

        getting_started_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                if(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean("initUser", false).commit()){
                    CameraMainActivity.open(getApplicationContext());
                    finish();
                }

            }
        });

//        checkPermissions();
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                realm = Realm.getInstance(GlobalApp.getRealmConfiguration());



                checkPermissions();

            }
        }, 1000);


    }

    @Override
    protected void onStop() {
        super.onStop();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


    }


    void checkPermissions() {
        // Checking if permission is not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQ_CODE);
            progress_view.setVisibility(View.GONE);
            warning_view.setVisibility(View.VISIBLE);
            getting_started_btn.setVisibility(View.GONE);
        } else {
//            startCamera(LENS_BACK);
            progress_view.setVisibility(View.GONE);
            warning_view.setVisibility(View.GONE);
            getting_started_btn.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_CODE) {
            // Checking whether user granted the permission or not.
            if (grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                // Showing the toast message
//                startCamera(LENS_BACK);
                progress_view.setVisibility(View.GONE);
                warning_view.setVisibility(View.GONE);
                getting_started_btn.setVisibility(View.VISIBLE);

            } else {
                Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
