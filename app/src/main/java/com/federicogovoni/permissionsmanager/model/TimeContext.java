package com.federicogovoni.permissionsmanager.model;

import android.content.Context;
import android.util.Log;

import com.federicogovoni.permissionsmanager.controller.Alarm;
import com.federicogovoni.permissionsmanager.controller.ContextManager;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Federico on 03/03/2017.
 */

public class TimeContext implements Serializable {

    private static final String TAG = "TimeContext";

    public static final int NONE = 0;
    public static final int DAILY = 1;
    public static final int WEEKLY = 2;

    private int frequency;
    private Date applyDate;
    private Date deactivationDate;
    private List<Integer> daysOfWeek;
    private int currentContextId;
    private Date lastAlarmDate;

    private static Locale locale = Locale.getDefault();

    public TimeContext(int frequency, Date applyDate, Date deactivationDate, List<Integer> daysOfWeek, int currentContextId, Context context) {
        this.frequency = frequency;
        this.daysOfWeek = daysOfWeek;
        this.currentContextId = currentContextId;

        //settaggio delle due date azzerando secondi e millisecondi
        Calendar c = Calendar.getInstance();
        c.setTime(applyDate);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        this.applyDate = c.getTime();

        c.setTime(deactivationDate);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        this.deactivationDate = c.getTime();

        //check if is in period of activation to set
        check(context);
    }

    public int getCurrentContextId() {
        return currentContextId;
    }

    public void setCurrentContextId(int policyId, Context context) {
        this.currentContextId = policyId;
        //check if in time and then set inperiod variable
    }

    public int getFrequency() {
        return frequency;
    }

    public Date getApplyDate() {
        return applyDate;
    }


    public Date getDeactivationDate() {return deactivationDate;   }

    public List<Integer> getDaysOfWeek() {
        return daysOfWeek;
    }

    public String getApplyTime() {
        if(applyDate != null) {
            DateFormat df = DateFormat.getTimeInstance(DateFormat.SHORT, locale);
            Calendar c = Calendar.getInstance();
            c.setTime(applyDate);
            return df.format(c.getTime());
        } else return "";
    }

    public String getDeactivationTime() {
        if(deactivationDate != null) {
            DateFormat df = DateFormat.getTimeInstance(DateFormat.SHORT, locale);
            Calendar c = Calendar.getInstance();
            c.setTime(deactivationDate);
            return df.format(c.getTime());
        }else return "";
    }



    //set di diableDate alla prossima volta
    public void calculateNextApplyDate() {
        if(frequency == NONE)
            return;

        Calendar current = Calendar.getInstance();
        current.set(Calendar.SECOND, 0);
        current.set(Calendar.MILLISECOND, 0);
        Calendar apply = Calendar.getInstance();
        apply.setTime(applyDate);
        apply.set(Calendar.SECOND, 0);
        apply.set(Calendar.MILLISECOND, 0);

        if(frequency != WEEKLY) {
            while(apply.getTime().before(current.getTime()))
                apply.add(Calendar.DAY_OF_MONTH, 1);
            applyDate = apply.getTime();
        } else {//weekly
            while (apply.getTime().before(current.getTime()) || !daysOfWeek.contains(apply.get(Calendar.DAY_OF_WEEK)))
                apply.add(Calendar.DAY_OF_MONTH, 1);
            applyDate = apply.getTime();
        }
        Log.d(TAG, "Next apply date setted on " + getApplyTime());
    }

    //set di disableDate alla prossima volta
    public void calculateNextDeactivationDate() {

        if(frequency == NONE)
            return;

        Calendar disable = Calendar.getInstance();
        disable.setTime(deactivationDate);
        disable.set(Calendar.SECOND, 0);
        disable.set(Calendar.MILLISECOND, 0);
        Calendar apply = Calendar.getInstance();
        calculateNextApplyDate();
        apply.setTime(applyDate);
        apply.set(Calendar.SECOND, 0);
        apply.set(Calendar.MILLISECOND, 0);

        while (disable.getTime().before(apply.getTime()))
            disable.add(Calendar.DAY_OF_MONTH, 1);
        deactivationDate = disable.getTime();
        Log.d(TAG, "Next deactivation date setted on " + getDeactivationTime());
    }

