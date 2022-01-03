package com.federicogovoni.permissionsmanager.controller.location;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.IBinder;
import android.util.Log;

import com.federicogovoni.permissionsmanager.controller.ContextManager;

/**
 * Created by Federico on 12/03/2017.
 */

public class LocationService extends Service {

    private static final String TAG = "LocationService";
    private LocationManager mLocationManager = null;
    //private static final int LOCATION_INTERVAL_PASSIVE = 1000;
    private static final int LOCATION_INTERVAL_ACTIVE = 1000 * 60 * 3;
    private static final float LOCATION_DISTANCE = 10f;

    LocationListener[] mLocationListeners = null;

    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.d(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate()
    {
        Log.d(TAG, "onCreate");
        if(ContextManager.getInstance(getApplicationContext()).getContexts().size() == 0)
            System.exit(0);

        if(mLocationListeners == null) {
            mLocationListeners = new LocationListener[]{
                    new StandardLocationListener(LocationManager.GPS_PROVIDER, getApplicationContext()),
                    new StandardLocationListener(LocationManager.NETWORK_PROVIDER, getApplicationContext()),
                    new StandardLocationListener(LocationManager.PASSIVE_PROVIDER, getApplicationContext())
            };
        }

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
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "passive provider not exist, " + ex.getMessage());
        }

    }

    @Override
    public void onDestroy()
    {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }
    }

    private void initializeLocationManager() {
        Log.d(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }
}
