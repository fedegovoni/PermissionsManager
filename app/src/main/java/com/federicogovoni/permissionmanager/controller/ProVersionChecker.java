package com.federicogovoni.permissionmanager.controller;

import android.app.ActivityManager;
import android.content.Context;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.federicogovoni.permissionmanager.R;
import com.federicogovoni.permissionmanager.billing.IabHelper;
import com.federicogovoni.permissionmanager.billing.IabResult;
import com.federicogovoni.permissionmanager.billing.Inventory;
import com.federicogovoni.permissionmanager.view.MainActivity;

public class ProVersionChecker {

    private Context mContext;
    private boolean isPro = false;
    private boolean alreadyChecked = false;
    private static ProVersionChecker instance;
    private IabHelper mHelper;
    private static final String TAG = "ProVersionChecker";

    //random values in order to make hacking more difficult
    private static final String PRO_VERSION = "WEOIFHWGJNASKJCAJC";
    private static final String PRO_VERSION_TRUE = "CNWOEIFHWIVALJBDCOAHBD";
    private static final String PRO_VERSION_FALSE = "AOHFWEUIFHERCOIU4";

    public ProVersionChecker(Context context, IabHelper helper) {
        mContext = context;
        this.mHelper = helper;

    }

    //To be invoked in MainActivity
    public static ProVersionChecker getInstance(Context context, IabHelper helper) {
        if(instance == null)
            instance = new ProVersionChecker(context, helper);
        return instance;
    }

    //Can be invoked in activities different from MainActivity (if the above method is called by MainActivity).
    public static ProVersionChecker getInstance() throws NullPointerException {
        if(instance == null)
            throw new NullPointerException();
        return instance;
    }

    private void setAlreadyChecked(boolean value) {
        alreadyChecked = value;
    }

    private void setPro(boolean value) {
        isPro = value;
        PreferenceManager.getDefaultSharedPreferences(mContext).edit().putString(PRO_VERSION, value ? PRO_VERSION_TRUE : PRO_VERSION_FALSE).apply();
    }

    public boolean checkPro() {
        return checkPro(false);
    }

    public boolean checkPro(boolean global) {
        //return false;
        //se già controllato, torno il risultato di prima
        if (alreadyChecked)
            return isPro;
        String result = PreferenceManager.getDefaultSharedPreferences(mContext).getString(PRO_VERSION, PRO_VERSION_FALSE);
        if (result.equals(PRO_VERSION_TRUE)) {
            alreadyChecked = true;
            isPro = true;
        }
        if(global) {
            try {
                mHelper.queryInventoryAsync(new MyQueryInventoryFinishedListener(mContext));
            } catch (IabHelper.IabAsyncInProgressException e) {
                e.printStackTrace();
            }
        }
        return isPro;
    }


    private class MyQueryInventoryFinishedListener implements IabHelper.QueryInventoryFinishedListener {
        private Context mContext;
        public MyQueryInventoryFinishedListener(Context context) {
            mContext = context;
        }

        @Override
        public void onQueryInventoryFinished(IabResult result, Inventory inv) {
            if(result.isSuccess()) {
                setAlreadyChecked(true);
                for(String s : MainActivity.GOOGLE_CATALOG) {
                    if(inv.hasPurchase(s)) {
                        setPro(true);
                        break;
                    }
                }
                if(!isPro)
                    setPro(false);
            } else {
                Toast.makeText(mContext, mContext.getResources().getString(R.string.get_pro_cant_verify_pro_version), Toast.LENGTH_LONG);
            }
        }
    }
}
