package com.federicogovoni.permissionsmanager.controller;

import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;

import com.federicogovoni.permissionsmanager.view.main.applications.PermissionsActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Federico on 25/02/2017.
 */

public class PermissionGroupAssociation {

    public static List<String> getGroupNamesList (PackageManager pm) {
        List<PermissionGroupInfo> groupInfoList = pm.getAllPermissionGroups(0);
        if (groupInfoList == null)
            return null;

        ArrayList<String> groupNameList = new ArrayList<>();
        for (PermissionGroupInfo groupInfo : groupInfoList) {
            String groupName = groupInfo.name;
            if (groupName != null) {
                groupNameList.add(groupName);
            }
        }
        groupNameList.add(PermissionsActivity.OTHER);
        return groupNameList;
    }

    private static ArrayList<String> getPermissionsForGroup(String groupName, PackageManager pm)
    {
        final ArrayList<String> permissionNameList = new ArrayList<>();

        try {
            List<PermissionInfo> permissionInfoList =
                    pm.queryPermissionsByGroup(groupName, PackageManager.GET_META_DATA);
            if (permissionInfoList != null) {
                for (PermissionInfo permInfo : permissionInfoList) {
                    String permName = permInfo.name;
                    if (permName == null) {
                        permName = "null";
                    } else if (permName.isEmpty()) {
                        permName = "empty";
                    }
                    permissionNameList.add(permName);
                }
            }
        }
            catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
        }

        Collections.sort(permissionNameList);

        return permissionNameList;
    }

    public static String resolveGroup(String permission, PackageManager pm) {
        for(String groupName : getGroupNamesList(pm))
            for(String tmpPermission : getPermissionsForGroup(groupName, pm))
                if(tmpPermission.equals(permission))
                    return groupName;
        return PermissionsActivity.OTHER;

    }
}
