package com.nexysquare.ddoyac.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.nexysquare.ddoyac.R;

public class SettingsActivity extends AppCompatActivity {
    private final static String TAG = SettingsActivity.class.getSimpleName();
    public static void open(Context context) {
        Intent intent = new Intent(context, SettingsActivity.class);


        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // back button pressed
                onBackPressed();
            }
        });
//        setActionbarTextColor(getSupportActionBar(), ContextCompat.getColor(getApplicationContext(), R.color.green_darker));

//        getSupportActionBar().setTitle(Html.fromHtml("<font color='#3a894e'>설 정</font>"));
//        int titleId;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//            titleId = getResources().getIdentifier("action_bar_title", "id", "android");
//        } else {
//            titleId = R.id.action_bar_title;
//        }

//        TextView actionbar_title = findViewById(titleId);
//        actionbar_title.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.green_darker));
        /**
         * We load a PreferenceFragment which is the recommended way by Android
         * see @http://developer.android.com/guide/topics/ui/settings.html#Fragment
         * @TargetApi(11)
         */
//        MyPreferenceFragment fragment = new MyPreferenceFragment();
//
//        setPreferenceFragment(fragment);
    }


}
