package com.nexysquare.ddoyac.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nexysquare.ddoyac.model.Drug;

public class JsonHelper {

    public static JsonObject drugsRealmToJson(Drug drug){
        JsonObject jsObj = new JsonObject();
        jsObj.addProperty("id", drug.getId());
        jsObj.addProperty("p_no", drug.getP_no());
        jsObj.addProperty("p_name", drug.getP_name());
        jsObj.addProperty("c_no", drug.getC_no());
        jsObj.addProperty("c_name", drug.getC_name());
        jsObj.addProperty("des", drug.getDes());
        jsObj.addProperty("img", drug.getImg());
        jsObj.addProperty("mark_front", drug.getMark_front());
        jsObj.addProperty("mark_back", drug.getMark_back());
        jsObj.addProperty("shape", drug.getShape());


        jsObj.addProperty("color_front", drug.getColor_front());
        jsObj.addProperty("color_back", drug.getColor_back());

        jsObj.addProperty("div_front", drug.getDiv_front());
        jsObj.addProperty("div_back", drug.getDiv_back());


        jsObj.addProperty("major_axis", drug.getMajor_axis());
        jsObj.addProperty("minor_axis", drug.getMinor_axis());

        jsObj.addProperty("thickness", drug.getThickness());


        jsObj.addProperty("img_created", drug.getImg_created());

        jsObj.addProperty("class_no", drug.getClass_no());
        jsObj.addProperty("class_name", drug.getClass_name());

        jsObj.addProperty("class_no", drug.getClass_no());
        jsObj.addProperty("class_name", drug.getClass_name());

        jsObj.addProperty("specialization", drug.getSpecialization());
        jsObj.addProperty("approval", drug.getApproval());
        jsObj.addProperty("searchable", drug.getSearchable());
        jsObj.addProperty("shape_code", drug.getShape_code());
        jsObj.addProperty("updated", drug.getUpdated());

        return jsObj;
    }

    public static String getNullAsEmptyString(JsonElement jsonElement) {
        if(jsonElement==null){
            return "";
        }else
            return jsonElement.isJsonNull() ? "" : jsonElement.getAsString();
    }


    public static String hasJsonAndGetString(JsonObject jsonObject, String name){
        if(jsonObject.has(name)){
            return getNullAsEmptyString(jsonObject.get(name));
        }else{
            return "";
        }
    }


    public static String hasJsonAndGetNumberString(JsonObject jsonObject, String name){
        if(jsonObject.has(name)){
            return getNullAsEmptyNumberString(jsonObject.get(name));
        }else{
            return "0";
        }
    }

    public static String getNullAsEmptyNumberString(JsonElement jsonElement) {
        if(jsonElement==null){
            return "0";
        }else
            return jsonElement.isJsonNull() ? "0" : jsonElement.getAsString();
    }

    public static String hasJsonAndGetStringNullable(JsonObject jsonObject, String name){
        if(jsonObject.has(name)){
            return getNullAsEmptyString(jsonObject.get(name));
        }else{
            return null;
        }
    }
}
