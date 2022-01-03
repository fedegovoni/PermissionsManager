package com.federicogovoni.permissionsmanager.view.main.contexts.newcontext;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;

import com.federicogovoni.permissionsmanager.controller.ProVersionChecker;
import com.federicogovoni.permissionsmanager.model.CurrentContext;
import com.federicogovoni.permissionsmanager.model.Permission;
import com.federicogovoni.permissionmanager.R;
import com.federicogovoni.permissionsmanager.model.TimeContext;
import com.federicogovoni.permissionsmanager.utils.AdRequestKeeper;
import com.federicogovoni.permissionsmanager.utils.TmpContextKeeper;
import com.federicogovoni.permissionsmanager.controller.ContextManager;
import com.federicogovoni.permissionsmanager.controller.PermissionsLoader;
import com.federicogovoni.permissionsmanager.view.main.BaseActivity;
import com.federicogovoni.permissionsmanager.view.main.MainActivity;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class ChoosePermissionsNewContextActivity extends BaseActivity implements SearchView.OnQueryTextListener, ProVersionChecker.IProVersionListener {

    private CurrentContext toCreate;
    private AppPermissionsExpandableListAdapter adapter;
    private SearchView searchView;

    @BindView(R.id.activity_choose_permissions_new_context_floating_action_button)
    FloatingActionButton newContextFab;

    @BindView(R.id.activity_choose_permissions_new_context_expandable_list_view)
    ExpandableListView listView;

    @BindView(R.id.acrtivity_choose_permissions_new_context_progress_bar)
    ProgressBar progressBar;

    @BindView(R.id.activity_choose_permissions_new_context_empty_list_relative_layout)
    RelativeLayout emptyListRelativeLayout;

    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        toCreate = TmpContextKeeper.getInstance().getCurrentContext();
        final String type = getIntent().getStringExtra("TYPE");

        if(type.equals(CurrentContext.GRANT))
            getSupportActionBar().setTitle(getResources().getString(R.string.permission_to_grant));
        else
            getSupportActionBar().setTitle(getResources().getString(R.string.permission_to_revoke));

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                Map<ApplicationInfo, List<Permission>> appPermissionsMap = PermissionsLoader.getInstance(getApplicationContext()).getAppPermissionMap();
                List<ApplicationInfo> applicationInfos = PermissionsLoader.getInstance(getApplicationContext()).getApplicationsList();
                fillExpandableListView(applicationInfos, appPermissionsMap);
                return null;
            }

        }.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Timber.d("Inflate search menu");
        getMenuInflater().inflate(R.menu.search_menu, menu);

        // Associate searchable configuration with the SearchView
        searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));

        searchView.setOnQueryTextListener(this);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        searchView.clearFocus();
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        Map<ApplicationInfo, List<Permission>> appPermissionsMap = PermissionsLoader.getInstance(this).getAppPermissionMap(newText);
        List<ApplicationInfo> applicationInfos =  PermissionsLoader.getInstance(getApplicationContext()).getApplicationsList(newText);
        fillExpandableListView(applicationInfos, appPermissionsMap);
        return true;
    }

    private void fillExpandableListView(final List<ApplicationInfo> applicationInfos, final Map<ApplicationInfo, List<Permission>> applicationInfoListMap) {
        adapter = new AppPermissionsExpandableListAdapter(getApplicationContext(), applicationInfos, applicationInfoListMap);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.GONE);
                if(applicationInfos.isEmpty()) {
                    emptyListRelativeLayout.setVisibility(View.VISIBLE);
                } else {
                    emptyListRelativeLayout.setVisibility(View.GONE);
                    listView.setVisibility(View.VISIBLE);
                }

                newContextFab.setVisibility(View.VISIBLE);

                listView.setAdapter(adapter);
            }
        });
    }

    @OnClick(R.id.activity_choose_permissions_new_context_floating_action_button)
    public void onNewContextFabClick(View v) {
        final String type = getIntent().getStringExtra("TYPE");
        Intent intent;
        if(type.equals(CurrentContext.REVOKE)) {
            intent = new Intent(ChoosePermissionsNewContextActivity.this, ChoosePermissionsNewContextActivity.class);
            intent.putExtra("TYPE", CurrentContext.GRANT);

            toCreate.setRevokePermissionsList(adapter.getRevokeSelectedPermissions());
            TmpContextKeeper.getInstance().setCurrentContext(toCreate);
            TmpContextKeeper.getInstance().setPhase(TmpContextKeeper.GRANT_PHASE);
        }
        else {
            toCreate.setGrantPermissionsList(adapter.getGrantSelectedPermission());
            ContextManager.getInstance(ChoosePermissionsNewContextActivity.this).addContext(toCreate);
            if(toCreate.getTimeContext() != null && toCreate.getTimeContext().getFrequency() == TimeContext.WEEKLY &&
                    (toCreate.getTimeContext().getDaysOfWeek() == null || toCreate.getTimeContext().getDaysOfWeek().isEmpty()))
                toCreate.setEnabled(false, getApplicationContext());
            else
                toCreate.setEnabled(true, getApplicationContext());
            intent = new Intent(ChoosePermissionsNewContextActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

            Bundle bundle = new Bundle();
            bundle.putString("CONTEXT_CREATION", "New context created");
            firebaseAnalytics.logEvent("CONTEXT_CREATION", bundle);
        }
        ChoosePermissionsNewContextActivity.this.startActivity(intent);
        finish();
    }

    @Override
    public void onProVersionResult(boolean isPro) {
        Timber.d("IProVersionListener invoked for %s", getClass().toString());
        super.onProVersionResult(isPro);
        if(isPro) {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) newContextFab.getLayoutParams();
            layoutParams.setMargins(0, 0, layoutParams.getMarginEnd(), layoutParams.getMarginEnd());
            newContextFab.setLayoutParams(layoutParams);

            float density = getResources().getDisplayMetrics().density;
            listView.setPadding(0, 0, 0, Math.round((float) 100 * density));
        }
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_choose_permissions_new_context;
    }
}
