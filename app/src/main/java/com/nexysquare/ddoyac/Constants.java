package com.nexysquare.ddoyac;

import androidx.core.content.ContextCompat;

import com.nexysquare.ddoyac.R;

import java.util.ArrayList;

public class Constants {



    final public static int FLASH_ON = 12345;
    final public static int FLASH_OFF = 23456;
    final public static int FLASH_AUTO = 34567;

    final public static int LENS_BACK = 987654;
    final public static int LENS_FRONT = 876543;

    public static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.7f;

    public final static String[] COLORS = {
            "빨강",
            "주황",
            "갈색",
            "노랑",
            "연두",
            "초록",
            "파랑",
            "분홍",
            "자주",
            "보라",
            "청록",
            "남색",
            "회색",
            "검정",
//            "옅은",
//            "진한",
            "하양",
            "투명"};
    public final static String[] COLORS_HEX = {
            "#d73522",
            "#ea9b39",
            "#5d2c16",
            "#fdea54",
            "#aece3a",
            "#3a844a",
            "#3d98f1",
            "#e7b5dc",
            "#9c2258",
            "#3c1381",
            "#3d8ea1",
            "#133487",
            "#cad1cd",
            "#000000",
            "#f4f4f4",
            "#3b3b3b",
            "#f1f1f1",
            "#ffffff"};

    public final static int[] COLORS_ID = {
            R.color.d_red,
            R.color.d_orange,
            R.color.d_brown,
            R.color.d_yellow,
            R.color.d_yellow_green,
            R.color.d_green,
            R.color.d_blue,
            R.color.d_pink,
            R.color.d_violet,
            R.color.d_purple,
            R.color.d_blue_green,
            R.color.d_dark_blue,
            R.color.d_gray,
            R.color.d_black,
            R.color.d_white,
            R.color.d_trans
            };

    public final static int[] TEXT_COLOR_ID = {
            R.color.white,
            R.color.white,
            R.color.white,
            R.color.black,
            R.color.black,
            R.color.white,
            R.color.white,
            R.color.black,
            R.color.white,
            R.color.white,
            R.color.white,
            R.color.white,
            R.color.white,
            R.color.white,
            R.color.black,
            R.color.black
    };

    public final static String[] SHAPES = {"원형", "장방형", "타원형", "사각형", "팔각형", "오각형", "삼각형", "마름모형", "육각형", "반원형", "기타"};


    public static ArrayList<String> getShapes(String shape){
        ArrayList<String> shapes = new ArrayList<>();
        if(shape == null){
            return shapes;
        }


        if(shape.equals("circle")){

            shapes.add("원형");

//            return "● 원형";
        }else if(shape.equals("oval")){
//            return "● 타원형";

            shapes.add("타원형");

        }else if(shape.equals("oblong")) {
//            return "■■ 장방형";

            shapes.add("장방형");

        }else if(shape.equals("other_shape")){

            shapes.add("사각형");
            shapes.add("기타");
            shapes.add("팔각형");
            shapes.add("오각형");
            shapes.add("삼각형");
            shapes.add("마름모형");
            shapes.add("육각형");
            shapes.add("반원형");
            //사각형, 기타, 팔각형, 오각형, 삼각형, 마름모형, 육각형, 반원형
        }


        return shapes;
    }
}
