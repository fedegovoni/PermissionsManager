package com.federicogovoni.permissionsmanager.view.main;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.federicogovoni.permissionmanager.R;
import com.federicogovoni.permissionsmanager.controller.ProVersionChecker;
import com.federicogovoni.permissionsmanager.utils.AdRequestKeeper;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.analytics.FirebaseAnalytics;

import butterknife.BindView;
import butterknife.ButterKnife;

public abstract class BaseActivity extends AppCompatActivity implements ProVersionChecker.IProVersionListener {

    @BindView(R.id.ad_view)
    AdView mAdView;

    protected FirebaseAnalytics firebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(getLayoutResourceId());
        ButterKnife.bind(this);

        AdRequest adRequest = AdRequestKeeper.getAdRequest(this);
        mAdView.loadAd(adRequest);

        ProVersionChecker.checkIfPro(this, this);

        firebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

    protected abstract int getLayoutResourceId();

    @Override
    public void onProVersionResult(boolean isPro) {
        if(isPro) {
            mAdView.setVisibility(View.GONE);
        } else {
            mAdView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ProVersionChecker.getInstance(this).unregisterIProVersionListener(this);
    }
}
