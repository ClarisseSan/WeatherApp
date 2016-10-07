package com.example.isse.weatherapp.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.isse.weatherapp.ui.WeatherListActivity;
import com.google.android.gms.gcm.TaskParams;

/**
 * Created by isse on 04/10/2016.
 */
public class WeatherIntentService extends IntentService{
    public WeatherIntentService(){
        super(WeatherIntentService.class.getName());
    }

    public WeatherIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String latitude = intent.getStringExtra(WeatherListActivity.LATITUDE);
        String longitude = intent.getStringExtra(WeatherListActivity.LONGITUDE);

        Bundle args = new Bundle();
        args.putString("lat", latitude);
        args.putString("long", longitude);

        Log.d(WeatherIntentService.class.getSimpleName(), "Weather Intent Service");
        WeatherTaskService weatherTaskService = new WeatherTaskService(this);
        // We can call OnRunTask from the intent service to force it to run immediately instead of
        // scheduling a task.
        weatherTaskService.onRunTask(new TaskParams("args", args));
    }
}
