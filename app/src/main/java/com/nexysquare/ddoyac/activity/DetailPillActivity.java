package com.nexysquare.ddoyac.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.nexysquare.ddoyac.R;

public class DetailPillActivity extends AppCompatActivity {



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pill_detail);
        TextView dataTxt = findViewById(R.id.data_txt);

        ImageView img_view = findViewById(R.id.img_view);
        String data = getIntent().getStringExtra("data");
        String img = getIntent().getStringExtra("img");
        if(img!=null){
            Log.d("DetailPillActivity", "img");
            Log.d("DetailPillActivity", img);
            Glide.with(this).load(img).into(img_view);
        }



        dataTxt.setText(data);
    }
    public static void open(Context context, String file, String data) {
        Intent intent = new Intent(context, DetailPillActivity.class);
        intent.putExtra("data", data);
        intent.putExtra("img", file);
        context.startActivity(intent);
    }
}
