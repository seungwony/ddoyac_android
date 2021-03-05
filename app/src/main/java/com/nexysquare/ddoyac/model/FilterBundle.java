package com.nexysquare.ddoyac.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class FilterBundle implements Parcelable {

    private ArrayList<String> colors = new ArrayList<>();
    private ArrayList<String> shapes = new ArrayList<>();
    private String mark_img;
    private String keyword;


    public FilterBundle(){}

    protected FilterBundle(Parcel in) {
        keyword = in.readString();
        colors = in.createStringArrayList();
        shapes = in.createStringArrayList();
        mark_img = in.readString();
    }

    public static final Creator<FilterBundle> CREATOR = new Creator<FilterBundle>() {
        @Override
        public FilterBundle createFromParcel(Parcel in) {
            return new FilterBundle(in);
        }

        @Override
        public FilterBundle[] newArray(int size) {
            return new FilterBundle[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(keyword);
        dest.writeStringList(colors);
        dest.writeStringList(shapes);
        dest.writeString(mark_img);
    }


    public void addColor(String color){
        if(!colors.contains(color))
            colors.add(color);
    }
    public void addShape(String shape){
        if(!shapes.contains(shape))
            shapes.add(shape);
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public ArrayList<String> getColors() {
        return colors;
    }

    public ArrayList<String> getShapes() {
        return shapes;
    }

    public String getMark_img() {
        return mark_img;
    }

    public void setMark_img(String mark_img) {
        this.mark_img = mark_img;
    }
}
