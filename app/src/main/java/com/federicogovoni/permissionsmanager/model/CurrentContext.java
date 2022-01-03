package com.federicogovoni.permissionsmanager.model;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.federicogovoni.permissionmanager.R;
import com.federicogovoni.permissionsmanager.controller.ContextManager;
import com.federicogovoni.permissionsmanager.view.main.MainActivity;
import com.federicogovoni.permissionsmanager.view.main.settings.SettingsFragment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Federico on 27/03/2017.
 */

public class CurrentContext implements Serializable {
    public static final String GRANT = "grant";
    public static final String REVOKE = "revoke";

    public static final String TAG = "CURRENT CONTEXT";

    public static final String RUNNING_STATE = "RUNNING_STATE_";
    public static final String IN_TIME = "IN_TIME_";
    public static final String IN_LOCATION = "IN_LOCATION_";
    public static final String ENABLED = "ENABLED_";

    public static final String NOTIFICATION_CHANNEL_NAME = "Permissions Manager";

    private transient Context context;
    private TimeContext timeContext;
    private LocationContext locationContext;

    private Map<Permission, String> applicationPermissions;
    private String name;
    private int id = -1;

    public CurrentContext (String name, Context context) {
        this.name = name;
        this.context = context;
        this.id = ContextManager.getInstance(context).getNewId();
        applicationPermissions = new HashMap<>();
    }

    public CurrentContext(String name, TimeContext timeContext, LocationContext locationContext, Context context) {
        this.timeContext = timeContext;
        this.locationContext = locationContext;
        this.name = name;
        this.id = ContextManager.getInstance(context).getNewId();
        applicationPermissions = new HashMap<>();

        this.timeContext.check(context);
    }

    public TimeContext getTimeContext() {
        return timeContext;
    }

    public void setTimeContext(TimeContext timeContext, Context context) {
        this.timeContext = timeContext;
        this.timeContext.check(context);
        ContextManager.getInstance(context).store();
    }

    public LocationContext getLocationContext() {
        return locationContext;
    }

    public void setLocationContext(LocationContext locationContext) {
        this.locationContext = locationContext;
        ContextManager.getInstance(context).store();
    }

    public boolean isInLocation(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(IN_LOCATION+id, false);
    }

    public void setInLocation(boolean value, Context context) {
        if(isInTime(context) && !isInLocation(context) && isEnabled(context) && value) {
            activate(context);
        } else if(!value){
            deactivate(context);
        }
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(IN_LOCATION+id, value).apply();
    }

    public boolean isInTime(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(IN_TIME+id, false);
    }

    public void setInTime(boolean value, Context context) {
        if(value && !isInTime(context) && isInLocation(context) && isEnabled(context)) {
            Log.d(TAG, "setInTime - invoked activate action");
            activate(context);
        } else if(!value){
            Log.d(TAG, "setInTime - invoked deactivate action");
            deactivate(context);
        }
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(IN_TIME+id, value).apply();
        //settaggio prossimo allarme
    }

    public Map<Permission, String> getApplicationPermissions() {
        return applicationPermissions;
    }

    public void setApplicationPermissions(Map<Permission, String> applicationPermissions) {
        this.applicationPermissions = applicationPermissions;
    }

    public synchronized void setRevokePermissionsList(List<Permission> revokeList) {
        if(applicationPermissions == null) {
            return;
        }
        Map<Permission, String> tmp = new HashMap<>();
        for(Permission p : applicationPermissions.keySet())
            if(applicationPermissions.get(p).equals(GRANT))
                tmp.put(p, GRANT);
        setApplicationPermissions(tmp);
        for(Permission p : revokeList)
            applicationPermissions.put(p, REVOKE);
    }

    public void setGrantPermissionsList(List<Permission> grantList) {
        if(applicationPermissions == null) {
            return;
        }
        Map<Permission, String> tmp = new HashMap<>();
        for(Permission p : applicationPermissions.keySet())
            if(applicationPermissions.get(p).equals(REVOKE))
                tmp.put(p, REVOKE);
        setApplicationPermissions(tmp);
        for(Permission p : grantList)
            applicationPermissions.put(p, GRANT);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id, Context context) {
        this.id = id;
        if(timeContext != null)
            timeContext.setCurrentContextId(id, context);
        if(locationContext != null)
            locationContext.setCurrentContextId(id);
    }

    public boolean isRunning(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(RUNNING_STATE + id, false);
    }

    public void setRunning(boolean running, Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(RUNNING_STATE + id, running).apply();
    }

