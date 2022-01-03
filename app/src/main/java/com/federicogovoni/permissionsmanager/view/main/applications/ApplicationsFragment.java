package com.federicogovoni.permissionsmanager.view.main.applications;

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
import android.widget.RelativeLayout;

import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;

import com.federicogovoni.permissionmanager.R;
import com.federicogovoni.permissionsmanager.controller.ApplicationsInfoManager;
import com.federicogovoni.permissionsmanager.controller.ProVersionChecker;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemClick;
import timber.log.Timber;

/**
 * Created by Federico on 22/03/2017.
 */

public class ApplicationsFragment extends Fragment implements SearchView.OnQueryTextListener {

    public static final String APPLICATIONS_LIST = "APPLICATIONS_LIST";
    public static final int ALL_APPLICATIONS = 0;
    public static final int USER_APPLICATIONS = 1;

    private SearchView searchView;
    private List<ApplicationInfo> filteredInstalledApplications;

    @BindView(R.id.fragment_applications_applications_list_view)
    ListView applicationsListView;

    @BindView(R.id.fragment_applications_empty_list_relative_layout)
    RelativeLayout emptyAppListRelativeLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_applications, container, false);
        ButterKnife.bind(this, view);

        getActivity().setTitle(R.string.app_title);

        filteredInstalledApplications = ApplicationsInfoManager.getInstance(getActivity().getApplicationContext()).getInstalledApplications();

        ProVersionChecker.checkIfPro(getContext(), isPro -> {
            Timber.d("IProVersionListener invoked for %s", getClass().toString());
            applicationsListView.setPadding(0,0,0,0);
        });

        fillListView();

        return view;
    }

    private void fillListView() {
        if(filteredInstalledApplications.isEmpty()) {
            applicationsListView.setVisibility(View.GONE);
            emptyAppListRelativeLayout.setVisibility(View.VISIBLE);
            return;
        } else {
            applicationsListView.setVisibility(View.VISIBLE);
            emptyAppListRelativeLayout.setVisibility(View.GONE);
        }
        AppAdapter adapter = new AppAdapter(getActivity(), R.layout.adapter_row_custom_application, filteredInstalledApplications);
        applicationsListView.setAdapter(adapter);
    }

    @OnItemClick(R.id.fragment_applications_applications_list_view)
    public void onAppClick(AdapterView<?> adapter, final View view, int pos, long id) {
        Intent intent = new Intent(getActivity(), PermissionsActivity.class);
        intent.putExtra("SELECTED_APP_NAME_ID", pos);
        intent.putExtra("SELECTED_APP_NAME", filteredInstalledApplications.get(pos).loadLabel(getActivity().getPackageManager()).toString());
        intent.putExtra("SORTED_APPS_LIST", filteredInstalledApplications.toArray());
        getActivity().startActivity(intent);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.search_menu, menu);

        searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));

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
        filteredInstalledApplications = ApplicationsInfoManager.getInstance(getContext()).getInstalledApplications(newText);
        fillListView();
        return true;
    }
}

