package com.federicogovoni.permissionsmanager.view.main.settings;

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

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.federicogovoni.permissionsmanager.controller.ProVersionChecker;
import com.federicogovoni.permissionsmanager.model.LocationContext;
import com.federicogovoni.permissionmanager.R;
import com.federicogovoni.permissionsmanager.controller.ApplicationsInfoManager;
import com.federicogovoni.permissionsmanager.controller.ContextManager;
import com.federicogovoni.permissionsmanager.controller.PermissionsLoader;
import com.federicogovoni.permissionsmanager.view.main.applications.ApplicationsFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemSelected;
import timber.log.Timber;

/**
 * Created by Federico on 31/03/2017.
 */

public class SettingsFragment extends Fragment implements ProVersionChecker.IProVersionListener {
    public static final String RECEIVE_NOTIFICATION_SHARED_PREFERENCE = "RECEIVE_NOTIFICATION";
    private SharedPreferences mSharedPreferences;

    @BindView(R.id.fragment_settings_distance_unit_spinner)
    Spinner distanceUnitSpinner;

    @BindView(R.id.fragment_settings_applications_list_spinner)
    Spinner applicationsListTypeSpiner;

    @BindView(R.id.fragment_settings_receive_notifications_switch)
    SwitchMaterial receiveNotificationsSwitch;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        ButterKnife.bind(this, view);

        getActivity().setTitle(R.string.settings);

        ProVersionChecker.checkIfPro(getActivity(), this::onProVersionResult);

        String measure = mSharedPreferences.getString(LocationContext.MEASURE, LocationContext.KM);
        if (measure.equals(LocationContext.KM))
            distanceUnitSpinner.setSelection(0);
        else
            distanceUnitSpinner.setSelection(1);

        int apps = mSharedPreferences.getInt(ApplicationsFragment.APPLICATIONS_LIST, ApplicationsFragment.USER_APPLICATIONS);
        applicationsListTypeSpiner.setSelection(apps);

        return view;
    }

    @Override
    public void onProVersionResult(boolean isPro) {
        Timber.d("IProVersionListener invoked for %s", getClass().toString());
        if(isPro) {
            receiveNotificationsSwitch.setEnabled(true);
            final boolean receiveNotifications = PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean(RECEIVE_NOTIFICATION_SHARED_PREFERENCE, true);
            receiveNotificationsSwitch.setChecked(receiveNotifications);
            receiveNotificationsSwitch.setOnClickListener(v -> {
                boolean value = receiveNotificationsSwitch.isChecked();
                PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putBoolean(RECEIVE_NOTIFICATION_SHARED_PREFERENCE, value).apply();
            });
        }
    }

    @OnItemSelected(R.id.fragment_settings_applications_list_spinner)
    public void onListTypeSelected(AdapterView<?> parent, View view, int position, long id) {
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

    @OnItemSelected(R.id.fragment_settings_distance_unit_spinner)
    public void onDistanceUnitSelected(AdapterView<?> parent, View view, int position, long id) {
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

