package com.nexysquare.ddoyac.util;

public class LabelHelper {


    public static String intuitionLabel(String labelName){

        //원형

        // 장방형

        //타원형



        //사각형, 기타, 팔각형, 오각형, 삼각형, 마름모형, 육각형, 반원형



        //        circle
//                oval
//        oblong
//                other_shape

        //■, ●, ■■■, ETC
        if(labelName.equals("circle")){
            return "원형";
//            return "● 원형";
        }else if(labelName.equals("oval")){
//            return "● 타원형";
            return "타원형";
        }else if(labelName.equals("oblong")) {
//            return "■■ 장방형";
            return "장방형";
        }else if(labelName.equals("other_shape")){
            return "기타";

        }else{
            return "기타";
        }
    }
}
