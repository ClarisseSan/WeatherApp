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

import android.Manifest;
import android.app.AlertDialog;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.isse.weatherapp.R;
import com.example.isse.weatherapp.adapter.MyWeatherCursorAdapter;
import com.example.isse.weatherapp.data.WeatherContract;
import com.example.isse.weatherapp.service.WeatherIntentService;
import com.example.isse.weatherapp.utility.Utility;

/**
 * An activity representing a list of Weather. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link WeatherDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class WeatherListActivity extends AppCompatActivity implements LoaderCallbacks<Cursor>, LocationListener {


    private static final int CURSOR_LOADER_ID = 0;
    private final String LOG_TAG = WeatherListActivity.class.getSimpleName();
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private Context mContext;
    private MyWeatherCursorAdapter mCursorAdapter;

    private Cursor mCursor;

    private int REQUEST_LOCATION = 1;
    boolean hasLocationPermission = false;
    LocationManager locationManager;

    private final static int DISTANCE_UPDATES = 1;//1 meter
    private final static int TIME_UPDATES = 1000 * 60 * 30;//30mins


    RecyclerView recyclerView;
    private FragmentManager fragmentManager;
    private MyLocationObserver myObserver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_list);


        //setup the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        mContext = this;

        if (findViewById(R.id.weather_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //Calling initLoader when the Loader has already been created
        // (this typically happens after configuration changes, for example)
        // tells the LoaderManager to deliver the Loader's most recent data to onLoadFinished immediately.
        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);

        //get the fragment manager and pass it later to the recyclerview adapter
        fragmentManager = getSupportFragmentManager();

        View emptyView = findViewById(R.id.recyclerview_weather_empty);
        mCursorAdapter = new MyWeatherCursorAdapter(this, mCursor, emptyView, mTwoPane, fragmentManager);
        recyclerView = (RecyclerView) findViewById(R.id.weather_list);
        if (recyclerView != null) {
            setupRecyclerView(recyclerView);
        }

        if (checkPermission()) {
            requestPermission();
        }

        //content observer
        myObserver = new MyLocationObserver(new Handler());
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLocationDetection();
        getLoaderManager().restartLoader(CURSOR_LOADER_ID, null, this);

        //Register your content observer to listen for changes
        getContentResolver().registerContentObserver(WeatherContract.WeatherEntry.CONTENT_URI, true, myObserver);
    }

    @Override
    protected void onPause() {
        super.onPause();

        //When you have registered a content observer, it is your responsibility to also unregister it.
        // Otherwise you would create a memory leak and your Activity would never be garbage collected.
        getContentResolver().unregisterContentObserver(myObserver);
    }

    @Override
    public void onLocationChanged(Location location) {
        Intent weatherIntentService = new Intent(this, WeatherIntentService.class);
        weatherIntentService.putExtra(WeatherIntentService.LATITUDE, location.getLatitude() + "");
        weatherIntentService.putExtra(WeatherIntentService.LONGITUDE, location.getLongitude() + "");

        boolean isConnected = Utility.isConnected(this);
        if (isConnected) {
            startService(weatherIntentService);
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {
        startLocationDetection();
    }

    @Override
    public void onProviderDisabled(String s) {
        if (checkPermission()) {
            locationManager.removeUpdates(this);
        }
        showSettingsAlert();
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


    /*
    * Since I am using a content provider as a client
    * and I want to know whenever the data changes,
    * Im going to use ContentObserver.
    * */
    class MyLocationObserver extends ContentObserver {
        public MyLocationObserver(Handler handler) {
            super(handler);
        }


        /*
        * these are called whenever a there's a change
        * */
        @Override
        public void onChange(boolean selfChange, Uri uri) {

            //Calling restartLoader destroys an already existing Loader (as well as any existing data associated with it)
            // and tells the LoaderManager to call onCreateLoader to create the new Loader and to initiate a new load.
            getLoaderManager().restartLoader(CURSOR_LOADER_ID, null, WeatherListActivity.this);

            Log.v("MyLocationObserver", "Observed a change in cursor URI..");
        }
    }


    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setAdapter(mCursorAdapter);
        mCursorAdapter.notifyDataSetChanged();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this, WeatherContract.WeatherEntry.CONTENT_URI,
                null,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        //remove all the underlying references with the cursor.
        // otherwise it will create a memory leak and your activity will not get garbage collected.
        mCursorAdapter.swapCursor(data);

        //refresh
        mCursorAdapter.notifyDataSetChanged();
        mCursor = data;

        //notify user of an empty view
        updateEmptyView();

    }


    /*
        Updates the empty list view with contextually relevant information that the user can
        use to determine why they aren't seeing weather.
     */
    private void updateEmptyView() {
        TextView tv = (TextView) findViewById(R.id.recyclerview_weather_empty);
        if (tv != null) {
            boolean isEmpty = mCursorAdapter.getItemCount() == 0;
            if (isEmpty) {
                int message;
                if (!Utility.isConnected(mContext)) {
                    //network is not available
                    message = R.string.empty_list_no_network;
                } else {
                    //network is available but still doesn't fetch data
                    message = R.string.empty_weather_list;
                }
                tv.setText(message);
                tv.setVisibility(View.VISIBLE);
            } else {
                tv.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //remove all the underlying references with the cursor.
        // otherwise it will create a memory leak and your activity will not get garbage collected.
        mCursorAdapter.swapCursor(null);
    }


    /*
    * When your app requests permissions, the system presents a dialog box to the user.
    * When the user responds, the system invokes your app's onRequestPermissionsResult() method,
     * passing it the user response. Your app has to override that method to find out whether the permission was granted.
    * */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_LOCATION) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0) {
                if (checkPermission()) {
                    startLocationDetection();
                } else {
                    showDisabledLocationUI();
                    requestPermission();
                }
            } else {
                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }


    /*
    * if the app's target SDK is 22 or lower:
    * If you list a dangerous permission in your manifest, the user has to grant the permission
    * when they install the app; if they do not grant the permission, the system does not install the app at all.
    *
    *
    * If the app's target SDK is 23 or higher:
    * The app has to list the permissions in the manifest, and it must request each dangerous permission
    * it needs while the app is running. The user can grant or deny each permission,
    * and the app can continue to run with limited capabilities even if the user denies a permission request.
    *
    * for more info:
    * https://developer.android.com/training/permissions/requesting.html
    * */
    private boolean checkPermission() {

        if (Build.VERSION.SDK_INT < 23) {
            return true;
        }
        hasLocationPermission = false;

        //This method returns immediately and you’re highly encouraged to use it in order to disable
        // some UI-controls that rely on that permission, or simply avoid SecurityExceptions in your app.
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (result == PackageManager.PERMISSION_GRANTED) {
            // permission was granted, yay!
            hasLocationPermission = true;
        }
        // permission denied, boo!
        return hasLocationPermission;
    }

    private void requestPermission() {
        //this method returns true if the app has requested this permission previously and the user denied the request.
        //If the user turned down the permission request in the past and chose the Don't ask again option in the permission
        // request system dialog, this method returns false. The method also returns false if a device policy prohibits the
        // app from having that permission.
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            //// Show an expanation to the user *asynchronously* -- don't block
            // this thread waiting for the user's response! After the user
            // sees the explanation, try again to request the permission.
            showDisabledLocationUI();
        } else {
            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }
    }

    private void showDisabledLocationUI() {
        Toast.makeText(mContext, getString(R.string.no_gps), Toast.LENGTH_SHORT).show();
    }


    /**
     * Function to show settings alert dialog
     */
    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        // Setting Dialog Title
        alertDialog.setTitle(R.string.GPS_Setting);

        // Setting Dialog Message
        alertDialog.setMessage(R.string.GPS_Setting_message);

        // On pressing Settings button
        alertDialog.setPositiveButton(R.string.settings, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }


}
