package com.federicogovoni.permissionmanager.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.federicogovoni.permissionmanager.model.CurrentContext;
import com.federicogovoni.permissionmanager.model.TimeContext;

import java.util.Calendar;

/**
 * Created by Federico on 12/03/2017.
 */

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        for(CurrentContext p : ContextManager.getInstance(context).getContexts()) {
            if(p.getTimeContext() != null && p.isEnabled(context)) {
                TimeContext tp = p.getTimeContext();
                CurrentContext currentContext = ContextManager.getInstance(context).getById(tp.getCurrentContextId());
                if (tp.getApplyDate().before(Calendar.getInstance().getTime()) &&
                        tp.getDeactivationDate().after(Calendar.getInstance().getTime()))
                    currentContext.setInTime(true, context);
                else {
                    tp.calculateNextApplyDate();
                    tp.calculateNextDeactivationDate();
                    if(tp.getApplyDate().before(Calendar.getInstance().getTime()) &&
                        tp.getDeactivationDate().after(Calendar.getInstance().getTime()))
                        currentContext.setInTime(true, context);
                    else {
                        currentContext.setInTime(false, context);
                    }
                }
            }
        }

        Intent startServiceIntent = new Intent(context, LocationService.class);
        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.O) {
            context.startForegroundService(startServiceIntent);
        }
        else {
            context.startService(startServiceIntent);
        }
    }
}
