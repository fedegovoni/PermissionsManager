package com.federicogovoni.permissionsmanager;

import android.app.Application;

import com.federicogovoni.permissionmanager.BuildConfig;
import com.federicogovoni.permissionsmanager.controller.ProVersionChecker;

import timber.log.Timber;

public class PermissionsManager extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if(BuildConfig.DEBUG){
            Timber.plant(new Timber.DebugTree());
        }

        //ProVersionChecker.createInstance(this);
    }
}
