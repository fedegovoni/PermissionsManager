package com.federicogovoni.permissionmanager.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.federicogovoni.permissionmanager.R;
import com.federicogovoni.permissionmanager.billing.IabHelper;
import com.federicogovoni.permissionmanager.billing.IabResult;
import com.federicogovoni.permissionmanager.controller.ProVersionChecker;

public class IabHelperInstance {
    private static final String GOOGLE_PUBKEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyqBRK3K+HtClYcdTpGfzCHueMHJGoYj2Cde6Cj5q5vspNnA8LKXhpZsz1AEYYBKqxCOu7lgtID13WJJQCFoDMLSdAP2TQQRTsobS12FpmzSHCfVfQDwmsWcDKdbcAfHHyAqGVGwEcLVH6DbG1sd1WYy5QKdffSZ9I+Qk+TtckwBOPkXHrN6PC6WSLnbVWyRbDWc+EzH+oJl11VQm8JZ+Nmz4KQ2v9I8lPrRn+CY6Y3BexBDP67P0B53I0I0eQzo0gKr/Xa6HndBUSaiINIbmKEbd9oFJ05uaB8Ex4a67uRaaUpDFeFwT7v+tXDAa90gf/XemDUZYuUDJHHsdADtmEQIDAQAB";
    private static IabHelper instance;

    public static IabHelper getInstance(Context context) {
        if(instance == null) {
            instance = new IabHelper(context, GOOGLE_PUBKEY);
            initializeInstance(context);
        }
        return instance;
    }

    public static IabHelper getInstance() {
        return instance;
    }

    private static void initializeInstance(final Context context) {
        instance.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (result.isSuccess()) {
                    ProVersionChecker.getInstance(context, instance).checkPro(true);
                }
            }
        });
    }
}
