package com.federicogovoni.permissionsmanager.model;

import android.content.pm.PackageManager;
import android.util.Log;

import com.federicogovoni.permissionmanager.R;
import com.federicogovoni.permissionsmanager.controller.PermissionGroupAssociation;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by Federico on 27/02/2017.
 */

public class Permission implements Comparable<Permission>{

    private String name;
    private int icon;
    private String group;
    private PackageManager pm;
    private String packageName;

    public Permission (String name, String packageName, PackageManager pm) {
        this.name = name;
        this.pm = pm;
        this.packageName = packageName;
        this.group = PermissionGroupAssociation.resolveGroup(name, pm);
        if (group.contains("CALENDAR"))
            icon = R.drawable.calendar;
        else if (group.contains("CAMERA"))
            icon = R.drawable.camera;
        else if (group.contains("CONTACTS"))
            icon = (R.drawable.contacts);
        else if (group.contains("LOCATION"))
            icon = (R.drawable.gps);
        else if (group.contains("MICROPHONE"))
            icon = (R.drawable.microphone);
        else if (group.contains("PHONE"))
            icon = (R.drawable.phone);
        else if (group.contains("SENSORS"))
            icon = (R.drawable.sensors);
        else if (group.contains("SMS"))
            icon = (R.drawable.sms);
        else if (group.contains("STORAGE"))
            icon = (R.drawable.storage);
        else
            icon = (R.drawable.other);
    }

    public String getName () {
        return name;
    }

    public String getGroup () {
        return group;
    }

    public boolean check () {
        return pm.checkPermission(name, packageName) == PackageManager.PERMISSION_GRANTED;
    }

    public String getPackageName() {
        return packageName;
    }

    public int getIconId() {
        return icon;
    }

    @Override
    public int compareTo(Permission o) {
        List<String> groups = PermissionGroupAssociation.getGroupNamesList(pm);

        int apos = groups.indexOf(this.getGroup());
        int bpos = groups.indexOf(o.getGroup());

        if(apos == bpos)
            return 0;
        return apos > bpos ?  1 : -1;
    }

    public boolean revoke() {
        try {
            Process su = Runtime.getRuntime().exec("su");
            DataOutputStream dos = new DataOutputStream(su.getOutputStream());
            String cmd;
            cmd = ("pm "+ CurrentContext.REVOKE + " " + getPackageName() + " " + getName());
            Log.e("REVOKED PERMISSION:", packageName + " " + name);
            dos.writeBytes(cmd);
            dos.flush();
            dos.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        //si è concluso con successo se il permesso non è concesso
        return !check();
    }

    public boolean grant() {
        try {
            Process su = Runtime.getRuntime().exec("su");
            DataOutputStream dos = new DataOutputStream(su.getOutputStream());
            String cmd;
            cmd = ("pm "+ CurrentContext.GRANT + " " + getPackageName() + " " + getName());
            Log.e("GRANTED PERMISSION:", packageName + " " + name);
            dos.writeBytes(cmd);
            dos.flush();
            dos.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {

        }
        //si è concluso con successo se il permesso è concesso
        return check();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Permission that = (Permission) o;

        if (!name.equals(that.name)) return false;
        if (!group.equals(that.group)) return false;
        return packageName.equals(that.packageName);

    }
}
