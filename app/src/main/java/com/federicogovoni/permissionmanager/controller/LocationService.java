package com.federicogovoni.permissionmanager.controller;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.federicogovoni.permissionmanager.model.CurrentContext;
import com.federicogovoni.permissionmanager.model.LocationContext;

/**
 * Created by Federico on 12/03/2017.
 */

public class LocationService extends Service {

    private static final String TAG = "LocationService";
    private LocationManager mLocationManager = null;
    //private static final int LOCATION_INTERVAL_PASSIVE = 1000;
    private static final int LOCATION_INTERVAL_ACTIVE = 1000 * 60 * 3;
    private static final float LOCATION_DISTANCE = 10f;

    private class LocationListener implements android.location.LocationListener
    {
        Location mLastLocation;

        public LocationListener(String provider)
        {
            Log.d(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location)
        {
            Log.d(TAG, "onLocationChanged: " + location);
            mLastLocation.set(location);

           
            ContextManager.init(getApplicationContext());
            if(ContextManager.getInstance(getApplicationContext()).getContexts().size() == 0)
                System.exit(0);
            for(CurrentContext p : ContextManager.getInstance(getApplicationContext()).getContexts()) {
                if(p.getLocationContext() != null && p.isEnabled(getApplicationContext())) {
                    float [] dist = new float[1];
                    Location.distanceBetween(
                            p.getLocationContext().getLatitude(),
                            p.getLocationContext().getLongitude(),
                            location.getLatitude(),
                            location.getLongitude(),
                            dist
                    );
                    double measure = 1;
                    if(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).
                            getString(LocationContext.MEASURE, LocationContext.KM).equals(LocationContext.MILES))
                        measure = 1.60934;
                    if(dist[0] <= p.getLocationContext().getRadius()*1000/measure && !p.isRunning(getApplicationContext())) {
                        Log.d(TAG, "In location context: " + p.getName());
                        p.setInLocation(true, getApplicationContext());
                    }
                    else if(dist[0] > p.getLocationContext().getRadius()*1000/measure && p.isRunning(getApplicationContext()))
                        p.setInLocation(false, getApplicationContext());
                }
            }
        }


        @Override
        public void onProviderDisabled(String provider)
        {
            Log.d(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider)
        {
            Log.d(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            Log.d(TAG, "onStatusChanged: " + provider);
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER),
            new LocationListener(LocationManager.PASSIVE_PROVIDER)
    };

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
