package com.federicogovoni.permissionmanager.view;

import android.annotation.SuppressLint;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;

import com.federicogovoni.permissionmanager.controller.ProVersionChecker;
import com.federicogovoni.permissionmanager.model.Permission;
import com.federicogovoni.permissionmanager.R;
import com.federicogovoni.permissionmanager.utils.AdRequestKeeper;
import com.federicogovoni.permissionmanager.view.adapter.PermissionAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PermissionsActivity extends AppCompatActivity {
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.*/
    public static final String OTHER = "OTHER";
    private AdView mAdView;

    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions);

        // Sample AdMob app ID: ca-app-pub-9125265928210219~3176045725
        mAdView = findViewById(R.id.activity_permissions_ad_view);
        AdRequest adRequest = AdRequestKeeper.getAdRequest(this);
        mAdView.loadAd(adRequest);

        try {
            if (ProVersionChecker.getInstance().checkPro()) {
                findViewById(R.id.activity_permissions_ad_view).setVisibility(View.GONE);
                findViewById(R.id.activity_permissions_list_view).setPadding(0,0,0,0);
            }
        } catch (NullPointerException e) {
        }

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
                ListView listView = (ListView) findViewById(R.id.activity_permissions_list_view);

                if(adapter.getCount() > 0) {
                    listView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    listView.setVisibility(View.VISIBLE);
                    findViewById(R.id.activity_permissions_progress_bar).setVisibility(View.GONE);
                } else {
                    findViewById(R.id.activity_permissions_progress_bar).setVisibility(View.GONE);
                    findViewById(R.id.activity_permissions_empty_list_relative_layout).setVisibility(View.VISIBLE);
                }
            }
        }.execute();
    }

}
