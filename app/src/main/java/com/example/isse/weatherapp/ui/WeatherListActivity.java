package com.example.isse.weatherapp.ui;

import android.Manifest;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
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
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.util.List;

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
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private final String LOG_TAG = WeatherListActivity.class.getSimpleName();
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private Context mContext;
    private MyWeatherCursorAdapter mCursorAdapter;

    private boolean isConnected;
    private Cursor mCursor;

    private String longitude;
    private String latitude;

    public static final String LATITUDE = "lat";
    public static final String LONGITUDE = "long";


    private int REQUEST_LOCATION = 1;
    boolean hasLocationPermission = false;
    LocationManager locationManager;

    private final static int DISTANCE_UPDATES = 1;
    private final static int TIME_UPDATES = 1000;

    RecyclerView recyclerView;
    private View emptyView;
    private FragmentManager fragmentManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_list);

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


        checkPlayServices();

        //setLocation();

        //request permission for GPS
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (checkPermission()) {
            showEnabledLocationUI();
            String networkProvider = getLocationProvider();
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, TIME_UPDATES, DISTANCE_UPDATES, this);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, TIME_UPDATES, DISTANCE_UPDATES, this);
            Location gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Location networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            Location location = gpsLocation != null ? gpsLocation : networkLocation;
            Log.v("create", "location(" + location + ")");
            onLocationChanged(location);
        } else {
            requestPermission();
        }

        if(latitude!=null&&longitude!=null){
            //get weather details
            fetchWeatherDetails();
        }

        recyclerView = (RecyclerView) findViewById(R.id.weather_list);

        //set empty view
         emptyView = findViewById(R.id.recyclerview_weather_empty);

        //get the fragment manager
         fragmentManager = getSupportFragmentManager();

        mCursorAdapter = new MyWeatherCursorAdapter(this, null, emptyView, mTwoPane, fragmentManager);

        if (recyclerView != null) {
            setupRecyclerView(recyclerView);
        }

    }

    private void fetchWeatherDetails() {

        // The intent service is for executing immediate pulls from the Weather API
        // GCMTaskService can only schedule tasks, they cannot execute immediately

            Intent weatherIntentService = new Intent(this, WeatherIntentService.class);
            Log.e(LOG_TAG, "Lat ----->" + latitude);
            Log.e(LOG_TAG, "Long ----->" + longitude);

            weatherIntentService.putExtra(LATITUDE, latitude);
            weatherIntentService.putExtra(LONGITUDE, longitude);

            isConnected = Utility.isConnected(mContext);

            if (isConnected) {
                startService(weatherIntentService);
            } else {
                networkToast();
            }
        }



    @Override
    protected void onStart() {
        super.onStart();
        //initialize cursor loader
        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);
    }

    private void networkToast() {
        Toast.makeText(mContext, getString(R.string.network_toast), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(CURSOR_LOADER_ID, null, this);
    }


    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(LOG_TAG, getString(R.string.unsupported_device));
                finish();
            }
            return false;
        }
        return true;
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
        mCursorAdapter.swapCursor(data);
        mCursor = data;

        //notify user of an empty view
        updateEmptyView();

    }

    public void setLocation() {

        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        List<String> p = locationManager.getProviders(true);
        Location loc = null;
        for (String s : p) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            if (locationManager.getLastKnownLocation(s) != null) {
                if (loc == null || locationManager.getLastKnownLocation(s).getAccuracy() < loc.getAccuracy()) {
                    loc = locationManager.getLastKnownLocation(s);
                }
            }
        }
        latitude = String.valueOf(loc.getLatitude());
        longitude = String.valueOf(loc.getLongitude());

        Log.e(LOG_TAG, "Lat ----->" + latitude);
        Log.e(LOG_TAG, "Long ----->" + longitude);

    }


    /*
        Updates the empty list view with contextually relevant information that the user can
        use to determine why they aren't seeing weather.
     */
    private void updateEmptyView() {
        if (mCursorAdapter.getItemCount() == 0) {
            TextView tv = (TextView) findViewById(R.id.recyclerview_weather_empty);
            if (null != tv) {
                int message;
                if (!Utility.isConnected(mContext)) {
                    //network is not available
                    message = R.string.empty_list_no_network;
                } else {
                    //network is available but still doesn't fetch data
                    message = R.string.empty_weather_list;
                }
                tv.setText(message);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }


    @Override
    public void onLocationChanged(Location location) {
        Log.v("onLocationChanged", "location changed (" + location + ")");
        if (location != null) {
            latitude = String.valueOf(location.getLatitude());
            longitude = String.valueOf(location.getLongitude());

            fetchWeatherDetails();
            if (recyclerView != null) {
                setupRecyclerView(recyclerView);
            }
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {
        if (checkPermission()) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, TIME_UPDATES, DISTANCE_UPDATES, this);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, TIME_UPDATES, DISTANCE_UPDATES, this);
        } else {
            requestPermission();
        }
        showEnabledLocationUI();
    }

    @Override
    public void onProviderDisabled(String s) {

        if (checkPermission()) {
            locationManager.removeUpdates(this);
        } else {
            requestPermission();
        }
        showDisabledLocationUI();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.length > 0) {
                if (checkPermission()) {
                    showEnabledLocationUI();
                    String locationProvider = getLocationProvider();
                    locationManager.requestLocationUpdates(locationProvider, TIME_UPDATES, DISTANCE_UPDATES, this);
                } else {
                    showDisabledLocationUI();
                    requestPermission();
                }
            } else {
                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT < 23) {
            return true;
        }
        hasLocationPermission = false;
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (result == PackageManager.PERMISSION_GRANTED) {
            hasLocationPermission = true;
        }
        return hasLocationPermission;
    }

    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            showDisabledLocationUI();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }
    }

    private void showDisabledLocationUI() {
        Toast.makeText(mContext, getString(R.string.no_gps), Toast.LENGTH_SHORT).show();
    }

    private void showEnabledLocationUI() {
        Toast.makeText(mContext, getString(R.string.wait_gps), Toast.LENGTH_SHORT).show();
    }

    private String getLocationProvider() {
        boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        Log.v("getLocationProvider", "gpsEnabled(" + gpsEnabled + ") networkEnabled(" + networkEnabled + ")");
        String locationProvider = gpsEnabled ? LocationManager.GPS_PROVIDER : LocationManager.NETWORK_PROVIDER;
        return locationProvider;
    }


}
