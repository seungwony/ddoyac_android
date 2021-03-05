package com.nexysquare.ddoyac.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nexysquare.ddoyac.R;
import com.nexysquare.ddoyac.adapter.MarkAdapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MarkImgPickerActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();

    private MarkAdapter adapter;
    private final int numberOfColumns = 6;
    private ArrayList<String> marks;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark_picker);
        marks = new ArrayList<>();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // back button pressed
                onBackPressed();
            }
        });

        RecyclerView recyclerview = findViewById(R.id.recyclerview);
        recyclerview.setLayoutManager(new GridLayoutManager(this, numberOfColumns));
        adapter = new MarkAdapter(getApplicationContext(), marks);

        adapter.setClickListener(new MarkAdapter.onClickListener() {
            @Override
            public void onClick(String img_url) {
                Intent intent = new Intent();
                intent.putExtra("img", img_url);
                setResult(RESULT_OK, intent);

                finish();
            }
        });
        recyclerview.setAdapter(adapter);
        loadMarkList();
    }


    private void loadMarkList(){

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(getAssets().open("mark.txt")));

            // do reading, usually loop until end of file reading
            String mLine;
            while ((mLine = reader.readLine()) != null) {
                //process line

                marks.add(mLine);
            }
        } catch (IOException e) {
            //log the exception
            Log.e(TAG, e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    //log the exception
                }
            }

            Log.d(TAG, "marks size : " + marks.size());
            adapter.notifyDataSetChanged();
        }


    }



}
