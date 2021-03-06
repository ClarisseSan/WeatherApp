/*
* Copyright 2016 Angela Sanchez

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
* */

package com.example.isse.weatherapp.ui;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.isse.weatherapp.R;
import com.example.isse.weatherapp.data.WeatherContract.WeatherEntry;
import com.example.isse.weatherapp.utility.Utility;

/**
 * A fragment representing a single Weather detail screen.
 * This fragment is either contained in a {@link WeatherListActivity}
 * in two-pane mode (on tablets) or a {@link WeatherDetailActivity}
 * on handsets.
 */
public class WeatherDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    /**
     * The fragment argument representing the item ID and uri that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";
    public static final String ARG_URI = "uri";
    private static final String LOG_TAG = WeatherDetailFragment.class.getSimpleName();
    private static final int CURSOR_LOADER_ID = 0;

    private String mId;
    private Uri mUri;
    private Context mContext;

    /*
    * Views
    * */
    private ImageView imgIcon;
    private TextView txtCity;
    private TextView txtDay;
    private TextView txtDate;
    private TextView txtDescription;
    private TextView txtHigh;
    private TextView txtLow;
    private TextView txtTempDay;
    private TextView txtTempEve;
    private TextView txtTempMorn;
    private TextView txtTempNight;
    private TextView txtHumidity;
    private TextView txtRain;
    private TextView txtWind;
    private CollapsingToolbarLayout appBarLayout;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public WeatherDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();

        if (getArguments().containsKey(ARG_ITEM_ID)) {

            mId = getArguments().getString(ARG_ITEM_ID);
            mUri = getArguments().getParcelable(ARG_URI);

            Log.e(LOG_TAG, "URI------>" + mUri.toString());

            Activity activity = this.getActivity();
            appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);

        }


    }

    @Override
    public void onStart() {
        super.onStart();
        //activate loader
        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(CURSOR_LOADER_ID, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.weather_detail, container, false);

        imgIcon = (ImageView) rootView.findViewById(R.id.img_weather_icon);
        txtCity = (TextView) rootView.findViewById(R.id.txt_city);
        txtDay = (TextView) rootView.findViewById(R.id.txt_weather_day);
        txtDate = (TextView) rootView.findViewById(R.id.txt_weather_date);
        txtDescription = (TextView) rootView.findViewById(R.id.txt_weather_description);
        txtHigh = (TextView) rootView.findViewById(R.id.txt_weather_high);
        txtLow = (TextView) rootView.findViewById(R.id.txt_weather_low);

        txtTempDay = (TextView) rootView.findViewById(R.id.txt_temp_day);
        txtTempEve = (TextView) rootView.findViewById(R.id.txt_temp_eve);
        txtTempMorn = (TextView) rootView.findViewById(R.id.txt_temp_morn);
        txtTempNight = (TextView) rootView.findViewById(R.id.txt_temp_night);

        txtHumidity = (TextView) rootView.findViewById(R.id.txt_humidity);
        txtRain = (TextView) rootView.findViewById(R.id.txt_rain);
        txtWind = (TextView) rootView.findViewById(R.id.txt_wind);

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader cursorLoader;
        if (mUri != null && id == CURSOR_LOADER_ID) {
            cursorLoader = new CursorLoader(mContext, mUri,
                    null,
                    null,
                    null,
                    null);
            return cursorLoader;
        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        final String DEGREE = "\u00b0";

        if (loader.getId() == CURSOR_LOADER_ID && cursor != null && cursor.moveToFirst()) {
            int weather_id = cursor.getInt(cursor.getColumnIndex(WeatherEntry._ID));

            //get values from database
            String day = cursor.getString(cursor.getColumnIndex(WeatherEntry.COLUMN_DAY));
            String mydate = cursor.getString(cursor.getColumnIndex(WeatherEntry.COLUMN_DATE));
            String description = cursor.getString(cursor.getColumnIndex(WeatherEntry.COLUMN_DESCRIPTION));
            String high = cursor.getString(cursor.getColumnIndex(WeatherEntry.COLUMN_HIGH));
            String low = cursor.getString(cursor.getColumnIndex(WeatherEntry.COLUMN_LOW));
            String temp_morn = cursor.getString(cursor.getColumnIndex(WeatherEntry.COLUMN_TEMP_MORNING));
            String temp_night = cursor.getString(cursor.getColumnIndex(WeatherEntry.COLUMN_TEMP_NIGHT));
            String temp_eve = cursor.getString(cursor.getColumnIndex(WeatherEntry.COLUMN_TEMP_EVENING));
            String temp_day = cursor.getString(cursor.getColumnIndex(WeatherEntry.COLUMN_TEMP_DAY));
            String icon = cursor.getString(cursor.getColumnIndex(WeatherEntry.COLUMN_ICON));
            String humidity = cursor.getString(cursor.getColumnIndex(WeatherEntry.COLUMN_HUMIDITY));
            String rain = cursor.getString(cursor.getColumnIndex(WeatherEntry.COLUMN_RAIN));
            String wind = cursor.getString(cursor.getColumnIndex(WeatherEntry.COLUMN_WIND));

            //get City from shared pref
            String city = Utility.getCity(mContext);

            //action bar title
            if (appBarLayout != null) {
                appBarLayout.setTitle(city);
            }

            //set values to views
            txtCity.setText(city);
            txtDay.setText(day + ", ");
            txtDate.setText(mydate);


            txtDescription.setText(description);
            txtHigh.setText(high);
            txtLow.setText(low);

            txtTempNight.setText(temp_night + DEGREE);
            txtTempMorn.setText(temp_morn + DEGREE);
            txtTempDay.setText(temp_day + DEGREE);
            txtTempEve.setText(temp_eve + DEGREE);

            txtHumidity.setText(humidity + "%");
            txtRain.setText(rain);
            txtWind.setText(wind + "m/s");


            //set icon image
            final String prefix = "ic_";
            Resources res = mContext.getResources();
            int resourceId = res.getIdentifier(prefix + icon, "drawable", mContext.getPackageName());
            imgIcon.setImageResource(resourceId);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }


}
