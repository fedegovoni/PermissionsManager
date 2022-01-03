package com.federicogovoni.permissionsmanager.view.main;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.viewbinding.BuildConfig;

import com.federicogovoni.permissionmanager.R;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.federicogovoni.permissionsmanager.controller.ProVersionChecker;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

@SuppressWarnings("deprecation")
@SuppressLint("NonConstantResourceId")
public class GetProFragment extends Fragment {

    @BindView(R.id.fragment_get_pro_description_text_view)
    TextView descriptionTextView;

    @BindView(R.id.fragment_get_pro_consume_fab)
    AppCompatButton consumeFab;

    @BindView(R.id.fragment_get_pro_progress_bar)
    LinearProgressIndicator progressBar;

    @BindView(R.id.fragment_get_pro_purchase_fab)
    AppCompatButton purchaseFab;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_get_pro, container, false);
        ButterKnife.bind(this, view);

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

        return view;
    }

    @OnClick(R.id.fragment_get_pro_purchase_fab)
    public void purchaseProVersion() throws NullPointerException {
        assert getContext() != null;
        assert ProVersionChecker.getInstance(getContext()) != null;
        try {
            Objects.requireNonNull(ProVersionChecker.getInstance(getContext())).startPurchaseFlow(getActivity());
        } catch (NullPointerException e) {
            Timber.e("NullPointerException on purchaseProVersion() method");
            e.printStackTrace();
        }
    }

    @OnClick(R.id.fragment_get_pro_consume_fab)
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