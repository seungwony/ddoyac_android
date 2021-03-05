package com.nexysquare.ddoyac.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.nexysquare.ddoyac.R;
import com.opencsv.CSVReader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button similarity_test_btn = findViewById(R.id.similarity_test_btn);

        Button extract_compare_test_btn = findViewById(R.id.extract_compare_test_btn);

        Button extract_color_test_btn = findViewById(R.id.extract_color_test_btn);

        Button opencv_camera_test_btn = findViewById(R.id.opencv_camera_test_btn);

        Button camerax_ocr_btn = findViewById(R.id.camerax_ocr_btn);

        Button opencv_contour_btn = findViewById(R.id.opencv_contour_btn);
        Button camera_btn = findViewById(R.id.camera_btn);

        Button search_drug_btn = findViewById(R.id.search_drug_btn);
        similarity_test_btn.setOnClickListener(this);
        extract_compare_test_btn.setOnClickListener(this);
        extract_color_test_btn.setOnClickListener(this);
        opencv_camera_test_btn.setOnClickListener(this);
        opencv_contour_btn.setOnClickListener(this);
        camera_btn.setOnClickListener(this);
        camerax_ocr_btn.setOnClickListener(this);
        search_drug_btn.setOnClickListener(this);

//        String result = "TPRN TPRN".replaceAll("[^A-Za-z0-9\\s]", "");
        BoardPlatform();


    }


    private void BoardPlatform(){
        String boardPlatform = "";

        try {
            Process sysProcess =
                    new ProcessBuilder("/system/bin/getprop", "ro.board.platform").
                            redirectErrorStream(true).start();

            BufferedReader reader = new BufferedReader
                    (new InputStreamReader(sysProcess.getInputStream()));
            String currentLine = null;

            while ((currentLine=reader.readLine()) != null){
                boardPlatform = currentLine;
            }
            sysProcess.destroy();
        } catch (IOException e) {}

        Log.d("Board Platform", boardPlatform);
    }

    private void initSearchableData(){

    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.similarity_test_btn){

            SimilarityTestActivity.open(getApplicationContext());

        }else if(v.getId() == R.id.extract_compare_test_btn){

            ExtractDescriptorActivity.open(getApplicationContext());
        }else if(v.getId() == R.id.extract_color_test_btn){

            ExtractColorTestActivity.open(getApplicationContext());
        }else if (v.getId() == R.id.opencv_camera_test_btn){
            OpenCVTestActivity.open(getApplicationContext());
        }else if(v.getId() == R.id.camera_btn){
            CameraActivity.open(getApplicationContext());
        }else if(v.getId() ==  R.id.opencv_contour_btn){
            OpenCVContourActivity.open(getApplicationContext());
        }else if (v.getId() == R.id.camerax_ocr_btn){
            CameraMainActivity.open(getApplicationContext());
        }else if(v.getId() == R.id.search_drug_btn){
            SearchDrugActivity.open(getApplicationContext());
        }
    }



}
