package com.federicogovoni.permissionsmanager.view.main;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.federicogovoni.permissionmanager.BuildConfig;
import com.federicogovoni.permissionmanager.R;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.federicogovoni.permissionsmanager.controller.ProVersionChecker;

import java.util.Objects;

import timber.log.Timber;

@SuppressWarnings("deprecation")
@SuppressLint("NonConstantResourceId")
public class GetProFragment extends Fragment {

    TextView descriptionTextView;
    AppCompatButton consumeFab;
    LinearProgressIndicator progressBar;
    AppCompatButton purchaseFab;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_get_pro, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        purchaseFab = getActivity().findViewById(R.id.fragment_get_pro_purchase_fab);
        descriptionTextView = getActivity().findViewById(R.id.fragment_get_pro_description_text_view);
        progressBar = getActivity().findViewById(R.id.fragment_get_pro_progress_bar);
        consumeFab = getActivity().findViewById(R.id.fragment_get_pro_consume_fab);

        purchaseFab.setOnClickListener(this::purchaseProVersion);
        consumeFab.setOnClickListener(this::consumePurchases);


        if(BuildConfig.DEBUG) {
            consumeFab.setVisibility(View.VISIBLE);
        }

        assert getActivity() != null;
        ProVersionChecker.checkIfPro(getActivity(), isPro -> {
            Timber.d("IProVersionListener invoked for %s", getClass().toString());
            progressBar.setVisibility(View.GONE);
            if(isPro) {
                descriptionTextView.setText(getResources().getString(R.string.get_pro_already_pro));
                //if(!BuildConfig.DEBUG) {
                purchaseFab.setVisibility(View.GONE);
                purchaseFab.setEnabled(false);
                //} else {
                //   purchaseFab.setEnabled(true);
                //}
            } else {
                String adaptedProDescription = getResources().getString(R.string.get_pro_description);
                String price = ProVersionChecker.getProVersionPrice();
                if(price != null) {
                    adaptedProDescription = adaptedProDescription.replace("%PRICE%", price);
                    descriptionTextView.setText(adaptedProDescription);
                }
                purchaseFab.setEnabled(true);
            }
        });
    }

    public void purchaseProVersion(View view) throws NullPointerException {
        assert getContext() != null;
        assert ProVersionChecker.getInstance(getContext()) != null;
        try {
            Objects.requireNonNull(ProVersionChecker.getInstance(getContext())).startPurchaseFlow(getActivity());
        } catch (NullPointerException e) {
            Timber.e("NullPointerException on purchaseProVersion() method");
            e.printStackTrace();
        }
    }

    public void consumePurchases (@SuppressWarnings("unused") View view) throws NullPointerException {
        Timber.d("Revert purchases pressed");
        assert getContext() != null;
        try {
            Objects.requireNonNull(ProVersionChecker.getInstance(getContext())).resetPurchases();
        } catch (NullPointerException e) {
            Timber.e("NullPointerException on consumePurchases() method");
            e.printStackTrace();
        }
    }
}