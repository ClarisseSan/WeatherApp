package com.example.isse.weatherapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.isse.weatherapp.R;
import com.example.isse.weatherapp.data.WeatherContract.WeatherEntry;
import com.example.isse.weatherapp.ui.WeatherDetailActivity;
import com.example.isse.weatherapp.ui.WeatherDetailFragment;
import com.example.isse.weatherapp.utility.Utility;

/**
 * Created by isse on 05/10/2016.
 */

public class MyWeatherCursorAdapter extends CursorRecyclerViewAdapter<MyWeatherCursorAdapter.ViewHolder> {

    private Context mContext;
    private boolean mTwoPane;
    private FragmentManager mFragmentManager;

    /*
    * Recyclerview list position
    * */
    private static final int TODAY_LAYOUT = 0;
    private static final int FUTURE_LAYOUT = 1;

    public MyWeatherCursorAdapter(Context context, Cursor cursor, View emptyView, boolean mTwoPane, FragmentManager fragmentManager) {
        super(context, cursor, emptyView);
        mContext = context;
        this.mTwoPane = mTwoPane;
        this.mFragmentManager = fragmentManager;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View mView;
        public ImageView imgIcon;
        public TextView txtDay;
        private TextView txtDate;
        public TextView txtForecast;
        public TextView txtHigh;
        public TextView txtLow;
        public TextView txtCity;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            imgIcon = (ImageView) view.findViewById(R.id.img_icon);
            txtDay = (TextView) view.findViewById(R.id.txt_date);
            txtDate = (TextView) view.findViewById(R.id.txt_date_2);
            txtForecast = (TextView) view.findViewById(R.id.txt_forecast);
            txtHigh = (TextView) view.findViewById(R.id.txt_high_temp);
            txtLow = (TextView) view.findViewById(R.id.txt_low_temp);
            txtCity = (TextView) view.findViewById(R.id.txt_city);
        }


    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder vh;
        if (parent instanceof RecyclerView) {
            int layoutId = -1;
            switch (viewType) {
                case TODAY_LAYOUT: {
                    layoutId = R.layout.weather_list_content_today;
                    break;
                }
                case FUTURE_LAYOUT: {
                    layoutId = R.layout.weather_list_content;
                    break;
                }

            }
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(layoutId, parent, false);
            itemView.setFocusable(false);
            vh = new ViewHolder(itemView);
        } else {
            throw new RuntimeException("Not bound to RecyclerView");
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Cursor cursor) {
        final String DEGREE = "\u00b0";
        final String id = String.valueOf(cursor.getInt(cursor.getColumnIndex(WeatherEntry._ID)));

        viewHolder.txtDay.setText(cursor.getString(cursor.getColumnIndex(WeatherEntry.COLUMN_DAY)));
        viewHolder.txtDate.setText(cursor.getString(cursor.getColumnIndex(WeatherEntry.COLUMN_DATE)));
        viewHolder.txtForecast.setText(cursor.getString(cursor.getColumnIndex(WeatherEntry.COLUMN_DESCRIPTION)));
        viewHolder.txtHigh.setText(cursor.getString(cursor.getColumnIndex(WeatherEntry.COLUMN_HIGH)) + DEGREE);
        viewHolder.txtLow.setText(cursor.getString(cursor.getColumnIndex(WeatherEntry.COLUMN_LOW)) + DEGREE);
        viewHolder.txtCity.setText(Utility.getCity(mContext));

        //set icon image
        final String prefix = "ic_";
        String icon_id = prefix + cursor.getString(cursor.getColumnIndex(WeatherEntry.COLUMN_ICON));
        Resources res = mContext.getResources();
        int resourceId = res.getIdentifier(icon_id, "drawable", mContext.getPackageName());
        viewHolder.imgIcon.setImageResource(resourceId);

        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri contentUri = WeatherEntry.buildDetailsUri(Long.valueOf(id));

                if (mTwoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putString(WeatherDetailFragment.ARG_ITEM_ID, String.valueOf(id));
                    arguments.putParcelable(WeatherDetailFragment.ARG_URI, contentUri);
                    WeatherDetailFragment fragment = new WeatherDetailFragment();
                    fragment.setArguments(arguments);
                    mFragmentManager.beginTransaction()
                            .replace(R.id.weather_detail_container, fragment)
                            .commit();
                } else {
                    Context context = mContext;
                    Intent intent = new Intent(context, WeatherDetailActivity.class);
                    intent.putExtra(WeatherDetailFragment.ARG_ITEM_ID, String.valueOf(id));
                    intent.putExtra(WeatherDetailFragment.ARG_URI, contentUri);
                    intent.setData(contentUri);

                    context.startActivity(intent);
                }

            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0) ? TODAY_LAYOUT : FUTURE_LAYOUT;

    }
}
