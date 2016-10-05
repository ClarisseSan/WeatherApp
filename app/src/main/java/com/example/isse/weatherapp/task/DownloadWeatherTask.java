package com.example.isse.weatherapp.task;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.example.isse.weatherapp.data.WeatherContract;
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

import android.text.format.Time;


/**
 * Created by isse on 04/10/2016.
 */

public class DownloadWeatherTask extends AsyncTask<String, Void, Void> {

    private static final String LOG_TAG = DownloadWeatherTask.class.getSimpleName();
    private Context mContext;

    public DownloadWeatherTask(Context context) {
        this.mContext = context;
    }

    @Override
    protected Void doInBackground(String... params) {

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
            final String LAT = "1.29";
            final String LON = "103.85";
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
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            forecastJsonStr = buffer.toString();
            getForecastFromJSON(forecastJsonStr);
            Log.e(LOG_TAG, "RESPONSE--->" + forecastJsonStr);


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
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


        return null;
    }

    private void getForecastFromJSON(String jsonString) throws JSONException {

        //weather data variables
        String min;
        String max;
        int humidity;
        String description = null;
        String icon = null;




        try {
            JSONObject forecastJson = new JSONObject(jsonString);

            String city = forecastJson.getJSONObject("city").getString("name");
            Log.e(LOG_TAG, "CITY --->" + city);
            JSONArray list = forecastJson.getJSONArray("list");

            //Using the Gregorian Calendar Class instead of Time Class to get current date
            Calendar gc = new GregorianCalendar();
            String day;

            for (int i = 0; i < list.length(); i++) {

                //Converting the integer value returned by Calendar.DAY_OF_WEEK to
                //a human-readable String
                day = gc.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.ENGLISH);

                //iterating to the next day
                gc.add(Calendar.DAY_OF_WEEK, 1);

                JSONObject dayForecast = list.getJSONObject(i);

                JSONObject temp = dayForecast.getJSONObject("temp");
                min = String.valueOf(temp.getDouble("min"));
                Log.e(LOG_TAG, "MIN --->" + min.toString());

                max = String.valueOf(temp.getDouble("max"));
                Log.e(LOG_TAG, "MAX --->" + max.toString());

                humidity = dayForecast.getInt("humidity");
                Log.e(LOG_TAG, "HUMIDITY --->" + String.valueOf(humidity));

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
                values.put(WeatherContract.WeatherEntry.COLUMN_DESCRIPTION, description);
                values.put(WeatherContract.WeatherEntry.COLUMN_HIGH, max);
                values.put(WeatherContract.WeatherEntry.COLUMN_LOW, min);
                values.put(WeatherContract.WeatherEntry.COLUMN_ICON, icon);
                values.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, humidity);

                mContext.getContentResolver().insert(WeatherContract.WeatherEntry.CONTENT_URI, values);
            }


        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

    }



}
