package com.example.isse.weatherapp.application;


import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.example.isse.weatherapp.service.WeatherIntentService;
import com.example.isse.weatherapp.utility.Utility;

public class MyApplication extends Application {

/*
public class MyApplication extends Application implements LocationListener {

    private final static int DISTANCE_UPDATES = 1;//1 meter
    private final static int TIME_UPDATES = 1000 * 60 * 30;//30mins
    private LocationManager locationManager;

    @Override
    public void onCreate() {
        Log.v("MyApplication", "Starting application..");
        startLocationDetection();
    }

    public void startLocationDetection() {
        this.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (checkPermission()) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, TIME_UPDATES, DISTANCE_UPDATES, this);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, TIME_UPDATES, DISTANCE_UPDATES, this);
            Location gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Location networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            Location location = gpsLocation != null ? gpsLocation : networkLocation;
            Log.v("create", "location(" + location + ")");
        }
    }

    public boolean checkPermission() {
        if (Build.VERSION.SDK_INT < 23) {
            return true;
        }
        boolean hasLocationPermission = false;
        int result = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        if (result == PackageManager.PERMISSION_GRANTED) {
            hasLocationPermission = true;
        }
        return hasLocationPermission;
    }

    @Override
    public void onLocationChanged(Location location) {
        Intent weatherIntentService = new Intent(this, WeatherIntentService.class);
        weatherIntentService.putExtra(WeatherIntentService.LATITUDE, location.getLatitude()+"");
        weatherIntentService.putExtra(WeatherIntentService.LONGITUDE, location.getLongitude()+"");

        boolean isConnected = Utility.isConnected(this);
        if (isConnected) {
            startService(weatherIntentService);
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {}

    @Override
    public void onProviderEnabled(String s) {
//        if (checkPermission()) {
//            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, TIME_UPDATES, DISTANCE_UPDATES, this);
//            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, TIME_UPDATES, DISTANCE_UPDATES, this);
//        }

        startLocationDetection();
    }

    @Override
    public void onProviderDisabled(String s) {
        if (checkPermission()) {
            locationManager.removeUpdates(this);
        }
    }
}
*/
}