package com.federicogovoni.permissionmanager.view;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.federicogovoni.permissionmanager.controller.ProVersionChecker;
import com.federicogovoni.permissionmanager.model.CurrentContext;
import com.federicogovoni.permissionmanager.model.LocationContext;
import com.federicogovoni.permissionmanager.R;
import com.federicogovoni.permissionmanager.model.TimeContext;
import com.federicogovoni.permissionmanager.utils.TmpContextKeeper;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import static com.federicogovoni.permissionmanager.R.id.activity_new_context_friday_checked_text_view;
import static com.federicogovoni.permissionmanager.R.id.activity_new_context_monday_checked_text_view;
import static com.federicogovoni.permissionmanager.R.id.activity_new_context_saturday_checked_text_view;
import static com.federicogovoni.permissionmanager.R.id.activity_new_context_sunday_checked_text_view;
import static com.federicogovoni.permissionmanager.R.id.activity_new_context_thursday_checked_text_view;
import static com.federicogovoni.permissionmanager.R.id.activity_new_context_tuesday_checked_text_view;
import static com.federicogovoni.permissionmanager.R.id.activity_new_context_wednesday_checked_text_view;
import static com.federicogovoni.permissionmanager.R.id.scroll;

public class NewContextActivity extends AppCompatActivity implements View.OnClickListener {

