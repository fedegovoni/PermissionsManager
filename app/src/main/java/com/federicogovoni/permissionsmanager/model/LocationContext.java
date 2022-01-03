package com.federicogovoni.permissionsmanager.model;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

/**
 * Created by Federico on 07/03/2017.
 */

public class LocationContext implements Serializable {

    public static final String KM = "km";
    public static final String MILES = "miles";
    public static final String MEASURE = "measure";

    private double lat;
    private double lng;
    private int radius;
    private String city;
    private int currentContextId;

    public LocationContext(LatLng latLng, String city, int radius, int currentContextId) {
        this.currentContextId = currentContextId;
        this.lat = latLng.latitude;
        this.lng = latLng.longitude;
        this.city = city;
        this.radius = radius;
    }

    public double getLatitude() {
        return lat;
    }

    public double getLongitude() { return lng; }

    public void setLatitude(double lat) {
        this.lat = lat;
    }

    public void setLongitude(double lng){
        this.lng = lng;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public String getCity() {
        return city;
    }

    public int getCurrentContextId() {
        return currentContextId;
    }

    public void setCurrentContextId(int currentContextId) {
        this.currentContextId = currentContextId;
    }

    public void setCity(String city) {
        this.city = city;
    }
}
