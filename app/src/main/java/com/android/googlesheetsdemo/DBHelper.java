package com.android.googlesheetsdemo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by OPTIMUSDOM ubuntu151 on 8/12/15.
 */
public class DBHelper extends SQLiteOpenHelper {

    public static final String TABLE_EMPLOYEES = "Employees";
    public static final String COLUMN_EMPCODE = "Emp_Code";
    public static final String COLUMN_NAME = "Name";
    public static final String COLUMN_EMAIL = "Email_Id";
    public static final String COLUMN_DAYLUNCH = "Day_Lunch";

    public static final String DATABASE_NAME = "EmployeesLunch.db";
    private static final int DATABASE_VERSION = 1;

    // Database creation sql statement
    private static final String DATABASE_CREATE = "CREATE TABLE "
            + TABLE_EMPLOYEES + " (" + COLUMN_EMPCODE + " INTEGER PRIMARY KEY,"
            + " " + COLUMN_NAME + " VARCHAR(255)," + " " + COLUMN_EMAIL
            + " VARCHAR(255)," + " " + COLUMN_DAYLUNCH + " INTEGER);";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EMPLOYEES);
        onCreate(db);
    }
}