    //controlla se una data appartiene ad un periodo di attivo
    private boolean isInTime(Date currentDate) {
        if(frequency == NONE) {
            return applyDate.before(currentDate) && deactivationDate.after(currentDate);
        } else if (frequency == DAILY) {
            Calendar current = Calendar.getInstance();
            current.setTime(currentDate);
            int currentHour = current.get(Calendar.HOUR_OF_DAY);
            int currentMinute = current.get(Calendar.MINUTE);
            current.add(Calendar.DAY_OF_MONTH, -1);
            Calendar apply = Calendar.getInstance();
            apply.setTime(applyDate);
            int applyHour = apply.get(Calendar.HOUR_OF_DAY);
            int applyMin = apply.get(Calendar.MINUTE);
            Calendar disable = Calendar.getInstance();
            disable.setTime(deactivationDate);
            int disableHour = disable.get(Calendar.HOUR_OF_DAY);
            int disableMin = disable.get(Calendar.MINUTE);
            current.setTime(currentDate);

            boolean applyMinDisable = (applyHour < disableHour || (applyHour == disableHour && applyMin < disableMin));
            boolean applyMinCurrent = (applyHour < currentHour || (applyHour == currentHour && applyMin < currentMinute));
            boolean currentMinDisable = (currentHour < disableHour || (currentHour == disableHour && currentMinute < disableMin));
            boolean disableMinApply = (disableHour < applyHour ||(disableHour == applyHour && disableMin < applyMin));

            //ora attuale compresa tra ora apply e ora deactivate e ora attivaz < ora disatt
            boolean a = (applyMinDisable && applyMinCurrent && currentMinDisable);
            //ora attuale < ora disatt o ora attuale > ora att e ora att > ora disatt
            boolean b = (disableMinApply && (currentMinDisable || applyMinCurrent));

            if( a || b )
                return true;
            else
                return false;
        } else { //frequency = weekly
            Calendar current = Calendar.getInstance();
            current.setTime(currentDate);
            int currentDayOfWeek = current.get(Calendar.DAY_OF_WEEK);
            int currentHour = current.get(Calendar.HOUR_OF_DAY);
            int currentMinute = current.get(Calendar.MINUTE);
            current.add(Calendar.DAY_OF_MONTH, -1);
            int pastDayOfWeek = current.get(Calendar.DAY_OF_WEEK);
            Calendar apply = Calendar.getInstance();
            apply.setTime(applyDate);
            int applyHour = apply.get(Calendar.HOUR_OF_DAY);
            int applyMin = apply.get(Calendar.MINUTE);
            Calendar disable = Calendar.getInstance();
            disable.setTime(deactivationDate);
            int disableHour = disable.get(Calendar.HOUR_OF_DAY);
            int disableMin = disable.get(Calendar.MINUTE);
            current.setTime(currentDate);

            boolean applyMinDisable = (applyHour < disableHour || (applyHour == disableHour && applyMin < disableMin));
            boolean applyMinCurrent = (applyHour < currentHour || (applyHour == currentHour && applyMin < currentMinute));
            boolean currentMinDisable = (currentHour < disableHour || (currentHour == disableHour && currentMinute < disableMin));
            boolean disableMinApply = (disableHour < applyHour ||(disableHour == applyHour && disableMin < applyMin));

            //ora attuale compresa tra ora apply e ora deactivate e ora attivaz < ora disatt
            boolean a = (applyMinDisable && applyMinCurrent && currentMinDisable);
            //ora attuale < ora disatt o ora attuale > ora att e ora att > ora disatt
            boolean b = (disableMinApply && (currentMinDisable || applyMinCurrent));

            if(daysOfWeek.contains(currentDayOfWeek) && (a || b))
                return true;
            else
                return false;
        }
    }

    //settaggio prossimo allarme e aggiornamento valore CurrentContext
    //metodo invocato soltanto dall'alarm
    //da currentContext setInTime(isInTime(Calendar.getInstance()), context)
    public void check(Context context) {
        check(context, ContextManager.getInstance(context).getById(currentContextId));
    }

    public void check(Context context, CurrentContext currentContext) {
        boolean value = isInTime(Calendar.getInstance().getTime());
        currentContext.setInTime(value, context);
        Alarm alarm = new Alarm();
        calculateNextApplyDate();
        calculateNextDeactivationDate();
        if(!value && frequency != NONE && !applyDate.equals(lastAlarmDate)) {
            alarm.setAlarm(context, this, applyDate, Alarm.ACTION_APPLY);
            lastAlarmDate = applyDate;
        }
        else if(value && !deactivationDate.equals(lastAlarmDate)) {
            alarm.setAlarm(context, this, deactivationDate, Alarm.ACTION_DISABLE);
            lastAlarmDate = deactivationDate;
        }
    }

    public void cancelAlarms(Context context) {
        new Alarm().cancelAlarm(context, this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TimeContext that = (TimeContext) o;

        if (frequency != that.frequency) return false;
        if (applyDate != null ? !applyDate.equals(that.applyDate) : that.applyDate != null)
            return false;
        if (deactivationDate != null ? !deactivationDate.equals(that.deactivationDate) : that.deactivationDate != null)
            return false;
        return daysOfWeek != null ? daysOfWeek.equals(that.daysOfWeek) : that.daysOfWeek == null;
    }
}
