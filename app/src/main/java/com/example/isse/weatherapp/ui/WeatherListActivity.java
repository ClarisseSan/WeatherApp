package com.example.isse.weatherapp.ui;

import android.Manifest;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
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
import com.example.isse.weatherapp.application.MyApplication;
import com.example.isse.weatherapp.data.WeatherContract;
import com.example.isse.weatherapp.utility.Utility;

/**
 * An activity representing a list of Weather. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link WeatherDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class WeatherListActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {


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


    RecyclerView recyclerView;
    private FragmentManager fragmentManager;
    private MyLocationObserver myObserver;


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


        // checkPlayServices();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //get the fragment manager
        fragmentManager = getSupportFragmentManager();

        View emptyView = findViewById(R.id.recyclerview_weather_empty);
        mCursorAdapter = new MyWeatherCursorAdapter(this, mCursor, emptyView, mTwoPane, fragmentManager);
        recyclerView = (RecyclerView) findViewById(R.id.weather_list);
        if (recyclerView != null) {
            setupRecyclerView(recyclerView);
        }

        MyApplication application = (MyApplication) getApplication();
        if (!application.checkPermission()) {
            requestPermission();
        }

        myObserver = new MyLocationObserver(new Handler());
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        //initialize cursor loader
        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(CURSOR_LOADER_ID, null, this);
        getContentResolver().registerContentObserver(WeatherContract.WeatherEntry.CONTENT_URI, true, myObserver);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getContentResolver().unregisterContentObserver(myObserver);
    }

    class MyLocationObserver extends ContentObserver {
        public MyLocationObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
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
        mCursorAdapter.swapCursor(data);
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
        mCursorAdapter.swapCursor(null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.length > 0) {
                if (checkPermission()) {
                    MyApplication application = (MyApplication) getApplication();
                    application.startLocationDetection();
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


}
