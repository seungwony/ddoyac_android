package com.nexysquare.ddoyac.model;

import io.realm.Realm;
import io.realm.RealmObject;

public class Contraindicant  extends RealmObject {
    String conType;
    String A_ingreCode;
    String A_productCode;
    String A_productName;
    String A_entpName;
    String A_pay;

    String B_ingreCode;
    String B_productCode;
    String B_productName;
    String B_entpName;
    String B_pay;

    String created_no;
    String created_date;

    String des;

    public String getDes() {
        return des;
    }

    public String getA_ingreCode() {
        return A_ingreCode;
    }

    public String getA_pay() {
        return A_pay;
    }

    public String getA_productCode() {
        return A_productCode;
    }

    public String getA_productName() {
        return A_productName;
    }

    public String getA_entpName() {
        return A_entpName;
    }

    public String getB_entpName() {
        return B_entpName;
    }

    public String getB_ingreCode() {
        return B_ingreCode;
    }

    public String getB_pay() {
        return B_pay;
    }

    public String getB_productCode() {
        return B_productCode;
    }

    public String getB_productName() {

        return B_productName;
    }

    public String getCreated_date() {
        return created_date;
    }



    public String getCreated_no() {
        return created_no;
    }

    public String getConType() {
        return conType;
    }

    public void setDes(String des) {
        this.des = des;
    }

    public void setA_ingreCode(String a_ingreCode) {
        A_ingreCode = a_ingreCode;
    }

    public void setA_pay(String a_pay) {
        A_pay = a_pay;
    }

    public void setA_productCode(String a_productCode) {
        A_productCode = a_productCode;
    }

    public void setA_productName(String a_productName) {
        A_productName = a_productName;
    }

    public void setA_entpName(String a_entpName) {
        A_entpName = a_entpName;
    }

    public void setB_ingreCode(String b_ingreCode) {
        B_ingreCode = b_ingreCode;
    }

    public void setB_pay(String b_pay) {
        B_pay = b_pay;
    }

    public void setB_productCode(String b_productCode) {
        B_productCode = b_productCode;
    }

    public void setB_productName(String b_productName) {
        B_productName = b_productName;
    }

    public void setB_entpName(String b_entpName) {
        B_entpName = b_entpName;
    }

    public void setCreated_date(String created_date) {
        this.created_date = created_date;
    }

    public void setCreated_no(String created_no) {
        this.created_no = created_no;
    }

    public void setConType(String conType) {
        this.conType = conType;
    }

    //    구분
//    성분명A
//    성분코드A
//    제품코드A
//    제품명A
//    업체명A
//    급여여부A
//
//    성분명B
//    성분코드B
//    제품코드B
//    제품명B
//    업체명B
//    급여여부B
//
//    고시번호
//    고시일자
//
//            상세정보

    public String toString(){
        return getConType() + " " + getA_ingreCode() + " " + getA_productCode() + " " + getA_productName() + " " + getA_entpName()
                + " " + getA_pay()  + " " + getB_ingreCode()  + " " + getB_productCode()  + " " + getB_productName()  + " " + getB_pay()
                + " " + getCreated_date() + " " + getCreated_no()  + " " + getDes();
    }

}
