package com.android.googlesheetsdemo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

/**
 * Created by OPTIMUSDOM ubuntu151 on 8/12/15.
 */
public class EmployeeDAO {

    public static void createFromArrayList(ArrayList<EmployeeModel> list, Context context) {

        DBHelper helper = new DBHelper(context);
        SQLiteDatabase database = helper.getWritableDatabase();

        for (EmployeeModel item : list) {
            ContentValues content = new ContentValues();
            content.put(DBHelper.COLUMN_EMPCODE, item.getEmpCode());
            content.put(DBHelper.COLUMN_NAME, item.getName());
            content.put(DBHelper.COLUMN_EMAIL, item.getMailId());
            if (item.isLunchCheck()) {
                content.put(DBHelper.COLUMN_DAYLUNCH, 1);
            } else {
                content.put(DBHelper.COLUMN_DAYLUNCH, 0);
            }
            database.insert(DBHelper.TABLE_EMPLOYEES, null, content);
        }

        helper.close();
    }

    public static ArrayList<EmployeeModel> getArrayList(Context context) {

        DBHelper helper = new DBHelper(context);
        SQLiteDatabase database = helper.getWritableDatabase();

        ArrayList<EmployeeModel> employess = new ArrayList();
        Cursor cursor = database.rawQuery("SELECT * FROM " + DBHelper.TABLE_EMPLOYEES, null);
        EmployeeModel employee;

        if (cursor.moveToFirst()) {
            do {
                employee = new EmployeeModel();
                employee.setEmpCode(cursor.getInt(0));
                employee.setName(cursor.getString(1));
                employee.setMailId(cursor.getString(2));
                if (cursor.getInt(3) == 0) {
                    employee.setLunchCheck(false);
                } else {
                    employee.setLunchCheck(true);
                }

                employess.add(employee);
            } while (cursor.moveToNext());
        }
        cursor.close();
        helper.close();
        return employess;
    }

    public static void emptyTable(Context context) {

        DBHelper helper = new DBHelper(context);
        SQLiteDatabase database = helper.getWritableDatabase();

        database.execSQL("DELETE FROM " + DBHelper.TABLE_EMPLOYEES);

        helper.close();
    }
}
