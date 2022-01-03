package com.federicogovoni.permissionsmanager.controller.location;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.federicogovoni.permissionsmanager.controller.ContextManager;
import com.federicogovoni.permissionsmanager.model.CurrentContext;
import com.federicogovoni.permissionsmanager.model.LocationContext;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

public class StandardLocationListener implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private Location mLastLocation;
    private final String TAG = "StandardLocationListener";
    private Context mContext = null;

    public StandardLocationListener(String provider, Context context)
    {
        Log.d(TAG, "LocationListener " + provider);
        mContext = context;
        mLastLocation = new Location(provider);
    }

    @Override
    public void onLocationChanged(Location location)
    {
        Log.d(TAG, "onLocationChanged: " + location);
        mLastLocation.set(location);


        ContextManager.init(mContext);
        if(ContextManager.getInstance(mContext).getContexts().size() == 0)
            System.exit(0);
        for(CurrentContext p : ContextManager.getInstance(mContext).getContexts()) {
            if(p.getLocationContext() != null && p.isEnabled(mContext)) {
                float [] dist = new float[1];
                Location.distanceBetween(
                        p.getLocationContext().getLatitude(),
                        p.getLocationContext().getLongitude(),
                        location.getLatitude(),
                        location.getLongitude(),
                        dist
                );
                double measure = 1;
                if(PreferenceManager.getDefaultSharedPreferences(mContext).
                        getString(LocationContext.MEASURE, LocationContext.KM).equals(LocationContext.MILES))
                    measure = 1.60934;
                if(dist[0] <= p.getLocationContext().getRadius()*1000/measure && !p.isRunning(mContext)) {
                    Log.d(TAG, "In location context: " + p.getName());
                    p.setInLocation(true, mContext);
                }
                else if(dist[0] > p.getLocationContext().getRadius()*1000/measure && p.isRunning(mContext))
                    p.setInLocation(false, mContext);
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

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "GoogleAPIClient Connected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "GoogleAPIClient Connection suspended");

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "Error trying to connect to GoogleAPIClient");
    }
}

