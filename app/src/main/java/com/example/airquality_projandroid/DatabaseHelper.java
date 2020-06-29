package com.example.airquality_projandroid;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "citys.db";
    private static final int DATABASE_VERSION = 3;
    public static final String TABLE_NAME = "City";
    public static final String COLUMN_NAME = "_name";
    public static final String COLUMN_AQI = "aqi";
    public static final String COLUMN_TIMESTAMP = "timestamp";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(" CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                COLUMN_NAME + " BLOB NOT NULL, " +
                COLUMN_AQI + " BLOB NOT NULL, " +
                COLUMN_TIMESTAMP + " BLOB NOT NULL);"
        );
    }

    /**
     * Returns all the data from database
     * @return
     */
    public Cursor getData(){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME;
        Cursor data = db.rawQuery(query, null);
        return data;
    }

    /**
     * create record
     **/
    public void saveCityRecord(CityData cityData) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, cityData.getCityName());
        values.put(COLUMN_AQI, cityData.getCityAQI());
        values.put(COLUMN_TIMESTAMP, cityData.getCityTimeStamp());
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    /**
     * delete record
     **/
    public void deleteCityRecord(String name, Context context) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME + " WHERE _name='" + name + "'");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        this.onCreate(sqLiteDatabase);
    }
}
