package com.federicogovoni.permissionsmanager.utils;

import com.federicogovoni.permissionmanager.R;

/**
 * Created by Federico on 22/03/2017.
 */

public class LastFragmentUsed {
    private static LastFragmentUsed lfu;
    private int fragmentId;

    private LastFragmentUsed() {
        fragmentId = R.id.nav_apps;
    }

    public static LastFragmentUsed getInstance() {
        if (lfu == null)
             lfu = new LastFragmentUsed();
        return lfu;
    }

    public void setNewId(int fragmentId) {
        this.fragmentId = fragmentId;
    }

    public int getLastFragmentId() {
        return fragmentId;
    }
}
