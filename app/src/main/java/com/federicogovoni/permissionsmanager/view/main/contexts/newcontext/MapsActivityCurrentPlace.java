package com.federicogovoni.permissionsmanager.view.main.contexts.newcontext;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;


import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.federicogovoni.permissionmanager.BuildConfig;
import com.federicogovoni.permissionmanager.R;
import com.federicogovoni.permissionsmanager.Constants;
import com.federicogovoni.permissionsmanager.utils.GeneralUtils;
import com.federicogovoni.permissionsmanager.view.main.BaseActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;


import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;

import com.google.android.material.progressindicator.LinearProgressIndicator;

/**
 * An activity that displays a map showing the place at the device's current location.
 */
public class MapsActivityCurrentPlace extends BaseActivity
        implements OnMapReadyCallback {

    private GoogleMap map;
    private CameraPosition cameraPosition;

    // The entry point to the Places API.
    private PlacesClient placesClient;

    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient fusedLocationProviderClient;

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng defaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean locationPermissionGranted;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location lastKnownLocation;

    // Keys for storing activity state.
    
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    
    @BindView(R.id.activity_place_picker_show_current_position_fab)
    FloatingActionButton showCurrentPositionFab;
    
    @BindView(R.id.activity_place_picker_select_position_fab)
    FloatingActionButton selectPositionFab;

    @BindView(R.id.activity_place_picker_progress_bar)
    LinearProgressIndicator progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        progressBar.setVisibility(View.VISIBLE);
        showCurrentPositionFab.setEnabled(false);
        selectPositionFab.setEnabled(false);

        // Construct a PlacesClient
        Places.initialize(getApplicationContext(), BuildConfig.GMAPS_API_KEY);
        placesClient = Places.createClient(this);


        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Build the map.
        
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.activity_place_picker_map_fragment);
        mapFragment.getMapAsync(this);
        
        
    }
    

    /**
     * Saves the state of the map when the activity is paused.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (map != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, map.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, lastKnownLocation);
        }
        super.onSaveInstanceState(outState);
    }

    
    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;

        progressBar.setVisibility(View.GONE);
        showCurrentPositionFab.setEnabled(true);
        selectPositionFab.setEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(false);

        // Prompt the user for permission.
        getLocationPermission();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();
    }


    /**
     * Gets the current location of the device, and positions the map's camera.
     */

    @OnClick(R.id.activity_place_picker_show_current_position_fab)
    public void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            lastKnownLocation = task.getResult();
                            if (lastKnownLocation != null) {
                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(lastKnownLocation.getLatitude(),
                                                lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            }
                        } else {
                            Timber.d("Current location is null. Using defaults.");
                            Timber.e("Exception: %s", task.getException());
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }
    

    /**
     * Prompts the user for permission to use the device location.
     */
    
    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }
    

    /**
     * Handles the result of the request for location permissions.
     */
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        locationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    
    @OnClick(R.id.activity_place_picker_select_position_fab)
    public void onPositionSelected() {
        Timber.d("Click su placeSelected");
        Timber.d("Camera position: %s", map.getCameraPosition());
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;
        try {
             addresses = geocoder.getFromLocation(map.getCameraPosition().target.latitude, map.getCameraPosition().target.longitude, 1);
             addresses.forEach(address -> Timber.d("Found address %s", address.getAddressLine(0)));
        } catch (IOException e) {
            Timber.e("Error fetching address from location %s", map.getCameraPosition().target);
            GeneralUtils.showSnackbar(findViewById(R.id.activity_place_picker_root_layout), getResources().getString(R.string.snackbar_error_get_address));
            e.printStackTrace();
        }

        Intent resultIntent = new Intent();
        resultIntent.putExtra(Constants.SUCCESS, addresses != null && addresses.get(0) != null);
        if(addresses != null && addresses.get(0) != null) {
            resultIntent.putExtra(Constants.SELECTED_PLACE, addresses.get(0));
        }
        setResult(Activity.RESULT_OK, resultIntent);
        finishActivity(0);
        finish();
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void updateLocationUI() {
        if (map == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                map.setMyLocationEnabled(true);
            } else {
                map.setMyLocationEnabled(false);
                lastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_place_picker;
    }

}
