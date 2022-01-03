package com.federicogovoni.permissionsmanager.utils;

import android.content.Context;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;

public class AdRequestKeeper {
    private static AdRequest adRequest = null;

    public static AdRequest getAdRequest(Context context) {
        if(adRequest == null) {
            //initiliazitation ID nel file manifest
            MobileAds.initialize(context);
            adRequest = new AdRequest.Builder().build();
        }
        return adRequest;
    }

}
