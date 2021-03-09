package com.nexysquare.ddoyac;

import android.app.Application;
import android.os.Environment;

import com.nexysquare.ddoyac.model.Migration;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class GlobalApp extends Application {
    public final static String IMAGE_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()+"/drugsdata/drug_data_cut_01";
    @Override
    public void onCreate() {
        super.onCreate();


        Realm.init(this);
//        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder().assetFile("raw/default.realm").schemaVersion(0).build();

//        Realm.setDefaultConfiguration(realmConfiguration);

//        try {
//            Realm.migrateRealm(realmConfiguration, new RealmMigration() {
//                @Override
//                public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
//                    RealmSchema schema = realm.getSchema();
//                    if (oldVersion == 0) {
//                        schema.create("Drugs")
//                                .addField("p_no", int.class)
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
//                                .addField("img_created", String.class)
//                                .addField("class_no", String.class)
//                                .addField("class_name", String.class)
//
//                                .addField("specialization", String.class)
//                                .addField("approval", int.class)
//                                .addField("shape_code", String.class)
//                                .addField("updated", int.class);
//
//
//                        schema.create("PillRealm")
//                                .addField("name", String.class)
//                                .addField("descriptor", String.class)
//                                .addField("matched", int.class);
//
//                        oldVersion++;
//                    }
//                }
//            });
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
    }

    public static RealmConfiguration getRealmConfiguration(){

        RealmConfiguration config = new RealmConfiguration.Builder()
                .assetFile("drugs.realm")
                .migration(new Migration())

//                .compactOnLaunch()
//                .readOnly()
                //.deleteRealmIfMigrationNeeded()
                .schemaVersion(2)

                .build();

        return config;

    }
}
