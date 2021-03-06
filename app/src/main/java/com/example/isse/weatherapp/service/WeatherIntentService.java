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

package com.example.isse.weatherapp.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.example.isse.weatherapp.data.WeatherContract;
import com.example.isse.weatherapp.task.DownloadWeatherTask;
import com.example.isse.weatherapp.utility.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Created by isse on 04/10/2016.
 */
public class WeatherIntentService extends IntentService {


    private static final String LOG_TAG = DownloadWeatherTask.class.getSimpleName();
    private Context mContext;
    private String mLatitude;
    private String mLongitude;


    public static final String LATITUDE = "lat";
    public static final String LONGITUDE = "long";

    public WeatherIntentService() {
        super(WeatherIntentService.class.getName());
    }

    public WeatherIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mContext = getApplicationContext();

        String latitude = intent.getStringExtra(LATITUDE);
        String longitude = intent.getStringExtra(LONGITUDE);


        mLatitude = latitude;
        mLongitude = longitude;

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        URL url = null;

        // Will contain the raw JSON response as a string.
        String forecastJsonStr = null;

        try {
            //"http://api.openweathermap.org/data/2.5/forecast/daily?lat=35&lon=139&cnt=10&mode=json&appid=ee3eae518914dcd05823d45966b449c3"
            final String BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";


            //url parameter
            final String LATITUDE = "lat";
            final String LONGITUDE = "lon";
            final String COUNT = "cnt";
            final String MODE = "mode";
            final String APP_ID = "appid";
            final String UNITS = "units";

            //values for parameters
            final String LAT = mLatitude;
            final String LON = mLongitude;

            Log.i(LOG_TAG, "latitude --> " + LAT);
            Log.i(LOG_TAG, "longitude --> " + LON);

            final String CNT = "10";
            final String JSON = "json";
            final String UNT = "metric";
            final String KEY_REF = Utility.WEATHER_API_KEY;


            Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                    .appendQueryParameter(LATITUDE, LAT)
                    .appendQueryParameter(LONGITUDE, LON)
                    .appendQueryParameter(COUNT, CNT)
                    .appendQueryParameter(MODE, JSON)
                    .appendQueryParameter(UNITS, UNT)
                    .appendQueryParameter(APP_ID, KEY_REF)
                    .build();

            Log.e(LOG_TAG, "URI--->" + builtUri.toString());

            url = new URL(builtUri.toString());
            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();

            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            forecastJsonStr = buffer.toString();
            Log.e(LOG_TAG, "RESPONSE--->" + forecastJsonStr);


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }


        Cursor cursor = mContext.getContentResolver().query(WeatherContract.WeatherEntry.CONTENT_URI, null, null, null, null);
        if (cursor != null) {
            //delete all contents of database to update data
            mContext.getContentResolver().delete(WeatherContract.WeatherEntry.CONTENT_URI, null, null);
            Log.v("onPostExecute", "Deleted.. couting again ");
            cursor = mContext.getContentResolver().query(WeatherContract.WeatherEntry.CONTENT_URI, null, null, null, null);
            Log.v("onPostExecute", cursor.getCount() + "");
            cursor.close();
        }
        try {
            //insert values to database
            getForecastFromJSON(forecastJsonStr);
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }


