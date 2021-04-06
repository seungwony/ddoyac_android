package com.nexysquare.ddoyac.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.nexysquare.ddoyac.Constants;

import java.util.Date;

public class SavedDatabaseHelper extends SQLiteOpenHelper {


    private static final String MAIN_TABLE_NAME = "saved_list";
    private static final String REL_TABLE_NAME = "saved_drug";

    public static final String COL_1 = "id";
    public static final String COL_2 = "priority";
    public static final String COL_3 = "name";
    public static final String COL_4 = "created";


    public SavedDatabaseHelper(@Nullable Context context) {
        super(context, Constants.DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("create table " + MAIN_TABLE_NAME + " (id INTEGER PRIMARY KEY AUTOINCREMENT, priority INTEGER, name TEXT, created TEXT)");
        db.execSQL("create table " + REL_TABLE_NAME + " (id INTEGER, priority INTEGER, rel_id INTEGER, created TEXT)");

        db.execSQL("INSERT INTO " + MAIN_TABLE_NAME + " (priority, name, created) VALUES (0, '기본 저장 목록', '"+DateUtil.convertedSimpleFormat(new Date())+"')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS "+MAIN_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS "+REL_TABLE_NAME);
    }


    public boolean insertData(int priority, String name, String created){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2,priority);
        contentValues.put(COL_3,name);
        contentValues.put(COL_4,created);

        long result = db.insert(MAIN_TABLE_NAME, null, contentValues);

        if(result == -1){
            return false;
        }else{
            return  true;

        }

    }



    public boolean insertDrugData(int id, int rel_id, int priority, String created){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("id",id);
        contentValues.put("priority",priority);
        contentValues.put("rel_id",rel_id);
        contentValues.put("created",created);

        long result = db.insert(REL_TABLE_NAME, null, contentValues);

        if(result == -1){
            return false;
        }else{
            return  true;

        }

    }

    public int getRelDrugDataCount(int id, int rel_id){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+ REL_TABLE_NAME + " where id = " + String.valueOf(id) + " AND rel_id = " + String.valueOf(rel_id),  null);
        return res.getCount();
    }


    public int getRelDrugDataCount(int id){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+ REL_TABLE_NAME + " where id = " + String.valueOf(id) ,  null);
        return res.getCount();
    }

    public Cursor getAllDrugDataById(int id){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+ REL_TABLE_NAME + " where id = " + String.valueOf(id) + " order by priority DESC",  null);

        return res;
    }

    public int deleteDrugData(int id){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(REL_TABLE_NAME, "id = ?", new String[] {String.valueOf(id)});
    }

    public int deleteDrugDataWithRel(int id, int rel_id){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(REL_TABLE_NAME, "id = ? AND rel_id = ?", new String[] {String.valueOf(id), String.valueOf(rel_id)});
    }


    public Cursor getAllData(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+ MAIN_TABLE_NAME + " order by priority DESC", null);
        return res;
    }


    public boolean updateData(int id, int priority, String name){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_1, id);
        contentValues.put(COL_2, priority);
        contentValues.put(COL_3, name);
        db.update(MAIN_TABLE_NAME, contentValues, "id = ?", new String[]{String.valueOf(id)});
        return true;
    }

    public boolean updateData(int id,  String name){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_1, id);
        contentValues.put(COL_3, name);
        db.update(MAIN_TABLE_NAME, contentValues, "id = ?", new String[]{String.valueOf(id)});
        return true;
    }

    public int deleteData(int id){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(MAIN_TABLE_NAME, "id = ?", new String[] {String.valueOf(id)});
    }
}
