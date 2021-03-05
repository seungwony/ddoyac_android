package com.nexysquare.ddoyac.model;

import android.os.Parcel;
import android.os.Parcelable;

public class MatchedInfo  implements Parcelable {

    int id ;

    int front_matched;
    int back_matched;

    public MatchedInfo(){

    }
    public MatchedInfo(int id, int front_matched, int back_matched, int max_matched){
        this.id = id;
        this.front_matched = front_matched;
        this.back_matched = back_matched;

    }

    protected MatchedInfo(Parcel in) {
        id = in.readInt();
        front_matched = in.readInt();
        back_matched = in.readInt();

    }

    public static final Creator<MatchedInfo> CREATOR = new Creator<MatchedInfo>() {
        @Override
        public MatchedInfo createFromParcel(Parcel in) {
            return new MatchedInfo(in);
        }

        @Override
        public MatchedInfo[] newArray(int size) {
            return new MatchedInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(front_matched);
        dest.writeInt(back_matched);
    }


    public void setId(int id) {
        this.id = id;
    }

    public void setBack_matched(int back_matched) {
        this.back_matched = back_matched;
    }

    public void setFront_matched(int front_matched) {
        this.front_matched = front_matched;
    }


    public int getId() {
        return id;
    }

    public int getBack_matched() {
        return back_matched;
    }

    public int getFront_matched() {
        return front_matched;
    }

    public int getMaxMatched(){
        if(front_matched>=back_matched) return front_matched;
        else return back_matched;
    }
}
