package com.federicogovoni.permissionmanager.view.fragment;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Switch;

import androidx.fragment.app.Fragment;

import com.federicogovoni.permissionmanager.controller.ProVersionChecker;
import com.federicogovoni.permissionmanager.model.LocationContext;
import com.federicogovoni.permissionmanager.R;
import com.federicogovoni.permissionmanager.controller.ApplicationsInfoManager;
import com.federicogovoni.permissionmanager.controller.ContextManager;
import com.federicogovoni.permissionmanager.controller.PermissionsLoader;
import com.federicogovoni.permissionmanager.utils.AdRequestKeeper;
import com.google.android.gms.ads.AdRequest;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.lang.ref.WeakReference;
import java.util.prefs.PreferenceChangeEvent;

/**
 * Created by Federico on 31/03/2017.
 */

public class SettingsFragment extends Fragment implements ProVersionChecker.IProVersionListener {
    public static final String RECEIVE_NOTIFICATION_SHARED_PREFERENCE = "RECEIVE_NOTIFICATION";
    private SharedPreferences mSharedPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().setTitle(R.string.settings);


        ProVersionChecker.checkIfPro(getContext(), this);

        String measure = mSharedPreferences.getString(LocationContext.MEASURE, LocationContext.KM);
        if (measure.equals(LocationContext.KM))
            ((Spinner) getActivity().findViewById(R.id.fragment_settings_distance_unit_spinner)).setSelection(0);
        else
            ((Spinner) getActivity().findViewById(R.id.fragment_settings_distance_unit_spinner)).setSelection(1);

        int apps = mSharedPreferences.getInt(ApplicationsFragment.APPLICATIONS_LIST, ApplicationsFragment.USER_APPLICATIONS);
        ((Spinner) getActivity().findViewById(R.id.fragment_settings_applications_list_spinner)).setSelection(apps);

        ((Spinner) getActivity().findViewById(R.id.fragment_settings_distance_unit_spinner)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    if (mSharedPreferences.getString(LocationContext.MEASURE, LocationContext.KM).equals(LocationContext.MILES)) {
                        mSharedPreferences.edit().putString(LocationContext.MEASURE, LocationContext.KM).apply();
                        ContextManager.getInstance(getActivity().getApplicationContext()).convertMeasuresTo(LocationContext.KM);
                    }
                } else if (position == 1) {
                    if (mSharedPreferences.getString(LocationContext.MEASURE, LocationContext.KM).equals(LocationContext.KM)) {
                        mSharedPreferences.edit().putString(LocationContext.MEASURE, LocationContext.MILES).apply();
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
                if (position == ApplicationsFragment.ALL_APPLICATIONS) {
                    if (mSharedPreferences.getInt(ApplicationsFragment.APPLICATIONS_LIST, ApplicationsFragment.USER_APPLICATIONS)== ApplicationsFragment.USER_APPLICATIONS) {
                        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(getContext(), R.style.AppTheme);
                        new MaterialAlertDialogBuilder(contextThemeWrapper)
                                .setTitle(R.string.attention)
                                .setMessage(R.string.dialog_all_apps_content)
                                .setPositiveButton(R.string.agree, (dialog, which) -> {
                                    mSharedPreferences.edit().putInt(ApplicationsFragment.APPLICATIONS_LIST, ApplicationsFragment.ALL_APPLICATIONS).apply();
                                    ApplicationsInfoManager.getInstance(getActivity().getApplicationContext()).reloadInstalledApplications();
                                    new BackgroundLoaderTask(getActivity()).execute();
                                })
                                .setNegativeButton(R.string.disagree, (dialog, which) -> {
                                    ((Spinner) getActivity().findViewById(R.id.fragment_settings_applications_list_spinner)).setSelection(ApplicationsFragment.USER_APPLICATIONS);
                                })
                                .show();
                    }
                } else if (position == ApplicationsFragment.USER_APPLICATIONS) {
                    if (mSharedPreferences.getInt(ApplicationsFragment.APPLICATIONS_LIST, ApplicationsFragment.USER_APPLICATIONS)== ApplicationsFragment.ALL_APPLICATIONS) {
                        mSharedPreferences.edit().putInt(ApplicationsFragment.APPLICATIONS_LIST, ApplicationsFragment.USER_APPLICATIONS).apply();
                        ApplicationsInfoManager.getInstance(getActivity().getApplicationContext()).reloadInstalledApplications();
                        new BackgroundLoaderTask(getActivity()).execute();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void onProVersionResult(boolean isPro) {
        final Switch receiveNotificationsSwitch = getActivity().findViewById(R.id.fragment_settings_receive_notifications_switch);
        if(isPro) {
            receiveNotificationsSwitch.setEnabled(true);
            final boolean receiveNotifications = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).getBoolean(RECEIVE_NOTIFICATION_SHARED_PREFERENCE, true);
            receiveNotificationsSwitch.setChecked(receiveNotifications);
            receiveNotificationsSwitch.setOnClickListener(v -> {
                boolean value = receiveNotificationsSwitch.isChecked();
                PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).edit().putBoolean(RECEIVE_NOTIFICATION_SHARED_PREFERENCE, value).apply();
            });
        }
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

