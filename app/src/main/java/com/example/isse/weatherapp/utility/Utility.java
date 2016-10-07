package com.example.isse.weatherapp.utility;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by isse on 05/10/2016.
 */

public class Utility {
    public static String WEATHER_API_KEY = "ee3eae518914dcd05823d45966b449c3";

    /**
     * Returns true if network suddenly becomes available
     *
     */

    static public boolean isConnected(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

}