    private LatLng latlng = null;
    private String city = null;
    private boolean waitingForPlacePicker = false;
    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_context);


        final EditText contextNameEditText = (EditText) findViewById(R.id.activity_new_context_name_edit_text);
        final EditText timeStartEditText = (EditText) findViewById(R.id.activity_new_context_activation_time_edit_text);
        final EditText timeEndEditText = (EditText) findViewById(R.id.activity_new_context_deactivation_time_edit_text);
        final EditText dateStartEditText = (EditText) findViewById(R.id.activity_new_context_activation_date_edit_text);
        final EditText dateEndEditText = (EditText) findViewById(R.id.activity_new_context_deactivation_date_edit_text);
        final Spinner frequencySpinner = (Spinner) findViewById(R.id.activity_new_context_frequency_spinner);
        final EditText radiusEdiText = (EditText) findViewById(R.id.activity_new_context_radius_edit_text);
        final TextInputLayout radiusTextInputLayout = (TextInputLayout) findViewById(R.id.activity_new_context_radius_text_input_layout);
        final EditText addressEditText = (EditText) findViewById(R.id.activity_new_context_address_edit_text);
        final Button nextButton = (Button) findViewById(R.id.activity_new_context_next_button);


        setTitle(getResources().getString(R.string.new_context));

        // Sample AdMob app ID: ca-app-pub-9125265928210219~3176045725
        MobileAds.initialize(this, "ca-app-pub-9125265928210219~3176045725");
        mAdView = findViewById(R.id.activity_new_context_ad_view);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        CurrentContext toModify = TmpContextKeeper.getInstance().getCurrentContext();
        if(toModify.getLocationContext() != null) {
            latlng = new LatLng(toModify.getLocationContext().getLatitude(), toModify.getLocationContext().getLongitude());
            city = toModify.getLocationContext().getCity();
        }

        if (toModify != null) {
            contextNameEditText.setText(toModify.getName());
            getSupportActionBar().setTitle(toModify.getName());
        }

        try {
            if (ProVersionChecker.getInstance().checkPro()) {
                findViewById(R.id.activity_new_context_ad_view).setVisibility(View.GONE);
                View scrollView = findViewById(R.id.activity_new_context_scroll_view);
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) scrollView.getLayoutParams();
                layoutParams.setMargins(0, 0, layoutParams.getMarginEnd(), layoutParams.getMarginEnd());
                scrollView.setLayoutParams(layoutParams);
            }

        } catch (NullPointerException e){
        }

        final List<CheckedTextView> daysOfWeekCTV = new ArrayList<>();

        daysOfWeekCTV.add((CheckedTextView) findViewById(activity_new_context_monday_checked_text_view));
        daysOfWeekCTV.add((CheckedTextView) findViewById(activity_new_context_tuesday_checked_text_view));
        daysOfWeekCTV.add((CheckedTextView) findViewById(activity_new_context_wednesday_checked_text_view));
        daysOfWeekCTV.add((CheckedTextView) findViewById(activity_new_context_thursday_checked_text_view));
        daysOfWeekCTV.add((CheckedTextView) findViewById(activity_new_context_friday_checked_text_view));
        daysOfWeekCTV.add((CheckedTextView) findViewById(activity_new_context_saturday_checked_text_view));
        daysOfWeekCTV.add((CheckedTextView) findViewById(activity_new_context_sunday_checked_text_view));

        if (toModify != null && toModify.getTimeContext() != null && toModify.getTimeContext().getDaysOfWeek() != null)
            for (int day : toModify.getTimeContext().getDaysOfWeek()) {
                day = (day - 2) % 7;
                if (day < 0)
                    day += 7;
                daysOfWeekCTV.get(day).setChecked(true);
                daysOfWeekCTV.get(day).setBackground(getResources().getDrawable(R.drawable.circle));
                daysOfWeekCTV.get(day).setTextColor(Color.WHITE);
            }

        for (CheckedTextView c : daysOfWeekCTV)
            c.setOnClickListener(this);

        if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).
                getString(LocationContext.MEASURE, LocationContext.KM).equals(LocationContext.KM))
            radiusTextInputLayout.setHint(getResources().getString(R.string.radius) + "(" + getResources().getString(R.string.km) + ")");
        else
            radiusTextInputLayout.setHint(getResources().getString(R.string.radius) + "(" + getResources().getString(R.string.miles) + ")");

        if (toModify != null && toModify.getTimeContext() != null) {
            TimeContext p = toModify.getTimeContext();
            Calendar c = Calendar.getInstance();
            c.setTime(p.getApplyDate());
            DateFormat dfTime = new SimpleDateFormat("HH:mm");
            DateFormat dfDate = new SimpleDateFormat("dd/MM/yyyy");
            timeStartEditText.setText(dfTime.format(c.getTime()));
            dateStartEditText.setText(dfDate.format(c.getTime()));
            c.setTime(p.getDeactivationDate());
            timeEndEditText.setText(dfTime.format(c.getTime()));
            dateEndEditText.setText(dfDate.format(c.getTime()));
            frequencySpinner.setSelection(p.getFrequency());
        }

        if (toModify != null && toModify.getLocationContext() != null) {
            LocationContext p = toModify.getLocationContext();
            radiusEdiText.setText(p.getRadius() + "");
            addressEditText.setText(p.getCity());
        }

        timeStartEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar mCurrentTime = Calendar.getInstance();
                int hour = mCurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mCurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(NewContextActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                        Calendar c = Calendar.getInstance();
                        c.set(Calendar.HOUR_OF_DAY, selectedHour);
                        c.set(Calendar.MINUTE, selectedMinute);
                        String time = sdf.format(c.getTime());
                        timeStartEditText.setText(time);
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle(getResources().getString(R.string.activation_time));
                mTimePicker.show();
            }
        });

        timeEndEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar mCurrentTime = Calendar.getInstance();
                int hour = mCurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mCurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(NewContextActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                        Calendar c = Calendar.getInstance();
                        c.set(Calendar.HOUR_OF_DAY, selectedHour);
                        c.set(Calendar.MINUTE, selectedMinute);
                        String time = sdf.format(c.getTime());
                        timeEndEditText.setText(time);
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle(getResources().getString(R.string.deactivation_time));
                mTimePicker.show();
            }
        });

        dateStartEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar mCurrentDate = Calendar.getInstance();
                int year, monthOfYear, dayOfMonth;

                year = mCurrentDate.get(Calendar.YEAR);
                monthOfYear = mCurrentDate.get(Calendar.MONTH);
                dayOfMonth = mCurrentDate.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePicker = new DatePickerDialog(NewContextActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear,
                                          int dayOfMonth) {
                        Calendar c = new GregorianCalendar();
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                        sdf.setTimeZone(c.getTimeZone());
                        c.set(year, monthOfYear, dayOfMonth);
                        dateStartEditText.setText(sdf.format(c.getTime()));
                    }
                }, year, monthOfYear, dayOfMonth);
                datePicker.setTitle(getResources().getString(R.string.activation_date));
                datePicker.show();
            }
        });


        dateEndEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar mCurrentDate = Calendar.getInstance();
                int year, monthOfYear, dayOfMonth;

                year = mCurrentDate.get(Calendar.YEAR);
                monthOfYear = mCurrentDate.get(Calendar.MONTH);
                dayOfMonth = mCurrentDate.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePicker = new DatePickerDialog(NewContextActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear,
                                          int dayOfMonth) {
                        Calendar c = new GregorianCalendar();
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                        sdf.setTimeZone(c.getTimeZone());
                        c.set(year, monthOfYear, dayOfMonth);
                        dateEndEditText.setText(sdf.format(c.getTime()));
                    }
                }, year, monthOfYear, dayOfMonth);
                datePicker.setTitle(getResources().getString(R.string.deactivation_date));
                datePicker.show();
            }
        });

        frequencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) { //frequency none
                    findViewById(R.id.activity_new_context_week_bar_text_input_layout).setVisibility(View.GONE);
                    findViewById(R.id.activity_new_context_attention_label_text_input_layout).setVisibility(View.GONE);
                    findViewById(R.id.activity_new_context_activation_date_text_input_layout).setVisibility(View.VISIBLE);
                    findViewById(R.id.activity_new_context_deactivation_date_text_input_layout).setVisibility(View.VISIBLE);
                } else if (position == 1) {//frequency daily
                    findViewById(R.id.activity_new_context_week_bar_text_input_layout).setVisibility(View.GONE);
                    findViewById(R.id.activity_new_context_attention_label_text_input_layout).setVisibility(View.VISIBLE);
                    findViewById(R.id.activity_new_context_activation_date_text_input_layout).setVisibility(View.GONE);
                    findViewById(R.id.activity_new_context_deactivation_date_text_input_layout).setVisibility(View.GONE);
                } else { //frequency weekly
                    findViewById(R.id.activity_new_context_week_bar_text_input_layout).setVisibility(View.VISIBLE);
                    findViewById(R.id.activity_new_context_attention_label_text_input_layout).setVisibility(View.VISIBLE);
                    findViewById(R.id.activity_new_context_activation_date_text_input_layout).setVisibility(View.GONE);
                    findViewById(R.id.activity_new_context_deactivation_date_text_input_layout).setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        addressEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!waitingForPlacePicker) {
                    waitingForPlacePicker = true;
                    if (checkCallingPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && checkCallingPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(NewContextActivity.this,
                                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                    } else {
                        int PLACE_PICKER_REQUEST = 1;
                        try {
                            PlacePicker.IntentBuilder intentBuilder =
                                    new PlacePicker.IntentBuilder();
                            Intent intent = intentBuilder.build(NewContextActivity.this);
                            startActivityForResult(intent, PLACE_PICKER_REQUEST);

                        } catch (GooglePlayServicesRepairableException
                                | GooglePlayServicesNotAvailableException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });


        if(Build.VERSION.SDK_INT >= 21)
            nextButton.setBackgroundTintList(getResources().getColorStateList(R.color.colorAccent));
        else nextButton.setBackgroundResource(R.color.colorAccent);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final CurrentContext savedInstance = TmpContextKeeper.getInstance().getCurrentContext();

                String name = ((EditText) findViewById(R.id.activity_new_context_name_edit_text)).getText().toString();
                //time Context
                int frequency = frequencySpinner.getSelectedItemPosition();
                String activationTime = ((EditText) findViewById(R.id.activity_new_context_activation_time_edit_text)).getText().toString();
                String deactivationTime = ((EditText) findViewById(R.id.activity_new_context_deactivation_time_edit_text)).getText().toString();
                String activationDateText = ((EditText) findViewById(R.id.activity_new_context_activation_date_edit_text)).getText().toString();
                String deactivationDateText = ((EditText) findViewById(R.id.activity_new_context_deactivation_date_edit_text)).getText().toString();

                if (frequency != 0) {
                    activationDateText = new SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().getTime());
                    deactivationDateText = new SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().getTime());
                }

                Date activationDate = null;
                Date deactivationDate = null;

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                try {
                    activationDate = sdf.parse(activationDateText + " " + activationTime);
                    deactivationDate = sdf.parse(deactivationDateText + " " + deactivationTime);
                } catch (ParseException e) {
                    e.printStackTrace();
                    Toast.makeText(getBaseContext(), getResources().getString(R.string.fill_each_field), Toast.LENGTH_LONG).show();
                    return;
                }

                if(latlng == null) {
                    Toast.makeText(getBaseContext(), getResources().getString(R.string.fill_each_field), Toast.LENGTH_LONG).show();
                    return;
                }

                if (frequency == 0 && activationDate.after(deactivationDate)) {
                    Calendar c = Calendar.getInstance();
                    c.setTime(deactivationDate);
                    c.add(Calendar.DAY_OF_MONTH, 1);
                    deactivationDate = c.getTime();
                }

                List<Integer> daysOfWeek = new ArrayList<>();
                for (CheckedTextView ctv : daysOfWeekCTV)
                    if (ctv.isChecked())
                        daysOfWeek.add(((daysOfWeekCTV.indexOf(ctv) + Calendar.SUNDAY) % 7) + 1);
                savedInstance.setName(name);
                savedInstance.setTimeContext(new TimeContext(frequency, activationDate, deactivationDate, daysOfWeek, savedInstance.getId(), getApplicationContext()), getApplicationContext());

                int radius;
                try {
                    radius = Integer.parseInt(radiusEdiText.getText().toString());
                } catch (NumberFormatException e) {
                    radius = 1;
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.invalid_radius) + radius, Toast.LENGTH_SHORT).show();
                }
                if(latlng != null)
                    savedInstance.setLocationContext(new LocationContext(latlng, city, radius, savedInstance.getId()));
                else
                    Toast.makeText(getBaseContext(), getResources().getString(R.string.fill_each_field), Toast.LENGTH_LONG).show();

                Intent intent = new Intent(NewContextActivity.this, ChoosePermissionsNewContextActivity.class);

                TmpContextKeeper.getInstance().setPhase(TmpContextKeeper.REVOKE_PHASE);
                intent.putExtra("TYPE", CurrentContext.REVOKE);
                startActivity(intent);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        int PLACE_PICKER_REQUEST = 1;
        final EditText addressEditText = (EditText) findViewById(R.id.activity_new_context_address_edit_text);
        if (requestCode == PLACE_PICKER_REQUEST
                && resultCode == Activity.RESULT_OK) {

            final Place place = PlacePicker.getPlace(this, data);
            if(place.getAddress() != null)
                addressEditText.setText(place.getAddress());
            else
                Toast.makeText(getBaseContext(), getResources().getString(R.string.place_picker_error), Toast.LENGTH_SHORT).show();
            latlng = place.getLatLng();
            city = place.getAddress().toString();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onClick(View v) {
        ((CheckedTextView) v).setChecked(!((CheckedTextView) v).isChecked());
        if (((CheckedTextView) v).isChecked()) {
            v.setBackground(getResources().getDrawable(R.drawable.circle));
            ((CheckedTextView) v).setTextColor(Color.WHITE);
        } else {
            v.setBackground(null);
            ((CheckedTextView) v).setTextColor(Color.BLACK);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    int PLACE_PICKER_REQUEST = 1;
                    try {
                        PlacePicker.IntentBuilder intentBuilder =
                                new PlacePicker.IntentBuilder();
                        Intent intent = intentBuilder.build(NewContextActivity.this);
                        startActivityForResult(intent, PLACE_PICKER_REQUEST);

                    } catch (GooglePlayServicesRepairableException
                            | GooglePlayServicesNotAvailableException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(NewContextActivity.this, getResources().getString(R.string.denied_position), Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        waitingForPlacePicker = false;
    }
}