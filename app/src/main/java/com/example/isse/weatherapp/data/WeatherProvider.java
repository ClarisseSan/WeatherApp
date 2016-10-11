package com.example.isse.weatherapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by isse on 05/10/2016.
 */

public class WeatherProvider extends ContentProvider {

    private static final String LOG_TAG = WeatherProvider.class.getSimpleName();
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private WeatherDbHelper mWeatherDbHelper;

    /*code for URI matcher*/
    private final static int WEATHER = 1;
    private final static int WEATHER_WITH_ID = 2;


    private static UriMatcher buildUriMatcher() {

        // Build a UriMatcher by adding a specific code to return based on a match
        // It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = WeatherContract.CONTENT_AUTHORITY;

        // add a code for each type of URI you want
        //URI for details
        matcher.addURI(authority, WeatherContract.WeatherEntry.TABLE_WEATHER, WEATHER);
        matcher.addURI(authority, WeatherContract.WeatherEntry.TABLE_WEATHER + "/#", WEATHER_WITH_ID);
        return matcher;

    }


    @Override
    public boolean onCreate() {
        mWeatherDbHelper = new WeatherDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        //query --> select statement in SQL

        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // return all weather forecast
            case WEATHER: {
                //readableDatabase because you're ony gonna fetch data
                retCursor = mWeatherDbHelper.getReadableDatabase().query(
                        WeatherContract.WeatherEntry.TABLE_WEATHER,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                return retCursor;
            }

            // Individual weather based on Id selected
            case WEATHER_WITH_ID: {
                retCursor = mWeatherDbHelper.getReadableDatabase().query(
                        WeatherContract.WeatherEntry.TABLE_WEATHER,
                        projection,//returns all columns
                        WeatherContract.WeatherEntry._ID + " = ?",//with a weather id of ?
                        new String[]{String.valueOf(ContentUris.parseId(uri))},
                        null,
                        null,
                        sortOrder);
                return retCursor;

            }
            default: {
                // By default, we assume a bad URI
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case WEATHER: {
                //returns all movies
                return WeatherContract.WeatherEntry.CONTENT_DIR_TYPE;
            }
            case WEATHER_WITH_ID: {
                //return a movie with the id
                return WeatherContract.WeatherEntry.CONTENT_ITEM_TYPE;
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        //open writable database for you're gonna insert data
        final SQLiteDatabase db = mWeatherDbHelper.getWritableDatabase();

        Uri returnUri;

        Log.e("My URI =============>", uri.toString());

        switch (sUriMatcher.match(uri)) {
            case WEATHER: {
                long _id = db.insert(WeatherContract.WeatherEntry.TABLE_WEATHER, null, contentValues);
                // insert unless it is already contained in the database
                if (_id > 0) {
                    returnUri = WeatherContract.WeatherEntry.buildDetailsUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into: " + uri);
                }
                break;
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] strings) {
        final SQLiteDatabase db = mWeatherDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if (null == selection) selection = "1";
        switch (match) {
            case WEATHER:
                rowsDeleted = db.delete(
                        WeatherContract.WeatherEntry.TABLE_WEATHER, selection, strings);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }
}
