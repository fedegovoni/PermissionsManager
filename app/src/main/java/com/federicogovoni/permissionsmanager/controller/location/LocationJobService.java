package com.federicogovoni.permissionsmanager.controller.location;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.federicogovoni.permissionsmanager.controller.ContextManager;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class LocationJobService extends JobService {

    private static final String TAG = "LocationJobService";
    private LocationManager mLocationManager = null;
    //private static final int LOCATION_INTERVAL_PASSIVE = 1000;
    private static final int LOCATION_INTERVAL_ACTIVE = 1000 * 60 * 3;
    private static final float LOCATION_DISTANCE = 10f;
    LocationListener[] mLocationListeners = new LocationListener[] {
            new StandardLocationListener(LocationManager.GPS_PROVIDER, getApplicationContext()),
            new StandardLocationListener(LocationManager.NETWORK_PROVIDER, getApplicationContext()),
            new StandardLocationListener(LocationManager.PASSIVE_PROVIDER, getApplicationContext())
    };


    @Override
    public boolean onStartJob(JobParameters params) {

        if (android.os.Debug.isDebuggerConnected())
            android.os.Debug.waitForDebugger();
        Log.d(TAG, "onStartJob");
        if(ContextManager.getInstance(getApplicationContext()).getContexts().size() == 0)
            System.exit(0);
        initializeLocationManager();
/*
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL_ACTIVE, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL_ACTIVE, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
*/
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.PASSIVE_PROVIDER, LOCATION_INTERVAL_ACTIVE, LOCATION_DISTANCE,
                    mLocationListeners[2]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
            return false;
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "passive provider not exist, " + ex.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listeners, ignore", ex);
                    return false;
                }
            }
        }
        return true;
    }

    private void initializeLocationManager() {
        Log.d(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }
}
