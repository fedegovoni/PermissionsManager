package com.federicogovoni.permissionmanager.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.RelativeLayout;

import com.federicogovoni.permissionmanager.controller.ProVersionChecker;
import com.federicogovoni.permissionmanager.model.CurrentContext;
import com.federicogovoni.permissionmanager.model.Permission;
import com.federicogovoni.permissionmanager.R;
import com.federicogovoni.permissionmanager.model.TimeContext;
import com.federicogovoni.permissionmanager.utils.AdRequestKeeper;
import com.federicogovoni.permissionmanager.utils.TmpContextKeeper;
import com.federicogovoni.permissionmanager.controller.ContextManager;
import com.federicogovoni.permissionmanager.controller.PermissionsLoader;
import com.federicogovoni.permissionmanager.view.adapter.AppPermissionsExpandableListAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.List;
import java.util.Map;

public class ChoosePermissionsNewContextActivity extends AppCompatActivity {

    private CurrentContext toCreate;
    private AppPermissionsExpandableListAdapter adapter;
    private AdView mAdView;

    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_permissions_new_context);

        // Sample AdMob app ID: ca-app-pub-9125265928210219~3176045725
        mAdView = findViewById(R.id.adView_choose_permissions_new_context_activity);
        AdRequest adRequest = AdRequestKeeper.getAdRequest(this);
        mAdView.loadAd(adRequest);

        try {
            if (ProVersionChecker.getInstance().checkPro()) {
                findViewById(R.id.adView_choose_permissions_new_context_activity).setVisibility(View.GONE);

                View buttonView = findViewById(R.id.activity_choose_permissions_new_context_floating_action_button);
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) buttonView.getLayoutParams();
                layoutParams.setMargins(0, 0, layoutParams.getMarginEnd(), layoutParams.getMarginEnd());
                buttonView.setLayoutParams(layoutParams);

                View listView =  findViewById(R.id.activity_choose_permissions_new_context_expandible_list_view);
                float density = getResources().getDisplayMetrics().density;
                listView.setPadding(0,0,0,Math.round((float) 100 * density));
            }
        } catch (NullPointerException e) {
        }

        toCreate = TmpContextKeeper.getInstance().getCurrentContext();
        final String type = getIntent().getStringExtra("TYPE");

        if(type.equals(CurrentContext.GRANT))
            getSupportActionBar().setTitle(getResources().getString(R.string.permission_to_grant));
        else
            getSupportActionBar().setTitle(getResources().getString(R.string.permission_to_revoke));

        new AsyncTask<Void, Void, AppPermissionsExpandableListAdapter>() {

            @Override
            protected AppPermissionsExpandableListAdapter doInBackground(Void... params) {
                Map<ApplicationInfo, List<Permission>> appPermissionsMap = PermissionsLoader.getInstance(getApplicationContext()).getAppPermissionMap();
                adapter = new AppPermissionsExpandableListAdapter(getApplicationContext(),
                        PermissionsLoader.getInstance(getApplicationContext()).getApplicationsList(), appPermissionsMap);

                return adapter;
            }

            @Override
            protected void onPostExecute(AppPermissionsExpandableListAdapter adapter) {
                ExpandableListView listView = (ExpandableListView) findViewById(R.id.activity_choose_permissions_new_context_expandible_list_view);

                findViewById(R.id.progressBar).setVisibility(View.GONE);
                FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.activity_choose_permissions_new_context_floating_action_button);
                fab.setVisibility(View.VISIBLE);

                listView.setVisibility(View.VISIBLE);
                listView.setAdapter(adapter);
            }
        }.execute();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.activity_choose_permissions_new_context_floating_action_button);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                }
                ChoosePermissionsNewContextActivity.this.startActivity(intent);
                finish();
            }
        });
    }
}
