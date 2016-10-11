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

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by isse on 05/10/2016.
 */

public class WeatherContract {


    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public WeatherContract() {
    }


    //Content provider variables
    public static final String CONTENT_AUTHORITY = "com.example.isse.weatherapp";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);


    public static abstract class WeatherEntry implements BaseColumns {
        //DETAIL TABLE
        public static final String TABLE_WEATHER = "weather_tbl";
        public static final String COLUMN_DAY = "day";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_HIGH = "high";
        public static final String COLUMN_LOW = "low";
        public static final String COLUMN_TEMP_DAY = "temp_day";
        public static final String COLUMN_TEMP_NIGHT = "temp_night";
        public static final String COLUMN_TEMP_EVENING = "temp_evening";
        public static final String COLUMN_TEMP_MORNING = "temp_morning";
        public static final String COLUMN_ICON = "icon";
        public static final String COLUMN_HUMIDITY = "humidity";
        public static final String COLUMN_RAIN = "rain";
        public static final String COLUMN_WIND = "wind";


        // create content uri
        //content://com.example.isse.weatherapp/weather_tbl
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(TABLE_WEATHER).build();
        // create cursor of base type directory for multiple entries
        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + TABLE_WEATHER;
        // create cursor of base type item for single entry
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + TABLE_WEATHER;

        // for building URIs with ID
        public static Uri buildDetailsUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }


    }

}
