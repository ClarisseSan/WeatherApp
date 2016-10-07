package com.example.isse.weatherapp.service;

import android.content.Context;
import android.database.Cursor;

import com.example.isse.weatherapp.data.WeatherContract;
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
        //get extras from intent service
        String latitude = taskParams.getExtras().getString("lat");
        String longitude = taskParams.getExtras().getString("long");

        //update weather forecast by
        //deleting all forecast data in database then load it again.

        Cursor cursor = mContext.getContentResolver().query(WeatherContract.WeatherEntry.CONTENT_URI,
                null,
                null,
                null,
                null);

        if (cursor != null) {
            mContext.getContentResolver().delete(WeatherContract.WeatherEntry.CONTENT_URI, null, null);
        }
        DownloadWeatherTask downloadWeatherTask = new DownloadWeatherTask(mContext, latitude, longitude);
        downloadWeatherTask.execute();

        return 0;
    }
}