    private void getForecastFromJSON(String jsonString) throws JSONException {

        //weather data variables
        String min;
        String max;
        int humidity;
        double rain;
        double wind;
        String description = null;
        String icon = null;
        String temp_day;
        String temp_eve;
        String temp_mor;
        String temp_night;


        try {
            JSONObject forecastJson = new JSONObject(jsonString);

            String city = forecastJson.getJSONObject("city").getString("name");
            Log.e(LOG_TAG, "CITY --->" + city);
            //save city in shared pref
            Utility.saveCity(mContext, city);

            JSONArray list = forecastJson.getJSONArray("list");


            //Using the Gregorian Calendar Class to get current date
            Calendar gc = new GregorianCalendar();
            String day;
            String myDate;

            for (int i = 0; i < list.length(); i++) {

                //Converting the integer value returned by Calendar.DAY_OF_WEEK to
                //a human-readable String
                day = gc.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.ENGLISH);
                //iterating to the next day
                gc.add(Calendar.DAY_OF_WEEK, 1);


                SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd");
                Calendar c = Calendar.getInstance();
                c.setTime(new Date()); // Now use today date.
                c.add(Calendar.DATE, i); // Adding a day
                myDate = sdf.format(c.getTime());

                Log.e(LOG_TAG, "DATE --->" + myDate);

                JSONObject dayForecast = list.getJSONObject(i);

                JSONObject temp = dayForecast.getJSONObject("temp");
                min = String.valueOf(temp.getDouble("min"));
                Log.e(LOG_TAG, "MIN --->" + min.toString());

                max = String.valueOf(temp.getDouble("max"));
                Log.e(LOG_TAG, "MAX --->" + max.toString());

                temp_day = String.valueOf(temp.getDouble("day"));
                Log.e(LOG_TAG, "TEMP_DAY --->" + temp_day.toString());

                temp_night = String.valueOf(temp.getDouble("night"));
                Log.e(LOG_TAG, "TEMP_NIGHT --->" + temp_night.toString());

                temp_eve = String.valueOf(temp.getDouble("eve"));
                Log.e(LOG_TAG, "TEMP_EVE --->" + temp_eve.toString());

                temp_mor = String.valueOf(temp.getDouble("morn"));
                Log.e(LOG_TAG, "TEMP_MORN --->" + temp_mor.toString());

                humidity = dayForecast.getInt("humidity");
                Log.e(LOG_TAG, "HUMIDITY --->" + String.valueOf(humidity));

                //sometimes rain is not available on API
                if (dayForecast.has("rain")) {
                    rain = dayForecast.getDouble("rain");
                } else {
                    rain = 0;
                }
                Log.e(LOG_TAG, "RAIN --->" + String.valueOf(rain));

                wind = dayForecast.getInt("speed");
                Log.e(LOG_TAG, "WIND --->" + String.valueOf(wind));

                JSONArray weather = dayForecast.getJSONArray("weather");
                for (int j = 0; j < weather.length(); j++) {
                    JSONObject obj = weather.getJSONObject(j);
                    description = obj.getString("description");
                    Log.e(LOG_TAG, "DESCRIPTION --->" + description);

                    icon = obj.getString("icon");
                    Log.e(LOG_TAG, "ICON --->" + icon);

                }

                // Now that the content provider is set up, inserting rows of data is pretty simple.
                // First create a ContentValues object to hold the data you want to insert.
                ContentValues values = new ContentValues();

                // Then add the data, along with the corresponding name of the data type,
                // so the content provider knows what kind of value is being inserted.
                values.put(WeatherContract.WeatherEntry.COLUMN_DAY, day);
                values.put(WeatherContract.WeatherEntry.COLUMN_DATE, myDate);
                values.put(WeatherContract.WeatherEntry.COLUMN_DESCRIPTION, description);
                values.put(WeatherContract.WeatherEntry.COLUMN_HIGH, max);
                values.put(WeatherContract.WeatherEntry.COLUMN_LOW, min);

                values.put(WeatherContract.WeatherEntry.COLUMN_TEMP_DAY, temp_day);
                values.put(WeatherContract.WeatherEntry.COLUMN_TEMP_EVENING, temp_eve);
                values.put(WeatherContract.WeatherEntry.COLUMN_TEMP_MORNING, temp_mor);
                values.put(WeatherContract.WeatherEntry.COLUMN_TEMP_NIGHT, temp_night);

                values.put(WeatherContract.WeatherEntry.COLUMN_ICON, icon);
                values.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, humidity);
                values.put(WeatherContract.WeatherEntry.COLUMN_RAIN, String.valueOf(rain));
                values.put(WeatherContract.WeatherEntry.COLUMN_WIND, String.valueOf(wind));

                mContext.getContentResolver().insert(WeatherContract.WeatherEntry.CONTENT_URI, values);
            }


        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

    }

}

