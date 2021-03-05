package com.nexysquare.ddoyac.model;

import java.util.concurrent.atomic.AtomicInteger;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class Drug extends RealmObject {
    public static final String FIELD_ID = "id";

    private static AtomicInteger INTEGER_COUNTER = new AtomicInteger(0);

    @PrimaryKey
    private int id;

    private int p_no;

    @Required
    private String p_name;

    private int c_no;
    @Required
    private String c_name;
    @Required
    private String des;
    @Required
    private String img;
    @Required
    private String mark_front;
    @Required
    private String mark_back;
    @Required
    private String shape;
    @Required
    private String color_front;
    @Required
    private String color_back;
    @Required
    private String div_front;
    @Required
    private String div_back;
    @Required
    private String major_axis;
    @Required
    private String minor_axis;
    @Required
    private String thickness;

    private int img_created;
    @Required
    private String class_no;
    @Required
    private String class_name;

    @Required
    private String mark_img_front;
    @Required
    private String mark_img_back;


    @Required
    private String mark_img_des_front;
    @Required
    private String mark_img_des_back;


    @Required
    private String specialization;
    private int approval;
    @Required
    private String shape_code;
    @Required
    private String updated;

    private String searchable;


    private String descriptor_front;

    private String descriptor_back;


    @Ignore
    private int matched_count_front;

    @Ignore
    private int matched_count_back;

//    public String getCountString() {
//        return Integer.toString(id);
//    }
//
//    //  create() & delete() needs to be called inside a transaction.
//    static void create(Realm realm) {
//        create(realm, false);
//    }

//    static void create(Realm realm, boolean randomlyInsert) {
//        Parent parent = realm.where(Parent.class).findFirst();
//        RealmList<Item> items = parent.getItemList();
//        Item counter = realm.createObject(Item.class, increment());
//        if (randomlyInsert && items.size() > 0) {
//            Random rand = new Random();
//            items.listIterator(rand.nextInt(items.size())).add(counter);
//        } else {
//            items.add(counter);
//        }
//    }

    static void delete(Realm realm, long id) {
        Drug item = realm.where(Drug.class).equalTo(FIELD_ID, id).findFirst();
        // Otherwise it has been deleted already.
        if (item != null) {
            item.deleteFromRealm();
        }
    }

    private static int increment() {
        return INTEGER_COUNTER.getAndIncrement();
    }


    public int getC_no() {
        return c_no;
    }

    public int getP_no() {
        return p_no;
    }


    public int getApproval() {
        return approval;
    }

    public String getC_name() {
        return c_name;
    }

    public String getDes() {
        return des;
    }

    public String getClass_name() {
        return class_name;
    }

    public String getColor_back() {
        return color_back;
    }

    public String getColor_front() {
        return color_front;
    }

    public String getDiv_back() {
        return div_back;
    }

    public String getMark_img_back() {
        return mark_img_back;
    }

    public String getMark_img_front() {
        return mark_img_front;
    }

    public String getDiv_front() {
        return div_front;
    }

    public String getImg() {
        return img;
    }

    public String getMajor_axis() {
        return major_axis;
    }

    public String getMark_back() {
        return mark_back;
    }

    public int getImg_created() {
        return img_created;
    }

    public String getClass_no() {
        return class_no;
    }

    public String getMark_front() {
        return mark_front;
    }

    public String getMinor_axis() {
        return minor_axis;
    }

    public String getUpdated() {
        return updated;
    }

    public String getP_name() {
        return p_name;
    }

    public String getShape() {
        return shape;
    }

    public String getShape_code() {
        return shape_code;
    }

    public String getSpecialization() {
        return specialization;
    }

    public String getThickness() {
        return thickness;
    }

    public void setApproval(int approval) {
        this.approval = approval;
    }


    public void setC_name(String c_name) {
        this.c_name = c_name;
    }

    public void setC_no(int c_no) {
        this.c_no = c_no;
    }

    public void setClass_name(String class_name) {
        this.class_name = class_name;
    }

    public void setClass_no(String class_no) {
        this.class_no = class_no;
    }

    public void setDes(String des) {
        this.des = des;
    }

    public void setDiv_back(String div_back) {
        this.div_back = div_back;
    }

    public void setDiv_front(String div_front) {
        this.div_front = div_front;
    }


    public void setColor_back(String color_back) {
        this.color_back = color_back;
    }

    public void setColor_front(String color_front) {
        this.color_front = color_front;
    }

    public void setMark_img_back(String mark_img_back) {
        this.mark_img_back = mark_img_back;
    }

    public void setMark_img_front(String mark_img_front) {
        this.mark_img_front = mark_img_front;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public void setImg_created(int img_created) {
        this.img_created = img_created;
    }

    public void setMajor_axis(String major_axis) {
        this.major_axis = major_axis;
    }

    public void setMark_back(String mark_back) {
        this.mark_back = mark_back;
    }

    public void setMark_front(String mark_front) {
        this.mark_front = mark_front;
    }

    public void setMinor_axis(String minor_axis) {
        this.minor_axis = minor_axis;
    }

    public void setP_name(String p_name) {
        this.p_name = p_name;
    }

    public void setP_no(int p_no) {
        this.p_no = p_no;
    }

    public void setShape(String shape) {
        this.shape = shape;
    }

    public void setShape_code(String shape_code) {
        this.shape_code = shape_code;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public void setThickness(String thickness) {
        this.thickness = thickness;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSearchable() {
        return searchable;
    }

    public void setSearchable(String searchable) {
        this.searchable = searchable;
    }


    public void setDescriptor_back(String descriptor_back) {
        this.descriptor_back = descriptor_back;
    }

    public void setDescriptor_front(String descriptor_front) {
        this.descriptor_front = descriptor_front;
    }

    public String getDescriptor_back() {
        return descriptor_back;
    }

    public String getDescriptor_front() {
        return descriptor_front;
    }

    public String getMark_img_des_back() {
        return mark_img_des_back;
    }

    public String getMark_img_des_front() {
        return mark_img_des_front;
    }

    public void setMatched_count_front(int matched_count_front) {
        this.matched_count_front = matched_count_front;
    }

    public void setMatched_count_back(int matched_count_back) {
        this.matched_count_back = matched_count_back;
    }

    public int getMatched_count_front() {
        return matched_count_front;
    }

    public int getMatched_count_back() {
        return matched_count_back;
    }
    public int getMaxMatchedCount(){
        if(matched_count_front >= matched_count_back) return  matched_count_front;
        else return matched_count_back;
    }

}

