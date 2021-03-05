package com.nexysquare.ddoyac.model;

import android.os.Parcel;
import android.os.Parcelable;

import io.realm.annotations.Required;

public class DrugParcelable implements Parcelable {

    private int p_no;

    private String p_name;

    private int c_no;
    private String c_name;
    private String des;
    private String img;
    private String mark_front;
    private String mark_back;
    private String shape;
    private String color_front;
    private String color_back;
    private String div_front;
    private String div_back;
    private String major_axis;
    private String minor_axis;
    private String thickness;

    private int img_created;
    private String class_no;
    private String class_name;

    private String mark_img_front;
    private String mark_img_back;


    private String mark_img_des_front;
    private String mark_img_des_back;


    private String specialization;
    private int approval;
    private String shape_code;
    private String updated;

    private String searchable;


//    private String descriptor_front;
//
//    private String descriptor_back;

    private int matched_count_front;
    private int matched_count_back;


    public DrugParcelable(Drug drug){
        p_no = drug.getP_no();
        p_name = drug.getP_name();
        c_no = drug.getC_no();
        c_name = drug.getC_name();
        des = drug.getDes();
        img = drug.getImg();
        mark_front = drug.getMark_front();
        mark_back = drug.getMark_back();

        shape = drug.getShape();
        color_front = drug.getColor_front();
        color_back = drug.getColor_back();

        div_front = drug.getDiv_front();
        div_back = drug.getDiv_back();

        major_axis = drug.getMajor_axis();
        minor_axis = drug.getMinor_axis();
        thickness = drug.getThickness();

        img_created  = drug.getImg_created();
        class_no = drug.getClass_no();
        class_name = drug.getClass_name();
        mark_img_front = drug.getMark_front();
        mark_img_back = drug.getMark_back();

        mark_img_des_front = drug.getMark_img_des_front();
        mark_img_des_back = drug.getMark_img_des_back();

        specialization = drug.getSpecialization();
        approval = drug.getApproval();
        shape_code = drug.getShape_code();
        updated = drug.getUpdated();
        searchable = drug.getSearchable();
//        descriptor_front =  drug.getDescriptor_front();
//        descriptor_back = drug.getDescriptor_back();

        matched_count_front= 0;
        matched_count_back = 0;
    }


    protected DrugParcelable(Parcel in) {
        p_no = in.readInt();
        p_name = in.readString();
        c_no = in.readInt();
        c_name = in.readString();
        des = in.readString();
        img = in.readString();
        mark_front = in.readString();
        mark_back = in.readString();
        shape = in.readString();
        color_front = in.readString();
        color_back = in.readString();
        div_front = in.readString();
        div_back = in.readString();
        major_axis = in.readString();
        minor_axis = in.readString();
        thickness = in.readString();
        img_created = in.readInt();
        class_no = in.readString();
        class_name = in.readString();
        mark_img_front = in.readString();
        mark_img_back = in.readString();
        mark_img_des_front = in.readString();
        mark_img_des_back = in.readString();
        specialization = in.readString();
        approval = in.readInt();
        shape_code = in.readString();
        updated = in.readString();
        searchable = in.readString();
//        descriptor_front = in.readString();
//        descriptor_back = in.readString();

        matched_count_front = in.readInt();
        matched_count_back = in.readInt();
    }

    public static final Creator<DrugParcelable> CREATOR = new Creator<DrugParcelable>() {
        @Override
        public DrugParcelable createFromParcel(Parcel in) {
            return new DrugParcelable(in);
        }

        @Override
        public DrugParcelable[] newArray(int size) {
            return new DrugParcelable[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(p_no);
        dest.writeString(p_name);
        dest.writeInt(c_no);
        dest.writeString(c_name);
        dest.writeString(des);
        dest.writeString(img);
        dest.writeString(mark_front);
        dest.writeString(mark_back);
        dest.writeString(shape);
        dest.writeString(color_front);
        dest.writeString(color_back);
        dest.writeString(div_front);
        dest.writeString(div_back);
        dest.writeString(major_axis);
        dest.writeString(minor_axis);
        dest.writeString(thickness);
        dest.writeInt(img_created);
        dest.writeString(class_no);
        dest.writeString(class_name);
        dest.writeString(mark_img_front);
        dest.writeString(mark_img_back);
        dest.writeString(mark_img_des_front);
        dest.writeString(mark_img_des_back);
        dest.writeString(specialization);
        dest.writeInt(approval);
        dest.writeString(shape_code);
        dest.writeString(updated);
        dest.writeString(searchable);
//        dest.writeString(descriptor_front);
//        dest.writeString(descriptor_back);
        dest.writeInt(matched_count_front);
        dest.writeInt(matched_count_back);
    }

    public String getMark_img_des_front() {
        return mark_img_des_front;
    }

    public String getShape() {
        return shape;
    }

    public String getMark_img_des_back() {
        return mark_img_des_back;
    }

    public String getDes() {
        return des;
    }

//    public String getDescriptor_front() {
//        return descriptor_front;
//    }

    public String getSearchable() {
        return searchable;
    }

//    public String getDescriptor_back() {
//        return descriptor_back;
//    }

    public String getMark_front() {
        return mark_front;
    }

    public String getMinor_axis() {
        return minor_axis;
    }

    public String getP_name() {
        return p_name;
    }

    public String getThickness() {
        return thickness;
    }

    public String getColor_back() {
        return color_back;
    }

    public String getColor_front() {
        return color_front;
    }

    public String getMark_img_front() {
        return mark_img_front;
    }

    public String getMark_img_back() {
        return mark_img_back;
    }

    public String getSpecialization() {
        return specialization;
    }

    public String getShape_code() {
        return shape_code;
    }

    public String getClass_no() {
        return class_no;
    }

    public String getMark_back() {
        return mark_back;
    }

    public String getMajor_axis() {
        return major_axis;
    }

    public String getImg() {
        return img;
    }

    public String getDiv_front() {
        return div_front;
    }

    public String getDiv_back() {
        return div_back;
    }

    public String getClass_name() {
        return class_name;
    }

    public String getC_name() {
        return c_name;
    }

    public int getApproval() {
        return approval;
    }

    public int getP_no() {
        return p_no;
    }

    public int getC_no() {
        return c_no;
    }

    public int getImg_created() {
        return img_created;
    }

    public int getMatched_count_back() {
        return matched_count_back;
    }

    public int getMatched_count_front() {
        return matched_count_front;
    }

    public String getUpdated() {
        return updated;
    }

    public void setMatched_count_back(int matched_count_back) {
        this.matched_count_back = matched_count_back;
    }

    public void setMatched_count_front(int matched_count_front) {
        this.matched_count_front = matched_count_front;
    }


    public int getMaxMatchedCount(){
        if(matched_count_front >= matched_count_back) return  matched_count_front;
        else return matched_count_back;
    }


}
