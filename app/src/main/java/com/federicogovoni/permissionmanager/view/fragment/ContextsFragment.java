package com.federicogovoni.permissionmanager.view.fragment;

import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.federicogovoni.permissionmanager.controller.ProVersionChecker;
import com.federicogovoni.permissionmanager.model.CurrentContext;
import com.federicogovoni.permissionmanager.R;
import com.federicogovoni.permissionmanager.utils.AdRequestKeeper;
import com.federicogovoni.permissionmanager.utils.TmpContextKeeper;
import com.federicogovoni.permissionmanager.controller.ContextManager;
import com.federicogovoni.permissionmanager.view.NewContextActivity;
import com.federicogovoni.permissionmanager.view.adapter.ContextCardAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class ContextsFragment extends Fragment {

    private AdView mAdView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_contexts, container, false);
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().setTitle(R.string.contexts);

        // Sample AdMob app ID: ca-app-pub-9125265928210219~3176045725
        mAdView = getActivity().findViewById(R.id.fragment_contexts_ad_view);
        AdRequest adRequest = AdRequestKeeper.getAdRequest(getActivity());
        mAdView.loadAd(adRequest);

        try {
            if (ProVersionChecker.getInstance().checkPro()) {
                getActivity().findViewById(R.id.fragment_contexts_ad_view).setVisibility(View.GONE);

                View buttonView = getActivity().findViewById(R.id.fragment_contexts_add_context_button);
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) buttonView.getLayoutParams();
                layoutParams.setMargins(0, 0, layoutParams.getMarginEnd(), layoutParams.getMarginEnd());
                buttonView.setLayoutParams(layoutParams);

                View listView =  getActivity().findViewById(R.id.fragment_contexts_main_list_view);
                float density = getResources().getDisplayMetrics().density;
                listView.setPadding(0,0,0,Math.round((float) 80 * density));
            }
        } catch (NullPointerException e) {
        }

        getActivity().findViewById(R.id.fragment_contexts_add_context_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), NewContextActivity.class);
                TmpContextKeeper.getInstance().setCurrentContext(new CurrentContext("", getActivity().getApplicationContext()));
                getActivity().startActivity(intent);
            }
        });

        if(ContextManager.getInstance(getActivity()).getContexts() != null && ContextManager.getInstance(getActivity()).getContexts().size() > 0) {
            getActivity().findViewById(R.id.fragment_contexts_empty_relative_layout).setVisibility(View.GONE);
            ListView listView = (ListView) getActivity().findViewById(R.id.fragment_contexts_main_list_view);
            listView.setVisibility(View.VISIBLE);
            ContextCardAdapter adapter = new ContextCardAdapter(getActivity());
            listView.setAdapter(adapter);
        } else {
            getActivity().findViewById(R.id.fragment_contexts_main_list_view).setVisibility(View.GONE);
            getActivity().findViewById(R.id.fragment_contexts_empty_relative_layout).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ((ListView) getActivity().findViewById(R.id.fragment_contexts_main_list_view)).setAdapter(new ContextCardAdapter(getActivity()));
    }
}
