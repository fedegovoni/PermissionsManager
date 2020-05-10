package com.federicogovoni.permissionmanager.view.fragment;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;

import com.federicogovoni.permissionmanager.R;
import com.federicogovoni.permissionmanager.controller.ApplicationsInfoManager;
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

public class ApplicationsFragment extends Fragment implements SearchView.OnQueryTextListener {

    public static final String APPLICATIONS_LIST = "APPLICATIONS_LIST";
    public static final int ALL_APPLICATIONS = 0;
    public static final int USER_APPLICATIONS = 1;

    private List<ApplicationInfo> installedApplications;

    private SearchView searchView;
    private AdView mAdView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
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

        installedApplications = ApplicationsInfoManager.getInstance(getActivity().getApplicationContext()).getInstalledApplications();

        try {
            if (ProVersionChecker.getInstance().checkPro()) {
                getActivity().findViewById(R.id.fragment_applications_ad_view).setVisibility(View.GONE);

                getActivity().findViewById(R.id.fragment_applications_applications_list_view).setPadding(0,0,0,0);
            }

        } catch (NullPointerException e){
        }

        fillListView(installedApplications);
    }

    private void fillListView(final List<ApplicationInfo> apps) {
        if(apps.isEmpty()) {
            getActivity().findViewById(R.id.fragment_applications_applications_list_view).setVisibility(View.GONE);
            getActivity().findViewById(R.id.fragment_applications_empty_list_relative_layout).setVisibility(View.VISIBLE);
            return;
        } else {
            getActivity().findViewById(R.id.fragment_applications_applications_list_view).setVisibility(View.VISIBLE);
            getActivity().findViewById(R.id.fragment_applications_empty_list_relative_layout).setVisibility(View.GONE);
        }
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.search_menu, menu);

        // Associate searchable configuration with the SearchView
        // SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));

        // searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(this);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        searchView.clearFocus();
       return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        List<ApplicationInfo> resultList = ApplicationsInfoManager.getInstance(getContext()).getInstalledApplications(newText);
        fillListView(resultList);
        return true;
    }
}

