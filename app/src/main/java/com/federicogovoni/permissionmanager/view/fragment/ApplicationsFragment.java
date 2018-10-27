package com.federicogovoni.permissionmanager.view.fragment;

import android.support.v4.app.Fragment;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.federicogovoni.permissionmanager.R;
import com.federicogovoni.permissionmanager.controller.ApplicationsInfoKeeper;
import com.federicogovoni.permissionmanager.controller.ProVersionChecker;
import com.federicogovoni.permissionmanager.utils.AdRequestKeeper;
import com.federicogovoni.permissionmanager.view.PermissionsActivity;
import com.federicogovoni.permissionmanager.view.adapter.AppAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.List;

/**
 * Created by Federico on 22/03/2017.
 */

public class ApplicationsFragment extends Fragment {

    public static final String APPLICATIONS_LIST = "APPLICATIONS_LIST";
    public static final int ALL_APPLICATIONS = 0;
    public static final int USER_APPLICATIONS = 1;
    private AdView mAdView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_applications, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().setTitle(R.string.app_title);

        // Sample AdMob app ID: ca-app-pub-9125265928210219~3176045725
        mAdView = getActivity().findViewById(R.id.fragment_applications_ad_view);
        AdRequest adRequest = AdRequestKeeper.getAdRequest(getActivity());
        mAdView.loadAd(adRequest);

        try {
            if (ProVersionChecker.getInstance().checkPro()) {
                getActivity().findViewById(R.id.fragment_applications_ad_view).setVisibility(View.GONE);

                getActivity().findViewById(R.id.fragment_applications_applications_list_view).setPadding(0,0,0,0);
            }

        } catch (NullPointerException e){
        }

        final List<ApplicationInfo> apps = ApplicationsInfoKeeper.getInstance(getActivity().getApplicationContext()).getInstalledAppplications();

        final ListView listView = (ListView) getActivity().findViewById(R.id.fragment_applications_applications_list_view);
        AppAdapter adapter = new AppAdapter(getActivity(), R.layout.adapter_row_custom_application, apps);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapter, final View view, int pos, long id) {
                //cosa fare a seguito del click sull'item

                Intent intent = new Intent(getActivity(), PermissionsActivity.class);
                intent.putExtra("SELECTED_APP_NAME_ID", pos);
                intent.putExtra("SELECTED_APP_NAME", apps.get(pos).loadLabel(getActivity().getPackageManager()).toString());
                intent.putExtra("SORTED_APPS_LIST", apps.toArray());
                getActivity().startActivity(intent);
            }

        });
    }
}

