package com.federicogovoni.permissionmanager.controller;

import android.app.ActivityManager;
import android.content.Context;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.federicogovoni.permissionmanager.Constants;
import com.federicogovoni.permissionmanager.R;
import com.federicogovoni.permissionmanager.view.MainActivity;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class ProVersionChecker implements PurchasesUpdatedListener {

    private Context mContext;
    private boolean mIsPro = false;
    private static ProVersionChecker instance;
    private static final String TAG = "ProVersionChecker";

    private SkuDetails mSkuDetails = null;
    private BillingClient mBillingClient = null;
    private List<Purchase> mPurchases = null;

    private List<IProVersionListener> iProVersionListeners = new ArrayList<>();


    public ProVersionChecker(Context context, IProVersionListener proVersionListener) {
        mContext = context;
        setupBillingClient(proVersionListener);
    }

    //To be invoked in MainActivity
    public static ProVersionChecker checkIfPro(Context context, IProVersionListener iProVersionListener) {
        if(instance == null) {
            instance = new ProVersionChecker(context, iProVersionListener);
        } else {
            instance.addIProVersionListener(iProVersionListener);
            iProVersionListener.onProVersionResult(instance.isPro());
        }
        return instance;
    }

    private void addIProVersionListener(IProVersionListener iProVersionListener) {
        iProVersionListeners.add(iProVersionListener);
    }

    public static void removeIProVersionListener(IProVersionListener iProVersionListener) {
        if(instance.iProVersionListeners != null && instance.iProVersionListeners.contains(iProVersionListener)) {
            instance.iProVersionListeners.remove(iProVersionListener);
        }
    }

    private boolean isPro() {
        return mIsPro;
    }

    private void setupBillingClient(IProVersionListener iProVersionListener) {
        final BillingClient billingClient = BillingClient.newBuilder(mContext)
                .enablePendingPurchases()
                .setListener(this)
                .build();
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    Timber.d("BILLING | startConnection | RESULT OK");
                    if(billingClient.isReady()) {
                        mBillingClient = billingClient;
                        SkuDetailsParams skuDetailsParams = SkuDetailsParams
                                .newBuilder()
                                .setSkusList(Constants.BILLING_CATALOG)
                                .setType(BillingClient.SkuType.INAPP)
                                .build();
                        billingClient.querySkuDetailsAsync(skuDetailsParams, (billingResult1, skuDetailsList) -> {
                            int responseCode = billingResult1.getResponseCode();
                            onBillingProductsReceived(skuDetailsList, responseCode);
                            if(responseCode == BillingClient.BillingResponseCode.OK) {
                                Timber.d("querySkuDetailsAsync, responseCode: %d", responseCode);
                            } else {
                                Timber.e("Can't querySkuDetailsAsync, responseCode: %d", responseCode);
                            }
                        });
                    } else {
                        Timber.e("BillingClient not ready");
                        Timber.e(String.valueOf(billingClient.getConnectionState()));
                    }
                } else {
                    Timber.e("BILLING | startConnection | RESULT: %d", billingResult.getResponseCode());
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                Timber.d("BILLING | onBillingServiceDisconnected | DISCONNECTED");
            }
        });
    }

    private void onBillingProductsReceived(List<SkuDetails> skuDetails, int responseCode) {
        if(responseCode == BillingClient.BillingResponseCode.OK) {
            if(skuDetails.size() == 0) {
                Timber.v("Could not retrieve SkuDetails");
            } else {
                mSkuDetails = skuDetails.get(0);
                Timber.v("-----------SkuDetail START------------");
                Timber.v("skuDetail.getType(): %s", mSkuDetails.getType());
                Timber.v("skuDetail.getTitle(): %s", mSkuDetails.getTitle());
                Timber.v("skuDetail.getSku(): %s", mSkuDetails.getSku());
                Timber.v("skuDetail.getDescription(): %s", mSkuDetails.getDescription());
                Timber.v("skuDetail.getIntroductoryPriceAmountMicros(): %s", mSkuDetails.getIntroductoryPriceAmountMicros());
                Timber.v("skuDetail.getPriceCurrencyCode(): %s", mSkuDetails.getPriceCurrencyCode());
                Timber.v("skuDetail.getPrice(): %s", mSkuDetails.getPrice());
                Timber.v("skuDetail.getPriceAmountMicros(): %s", mSkuDetails.getPriceAmountMicros());
                Timber.v("-----------SkuDetail END------------> check if purchased items");
                mBillingClient.queryPurchasesAsync(mSkuDetails.getType(), this::onPurchasesUpdated);
            }
        } else {
            Timber.e("There was an error when trying to download skuDetails");
        }
    }

    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> purchases) {
        Timber.v("PurchasesResult got with responseCode: %d and purchases size: %d", billingResult.getResponseCode(), purchases.size());
        boolean isPro = false;
        mPurchases = purchases;
        for (Purchase purchase : mPurchases) {
            Timber.v("-----------Purchase start-----------");
            Timber.v("purchase.getOrderId(): %s", purchase.getOrderId());
            Timber.v("purchase.getSku(): %s", purchase.getSkus());
            Timber.v("purchase.getPackageName(): %s", purchase.getPackageName());
            Timber.v("purchase.getPurchaseToken(): %s", purchase.getPurchaseToken());
            Timber.v("purchase.getSignature(): %s", purchase.getSignature());
            Timber.v("purchase.getPurchaseTime(): %d", purchase.getPurchaseTime());
            Timber.v("purchase.getOriginalJson(): %s", purchase.getOriginalJson());
            Timber.v("-----------Purchase end-----------");
            Timber.d("Confirmed ProVersion acquired");

            //ciclo diverse volte su tutti gli acquisti e se è pro ne tengo il riferimento
            if (purchase.getSkus().size() > 0 && Constants.BILLING_CATALOG.contains(purchase.getSkus().get(0))) {
                isPro = true;
            }
        }
        //Soltanto alla fine del ciclo applico l'operazione
        setProVersion(isPro);
    }

    private void setProVersion(boolean isPro) {
        mIsPro = isPro;

        //Funzione pro non ancora comprata
        for(IProVersionListener iProVersionListener : iProVersionListeners) {
            if(iProVersionListener == null) {
                iProVersionListeners.remove(iProVersionListener);
            } else {
                iProVersionListener.onProVersionResult(isPro);
            }
        }
        if (isPro) {
            Timber.v("Setting Pro Version");
        } else {
            Timber.v("Removing Pro Version");
        }
    }

    public interface IProVersionListener {
        void onProVersionResult(boolean isPro);
    }
}
