package com.nexysquare.ddoyac.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Pill implements Parcelable {
    private String id;
    private String name;
    private int matched;
    private String descriptor;
    public Pill(){

    }

    protected Pill(Parcel in) {
        id = in.readString();
        name = in.readString();
        matched = in.readInt();
        descriptor = in.readString();
    }

    public static final Creator<Pill> CREATOR = new Creator<Pill>() {
        @Override
        public Pill createFromParcel(Parcel in) {
            return new Pill(in);
        }

        @Override
        public Pill[] newArray(int size) {
            return new Pill[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeInt(matched);
        dest.writeString(descriptor);
    }

    public void setDescriptor(String descriptor) {
        this.descriptor = descriptor;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setMatched(int matched) {
        this.matched = matched;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public int getMatched() {
        return matched;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public String getName() {
        return name;
    }
}
