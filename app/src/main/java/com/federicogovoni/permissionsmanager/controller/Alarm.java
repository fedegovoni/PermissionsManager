package com.federicogovoni.permissionsmanager.controller;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

import com.federicogovoni.permissionsmanager.model.CurrentContext;
import com.federicogovoni.permissionsmanager.model.TimeContext;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


/**
 * Created by Federico on 16/03/2017.
 */

public class Alarm extends BroadcastReceiver {

    private static final String POLICY_HASH = "POLICY_HASH";
    private static final String ACTION = "ACTION";
    public static final String ACTION_APPLY = "APPLY";
    public static final String ACTION_DISABLE = "DISABLE";
    private static final String TAG = "MY_ALARM_MANAGER";
    private static final String STOP = "ACTION_STOP";

    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        wl.acquire(10*60*1000L /*10 minutes*/);

        int id = (int) intent.getExtras().get(POLICY_HASH);
        Log.d(TAG, "Alarm manager onReceive invoked, searching for id = "+ id);

        CurrentContext p = ContextManager.getInstance(context).getById(id);
        if(p != null) {
            String action = (String) intent.getExtras().get(ACTION);
            Log.d(TAG, "id = " + p.getId() + " action: " + action);
            if (action.equals(ACTION_APPLY)) {
                p.getTimeContext().check(context, p);
                Log.d(TAG, ACTION_APPLY);
            } else if (action.equals(ACTION_DISABLE)) {
                p.getTimeContext().check(context, p);
                Log.d(TAG, "out of timeContext context " + p.getName());
            } else if (action.equals(STOP))
                Log.d(TAG, "Stopped alarm");
            else
                Log.e(TAG, "Invalid tag operation");
        }

        wl.release();
    }

    public void setAlarm(Context context, TimeContext timeContext, Date applyDate, String action) {
        AlarmManager am =(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, Alarm.class);
        i.putExtra(POLICY_HASH, timeContext.getCurrentContextId());
        i.putExtra(ACTION, action);
        int id = timeContext.getCurrentContextId();
        PendingIntent pi = PendingIntent.getBroadcast(context, id, i, PendingIntent.FLAG_UPDATE_CURRENT);
        Calendar c = Calendar.getInstance();
        c.setTime(applyDate);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Log.e(TAG, "SingleContext id = " + id + " next alarm setted at " + sdf.format(c.getTime()));
        am.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pi); // Millisec * Second * Minute
    }

    public void cancelAlarm(Context context, TimeContext timeContext) {
        int id = timeContext.getCurrentContextId();
        Log.e("TAG", "deleting alarm id = " + id);
        Intent intent = new Intent(context, Alarm.class);
        intent.putExtra(POLICY_HASH, timeContext.hashCode());
        intent.putExtra(ACTION, ACTION_APPLY);
        PendingIntent sender = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }
}
