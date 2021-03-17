package com.nexysquare.ddoyac.activity;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.nexysquare.ddoyac.Constants;
import com.nexysquare.ddoyac.GlobalApp;
import com.nexysquare.ddoyac.R;
import com.nexysquare.ddoyac.model.Drug;
import com.nexysquare.ddoyac.model.FilterBundle;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

public class SearchFilterActivity extends AppCompatActivity implements View.OnClickListener {
    private final int REQUEST_MARK_PICKER = 1001;
    private int idx = 0;
    private EditText search_edittext;
    private ChipGroup shape_chip_group, color_chip_group;
    private Button select_mark_btn;

    private String str_mark_img;
    private ImageView mark_img;

    private View intuition_color_picker;

    public static void open(Activity activity, View v, String keyword, ArrayList<String> filter_shapes, ArrayList<String> filter_colors, String mark_img, int REQUEST_FILTER) {

        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(activity, v, "search_edit_view");
        Intent intent = new Intent(activity, SearchFilterActivity.class);

        intent.putExtra("keyword", keyword);

        intent.putExtra("shapes", filter_shapes);
        intent.putExtra("colors", filter_colors);
        intent.putExtra("mark_img", mark_img);

        activity.startActivityForResult(intent, REQUEST_FILTER, options.toBundle());
    }



    public static void open(Activity activity, View v, String keyword, int REQUEST_FILTER) {

        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(activity, v, "search_edit_view");
        Intent intent = new Intent(activity, SearchFilterActivity.class);

        intent.putExtra("keyword", keyword);
        activity.startActivityForResult(intent, REQUEST_FILTER, options.toBundle());
    }

    public static void open(Activity activity, View v, String keyword) {

        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(activity, v, "search_edit_view");
        Intent intent = new Intent(activity, SearchFilterActivity.class);

        intent.putExtra("keyword", keyword);
        activity.startActivity(intent, options.toBundle());
    }

    public static void open(Context context) {
        Intent intent = new Intent(context, SearchFilterActivity.class);

        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_filter);

        ImageButton back_btn = findViewById(R.id.back_btn);
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//
//        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // back button pressed
//                onBackPressed();
//            }
//        });
        intuition_color_picker = findViewById(R.id.intuition_color_picker);
        shape_chip_group = findViewById(R.id.shape_chip_group);
        color_chip_group = findViewById(R.id.color_chip_group);
        select_mark_btn = findViewById(R.id.select_mark_btn);
        mark_img = findViewById(R.id.mark_img);

        AppCompatCheckBox color_picker_checkbox = findViewById(R.id.color_picker_checkbox);

        Button reset_filter_btn = findViewById(R.id.reset_filter_btn);
        Button apply_filter_btn = findViewById(R.id.apply_filter_btn);
        search_edittext = findViewById(R.id.search_edittext);
//        extractFilterList();

        String keyword = getIntent().getStringExtra("keyword");
        if(keyword!=null){
            search_edittext.setText(keyword);
        }



        mark_img.setVisibility(View.GONE);
        select_mark_btn.setVisibility(View.VISIBLE);