    public boolean isEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(ENABLED+id, true);
    }

    public void setEnabled(boolean value, Context context) {
        if (value && timeContext != null){
            Log.d(TAG, getName() + " " + context.getResources().getString(R.string.enabled));
            timeContext.check(context);
        }
        else if (!value && timeContext != null) {
            Log.d(TAG, getName() + " " + context.getResources().getString(R.string.enabled));
            timeContext.cancelAlarms(context);
        }
        if (isInTime(context) && isInLocation(context) && value && !isEnabled(context)) {
            activate(context);
            setRunning(true, context);
        } else if (isInTime(context) && isInLocation(context) && isEnabled(context) && !value) {
            deactivate(context);
            setRunning(false, context);
        }
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(ENABLED+id, value).apply();
    }

    public List<Permission> getGrantList() {
        List<Permission> res = new ArrayList<>();
        for(Permission p : applicationPermissions.keySet())
            if(applicationPermissions.get(p).equals(GRANT))
                res.add(p);
        return res;
    }

    public List<Permission> getRevokeList() {
        List<Permission> res = new ArrayList<>();
        for(Permission p : applicationPermissions.keySet())
            if(applicationPermissions.get(p).equals(REVOKE))
                res.add(p);
        return res;
    }

    private void activate(Context context) {
        if (!isRunning(context)) {
            setRunning(true, context);
            showNotification(context.getResources().getString(R.string.applied_context) + " " + name, context, id);
            for (Permission p : applicationPermissions.keySet()) {
                boolean res = applicationPermissions.get(p).equals(GRANT) ? p.grant() : p.revoke();
                if (res)
                    Log.d("APPLIED CONTEXT", this.getName() + ": " + p.getName());
                else
                    Log.d("APPLIED CONTEXT", "Can't get root access");
            }
            timeContext.check(context);
        }
    }

    private void deactivate(Context context) {
        if (isRunning(context)) {
            setRunning(false, context);
            showNotification(context.getResources().getString(R.string.disabled_context) + " " + name, context, id);
            for (Permission p : applicationPermissions.keySet()) {
                boolean res = applicationPermissions.get(p).equals(GRANT) ? p.revoke() : p.grant();
                if (res)
                    Log.d("UNAPPLIED CONTEXT", this.getName() + ": " + p.getName());
                else
                    Log.d("UNAPPLY CONTEXT", "Can't get root access");
            }
            timeContext.check(context);
        }
    }

    private void showNotification(String message, Context context, int id) {

        boolean receiveNotifications = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext()).getBoolean(SettingsFragment.RECEIVE_NOTIFICATION_SHARED_PREFERENCE, true);
        if(!receiveNotifications)
            return;

        NotificationCompat.Builder mBuilder;
        Log.d(TAG, "Show notification: " + message);

        Intent resultIntent = new Intent(context , MainActivity.class);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(context,
                0 /* Request code */, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            mBuilder = new NotificationCompat.Builder(context);
            mBuilder.setSmallIcon(R.drawable.notification_icon);
            mBuilder.setColor(context.getResources().getColor(R.color.colorPrimary, context.getTheme()));
            mBuilder.setContentTitle(context.getResources().getString(R.string.app_name))
                    .setContentText(message)
                    .setAutoCancel(true)
                    .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                    .setContentIntent(resultPendingIntent);

            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            NotificationChannel notificationChannel = new NotificationChannel(id + ""
                    , NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(context.getResources().getColor(R.color.colorPrimary, context.getTheme()));
            notificationChannel.enableVibration(true);
            assert mNotificationManager != null;
            mBuilder.setChannelId(id + "");
            mNotificationManager.createNotificationChannel(notificationChannel);

            assert mNotificationManager != null;
            mNotificationManager.notify(0 /* Request Code */, mBuilder.build());
        } else {
            NotificationCompat.Builder oldBuilder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(R.drawable.notification_icon)
                            .setContentTitle(context.getResources().getString(R.string.app_name))
                            .setContentText(message);

            Intent openHomePageActivity = new Intent(context, MainActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addNextIntent(openHomePageActivity);

            PendingIntent oldResultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            oldBuilder.setContentIntent(oldResultPendingIntent);
            oldBuilder.setAutoCancel(true);
            oldBuilder.setColor(context.getResources().getColor(R.color.colorPrimary));
            NotificationManager mNotificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            mNotificationManager.notify(id, oldBuilder.build());
        }

    }

}
