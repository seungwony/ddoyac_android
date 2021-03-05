package com.nexysquare.ddoyac.model;

import android.util.Log;

import androidx.annotation.Nullable;

import io.realm.DynamicRealm;
import io.realm.DynamicRealmObject;
import io.realm.FieldAttribute;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

public class Migration implements RealmMigration {

    private int currentKey = 0;
    @Override
    public int hashCode() {
        return Migration.class.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object object) {
        if(object == null) {
            return false;
        }
        return object instanceof Migration;
    }

    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {

        Log.d("Migration", "old version: " + oldVersion  + " new version : " + newVersion);

        RealmSchema schema = realm.getSchema();


        if (oldVersion < 1) {


            if(!schema.get("Drug").hasField("id")){
                schema.get("Drug").addField("id", int.class, FieldAttribute.INDEXED)
                        .transform(new RealmObjectSchema.Function() {
                            @Override
                            public void apply(DynamicRealmObject obj) {
                                obj.setInt("id",currentKey++);
                            }
                        }).addPrimaryKey("id");
            }


//            descriptor_back, descriptor_front
            if(!schema.get("Drug").hasField("descriptor_front")) {
                schema.get("Drug").addField("descriptor_front", String.class);
            }
            if(!schema.get("Drug").hasField("descriptor_back")) {
                schema.get("Drug").addField("descriptor_back", String.class);
            }




            if(!schema.get("Drug").hasField("searchable")){
                schema.get("Drug").addField("searchable", String.class)
                        .transform(new RealmObjectSchema.Function() {
                            @Override
                            public void apply(DynamicRealmObject obj) {

                                String p_name = obj.getString("p_name");

                                String mark_front = obj.getString("mark_front");
                                String mark_back = obj.getString("mark_back");

                                String mark_img_des_front = obj.getString("mark_img_des_front");
                                String mark_img_des_back = obj.getString("mark_img_des_back");

                                String re_mark_front = obj.getString("mark_front").replaceAll("[^A-Za-z0-9\\s]", "");

                                String re_mark_back = obj.getString("mark_back").replaceAll("[^A-Za-z0-9\\s]", "");

                                String searchable = p_name+" "+mark_front + " " + mark_back +  " " + re_mark_front+ " "+re_mark_back + " " + mark_img_des_front + " " + mark_img_des_back;
                                obj.setString("searchable", searchable);
                            }
                        });
            }


//            schema.create("Drug").addPrimaryKey("p_no");
//                        schema.create("Drugs")
//                                .addField("p_no", int.class)
//
//                                .addField("p_name", String.class)
//                                .addField("c_no", int.class)
//                                .addField("c_name", String.class)
//                                .addField("des", String.class)
//                                .addField("img", String.class)
//                                .addField("mark_front", String.class)
//                                .addField("mark_back", String.class)
//                                .addField("shape", String.class)
//                                .addField("front_color", String.class)
//                                .addField("back_color", String.class)
//
//                                .addField("div_front", String.class)
//                                .addField("div_back", String.class)
//
//                                .addField("major_axis", String.class)
//                                .addField("minor_axis", String.class)
//
//                                .addField("thickness", String.class)
//
//                                .addField("img_created", int.class)
//                                .addField("class_no", String.class)
//                                .addField("class_name", String.class)
//
//                                .addField("specialization", String.class)
//                                .addField("approval", int.class)
//                                .addField("shape_code", String.class)
//                                .addField("updated", String.class);


//                                .addField("p_name", String.class).setNullable("p_name", TRUE)
//                                .addField("c_no", int.class)
//                                .addField("c_name", String.class).setNullable("c_name", TRUE)
//                                .addField("des", String.class).setNullable("des", TRUE)
//                                .addField("img", String.class).setNullable("img", TRUE)
//                                .addField("mark_front", String.class).setNullable("mark_front", TRUE)
//                                .addField("mark_back", String.class).setNullable("mark_back", TRUE)
//                                .addField("shape", String.class).setNullable("shape", TRUE)
//                                .addField("front_color", String.class).setNullable("front_color", TRUE)
//                                .addField("back_color", String.class).setNullable("back_color", TRUE)
//
//                                .addField("div_front", String.class).setNullable("div_front", TRUE)
//                                .addField("div_back", String.class).setNullable("div_back", TRUE)
//
//                                .addField("major_axis", String.class).setNullable("major_axis", TRUE)
//                                .addField("minor_axis", String.class).setNullable("minor_axis", TRUE)
//
//                                .addField("thickness", String.class).setNullable("thickness", TRUE)
//
//                                .addField("img_created", int.class)
//                                .addField("class_no", String.class).setNullable("class_no", TRUE)
//                                .addField("class_name", String.class).setNullable("class_name", TRUE)
//
//                                .addField("specialization", String.class).setNullable("specialization", TRUE)
//                                .addField("approval", int.class)
//                                .addField("shape_code", String.class).setNullable("shape_code", TRUE)
//                                .addField("updated", String.class).setNullable("updated", TRUE);


                        schema.create("PillRealm")
                                .addField("name", String.class)
                                .addField("descriptor", String.class)
                                .addField("matched", int.class);


            schema.create("Contraindicant")
                    .addField("conType", String.class)
                    .addField("A_ingreCode", String.class)
                    .addField("A_productCode", String.class)
                    .addField("A_productName", String.class)
                    .addField("A_entpName", String.class)
                    .addField("A_pay", String.class)
                    .addField("B_ingreCode", String.class)
                    .addField("B_productCode", String.class)
                    .addField("B_productName", String.class)
                    .addField("B_entpName", String.class)
                    .addField("B_pay", String.class)
                    .addField("created_no", String.class)
                    .addField("created_date", String.class)
                    .addField("des", String.class);



                        oldVersion++;

                    }
    }

}