        mark_img.setOnClickListener(this);
        select_mark_btn.setOnClickListener(this);
        reset_filter_btn.setOnClickListener(this);
        apply_filter_btn.setOnClickListener(this);
        initFilterList();



    }

    private void initFilterList(){
        for(int i=0; i< Constants.COLORS.length; i++){
//            addColorChipView(color_chip_group, Constants.COLORS[i], Constants.COLORS_HEX[i]);
//            addChipView(color_chip_group, i, Constants.COLORS[i]);
            addColorChipView(color_chip_group, i, Constants.COLORS[i], Constants.COLORS_ID[i], Constants.TEXT_COLOR_ID[i]);
        }

        for(int i=0; i< Constants.SHAPES.length; i++){
//            addColorChipView(color_chip_group, Constants.COLORS[i], Constants.COLORS_HEX[i]);
            addChipView(shape_chip_group, i, Constants.SHAPES[i]);
        }

//        for(String shape : Constants.SHAPES){
//            addChipView(shape_chip_group, shape);
//        }



        ArrayList<String> shapes = getIntent().getStringArrayListExtra("shapes");

        if(shapes!=null){

            for(String shape : shapes){

                for (int i=0; i<shape_chip_group.getChildCount();i++){
                    Chip chip = (Chip)shape_chip_group.getChildAt(i);
                    if(chip.getText().equals(shape)){
                        chip.setChecked(true);
                        break;
                    }
                }



            }
        }
        AppCompatCheckBox color_picker_checkbox = findViewById(R.id.color_picker_checkbox);
        color_picker_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    showIntuitionColorPicker();
                }else{
                    hideIntuitionColorPicker();
                }
            }
        });

        showIntuitionColorPicker();

        ArrayList<String> colors = getIntent().getStringArrayListExtra("colors");

        if(colors!=null){
            if(colors.size()>0){
//                hideIntuitionColorPicker();
                color_picker_checkbox.setChecked(false);
            } else{
//                showIntuitionColorPicker();
                color_chip_group.setVisibility(View.GONE);
                color_picker_checkbox.setChecked(true);
            }


            for(String color : colors){
//                int colorIdx = getIdxColor(color);
//                if(colorIdx!=-1){
//
//                }

                for (int i=0; i<color_chip_group.getChildCount();i++){
                    Chip chip = (Chip)color_chip_group.getChildAt(i);
                    if(chip.getText().equals(color)){
                        chip.setChecked(true);
                        break;
                    }
                }
            }
        }else{

            color_picker_checkbox.setChecked(true);
//            showIntuitionColorPicker();
            color_chip_group.setVisibility(View.GONE);
        }

        str_mark_img = getIntent().getStringExtra("mark_img");

        if(str_mark_img!=null && !str_mark_img.equals("")){
            Glide.with(getApplicationContext())
                    .load(str_mark_img)
                    .centerCrop()
                    .into(mark_img);
            mark_img.setVisibility(View.VISIBLE);
            select_mark_btn.setVisibility(View.GONE);
        }
    }


    private int getIdxColor(String color){
        for(int i = 0; i < Constants.COLORS.length; i++){
            if(Constants.COLORS[i].equals(color)){
                return i;
            }
        }

        return -1;
    }

    private void showIntuitionColorPicker(){
        intuition_color_picker.setVisibility(View.VISIBLE);


        Button color_picker_red_btn = findViewById(R.id.color_picker_red_btn);
        Button color_picker_orange_btn = findViewById(R.id.color_picker_orange_btn);
        Button color_picker_yellow_btn = findViewById(R.id.color_picker_yellow_btn);
        Button color_picker_green_btn = findViewById(R.id.color_picker_green_btn);
        Button color_picker_blue_btn = findViewById(R.id.color_picker_blue_btn);
        Button color_picker_purple_btn = findViewById(R.id.color_picker_purple_btn);
        Button color_picker_bright_btn = findViewById(R.id.color_picker_bright_btn);
        Button color_picker_dark_btn = findViewById(R.id.color_picker_dark_btn);


        color_picker_red_btn.setOnClickListener(colorPicker);
        color_picker_orange_btn.setOnClickListener(colorPicker);
        color_picker_yellow_btn.setOnClickListener(colorPicker);
        color_picker_green_btn.setOnClickListener(colorPicker);
        color_picker_blue_btn.setOnClickListener(colorPicker);
        color_picker_purple_btn.setOnClickListener(colorPicker);
        color_picker_bright_btn.setOnClickListener(colorPicker);
        color_picker_dark_btn.setOnClickListener(colorPicker);
    }

    View.OnClickListener colorPicker = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            color_chip_group.setVisibility(View.VISIBLE);
            ArrayList<String> colors = new ArrayList<>();
            if(v.getId() == R.id.color_picker_red_btn){
                colors.add("빨강");
                colors.add("주황");
                colors.add("갈색");
            }else if(v.getId() == R.id.color_picker_orange_btn){
                colors.add("빨강");
                colors.add("주황");
                colors.add("갈색");
                colors.add("노랑");
            }else if(v.getId() == R.id.color_picker_yellow_btn){
                colors.add("주황");
                colors.add("갈색");
                colors.add("노랑");
            }else if(v.getId() == R.id.color_picker_green_btn){
                colors.add("연두");
                colors.add("청록");
                colors.add("초록");
            }else if(v.getId() == R.id.color_picker_blue_btn){
                colors.add("파랑");
                colors.add("청록");
                colors.add("남색");
            }else if(v.getId() == R.id.color_picker_purple_btn){
                colors.add("보라");
                colors.add("분홍");
                colors.add("자주");
            }else if(v.getId() == R.id.color_picker_bright_btn){
                colors.add("하양");
                colors.add("노랑");
                colors.add("투명");
            }else if(v.getId() == R.id.color_picker_dark_btn){
                colors.add("갈색");
                colors.add("검정");
                colors.add("진한");
                colors.add("남색");
                colors.add("회색");
            }

            clearColorSelected();

            for(String color : colors){

                for (int i=0; i<color_chip_group.getChildCount();i++){
                    Chip chip = (Chip)color_chip_group.getChildAt(i);
                    if(chip.getText().equals(color)){
                        chip.setChecked(true);
                        break;
                    }
                }
            }
        }


    };

    private void clearColorSelected(){
        for (int i=0; i<color_chip_group.getChildCount();i++){
            Chip chip = (Chip)color_chip_group.getChildAt(i);
            chip.setChecked(false);
        }
    }

    private void hideIntuitionColorPicker(){
        intuition_color_picker.setVisibility(View.GONE);
        color_chip_group.setVisibility(View.VISIBLE);
//        color_chip_group.setVisibility(View.VISIBLE);
    }

    private void extractFilterList(){
        Realm realm = Realm.getInstance(GlobalApp.getRealmConfiguration());
        RealmResults<Drug> realmResults = realm.where(Drug.class).findAllAsync();


        ArrayList<String> mark_list = new ArrayList<>();

        ArrayList<String> color_list = new ArrayList<>();
        ArrayList<String> shape_list = new ArrayList<>();


        for( Drug drug : realmResults){

            String shape = drug.getShape();
//
//            String mark_img_back = drug.getMark_img_back();


            String color_front = drug.getColor_front();
            String color_back = drug.getColor_back();


            String mark_img_front = drug.getMark_img_front();
            String mark_img_back = drug.getMark_img_back();

            if(!mark_list.contains(mark_img_front)){
                mark_list.add(mark_img_front);
            }

            if(!mark_list.contains(mark_img_back)){
                mark_list.add(mark_img_back);
            }
            if(!shape_list.contains(shape)){
                shape_list.add(shape);
            }

            String[] colorFArr = color_front.split(",");
            for( String color : colorFArr){
                if(!color_list.contains(color)){

                    color_list.add(color);
                }
            }


            String[] colorBArr = color_back.split(",");
            for( String color : colorBArr){
                if(!color_list.contains(color)){

                    color_list.add(color);
                }
            }
        }


        String result = "";


        result += "=== mark list ===\n";

        for(String mark : mark_list){
            result += mark + "\n";



//            chip.
        }


        result += "\n=== color list ===\n";

        for(String color : color_list){

            result += color + "\n";
//            addColorChipView(color_chip_group, color);

        }

        result += "\n=== shape list ===\n";
        for(String shape : shape_list){
            result += shape + "\n";
            addChipView(shape_chip_group, shape);
        }


        Log.d("filter list", result);

//        writeFilterList(result);
    }
    private void addChipView(ChipGroup gp, int idx, String chipText) {
        LayoutInflater layoutInflater = getLayoutInflater();
        Chip chip = (Chip) layoutInflater.inflate(R.layout.row_chip_view, gp, false);
        chip.setText(chipText);
        chip.setId(idx);
        chip.setTag(chipText);
        //...
        chip.setCheckedIconTint(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.darker_gray)));
        // This is ChipGroup view
        gp.addView(chip);
    }

    private void addChipView(ChipGroup gp, String chipText) {
        int id = idx++;
        LayoutInflater layoutInflater = getLayoutInflater();
        Chip chip = (Chip) layoutInflater.inflate(R.layout.row_chip_view, gp, false);
        chip.setText(chipText);
        chip.setId(id);
        chip.setTag(id);
        //...

        // This is ChipGroup view
        gp.addView(chip);
    }
    private void addColorChipView(ChipGroup gp, int idx, String chipText, int res_id, int text_res_id) {

        LayoutInflater layoutInflater = getLayoutInflater();
        Chip chip = (Chip) layoutInflater.inflate(R.layout.row_chip_view, gp, false);
//        chip.setChipIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_lens));
//        chip.setChipIconTint(stateList);
//        chip.setTextColor(ContextCompat.getColor(getApplicationContext(), text_res_id));
        chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.white)));
        chip.setText(chipText);
        chip.setTag(idx);
        chip.setChipStrokeWidth(3);
