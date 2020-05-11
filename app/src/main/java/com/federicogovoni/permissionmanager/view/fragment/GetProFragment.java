/*
 * Copyright (C) 2011-2015 Dominik Schürmann <dominik@dominikschuermann.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.federicogovoni.permissionmanager.view.fragment;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.afollestad.materialdialogs.MaterialDialog;
import com.federicogovoni.permissionmanager.R;

import com.federicogovoni.permissionmanager.billing.IabException;
import com.federicogovoni.permissionmanager.billing.IabHelper;
import com.federicogovoni.permissionmanager.billing.IabResult;
import com.federicogovoni.permissionmanager.billing.Inventory;
import com.federicogovoni.permissionmanager.billing.Purchase;
import com.federicogovoni.permissionmanager.controller.ProVersionChecker;
import com.federicogovoni.permissionmanager.utils.AdRequestKeeper;
import com.federicogovoni.permissionmanager.utils.IabHelperInstance;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import timber.log.Timber;

public class GetProFragment extends Fragment {

    public static final String ARG_DEBUG = "debug";

    public static final String ARG_GOOGLE_ENABLED = "googleEnabled";
    public static final String ARG_GOOGLE_CATALOG = "googleCatalog";
    public static final String ARG_GOOGLE_CATALOG_VALUES = "googleCatalogValues";

    private static final String TAG = "Donations Library";

    // http://developer.android.com/google/play/billing/billing_testing.html

    // Google Play helper object
    private IabHelper mHelper;

    protected boolean mDebug = true;

    protected boolean mGoogleEnabled = true;
    protected String[] mGoogleCatalog = new String[]{};
    protected String[] mGoogleCatalogValues = new String[]{};

    private AdView mAdView;

    // Callback for when a purchase is finished
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            if (mDebug)
                Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            if (result.isSuccess()) {
                if (mDebug)
                    Log.d(TAG, "Purchase successful.");

                // directly consume in-app purchase, so that people can donate multiple times
                try {
                    mHelper.consumeAsync(purchase, mConsumeFinishedListener);

                    // show thanks openDialog
                    openDialog(android.R.drawable.ic_dialog_info, R.string.get_pro_thanks,
                            getString(R.string.get_pro_thanks_dialog));
                } catch (IabHelper.IabAsyncInProgressException e) {
                    e.printStackTrace();

                    //aprire dialog di errore
                }

                Log.d(TAG, "Eseguito onPurhcaseFinished");
            }
        }
    };

    // Called when consumption is complete
    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        public void onConsumeFinished(Purchase purchase, IabResult result) {
            if (mDebug)
                Log.d(TAG, "Consumption finished. Purchase: " + purchase + ", result: " + result);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            if (result.isSuccess()) {
                if (mDebug)
                    Log.d(TAG, "Consumption successful. Provisioning.");
            }
            if (mDebug)
                Log.d(TAG, "End consumption flow.");
            Log.d(TAG, "Eseguito onConsumeFinished");
        }
    };

    /**
     * Instantiate GetProFragment.
     *
     * @param debug               You can use BuildConfig.DEBUG to propagate the debug flag from your app to the Donations library
     * @param googleEnabled       Enabled Google Play donations
     * @param googleCatalog       Possible item names that can be purchased from Google Play
     * @param googleCatalogValues Values for the names
     * @return GetProFragment
     */
    public static GetProFragment newInstance(boolean debug, boolean googleEnabled, String[] googleCatalog,
                                             String[] googleCatalogValues) {
        GetProFragment donationsFragment = new GetProFragment();
        Bundle args = new Bundle();

        args.putBoolean(ARG_DEBUG, debug);

        args.putBoolean(ARG_GOOGLE_ENABLED, googleEnabled);
        args.putStringArray(ARG_GOOGLE_CATALOG, googleCatalog);
        args.putStringArray(ARG_GOOGLE_CATALOG_VALUES, googleCatalogValues);

        donationsFragment.setArguments(args);
        return donationsFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDebug = getArguments().getBoolean(ARG_DEBUG);
        mGoogleEnabled = getArguments().getBoolean(ARG_GOOGLE_ENABLED);
        mGoogleCatalog = getArguments().getStringArray(ARG_GOOGLE_CATALOG);
        mGoogleCatalogValues = getArguments().getStringArray(ARG_GOOGLE_CATALOG_VALUES);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_get_pro, container, false);
    }

    @TargetApi(11)
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Sample AdMob app ID: ca-app-pub-9125265928210219~3176045725
        mAdView = getActivity().findViewById(R.id.fragment_get_pro_ad_view);
        AdRequest adRequest = AdRequestKeeper.getAdRequest(getActivity());
        mAdView.loadAd(adRequest);

        /* Google */
        if (mGoogleEnabled) {
            // inflate google view into stub
            // choose donation amount
            ArrayAdapter<CharSequence> adapter;
            if (mDebug) {
                adapter = new ArrayAdapter<CharSequence>(getActivity(),
                        android.R.layout.simple_spinner_item, mGoogleCatalog);
            } else {
                adapter = new ArrayAdapter<CharSequence>(getActivity(),
                        android.R.layout.simple_spinner_item, mGoogleCatalogValues);
            }
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            Button btGoogle = (Button) getActivity().findViewById(
                    R.id.donations__fragment_google_get_pro_button);
            if(Build.VERSION.SDK_INT >= 21)
                btGoogle.setBackgroundTintList(getActivity().getResources().getColorStateList(R.color.colorAccent));
            else
                btGoogle.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            btGoogle.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    donateGoogleOnClick(v);
                }
            });

            //imposto il valore del prezzo
            TextView priceText = getActivity().findViewById(R.id.fragment_get_pro_price_text_view);
            try {
                Inventory inventory = IabHelperInstance.getInstance().queryInventory();
                //@todo inserire il prezzo dinamicamente
                priceText.setText("jioj");
            } catch (IabException e) {
                Timber.e(e.getResult());
                e.printStackTrace();
            }


            try {
                if (ProVersionChecker.getInstance().checkPro()) {
                    getActivity().findViewById(R.id.fragment_get_pro_ad_view).setVisibility(View.GONE);
                    Button button = (Button) getActivity().findViewById(R.id.donations__fragment_google_get_pro_button);
                    button.setActivated(false);
                    button.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new MaterialDialog.Builder(getActivity())
                                    .items(new String[] {getResources().getString(R.string.get_pro_already_pro)})
                                    .positiveText(getResources().getString(R.string.ok))
                                    .itemsCallback(new MaterialDialog.ListCallback() {
                                        @Override
                                        public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                        }
                                    })
                                    .show();
                        }
                    });
                }
            }catch (NullPointerException e) {
                e.printStackTrace();
            }

            // Create the helper, passing it our context and the public key to verify signatures with
            if (mDebug)
                Log.d(TAG, "Creating IAB helper.");
            mHelper = IabHelperInstance.getInstance(getActivity().getApplicationContext());

            // Start setup. This is asynchronous and the specified listener
            // will be called once setup completes.
        }
    }

    /**
     * Open dialog
     */
    void openDialog(int icon, int title, String message) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setIcon(icon);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setCancelable(true);
        dialog.setNeutralButton(R.string.get_pro_button_close,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }
        );
        dialog.show();
    }

    /**
     * Donate button executes donations based on selection in spinner
     */
    public void donateGoogleOnClick(View view) {
        final int index;
        index = 0;
        if (mDebug)
            Log.d(TAG, "selected item in spinner: " + index);

        try {
            if (mDebug) {
                // when debugging, choose android.test.x item
                mHelper.launchPurchaseFlow(getActivity(), mGoogleCatalog[index], 0, mPurchaseFinishedListener, IabHelper.ITEM_TYPE_INAPP);
                //mHelper.launchPurchaseFlow(getActivity(), mGgoogleCatalog[index], IabHelper.ITEM_TYPE_INAPP,0, mPurchaseFinishedListener, null);
            } else {
                mHelper.launchPurchaseFlow(getActivity(), mGoogleCatalog[index], 0, mPurchaseFinishedListener, IabHelper.ITEM_TYPE_INAPP);
                //mHelper.launchPurchaseFlow(getActivity(), mGgoogleCatalog[index], IabHelper.ITEM_TYPE_INAPP,0, mPurchaseFinishedListener, null);
            }
        } catch (IabHelper.IabAsyncInProgressException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mDebug)
            Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);
        if (mHelper == null) return;

        // Pass on the fragment result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            super.onActivityResult(requestCode, resultCode, data);
        } else {
            if (mDebug)
                Log.d(TAG, "onActivityResult handled by IABUtil.");
        }
    }
}