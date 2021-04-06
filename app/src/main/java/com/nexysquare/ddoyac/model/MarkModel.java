package com.nexysquare.ddoyac.model;

public class MarkModel {
    private String imgSrc;
    private String des;

    public MarkModel(String imgSrc, String des){
        this.imgSrc = imgSrc;
        this.des = des;
    }

    public String getDes() {
        return des;
    }

    public String getImgSrc() {
        return imgSrc;
    }

    public void setDes(String des) {
        this.des = des;
    }

    public void setImgSrc(String imgSrc) {
        this.imgSrc = imgSrc;
    }
}
