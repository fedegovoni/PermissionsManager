package com.federicogovoni.permissionsmanager.controller;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;

import com.federicogovoni.permissionsmanager.view.main.applications.ApplicationsFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Federico on 05/12/2017.
 */

public class ApplicationsInfoManager {

    private static ApplicationsInfoManager lfu;
    private List<ApplicationInfo> installedApplications;
    private Context context = null;

    private ApplicationsInfoManager(Context context) {
        this.context = context;
        reloadInstalledApplications();
    }

    public static ApplicationsInfoManager getInstance(Context context) {
        if (lfu == null)
            lfu = new ApplicationsInfoManager(context);
        return lfu;
    }

    public List<ApplicationInfo> getInstalledApplications() {
        return installedApplications;
    }

    public List<ApplicationInfo> getInstalledApplications(String startsWith) {
        List<ApplicationInfo> supportList = new ArrayList<>();
        for(ApplicationInfo applicationInfo : installedApplications) {
            if(applicationInfo.loadLabel(context.getPackageManager()).toString().toLowerCase().startsWith(startsWith.toLowerCase())) {
                supportList.add(applicationInfo);
            }
        }
        return supportList;
    }

    public void reloadInstalledApplications() {
        installedApplications = new ArrayList<>();
        List<PackageInfo> apps = context.getPackageManager().getInstalledPackages(PackageManager.GET_META_DATA);

        for (int i = 0; i < apps.size(); i++) {
            if (apps.get(i).versionName != null && (((apps.get(i).applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 1) ||
                    PreferenceManager.getDefaultSharedPreferences(context).getInt(ApplicationsFragment.APPLICATIONS_LIST, ApplicationsFragment.USER_APPLICATIONS) == ApplicationsFragment.ALL_APPLICATIONS)) {
                installedApplications.add(apps.get(i).applicationInfo);
            }


            Collections.sort(installedApplications, new Comparator<ApplicationInfo>() {
                @Override
                public int compare(ApplicationInfo one, ApplicationInfo two) {
                    return one.loadLabel(context.getPackageManager()).toString().compareTo(two.loadLabel(context.getPackageManager()).toString());
                }
            });
        }
    }
}
