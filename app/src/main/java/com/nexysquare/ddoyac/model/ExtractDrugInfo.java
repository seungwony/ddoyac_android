package com.nexysquare.ddoyac.model;

import android.util.Log;

import com.nexysquare.ddoyac.util.ColorUtils;
import com.nexysquare.ddoyac.util.Utils;

public class ExtractDrugInfo {
    private final static String TAG = "ExtractDrugInfo";
    ////{이름}_{모양}_{식벽번호}_{색깔}_{방향}

    private int proNumber;
    private String productName;
    private String shape;
    private String colorName;
    private boolean isBack;

    public ExtractDrugInfo(String filename){

        extractedColor(filename);
        extractedName(filename);
        extractedProNumber(filename);
        extractedIsFront(filename);
        extractedShape(filename);
    }

    private void extractedColor(String filename){

//        ArrayList<String> colors = new ArrayList<>();
        String[] split = filename.split("_");
        if(split.length>2){



            String colorName = split[split.length - 1 - 1];

            //trim and split comma
//            colorName = colorName.replaceAll(" ", "");



            this.colorName =  Utils.normalizeNfc(colorName);

            String[] pillArr = colorName.split(",");

            for (String p:pillArr){
                int colorIdx = ColorUtils.getColorIdx(p);
            }
        }


    }

    private void extractedName(String filename){

//        ArrayList<String> colors = new ArrayList<>();
        String[] split = filename.split("_");
        productName = Utils.normalizeNfc(split[0]);


    }

    private void extractedShape(String filename){

//        ArrayList<String> colors = new ArrayList<>();
        String[] split = filename.split("_");
        if(split.length>2){



            String name = split[ split.length - 3 - 1];

            shape = Utils.normalizeNfc(name);
        }


    }

    private void extractedProNumber(String filename){

//        ArrayList<String> colors = new ArrayList<>();
        String[] split = filename.split("_");
        if(split.length>2){

            proNumber = Integer.parseInt( split[ split.length - 2 - 1]);
        }


    }


    private void extractedIsFront(String filename){

//        ArrayList<String> colors = new ArrayList<>();
        String[] split = filename.split("_");


        if(split.length>2){

            String _s = split[split.length - 1];

            String[] split2 = _s.split("\\.");
            String front = split2[0];

            front = Utils.normalizeNfc(front);



            isBack = !front.equals("앞");
//            Log.d(TAG, "front : " + front + " isback : " + isBack);
        }


    }

    public boolean hasPic(){

//        ArrayList<String> colors = new ArrayList<>();
        String[] split = shape.split("\\(");
        if(split.length>1) {
            String mark = split[1];
            mark = Utils.normalizeNfc(mark);
            if(mark.contains("마크")){

                return true;
            }
        }
        return false;
    }

    public String getMarkImg(){
        String[] split = shape.split("\\(");
        if(split.length>1) {
            String mark = split[1].replaceAll("\\)", "");
            mark = Utils.normalizeNfc(mark);
            if(mark.contains("마크")){

                String[] ss = mark.split(",");

                String img_idx = ss[ss.length - 1];

                return "https://nedrug.mfds.go.kr/pbp/cmn/itemImageDownload/" + img_idx;

            }
        }
        return "";
    }
    public boolean hasMarkinfo(){

//        ArrayList<String> colors = new ArrayList<>();
        String[] split = shape.split("\\(");
        return split.length>1;
    }

    public String getMark(){
        String[] split = shape.split("\\(");
        if(split.length>1){
            String mark =  split[1].replaceAll("\\)", "");;
            mark = Utils.normalizeNfc(mark);
            if(!mark.contains("마크")){
                return mark;
            }


        }
        return "";

    }

    public String getShape() {

        String[] split = shape.split("\\(");
        return split[0];
    }

    public int getProNumber() {
        return proNumber;
    }

    public String getColorName() {
        return colorName;
    }

    public String getProductName() {
        return productName;
    }

    public boolean isBack() {
        return isBack;
    }


}
