package com.example.fypproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ScannedVehiclesHelper extends SQLiteOpenHelper {

    public static final String CARS_TABLE = "CARS_TABLE";
    public static final String ID = "ID";
    public static final String CAR_PLATE = "CAR_PLATE";
    public static final String LOCATION = "LOCATION";
    public static final String TIME = "TIME";
    public static final String DATE = "DATE";
    public static final String TYPE = "TYPE";
    public static final String RESULT = "RESULT";

    public String date;

    public ScannedVehiclesHelper(@Nullable Context context) {
        super(context, "scannedvehicles.db", null, 1);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS '" + CARS_TABLE + "'");
        String createTableStatement = "CREATE TABLE " + CARS_TABLE + " (" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + CAR_PLATE + " TEXT, " + LOCATION + " TEXT, " + DATE + " TEXT, " + TIME + " TEXT, " + TYPE + " TEXT, " + RESULT + " TEXT)";
        db.execSQL(createTableStatement);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public  boolean addOne (ScannedVehicles cars){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(CAR_PLATE, cars.getCar_num());
        cv.put(LOCATION, cars.getLocation());
        cv.put(DATE, cars.getDate());
        cv.put(TIME, cars.getTime());
        cv.put(TYPE, cars.getType());
        cv.put(RESULT, cars.getResults());

        long insert =  db.insert(CARS_TABLE,null,cv);

        if (insert == -1){
            return  false;
        } else{
            return true;
        }
    }

    public List<ScannedVehicles> getEveryThing(){
        List<ScannedVehicles> returnList = new ArrayList<>();
        date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());

        String queryString = "SELECT * FROM " + CARS_TABLE + " WHERE DATE=" +"'" + date + "'";
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(queryString, null);
        if (cursor.moveToFirst()){
            do {
                int carID = cursor.getInt(0);
                String carNum = cursor.getString(1);
                String location = cursor.getString(2);
                String date = cursor.getString(3);
                String time = cursor.getString(4);
                String type = cursor.getString(5);
                String result = cursor.getString(6);

                ScannedVehicles cars = new ScannedVehicles(carID,carNum,location,date,time,type,result);
                returnList.add(cars);

            }while (cursor.moveToNext());
        }else {
        }
        cursor.close();
        db.close();
        return  returnList;
    }

    public boolean deleteOld(String date1){
        SQLiteDatabase db = this.getWritableDatabase();
        String queryString = "DELETE FROM " + CARS_TABLE + " WHERE DATE=" + "'" + date1 + "'";
        Cursor cursor = db.rawQuery(queryString,null);

        if (cursor.moveToFirst()){
            return true;
        }else {
            return false;
        }
    }
}