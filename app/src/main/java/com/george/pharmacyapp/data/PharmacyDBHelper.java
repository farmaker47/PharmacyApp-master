package com.george.pharmacyapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class PharmacyDBHelper extends SQLiteOpenHelper {

    // Name of the database file
    private static final String DATABASE_NAME = "pharmacy.db";

    //Database version. If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    //Creating the constructor
    public PharmacyDBHelper(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        // Create a String that contains the SQL statement to create the pets table
        String SQL_CREATE_PHARMACY_TABLE =  "CREATE TABLE " + PharmacyContract.PharmacyEntry.TABLE_NAME + " ("
                + PharmacyContract.PharmacyEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + PharmacyContract.PharmacyEntry.COLUMN_NAME + " TEXT NOT NULL, "
                + PharmacyContract.PharmacyEntry.COLUMN_QUANTITY + " INTEGER, "
                + PharmacyContract.PharmacyEntry.COLUMN_PRICE + " INTEGER NOT NULL, "
                + PharmacyContract.PharmacyEntry.COLUMN_IMAGE + " TEXT);";

        // Execute the SQL statement
        sqLiteDatabase.execSQL(SQL_CREATE_PHARMACY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PharmacyContract.PharmacyEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
        sqLiteDatabase.close();
    }
}
