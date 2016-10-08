package com.example.isse.weatherapp.utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

/**
 * Created by isse on 05/10/2016.
 */

public class Utility {
    public static String WEATHER_API_KEY = "ee3eae518914dcd05823d45966b449c3";
    private static final String CITY = "city";
    private static final String LONGITUDE = "longitude";
    private static final String LATITUDE = "latitude";


    public static void saveCity(Context context, String city) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putString(CITY, city).commit();
    }

    public static String getCity(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(CITY, " ");
    }

    /**
     * Returns true if network suddenly becomes available
     */

    public static boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

}