//        chip.setChipStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.bright_gray)));

        chip.setChipStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), res_id)));
//        chip.setCheckedIconTint(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.darker_gray)));
//        chip.setCheckedIconTintResource(text_res_id);
//        chip.setChipIconTintResource(text_res_id);

//        DrawableCompat.setTint(chip.getCheckedIcon(), ContextCompat.getColor(getApplicationContext(), text_res_id));
//        chip.setCheckedIconTint(DrawableCompat.setTint(chip.getCheckedIcon(), ContextCompat.getColor(getApplicationContext(), text_res_id)));
        chip.setId(idx);
        //...

        // This is ChipGroup view
        gp.addView(chip);
    }

    private void addColorChipView(ChipGroup gp, String chipText, String hexColor) {
        ColorStateListBuilder builder = new ColorStateListBuilder();
        builder.addState(new int[] { android.R.attr.state_pressed }, Color.parseColor(hexColor))
                .addState(new int[] { android.R.attr.state_selected }, Color.parseColor(hexColor));
        ColorStateList stateList = builder.build();
        LayoutInflater layoutInflater = getLayoutInflater();
        Chip chip = (Chip) layoutInflater.inflate(R.layout.row_chip_view, gp, false);
//        chip.setChipIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_lens));
//        chip.setChipIconTint(stateList);

        chip.setBackgroundColor(Color.parseColor(hexColor.toUpperCase()));
        chip.setText(chipText);
        chip.setId(idx++);
        //...

        // This is ChipGroup view
        gp.addView(chip);
    }


    private void writeFilterList(String des){
        try {


            File logs = new File(Environment
                    .getExternalStorageDirectory()
                    .getAbsolutePath()
                    + "/opencv4test/filter_list2.txt");

            FileWriter fw;
            BufferedWriter bw;

            File dir = new File(Environment
                    .getExternalStorageDirectory()
                    .getAbsolutePath() + "/opencv4test/");
            dir.mkdirs();

            logs.createNewFile();

            logs = new File(
                    Environment
                            .getExternalStorageDirectory()
                            .getAbsolutePath()
                            + "/opencv4test/filter_list2.txt");

            fw = new FileWriter(logs, true);
            bw = new BufferedWriter(fw);

            bw.write(des + "\n");
            bw.close();



        } catch (IOException e1) {

            // TODO Auto-generated catch block
            e1.printStackTrace();

        }
    }

    @Override
    public void onClick(View v) {

        if(v.getId() == R.id.apply_filter_btn){


            String result = "색상\n";


            FilterBundle filterbundle = new FilterBundle();

            for(int id : color_chip_group.getCheckedChipIds()){
                result += Constants.COLORS[id] + " ";
                filterbundle.addColor(Constants.COLORS[id]);
            }

            result+="\n\n모양\n";
            for(int id : shape_chip_group.getCheckedChipIds()){
                result += Constants.SHAPES[id]+ " ";
                filterbundle.addShape(Constants.SHAPES[id]);
            }


            Log.d("result", result);


            if(str_mark_img!=null && !str_mark_img.equals("")){
                filterbundle.setMark_img(str_mark_img);
            }

            if(!search_edittext.getText().toString().trim().equals("")){
                String keyword = search_edittext.getText().toString();
                filterbundle.setKeyword(keyword);
            }

            Intent intent = new Intent();
            intent.putExtra("filter", filterbundle);
            setResult(RESULT_OK, intent);
            finish();
//            JsonObject jsObj = new JsonObject();



//            filterbundle

//            jsObj.ad
        }else if(v.getId() == R.id.reset_filter_btn){
            resetFilter();

        }else if(v.getId() == R.id.select_mark_btn || v.getId() == R.id.mark_img){
            Intent intent = new Intent(getApplicationContext(), MarkImgPickerActivity.class);

            startActivityForResult(intent, REQUEST_MARK_PICKER);
        }
    }


    private void resetFilter(){
        color_chip_group.clearCheck();
        shape_chip_group.clearCheck();
        str_mark_img = null;
        mark_img.setVisibility(View.GONE);
        select_mark_btn.setVisibility(View.VISIBLE);

    }
    private class ColorStateListBuilder {
        List<Integer> colors = new ArrayList<>();
        List<int[]> states = new ArrayList<>();

        public ColorStateListBuilder addState(int[] state, int color) {
            states.add(state);
            colors.add(color);
            return this;
        }

        public ColorStateList build() {
            return new ColorStateList(convertToTwoDimensionalIntArray(states),
                    convertToIntArray(colors));
        }

        private int[][] convertToTwoDimensionalIntArray(List<int[]> integers) {
            int[][] result = new int[integers.size()][1];
            Iterator<int[]> iterator = integers.iterator();
            for (int i = 0; iterator.hasNext(); i++) {
                result[i] = iterator.next();
            }
            return result;
        }

        private int[] convertToIntArray(List<Integer> integers) {
            int[] result = new int[integers.size()];
            Iterator<Integer> iterator = integers.iterator();
            for (int i = 0; iterator.hasNext(); i++) {
                result[i] = iterator.next();
            }
            return result;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_MARK_PICKER && resultCode == RESULT_OK){
            str_mark_img = data.getStringExtra("img");
            Glide.with(getApplicationContext())
                    .load(str_mark_img)
                    .centerCrop()
                    .into(mark_img);
            mark_img.setVisibility(View.VISIBLE);
            select_mark_btn.setVisibility(View.GONE);

        }
    }


    //    MarkImgPickerActivity
}
