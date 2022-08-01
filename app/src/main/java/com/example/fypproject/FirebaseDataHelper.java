package com.example.fypproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class FirebaseDataHelper extends SQLiteOpenHelper {

    public static final String FIREBASE_DATA = "FIREBASE_DATA";
    public static final String ID = "ID";
    public static final String CAR_PLATE = "CAR_PLATE";
    public static final String DRIVER_ID = "DRIVE_ID";
    public static final String DRIVER_NAME = "DRIVER_NAME";
    public static final String COLOR = "COLOR";
    public static final String BRAND = "BRAND";
    public static final String PARKING_SPACE = "PARKING_SPACE";
    public static final String CARPARK_TYPE = "CARPARK_TYPE";


    public FirebaseDataHelper(@Nullable Context context) {
        super(context, "firebase.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableStatement = "CREATE TABLE " + FIREBASE_DATA + " (" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + CAR_PLATE + " TEXT, " + DRIVER_ID + " TEXT, " + DRIVER_NAME + " TEXT, " + COLOR + " TEXT, "+ BRAND + " TEXT, "+ PARKING_SPACE +" TEXT,"+ CARPARK_TYPE +" TEXT)";
        db.execSQL(createTableStatement);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public  boolean addOne (FirebaseData firebaseData){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(CAR_PLATE, firebaseData.getCar_numFb());
        cv.put(DRIVER_ID, firebaseData.getDriver_id());
        cv.put(DRIVER_NAME, firebaseData.getDriver_name());
        cv.put(COLOR, firebaseData.getColor());
        cv.put(BRAND, firebaseData.getBrand());
        cv.put(PARKING_SPACE, firebaseData.getApproved_parking());
        cv.put(CARPARK_TYPE, firebaseData.getCarpark_type());

        long insert =  db.insert(FIREBASE_DATA,null,cv);

        if (insert == -1){
            return  false;
        } else{
            return true;
        }
    }

    public FirebaseData getCarDetails(String car_num){
        FirebaseData firebaseData1 = null;

        String queryString = "SELECT * FROM " + FIREBASE_DATA + " WHERE CAR_PLATE=" + "'" + car_num + "'";
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(queryString, null);
        if (cursor.moveToFirst()){
            do {
                int carID = cursor.getInt(0);
                String carNum = cursor.getString(1);
                String driver_id = cursor.getString(2);
                String driver_name = cursor.getString(3);
                String color = cursor.getString(4);
                String brand = cursor.getString(5);
                String parking = cursor.getString(6);
                String type = cursor.getString(7);

                firebaseData1 = new FirebaseData(carID,carNum,driver_id,driver_name,color,brand,parking,type);

            }while (cursor.moveToNext());
        }else {
        }
        cursor.close();
        db.close();
        return firebaseData1;
    }

    public boolean deleteAll(){

        SQLiteDatabase db = this.getWritableDatabase();
        String queryString = "DELETE FROM " + FIREBASE_DATA;
        Cursor cursor = db.rawQuery(queryString,null);

        if (cursor.moveToFirst()){
            return true;
        }else {
            return false;
        }
    }
}