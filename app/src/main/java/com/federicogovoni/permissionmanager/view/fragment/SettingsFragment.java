package com.federicogovoni.permissionmanager.view.fragment;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Switch;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.federicogovoni.permissionmanager.controller.ProVersionChecker;
import com.federicogovoni.permissionmanager.model.LocationContext;
import com.federicogovoni.permissionmanager.R;
import com.federicogovoni.permissionmanager.controller.ApplicationsInfoKeeper;
import com.federicogovoni.permissionmanager.controller.ContextManager;
import com.federicogovoni.permissionmanager.controller.PermissionsLoader;
import com.federicogovoni.permissionmanager.utils.AdRequestKeeper;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.lang.ref.WeakReference;

/**
 * Created by Federico on 31/03/2017.
 */

public class SettingsFragment extends Fragment {
    public static final String RECEIVE_NOTIFICATION_SHARED_PREFERENCE = "RECEIVE_NOTIFICATION";
    private AdView mAdView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().setTitle(R.string.settings);

        mAdView = getActivity().findViewById(R.id.fragment_settings_ad_view);
        final Switch receiveNotificationsSwitch = getActivity().findViewById(R.id.fragment_settings_receive_notifications_switch);

        // Sample AdMob app ID: ca-app-pub-9125265928210219~3176045725
        AdRequest adRequest = AdRequestKeeper.getAdRequest(getActivity());
        mAdView.loadAd(adRequest);
        try {
            if (ProVersionChecker.getInstance().checkPro()) {
                mAdView.setVisibility(View.GONE);
                receiveNotificationsSwitch.setEnabled(true);
                final boolean receiveNotifications = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).getBoolean(RECEIVE_NOTIFICATION_SHARED_PREFERENCE, true);
                receiveNotificationsSwitch.setChecked(receiveNotifications);
                receiveNotificationsSwitch.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean value = receiveNotificationsSwitch.isChecked();
                        PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).edit().putBoolean(RECEIVE_NOTIFICATION_SHARED_PREFERENCE, value).apply();
                    }
                });
            }
        } catch (NullPointerException e) {
        }

        String measure = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).getString(LocationContext.MEASURE, LocationContext.KM);
        if (measure.equals(LocationContext.KM))
            ((Spinner) getActivity().findViewById(R.id.fragment_settings_distance_unit_spinner)).setSelection(0);
        else
            ((Spinner) getActivity().findViewById(R.id.fragment_settings_distance_unit_spinner)).setSelection(1);

        int apps = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).getInt(ApplicationsFragment.APPLICATIONS_LIST, ApplicationsFragment.USER_APPLICATIONS);
        ((Spinner) getActivity().findViewById(R.id.fragment_settings_applications_list_spinner)).setSelection(apps);

        ((Spinner) getActivity().findViewById(R.id.fragment_settings_distance_unit_spinner)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
                if (position == 0) {
                    if (sp.getString(LocationContext.MEASURE, LocationContext.KM).equals(LocationContext.MILES)) {
                        sp.edit().putString(LocationContext.MEASURE, LocationContext.KM).apply();
                        ContextManager.getInstance(getActivity().getApplicationContext()).convertMeasuresTo(LocationContext.KM);
                    }
                } else if (position == 1) {
                    if (sp.getString(LocationContext.MEASURE, LocationContext.KM).equals(LocationContext.KM)) {
                        sp.edit().putString(LocationContext.MEASURE, LocationContext.MILES).apply();
                        ContextManager.getInstance(getActivity().getApplicationContext()).convertMeasuresTo(LocationContext.MILES);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ((Spinner) getActivity().findViewById(R.id.fragment_settings_applications_list_spinner)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
                if (position == ApplicationsFragment.ALL_APPLICATIONS) {
                    if (sp.getInt(ApplicationsFragment.APPLICATIONS_LIST, ApplicationsFragment.USER_APPLICATIONS)== ApplicationsFragment.USER_APPLICATIONS) {
                        new MaterialDialog.Builder(getActivity())
                                .title(R.string.attention)
                                .content(R.string.dialog_all_apps_content)
                                .positiveText(R.string.agree)
                                .negativeText(R.string.disagree)
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        sp.edit().putInt(ApplicationsFragment.APPLICATIONS_LIST, ApplicationsFragment.ALL_APPLICATIONS).apply();
                                        ApplicationsInfoKeeper.getInstance(getActivity().getApplicationContext()).reloadInstalledApplications();
                                        new BackgroundLoaderTask(getActivity()).execute();
                                    }
                                }).onNegative(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        ((Spinner) getActivity().findViewById(R.id.fragment_settings_applications_list_spinner)).setSelection(ApplicationsFragment.USER_APPLICATIONS);
                                    }
                                })
                                .show();
                    }
                } else if (position == ApplicationsFragment.USER_APPLICATIONS) {
                    if (sp.getInt(ApplicationsFragment.APPLICATIONS_LIST, ApplicationsFragment.USER_APPLICATIONS)== ApplicationsFragment.ALL_APPLICATIONS) {
                        sp.edit().putInt(ApplicationsFragment.APPLICATIONS_LIST, ApplicationsFragment.USER_APPLICATIONS).apply();
                        ApplicationsInfoKeeper.getInstance(getActivity().getApplicationContext()).reloadInstalledApplications();
                        new BackgroundLoaderTask(getActivity()).execute();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
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

