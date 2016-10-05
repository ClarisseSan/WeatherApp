package com.example.isse.weatherapp.service;

import android.content.Context;
import android.os.AsyncTask;

import com.example.isse.weatherapp.task.DownloadWeatherTask;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;

/**
 * Created by isse on 04/10/2016.
 */
public class WeatherTaskService extends GcmTaskService {
    private Context mContext;
    public WeatherTaskService(Context context) {
        this.mContext = context;
    }
    public WeatherTaskService() {
    }

    @Override
    public int onRunTask(TaskParams taskParams) {
        if (taskParams.getTag().equals("init")) {
            DownloadWeatherTask downloadWeatherTask = new DownloadWeatherTask(mContext);
            downloadWeatherTask.execute();
        }
        return 0;
    }
}
