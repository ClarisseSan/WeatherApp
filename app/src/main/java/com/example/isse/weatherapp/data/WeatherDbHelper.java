/*
* Copyright 2016 Angela Sanchez

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
* */

package com.example.isse.weatherapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class WeatherDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "weather.db";
    private static final int DATABASE_VERSION = 4;

    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER DEFAULT 0";
    private static final String COMMA_SEP = ",";


    /* query for creating detail_tbl*/
    private static final String SQL_CREATE_TABLE_WEATHER =
            "CREATE TABLE " + WeatherContract.WeatherEntry.TABLE_WEATHER + " (" +
                    WeatherContract.WeatherEntry._ID + " INTEGER PRIMARY KEY," +
                    WeatherContract.WeatherEntry.COLUMN_DAY + TEXT_TYPE + COMMA_SEP +
                    WeatherContract.WeatherEntry.COLUMN_DATE + TEXT_TYPE + COMMA_SEP +
                    WeatherContract.WeatherEntry.COLUMN_DESCRIPTION + TEXT_TYPE + COMMA_SEP +
                    WeatherContract.WeatherEntry.COLUMN_HIGH + TEXT_TYPE + COMMA_SEP +
                    WeatherContract.WeatherEntry.COLUMN_LOW + TEXT_TYPE + COMMA_SEP +
                    WeatherContract.WeatherEntry.COLUMN_TEMP_DAY + TEXT_TYPE + COMMA_SEP +
                    WeatherContract.WeatherEntry.COLUMN_TEMP_EVENING + TEXT_TYPE + COMMA_SEP +
                    WeatherContract.WeatherEntry.COLUMN_TEMP_MORNING + TEXT_TYPE + COMMA_SEP +
                    WeatherContract.WeatherEntry.COLUMN_TEMP_NIGHT + TEXT_TYPE + COMMA_SEP +
                    WeatherContract.WeatherEntry.COLUMN_ICON + TEXT_TYPE + COMMA_SEP +
                    WeatherContract.WeatherEntry.COLUMN_WIND + TEXT_TYPE + COMMA_SEP +
                    WeatherContract.WeatherEntry.COLUMN_RAIN + TEXT_TYPE + COMMA_SEP +
                    WeatherContract.WeatherEntry.COLUMN_HUMIDITY + INTEGER_TYPE +
                    " )";

    /* query for deleting review_tbl*/
    private static final String SQL_DELETE_DETAIL =
            "DROP TABLE IF EXISTS " + WeatherContract.WeatherEntry.TABLE_WEATHER;


    public WeatherDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        //sql statement to create a table
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE_WEATHER);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        sqLiteDatabase.execSQL(SQL_DELETE_DETAIL);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onDowngrade(db, oldVersion, newVersion);
    }
}
