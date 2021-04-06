package com.nexysquare.ddoyac.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.nexysquare.ddoyac.Constants;

public class SavedDrugDBHelper extends SQLiteOpenHelper {


    private static final String TABLE_NAME = "saved_drug";

    private static final String COL_1 = "id";
    private static final String COL_2 = "priority";
    private static final String COL_3 = "rel_id";
    private static final String COL_4 = "created";


    public SavedDrugDBHelper(@Nullable Context context) {
        super(context, Constants.DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("create table " + TABLE_NAME + " (id INTEGER, priority INTEGER, rel_id INTEGER, created TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
    }


    public boolean insertData(int id, int rel_id, int priority, String created){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2,priority);
        contentValues.put(COL_3,rel_id);
        contentValues.put(COL_4,created);

        long result = db.insert(TABLE_NAME, null, contentValues);

        if(result == -1){
            return false;
        }else{
            return  true;
        }
    }


    public Cursor getAllData(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+ TABLE_NAME, null);
        return res;
    }


    public Cursor getAllDataById(int id){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+ TABLE_NAME + " where id = " + String.valueOf(id),  null);

        return res;
    }

    public boolean updateData(int id, int priority, int rel_id){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_1, id);
        contentValues.put(COL_2, priority);
        contentValues.put(COL_3, rel_id);
        db.update(TABLE_NAME, contentValues, "id = ? AND rel_id = ?", new String[]{String.valueOf(id), String.valueOf(rel_id)});
        return true;
    }

    public int deleteData(int id){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, "id = ?", new String[] {String.valueOf(id)});
    }
}
