package com.nexysquare.ddoyac.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.nexysquare.ddoyac.GlobalApp;
import com.nexysquare.ddoyac.util.ColorUtils;
import com.nexysquare.ddoyac.R;
import com.nexysquare.ddoyac.model.PillRealm;
import com.nexysquare.ddoyac.util.BitmapUtil;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmResults;

public class ExtractColorTestActivity extends AppCompatActivity  {
    private final static String TAG = "ExtractColorTestActivity";
    private static final int PICK_PHOTO_REQUEST_CODE = 101;
    private ImageView preview_img, cropped_preview_img;
    private View dominant_color_view, closest_color_view;
    private TextView dominant_color_txt, closest_color_txt, result_color_txt;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_picker_test);
        preview_img = findViewById(R.id.preview_img);
        cropped_preview_img = findViewById(R.id.cropped_preview_img);

        Button pick_img_btn = findViewById(R.id.pick_img_btn);
        Button list_colors_from_files_btn = findViewById(R.id.list_colors_from_files_btn);

        dominant_color_view = findViewById(R.id.dominant_color_view);
        closest_color_view = findViewById(R.id.closest_color_view);
        dominant_color_txt = findViewById(R.id.dominant_color_txt);
        closest_color_txt = findViewById(R.id.closest_color_txt);
        result_color_txt = findViewById(R.id.result_color_txt);


        pick_img_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, PICK_PHOTO_REQUEST_CODE);
            }
        });

        list_colors_from_files_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Realm realm = Realm.getInstance(GlobalApp.getRealmConfiguration());
                RealmResults<PillRealm> pills = realm.where(PillRealm.class).findAll();

                Log.d(TAG,"total size = "+ pills.size());
//                new Handler().post(new Runnable() {
//                    @Override
//                    public void run() {
//
//                    }
//                });
                ArrayList<String> colors = new ArrayList<>();
                for(PillRealm pill : pills){
                    String name = pill.getName();

                    //아네톤정_장방형_200101778_자주_0
                    String[] split = name.split("_");
                    if(split.length>2){


                        String colorName = split[split.length - 1 - 1];

                        //trim and split comma
                        colorName = colorName.replaceAll(" ", "");
                        String[] pillArr = colorName.split(",");

                        for (String p : pillArr){
                            if(!colors.contains(p)){
                                colors.add(p);
                            }
                        }
                    }
                }

                Log.d("Color :: ", "Color list");
                for(String name : colors){
                    Log.d("Color :: ", name);
                }

                realm.close();
            }


        });

    }

    public static void open(Context context) {
        Intent intent = new Intent(context, ExtractColorTestActivity.class);
        context.startActivity(intent);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == PICK_PHOTO_REQUEST_CODE){



            if (resultCode == RESULT_OK) {


                Uri selectedImage = data.getData();
                try {
                    InputStream imageStream = getContentResolver().openInputStream(
                            selectedImage);


                    Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
//                    Bitmap fastblur = BitmapUtil.fastblur(selectedImageBitmap, (float)0.8, 2);
//                    Bitmap resized = BitmapUtil.resizeBitmapImage(selectedImageBitmap, 160);


                    Bitmap focusBitmap = BitmapUtil.fixedSizeWithCenterCrop50x50(bitmap);
                    preview_img.setImageBitmap(bitmap);
                    preview_img.invalidate();

                    cropped_preview_img.setImageBitmap(focusBitmap);
                    cropped_preview_img.invalidate();

                    getMostCommonColour(focusBitmap);


                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    /**/
    public String getMostCommonColour(Bitmap bitmap) {


        try {
            Map map = ColorUtils.getInstance().imageColour(bitmap);
            List list = new LinkedList(map.entrySet());
            Collections.sort(list, new Comparator() {
                public int compare(Object o1, Object o2) {

                    return ((Comparable) ((Map.Entry) (o1)).getValue())
                            .compareTo(((Map.Entry) (o2)).getValue());

                }

            });
            String colourTemp;

//        if(list.size()==0){
//            return "White";
//        }
            Map.Entry me = (Map.Entry) list.get(list.size() - 1);
            int[] rgb = ColorUtils.getRGBArr((Integer) me.getKey());

//            redTextView.setText("Red: "+ ColorUtils.To00Hex(rgb[0]));
//            greenTextView.setText("Green: "+ ColorUtils.To00Hex(rgb[1]));
//            blueTextView.setText("Blue: "+ ColorUtils.To00Hex(rgb[2]));
            dominant_color_txt.setText("Dominant Color: #"+ ColorUtils.To00Hex(rgb[0]) + ColorUtils.To00Hex(rgb[1])  + ColorUtils.To00Hex(rgb[2]));

            //Find name of color
            String colorName = ColorUtils.getInstance().getNearestColorNameFromRgb(rgb[0], rgb[1], rgb [2]);

            ArrayList<String> Colors_kor = ColorUtils.getInstance().getListOfColorInKoreanFromEngList(colorName);

            int closestColorInt = ColorUtils.getInstance().getNearestColorIntFromRgb(rgb[0], rgb[1], rgb [2]);

            closest_color_view.setBackgroundColor(closestColorInt);

            closest_color_txt.setText("Closest Named Color: " + ColorUtils.getInstance().getNearestColorHexFromRgb(rgb[0], rgb[1], rgb [2])) ;
            result_color_txt.setText(colorName + "\n"+ Colors_kor.toString());


            //Converting RGB color value to hex string
            colourTemp= "#" + ColorUtils.To00Hex(rgb[0]) + ColorUtils.To00Hex(rgb[1]) +  ColorUtils.To00Hex(rgb[2]);
            int colorInt = Color.parseColor(colourTemp);
            dominant_color_view.setBackgroundColor(colorInt);

//            return To00Hex(rgb[0]) + To00Hex(rgb[1])  + To00Hex(rgb[2]);
            return colorName;


        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";

    }
}
