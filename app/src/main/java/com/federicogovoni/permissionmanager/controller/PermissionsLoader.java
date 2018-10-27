package com.federicogovoni.permissionmanager.controller;


import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;

import com.federicogovoni.permissionmanager.view.fragment.ApplicationsFragment;
import com.federicogovoni.permissionmanager.model.Permission;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Federico on 12/03/2017.
 */

public class PermissionsLoader {
    private Map<ApplicationInfo, List<Permission>> appPermissionMap;
    private static PermissionsLoader pl;
    private List<ApplicationInfo> applicationsList;
    private Context mContext;
    private static int list_type = -1;

    private PermissionsLoader(final Context context) {
        list_type = PreferenceManager.getDefaultSharedPreferences(context).getInt(ApplicationsFragment.APPLICATIONS_LIST, ApplicationsFragment.USER_APPLICATIONS);
        this.mContext = context;
        List<PackageInfo> apps = context.getPackageManager().getInstalledPackages(PackageManager.GET_META_DATA);
        applicationsList = new ArrayList<>();

        for (int i = 0; i < apps.size(); i++) {
            if (apps.get(i).versionName != null && (((apps.get(i).applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 1) ||
                    PreferenceManager.getDefaultSharedPreferences(context).getInt(ApplicationsFragment.APPLICATIONS_LIST, ApplicationsFragment.USER_APPLICATIONS) == ApplicationsFragment.ALL_APPLICATIONS)) {
                applicationsList.add(apps.get(i).applicationInfo);
            }
        }

        Collections.sort(applicationsList, new Comparator<ApplicationInfo>() {
            @Override
            public int compare (ApplicationInfo one, ApplicationInfo two) {
                return one.loadLabel(context.getPackageManager()).toString().compareTo(two.loadLabel(context.getPackageManager()).toString());
            }

        });

        HashMap<ApplicationInfo, List<Permission>> appPermissionsMap = new HashMap<>();

        for(ApplicationInfo appInfo : applicationsList) {
            String[] requestedPermissions = null;
            try {
                PackageInfo packageInfo = context.getPackageManager().getPackageInfo(appInfo.packageName, PackageManager.GET_PERMISSIONS);
                requestedPermissions = packageInfo.requestedPermissions;

            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            appPermissionsMap.put(appInfo, new ArrayList<Permission>());
            if(requestedPermissions != null)
                for(String p : requestedPermissions)
                    appPermissionsMap.get(appInfo).add(new Permission(p, appInfo.packageName, context.getPackageManager()));

            Collections.sort(appPermissionsMap.get(appInfo));
        }

        this.appPermissionMap = appPermissionsMap;
    }

    public static PermissionsLoader getInstance(Context context) {
        if(pl == null || PreferenceManager.getDefaultSharedPreferences(context).getInt(ApplicationsFragment.APPLICATIONS_LIST, ApplicationsFragment.USER_APPLICATIONS) != list_type) {
            list_type = PreferenceManager.getDefaultSharedPreferences(context).getInt(ApplicationsFragment.APPLICATIONS_LIST, ApplicationsFragment.USER_APPLICATIONS);
            pl = new PermissionsLoader(context);
        }
        return pl;
    }

    public Map<ApplicationInfo, List<Permission>> getAppPermissionMap() {
        return appPermissionMap;
    }

    public List<Permission> getAppPermissions (ApplicationInfo applicationInfo) {
        return appPermissionMap.get(applicationInfo);
    }

    public List<ApplicationInfo> getApplicationsList() {
        return applicationsList;
    }

    public ApplicationInfo searchApplicationInfoByPakcageName(String packageName) {
        for(ApplicationInfo applicationInfo : getApplicationsList()) {
            if(applicationInfo.packageName.equals(packageName))
                return applicationInfo;
        }
        return null;
    }
}
