package com.federicogovoni.permissionsmanager.view.main;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ContextThemeWrapper;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewbinding.ViewBinding;

import com.federicogovoni.permissionsmanager.Constants;
import com.federicogovoni.permissionsmanager.controller.ProVersionChecker;
import com.federicogovoni.permissionsmanager.model.Permission;
import com.federicogovoni.permissionsmanager.utils.AdRequestKeeper;
import com.federicogovoni.permissionsmanager.utils.GeneralUtils;
import com.federicogovoni.permissionsmanager.utils.LastFragmentUsed;
import com.federicogovoni.permissionmanager.R;
import com.federicogovoni.permissionsmanager.utils.RootUtil;
import com.federicogovoni.permissionsmanager.controller.PermissionsLoader;
import com.federicogovoni.permissionsmanager.view.main.applications.ApplicationsFragment;
import com.federicogovoni.permissionsmanager.view.main.contexts.ContextsFragment;
import com.federicogovoni.permissionsmanager.view.main.settings.SettingsFragment;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;


import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener, ProVersionChecker.IProVersionListener {

    private boolean mDebug = false;


    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;

    @BindView(R.id.activity_main_nav_view)
    NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setSupportActionBar(toolbar);

        new BackgroundLoaderTask(this).execute();

        //check if the background location service is running and then start it.
        GeneralUtils.checkAndStartLocationService(getApplicationContext());

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerStateChanged(int stateChanged) {
                super.onDrawerStateChanged(stateChanged);
                try {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                    //Find the currently focused view, so we can grab the correct window token from it.
                    View focusedView = getCurrentFocus();
                    //If no view currently has focus, create a new one, just so we can grab a window token from it
                    if (focusedView == null) {
                        focusedView = new View(MainActivity.this);
                    }
                    imm.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
                } catch (Exception e) {

                }
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        LastFragmentUsed lfu = LastFragmentUsed.getInstance();
        int fragment = lfu.getLastFragmentId();

        Fragment firstFragment = null;

        if(fragment == R.id.nav_apps) {
            navigationView.setCheckedItem(fragment);
            firstFragment = new ApplicationsFragment();
        } else if (fragment == R.id.nav_contexts) {
            navigationView.setCheckedItem(fragment);
            firstFragment = new ContextsFragment();
        } else if (fragment == R.id.nav_settings) {
            navigationView.setCheckedItem(fragment);
            firstFragment = new SettingsFragment();
        } else if (fragment == R.id.nav_support){
            navigationView.setCheckedItem(fragment);
            firstFragment = new GetProFragment();
        } else
            firstFragment = new ApplicationsFragment();

        lfu.setNewId(fragment);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_applications_main_relative_layout, firstFragment).addToBackStack(null).commit();

        boolean showDialog = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(Constants.ROOT_DIALOG, true);

        if(!RootUtil.isDeviceRooted() && showDialog) {
            ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(MainActivity.this, R.style.AppTheme);
            new MaterialAlertDialogBuilder(contextThemeWrapper)
                    .setTitle(R.string.no_root_title)
                    .setMessage(R.string.no_root_text)
                    .setPositiveButton(getResources().getString(R.string.ok), (dialog, which) -> dialog.dismiss())
                    .setNegativeButton(getResources().getString(R.string.dont_show_again), (dialog, which) -> {
                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean(Constants.ROOT_DIALOG, false).apply();
                    })
                    .show();
        }

        //Mostro il dialog di impossibilità modificare i permessi
        boolean firstOpen = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(Constants.FIRST_OPEN, true);
        if (firstOpen && RootUtil.isDeviceRooted()) {
            ApplicationInfo applicationInfo = PermissionsLoader.getInstance(this).searchApplicationInfoByPackageName(this.getPackageName());
            Permission permission = PermissionsLoader.getInstance(this).getAppPermissions(applicationInfo).get(0);
            boolean currentPermissionStatus = permission.check();
            boolean newPermissionStatus = permission.check() ? permission.revoke() : permission.grant();
            if (currentPermissionStatus == newPermissionStatus) {
                //mostro messaggio di errore
                ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(MainActivity.this, R.style.AppTheme);
                new MaterialAlertDialogBuilder(contextThemeWrapper)
                        .setTitle(R.string.not_working_title)
                        .setMessage(R.string.not_working_text)
                        .setPositiveButton(getResources().getString(R.string.ok), (dialog, which) -> dialog.dismiss())
                        .setNegativeButton(getResources().getString(R.string.dont_show_again), (dialog, which) -> {
                            PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean(Constants.FIRST_OPEN, false).apply();
                        })
                        .show();
            } else {
                //torno a mettere il permesso come prima
                if (currentPermissionStatus) {
                    permission.grant();
                } else {
                    permission.revoke();
                }
                //tutto a posto e metto lo sharedpreference a false
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean(Constants.FIRST_OPEN, false).apply();
            }
        }

        //controllo se ha la versione pro e nel caso tolgo la visualizzazione della pubblicità
        ProVersionChecker.checkIfPro(this, this);
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            finish();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        LastFragmentUsed lfu = LastFragmentUsed.getInstance();
        FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
        if (id == R.id.nav_apps && lfu.getLastFragmentId() != id) {
            lfu.setNewId(id);
            toolbar.inflateMenu(R.menu.search_menu);
            Fragment aFragment = new ApplicationsFragment();
            trans.replace(R.id.fragment_applications_main_relative_layout, aFragment);
            trans.addToBackStack("fragBack");
            trans.commit();
        } else if (id == R.id.nav_contexts && lfu.getLastFragmentId() != id) {
            lfu.setNewId(id);
            Fragment pFragment = new ContextsFragment();
            trans.replace(R.id.fragment_applications_main_relative_layout, pFragment);
            trans.addToBackStack("fragBack");
            trans.commit();
        } else if (id == R.id.nav_settings && lfu.getLastFragmentId() != id) {
            lfu.setNewId(id);
            Fragment sFragment = new SettingsFragment();
            trans.replace(R.id.fragment_applications_main_relative_layout, sFragment);
            trans.addToBackStack("fragBack");
            trans.commit();
        } else if (id == R.id.nav_support && lfu.getLastFragmentId() != id) {
            lfu.setNewId(id);
            Fragment sFragment = new GetProFragment();
            trans.replace(R.id.fragment_applications_main_relative_layout, sFragment);
            trans.addToBackStack("fragBack");
            trans.commit();
        } else if (id == R.id.nav_share){
            try {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_SUBJECT, "My application name");
                String sAux = "\nLet me recommend you this application\n\n";
                sAux = sAux + "http://play.google.com/store/apps/details?id=" + getPackageName()+ "\n\n";
                i.putExtra(Intent.EXTRA_TEXT, sAux);
                startActivity(Intent.createChooser(i, "choose one"));
            } catch(Exception e) {
                //e.toString();
            }
        } else if (id == R.id.nav_rate ) {
            Uri uri = Uri.parse("market://details?id=" + getPackageName());
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            // To count with Play market backstack, After pressing back button,
            // to taken back to our application, we need to add following flags to intent.
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            try {
                startActivity(goToMarket);
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
            }
        } else if (id == R.id.nav_changelog) {
            ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(MainActivity.this, R.style.AppTheme);
            new MaterialAlertDialogBuilder(contextThemeWrapper)
                    .setTitle(getResources().getString(R.string.changelog_content_title))
                    .setItems(R.array.changelog_content_value, null)
                    .setPositiveButton(getResources().getString(R.string.ok), (dialog, which) -> dialog.dismiss())
                    .show();
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_main;
    }

    private static class BackgroundLoaderTask extends AsyncTask<Void, Void, Void> {
        private WeakReference<Activity> activityReference;

        BackgroundLoaderTask(Activity context) {
            this.activityReference = new WeakReference<>(context);
        }

        @Override
        protected Void doInBackground(Void... params) {
            PermissionsLoader.getInstance(activityReference.get().getApplicationContext());
            return null;
        }
    }
}
