package com.federicogovoni.permissionsmanager.view.main.applications;

import android.annotation.SuppressLint;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.federicogovoni.permissionsmanager.controller.ProVersionChecker;
import com.federicogovoni.permissionsmanager.model.Permission;
import com.federicogovoni.permissionmanager.R;
import com.federicogovoni.permissionsmanager.utils.AdRequestKeeper;
import com.federicogovoni.permissionsmanager.view.main.BaseActivity;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class PermissionsActivity extends BaseActivity {
    public static final String OTHER = "OTHER";

    @BindView(R.id.activity_permissions_progress_bar)
    ProgressBar progressBar;

    @BindView(R.id.activity_permissions_list_view)
    ListView activityPermissionsListView;


    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        String appTitle = (String) getIntent().getExtras().get("SELECTED_APP_NAME");
        getSupportActionBar().setTitle(appTitle);

        final AppCompatActivity thisActivity = this;

        new AsyncTask<Void, Void, PermissionAdapter>() {

            @Override
            protected PermissionAdapter doInBackground(Void... params) {
                int id = (int) getIntent().getExtras().get("SELECTED_APP_NAME_ID");
                Object[] aux = (Object[] ) getIntent().getExtras().get("SORTED_APPS_LIST");

                ApplicationInfo appInfo = (ApplicationInfo) aux[id];

                PackageManager pm = getPackageManager();

                String[] requestedPermissions = null;

                try {
                    PackageInfo packageInfo = pm.getPackageInfo(appInfo.packageName, PackageManager.GET_PERMISSIONS);
                    requestedPermissions = packageInfo.requestedPermissions;

                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

                List<Permission> toWrite = new ArrayList<>();
                if(requestedPermissions != null) {
                    for (String s : requestedPermissions)
                        toWrite.add(new Permission(s, appInfo.packageName, pm));
                    Collections.sort(toWrite);
                }
                //PermissionAdapter adapter = new PermissionAdapter( getApplicationContext(),id, toWrite, appInfo.packageName);
                PermissionAdapter adapter = new PermissionAdapter(toWrite, thisActivity, appInfo);
                return adapter;
            }

            @Override
            protected void onPostExecute(PermissionAdapter adapter) {
                if(adapter.getCount() > 0) {
                    activityPermissionsListView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    activityPermissionsListView.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                } else {
                    progressBar.setVisibility(View.GONE);
                    activityPermissionsListView.setVisibility(View.VISIBLE);
                }
            }
        }.execute();
    }

    @Override
    public void onProVersionResult(boolean isPro) {
        Timber.d("IProVersionListener invoked for %s", getClass().toString());
        super.onProVersionResult(isPro);
        if(isPro) {
            activityPermissionsListView.setPadding(0, 0, 0, 0);
        }
    }

    @Override
    protected int getLayoutResourceId() {
       return R.layout.activity_permissions;
    }
}
