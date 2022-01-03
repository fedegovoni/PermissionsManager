package com.federicogovoni.permissionsmanager.controller

import android.annotation.SuppressLint
import com.federicogovoni.permissionsmanager.controller.ProVersionChecker.IProVersionListener
import timber.log.Timber
import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.federicogovoni.permissionsmanager.Constants
import com.federicogovoni.permissionsmanager.controller.ProVersionChecker
import java.util.ArrayList
import java.util.function.Consumer

class ProVersionChecker private constructor(private val mContext: Context) : PurchasesUpdatedListener {

    private var mIsPro = false
    private var mSkuDetails: List<SkuDetails>? = null
    private var mBillingClient: BillingClient? = null
    private val mPurchases: MutableList<Purchase> = ArrayList()
    private var proSkuDetails: SkuDetails? = null
    private val iProVersionListeners: MutableList<IProVersionListener> = ArrayList()

    fun addIProVersionListener(iProVersionListener: IProVersionListener) {
        iProVersionListeners.add(iProVersionListener)
    }

    init {
        setupBillingClient()
    }

    private fun setupBillingClient() {
        val billingClient = BillingClient.newBuilder(mContext)
            .enablePendingPurchases()
            .setListener(this)
            .build()
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Timber.d("BILLING | startConnection | RESULT OK")
                    if (billingClient.isReady) {
                        mBillingClient = billingClient
                        val skuDetailsParams = SkuDetailsParams
                            .newBuilder()
                            .setSkusList(Constants.BILLING_CATALOG)
                            .setType(BillingClient.SkuType.INAPP)
                            .build()
                        billingClient.querySkuDetailsAsync(skuDetailsParams) { billingResult1: BillingResult, skuDetailsList: List<SkuDetails>? ->
                            val responseCode = billingResult1.responseCode
                            onBillingProductsReceived(skuDetailsList, responseCode)
                            if (responseCode == BillingClient.BillingResponseCode.OK) {
                                Timber.d("querySkuDetailsAsync, responseCode: %d", responseCode)
                                setProVersion(true)
                            } else {
                                Timber.e(
                                    "Can't querySkuDetailsAsync, responseCode: %d",
                                    responseCode
                                )
                            }
                        }
                    } else {
                        Timber.e("BillingClient not ready")
                        Timber.e(billingClient.connectionState.toString())
                    }
                } else {
                    Timber.e("BILLING | startConnection | RESULT: %d", billingResult.responseCode)
                }
            }

            override fun onBillingServiceDisconnected() {
                Timber.d("BILLING | onBillingServiceDisconnected | DISCONNECTED")
            }
        })
    }

    //Purchases
    private fun onBillingProductsReceived(skuDetails: List<SkuDetails>?, responseCode: Int) {
        if (responseCode == BillingClient.BillingResponseCode.OK) {
            if (skuDetails!!.isEmpty()) {
                Timber.v("Could not retrieve SkuDetails")
            } else {
                mSkuDetails = skuDetails
                mSkuDetails!!.forEach(Consumer { skuDetail: SkuDetails ->
                    Timber.v("-----------SkuDetail START------------")
                    Timber.v("skuDetail.getType(): %s", skuDetail.type)
                    Timber.v("skuDetail.getTitle(): %s", skuDetail.title)
                    Timber.v("skuDetail.getSku(): %s", skuDetail.sku)
                    Timber.v("skuDetail.getDescription(): %s", skuDetail.description)
                    Timber.v("skuDetail.getIntroductoryPriceAmountMicros(): %s",skuDetail.introductoryPriceAmountMicros)
                    Timber.v("skuDetail.getPriceCurrencyCode(): %s", skuDetail.priceCurrencyCode)
                    Timber.v("skuDetail.getPrice(): %s", skuDetail.price)
                    Timber.v("skuDetail.getPriceAmountMicros(): %s", skuDetail.priceAmountMicros)
                    Timber.v("-----------SkuDetail END------------> check if purchased items")
                })
                proSkuDetails = mSkuDetails!!.stream()
                    .filter { purchase: SkuDetails -> purchase.title.contains("Pro") }
                    .toArray()[0] as SkuDetails
                mBillingClient!!.queryPurchasesAsync(mSkuDetails!![0].type) { billingResult: BillingResult, purchases: List<Purchase>? ->
                    onPurchasesUpdated(
                        billingResult,
                        purchases
                    )
                }
            }
        } else {
            Timber.e("There was an error when trying to download skuDetails")
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        Timber.v(
            "PurchasesResult got with responseCode: %d and purchases size: %d",
            billingResult.responseCode,
            purchases?.size
        )
        mIsPro = false
        if(purchases != null) {
            mPurchases.addAll(purchases)
        }
        for (purchase in mPurchases) {
            Timber.v("-----------Purchase start-----------")
            Timber.v("purchase.getOrderId(): %s", purchase.orderId)
            Timber.v("purchase.getSku(): %s", purchase.skus)
            Timber.v("purchase.getPackageName(): %s", purchase.packageName)
            Timber.v("purchase.getPurchaseToken(): %s", purchase.purchaseToken)
            Timber.v("purchase.getSignature(): %s", purchase.signature)
            Timber.v("purchase.getPurchaseTime(): %d", purchase.purchaseTime)
            Timber.v("purchase.getOriginalJson(): %s", purchase.originalJson)
            Timber.v("-----------Purchase end-----------")
            Timber.d("Confirmed ProVersion acquired")

            //ciclo diverse volte su tutti gli acquisti e se è pro ne tengo il riferimento
            if(!mIsPro) {
                mIsPro = purchase.skus.size > 0 && Constants.BILLING_CATALOG.contains(purchase.skus[0])
            }
        }
        //Soltanto alla fine del ciclo applico l'operazione
        setProVersion(mIsPro)
    }

    private fun setProVersion(isPro: Boolean) {
        mIsPro = isPro

        //Funzione pro non ancora comprata
        iProVersionListeners.forEach { iProVersionListener ->
            iProVersionListener.onProVersionResult(isPro)
        }
        Timber.v("Setting Pro Version: %b", isPro)
    }

    fun startPurchaseFlow(activity: Activity?) {
        if (mSkuDetails != null && mBillingClient != null) {
            val billingFlowParams = BillingFlowParams.newBuilder().setSkuDetails(proSkuDetails!!).build()
            mBillingClient?.launchBillingFlow(activity!!, billingFlowParams)
        }
    }

    fun resetPurchases() {
        mPurchases.map { p ->
            {
                val consumeParams = ConsumeParams.newBuilder().setPurchaseToken(p.purchaseToken).build()
                mBillingClient!!.consumeAsync(consumeParams) { billingResult, purchaseToken ->
                    Timber.d("PurchaseToken %s reset with result %d", purchaseToken, billingResult.responseCode)
                }
                setProVersion(false);
            }
        }
    }

    fun unregisterIProVersionListener(iProVersionListener: IProVersionListener) {
        iProVersionListeners.remove(iProVersionListener)
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var instance: ProVersionChecker? = null

        @JvmStatic
        fun getInstance(context: Context): ProVersionChecker? {
            if (instance == null) {
                instance = ProVersionChecker(context)
            }
            return instance
        }

        @JvmStatic
        fun checkIfPro(context: Context,iProVersionListener: IProVersionListener) {
            val instance = getInstance(context)
            instance!!
            instance.addIProVersionListener(iProVersionListener)
            iProVersionListener.onProVersionResult(instance.mIsPro)

        }

        @JvmStatic
        val proVersionPrice: String?
            get() = if (instance != null && instance!!.proSkuDetails != null) {
                instance!!.proSkuDetails!!.price
            } else {
                null
            }
    }

    interface IProVersionListener {
        fun onProVersionResult(isPro: Boolean)
    }
}