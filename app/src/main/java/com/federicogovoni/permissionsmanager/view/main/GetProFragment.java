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

package com.federicogovoni.permissionsmanager.view.main;

import android.app.ActionBar;
import android.os.Bundle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.federicogovoni.permissionmanager.BuildConfig;
import com.federicogovoni.permissionmanager.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.federicogovoni.permissionsmanager.controller.ProVersionChecker;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class GetProFragment extends Fragment {

    @BindView(R.id.fragment_get_pro_description_text_view)
    TextView descriptionTextView;

    @BindView(R.id.fragment_get_pro_consume_fab)
    FloatingActionButton consumeFab;

    @BindView(R.id.fragment_get_pro_progress_bar)
    LinearProgressIndicator progressBar;

    @BindView(R.id.fragment_get_pro_purchase_fab)
    FloatingActionButton purchaseFab;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_get_pro, container, false);
        ButterKnife.bind(this, view);

        if(BuildConfig.DEBUG) {
            consumeFab.setVisibility(View.VISIBLE);
        }

        ProVersionChecker.checkIfPro(getActivity(), isPro -> {
            Timber.d("IProVersionListener invoked for %s", getClass().toString());
            progressBar.setVisibility(View.GONE);
            if(isPro) {
                descriptionTextView.setText(getResources().getString(R.string.get_pro_already_pro));
                if(!BuildConfig.DEBUG) {
                    purchaseFab.setVisibility(View.GONE);
                    ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);
                    params.setMargins(0,0,8, 8);
                    purchaseFab.setLayoutParams(params);
                    purchaseFab.setEnabled(false);
                } else {
                    purchaseFab.setEnabled(true);
                }
            } else {
                String adaptedProDescription = getResources().getString(R.string.get_pro_description);
                String price = ProVersionChecker.getProVersionPrice();
                if(price != null) {
                    adaptedProDescription = adaptedProDescription.replace("%PRICE%", price);
                    descriptionTextView.setText(adaptedProDescription);
                }
                purchaseFab.setEnabled(true);
                purchaseFab.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            }
        });

        return view;
    }

    @OnClick(R.id.fragment_get_pro_purchase_fab)
    public void purchaseProVersion() {
        ProVersionChecker.getInstance(getContext()).startPurchaseFlow(getActivity());
    }

    @OnClick(R.id.fragment_get_pro_consume_fab)
    public void consumePurchases(View view) {
        Timber.d("Reset Pruchases pressed");
        ProVersionChecker.getInstance(getContext()).resetPurchases();
    }
}