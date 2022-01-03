package com.federicogovoni.permissionsmanager.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.federicogovoni.permissionsmanager.model.CurrentContext;
import com.federicogovoni.permissionsmanager.model.TimeContext;
import com.federicogovoni.permissionsmanager.utils.GeneralUtils;

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


        GeneralUtils.checkAndStartLocationService(context);
    }
}
