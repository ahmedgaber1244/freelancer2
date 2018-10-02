package com.example.gaber.freelancer2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gaber on 21/07/2018.
 */

public class database_operations extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "agent_database";

    Context context;


    public database_operations(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context=context;
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {

        // create  table
        db.execSQL(data_model.CREATE_TABLE);

    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + data_model.TABLE_NAME);


        // Create tables again
        onCreate(db);
    }


    //data_model
    public void insert_data_model(String from,String to,String message,String type,String time,String storage_url)
    {
        database_operations mDbHelper = new database_operations(context);
        // get writable database as we want to write data
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

// Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(data_model.to_sql, to);
        values.put(data_model.from_sql, from);
        values.put(data_model.message_sql, message);
        values.put(data_model.type_sql, type);
        values.put(data_model.time_sql, time);
        values.put(data_model.storage_url_sql, storage_url);

// Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(data_model.TABLE_NAME, null, values);


        // return newly inserted row id

    }
    public List<data_model> getAll_notification_model(String from,String to)
    {
        List<data_model> data_modelList = new ArrayList<>();

        // Select All Query
        String countQuery = "SELECT  * FROM " + data_model.TABLE_NAME+" WHERE "
                +"("+data_model.to_sql+"=? AND "+data_model.from_sql+"=? )"+
                "OR ("+data_model.from_sql+"=? AND "+data_model.to_sql+"=? )";

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(countQuery, new String[]{to,from,to,from});

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                data_model data = new data_model();
                data.from=cursor.getString(cursor.getColumnIndex(data_model.from_sql));
                data.message=cursor.getString(cursor.getColumnIndex(data_model.message_sql));
                data.time=cursor.getString(cursor.getColumnIndex(data_model.time_sql));
                data.type=cursor.getString(cursor.getColumnIndex(data_model.type_sql));
                data.storage_url=cursor.getString(cursor.getColumnIndex(data_model.storage_url_sql));
                data_modelList.add(data);

            } while (cursor.moveToNext());
        }

        // close db connection
        db.close();

        // return notes list
        return data_modelList;
    }
    public int getmessagesCount()
    {

        String countQuery = "SELECT  * FROM " + data_model.TABLE_NAME;

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(countQuery, null);


        int count = cursor.getCount();

        cursor.close();



        // return count

        return count;
    }









}
