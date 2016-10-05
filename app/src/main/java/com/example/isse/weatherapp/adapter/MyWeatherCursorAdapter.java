package com.example.isse.weatherapp.adapter;

import android.content.Context;
import android.database.Cursor;
import android.media.Image;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.isse.weatherapp.R;
import com.example.isse.weatherapp.data.WeatherContract.WeatherEntry;

/**
 * Created by isse on 05/10/2016.
 */

public class MyWeatherCursorAdapter extends CursorRecyclerViewAdapter<MyWeatherCursorAdapter.ViewHolder> {
    public MyWeatherCursorAdapter(Context context, Cursor cursor){
        super(context,cursor);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imgIcon;
        public TextView txtDay;
        public TextView txtForecast;
        public TextView txtHigh;
        public TextView txtLow;
        public ViewHolder(View view) {
            super(view);
            imgIcon = (ImageView) view.findViewById(R.id.img_icon);
            txtDay  = (TextView) view.findViewById(R.id.txt_date);
            txtForecast  = (TextView) view.findViewById(R.id.txt_forecast);
            txtHigh  = (TextView) view.findViewById(R.id.txt_high_temp);
            txtLow  = (TextView) view.findViewById(R.id.txt_low_temp);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.weather_list_content, parent, false);
        ViewHolder vh = new ViewHolder(itemView);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Cursor cursor) {
        //TODO include image view
        viewHolder.txtDay.setText(cursor.getString(cursor.getColumnIndex(WeatherEntry.COLUMN_DAY)));
        viewHolder.txtForecast.setText(cursor.getString(cursor.getColumnIndex(WeatherEntry.COLUMN_DESCRIPTION)));
        viewHolder.txtHigh.setText(cursor.getString(cursor.getColumnIndex(WeatherEntry.COLUMN_HIGH)));
        viewHolder.txtLow.setText(cursor.getString(cursor.getColumnIndex(WeatherEntry.COLUMN_LOW)));
        }

}
