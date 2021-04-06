package com.nexysquare.ddoyac.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.nexysquare.ddoyac.R;
import com.nexysquare.ddoyac.adapter.MarkAdapter;
import com.nexysquare.ddoyac.model.MarkModel;
import com.nexysquare.ddoyac.util.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MarkImgPickerActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();

    private MarkAdapter adapter;
    private final int numberOfColumns = 6;
    private ArrayList<MarkModel> marks;

    private ArrayList<MarkModel> result;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark_picker);
        marks = new ArrayList<>();
        result = new ArrayList<>();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // back button pressed
                onBackPressed();
            }
        });

        TextInputEditText search_tf = findViewById(R.id.search_tf);
        RecyclerView recyclerview = findViewById(R.id.recyclerview);
        recyclerview.setLayoutManager(new GridLayoutManager(this, numberOfColumns));
        adapter = new MarkAdapter(getApplicationContext(), result);

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



        search_tf.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
//                    callSearchListener();
                    Utils.hideInputMethod(v);
                    return true;
                }
                return false;
            }
        });

        search_tf.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString();

                if(input.length()>0){
                    result.clear();
                    for(MarkModel markModel : marks ){
                        if(markModel.getDes().toLowerCase().contains(input.toLowerCase())){
                            result.add(markModel);
                        }

                    }
                    adapter.notifyDataSetChanged();
                }else{
                    result.clear();
                    result.addAll(marks);
                    adapter.notifyDataSetChanged();
                }

            }
        });
    }



    private void loadMarkList(){

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(getAssets().open("mark_list.txt")));

            // do reading, usually loop until end of file reading
            String mLine;
            while ((mLine = reader.readLine()) != null) {
                //process line

                Log.d(TAG, "read : " + mLine);
                String[] split = mLine.split(", ");
                MarkModel markModel;
                if(split.length==2){
                    markModel = new MarkModel(split[0], split[1]);
                }else{
                    markModel = new MarkModel(split[0], "");
                }

                marks.add(markModel);
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

            result.addAll(marks);
            adapter.notifyDataSetChanged();
        }


    }



}
