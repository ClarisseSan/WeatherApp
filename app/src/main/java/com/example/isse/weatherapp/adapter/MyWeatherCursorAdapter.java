package com.example.isse.weatherapp.adapter;

import android.content.Context;
import android.content.res.Resources;
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
    private Context mContext;

    public MyWeatherCursorAdapter(Context context, Cursor cursor, View emptyView){
        super(context,cursor,emptyView);
        mContext = context;
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
        final String DEGREE  = "\u00b0";

        viewHolder.txtDay.setText(cursor.getString(cursor.getColumnIndex(WeatherEntry.COLUMN_DAY)));
        viewHolder.txtForecast.setText(cursor.getString(cursor.getColumnIndex(WeatherEntry.COLUMN_DESCRIPTION)));
        viewHolder.txtHigh.setText(cursor.getString(cursor.getColumnIndex(WeatherEntry.COLUMN_HIGH)) + DEGREE);
        viewHolder.txtLow.setText(cursor.getString(cursor.getColumnIndex(WeatherEntry.COLUMN_LOW)) + DEGREE);

        //set icon image
        final String prefix = "ic_";
        String icon_id = prefix + cursor.getString(cursor.getColumnIndex(WeatherEntry.COLUMN_ICON));
        Resources res = mContext.getResources();
        int resourceId = res.getIdentifier(icon_id, "drawable", mContext.getPackageName() );
        viewHolder.imgIcon.setImageResource( resourceId );


    }

}
