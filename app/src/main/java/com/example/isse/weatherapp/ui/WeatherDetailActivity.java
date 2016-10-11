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

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.widget.ImageView;

import com.example.isse.weatherapp.R;

import java.util.Calendar;
import java.util.Date;



/**
 * An activity representing a single Weather detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link WeatherListActivity}.
 *
 *
 */
public class WeatherDetailActivity extends AppCompatActivity {



    /*
    * TIME oF DAY
    * */
    private  final String MORNING = "morning";
    private final String DAY = "day";
    private final String EVENING = "evening";
    private final String NIGHT = "night";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //setImage
        ImageView imgTime = (ImageView) findViewById(R.id.img_time);
        switch (getTimeOfDay()){
            case MORNING:
                imgTime.setImageResource(R.drawable.bg_morning);
                break;
            case DAY:
                imgTime.setImageResource(R.drawable.bg_day);
                break;
            case EVENING:
                imgTime.setImageResource(R.drawable.bg_eve);
                break;
            case NIGHT:
                imgTime.setImageResource(R.drawable.bg_night);
                break;
            default:imgTime.setImageResource(R.drawable.bg_day);
        }

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(WeatherDetailFragment.ARG_ITEM_ID,getIntent().getStringExtra(WeatherDetailFragment.ARG_ITEM_ID));
            arguments.putParcelable(WeatherDetailFragment.ARG_URI,getIntent().getParcelableExtra(WeatherDetailFragment.ARG_URI));

            WeatherDetailFragment fragment = new WeatherDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.weather_detail_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            NavUtils.navigateUpTo(this, new Intent(this, WeatherListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

/*
* returns part of day base on time (morning, day, eve, night)
* This will be used for setting background image on toolbar
* */
    private String  getTimeOfDay() {
        Date dt = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(dt);
        int hours = c.get(Calendar.HOUR_OF_DAY);

        if(hours>=4 && hours<=8){
            return MORNING;
        }else if(hours>=8 && hours<=17){
            return DAY;
        }else if(hours>=17 && hours<=21){
            return EVENING;
        }else if(hours>=21){
            return NIGHT;
        }
        return null;
    }
}
