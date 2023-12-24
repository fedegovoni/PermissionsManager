package com.federicogovoni.permissionsmanager.view.main.contexts.newcontext;

import android.app.Activity;
import android.app.DatePickerDialog;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TimePicker;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentContainerView;

import com.federicogovoni.permissionsmanager.Constants;
import com.federicogovoni.permissionsmanager.controller.ProVersionChecker;
import com.federicogovoni.permissionsmanager.model.CurrentContext;
import com.federicogovoni.permissionsmanager.model.LocationContext;
import com.federicogovoni.permissionmanager.R;
import com.federicogovoni.permissionsmanager.model.TimeContext;
import com.federicogovoni.permissionsmanager.utils.AdRequestKeeper;
import com.federicogovoni.permissionsmanager.utils.GeneralUtils;
import com.federicogovoni.permissionsmanager.utils.TmpContextKeeper;
import com.federicogovoni.permissionsmanager.view.main.BaseActivity;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

public class NewContextActivity extends BaseActivity {

    private Address address;
    private ActivityResultLauncher<Intent> mapsActivityResultLauncher;

    EditText contextNameEditText;
    EditText timeStartEditText;
    EditText dateStartEditText;
    EditText timeEndEditText;
    EditText dateEndEditText;
    Spinner frequencySpinner;
    EditText radiusEdiText;
    TextInputLayout radiusTextInputLayout;
    EditText addressEditText;
    Button nextButton;
    ConstraintLayout rootLayout;
    View scrollView;
    FragmentContainerView fragmentContainerView;

    CheckedTextView mondayCheckedTextView;
    CheckedTextView tuesdayCheckedTextView;
    CheckedTextView wednesdayCheckedTextView;
    CheckedTextView thursdayCheckedTextView;
    CheckedTextView fridayCheckedTextView;
    CheckedTextView saturdayCheckedTextView;
    CheckedTextView sundayCheckedTextView;
    List<CheckedTextView> daysOfWeekCTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_new_context);

        contextNameEditText = findViewById(R.id.activity_new_context_name_edit_text);
        timeStartEditText = findViewById(R.id.activity_new_context_activation_time_edit_text);
        dateStartEditText = findViewById(R.id.activity_new_context_activation_date_edit_text);
        timeEndEditText  = findViewById(R.id.activity_new_context_deactivation_time_edit_text);
        dateEndEditText = findViewById(R.id.activity_new_context_deactivation_date_edit_text);
        frequencySpinner = findViewById(R.id.activity_new_context_frequency_spinner);
        radiusEdiText = findViewById(R.id.activity_new_context_radius_edit_text);
        radiusTextInputLayout = findViewById(R.id.activity_new_context_radius_text_input_layout);
        addressEditText = findViewById(R.id.activity_new_context_address_edit_text);
        nextButton = findViewById(R.id.activity_new_context_next_button);
        rootLayout = findViewById(R.id.activity_new_context_root_layout);
        scrollView = findViewById(R.id.activity_new_context_scroll_view);
        fragmentContainerView = findViewById(R.id.activity_main_place_picker_container_fragment_container_view);

        mondayCheckedTextView = findViewById(R.id.activity_new_context_monday_checked_text_view);
        tuesdayCheckedTextView = findViewById(R.id.activity_new_context_tuesday_checked_text_view);
        wednesdayCheckedTextView = findViewById(R.id.activity_new_context_wednesday_checked_text_view);
        thursdayCheckedTextView = findViewById(R.id.activity_new_context_thursday_checked_text_view);
        fridayCheckedTextView = findViewById(R.id.activity_new_context_friday_checked_text_view);
        saturdayCheckedTextView = findViewById(R.id.activity_new_context_saturday_checked_text_view);
        sundayCheckedTextView = findViewById(R.id.activity_new_context_sunday_checked_text_view);

        daysOfWeekCTV = Arrays.asList(mondayCheckedTextView,tuesdayCheckedTextView,wednesdayCheckedTextView,thursdayCheckedTextView,fridayCheckedTextView,saturdayCheckedTextView,sundayCheckedTextView);

        ArrayAdapter<CharSequence> daysOfWeekAdapter = ArrayAdapter.createFromResource(this, R.array.application_frequency, android.R.layout.simple_spinner_item);
        daysOfWeekAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        frequencySpinner.setAdapter(daysOfWeekAdapter);

        frequencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                frequencySpinnerOnItemSelected(adapterView, view, i, l);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Timber.d("Nothing selected");
            }
        });

        daysOfWeekCTV.forEach(dayItem -> dayItem.setOnClickListener(this::onDayItemClick));

        timeStartEditText.setOnClickListener(this::activationTimeEditTextOnClick);
        addressEditText.setOnClickListener(this::onAddressEditTextClick);
        nextButton.setOnClickListener(this::onNextButtonClick);
        dateEndEditText.setOnClickListener(this::dateEndEditTextOnClick);
        timeEndEditText.setOnClickListener(this::onTimeEndEditTextClick);
        dateStartEditText.setOnClickListener(this::onDateStartEditTextClick);

        checkIfInModifyScenarioAndSetFields();

        setRadiusHint();

        if(Build.VERSION.SDK_INT >= 21)
            nextButton.setBackgroundTintList(getResources().getColorStateList(R.color.colorAccent));
        else nextButton.setBackgroundResource(R.color.colorAccent);

        mapsActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                (result -> {
                    if(result != null && result.getData() != null) {
                        Timber.d("Got result from activity with data %s and resultcode: %d", result.getData().getData(), result.getResultCode());
                    } else {
                        Timber.d("Result message from map activity: %d", result.getResultCode());
                    }
                    if (result != null &&
                            result.getResultCode() == Activity.RESULT_OK &&
                            result.getData() != null &&
                            ((boolean) result.getData().getExtras().get(Constants.SUCCESS)) &&
                            result.getData().getExtras().get(Constants.SELECTED_PLACE) != null) {
                        Address selectedAddress = (Address) result.getData().getExtras().get(Constants.SELECTED_PLACE);
                        Timber.d("Selected Address START");
                        Timber.d("Address Line: %s", selectedAddress.getAddressLine(0));
                        Timber.d("Country Code: %s", selectedAddress.getCountryCode());
                        Timber.d("Latitude: %s", selectedAddress.getLatitude());
                        Timber.d("Longitude: %s", selectedAddress.getLongitude());
                        Timber.d("Selected Address END");

                        addressEditText.setText(selectedAddress.getAddressLine(0));
                        address = selectedAddress;
                    }
                }));
    }



    @Override
    public void onResume() {
        super.onResume();
    }

    public void frequencySpinnerOnItemSelected(AdapterView<?> parent, View view, int position, long id) {
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

    public void activationTimeEditTextOnClick(View v) {
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

    public void onAddressEditTextClick(View v) {
        //Open GMaps
        Intent intent = new Intent(this, MapsActivityCurrentPlace.class);
        mapsActivityResultLauncher.launch(intent);
    }


    public void onNextButtonClick(View view) {
        final CurrentContext savedInstance = TmpContextKeeper.getInstance().getCurrentContext();

        String name = contextNameEditText.getText().toString();
        int frequency = frequencySpinner.getSelectedItemPosition();
        String activationTime = timeStartEditText.getText().toString();
        String deactivationTime = timeEndEditText.getText().toString();
        String activationDateText = dateStartEditText.getText().toString();
        String deactivationDateText = dateEndEditText.getText().toString();

        if (frequency != Constants.FREQUENCY_NONE) {
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
            GeneralUtils.showSnackbar(rootLayout, getResources().getString(R.string.fill_each_field));
            return;
        }

        if(address == null) {
            GeneralUtils.showSnackbar(rootLayout, getResources().getString(R.string.fill_each_field_location_missing));
            return;
        }

        if (frequency == Constants.FREQUENCY_NONE && activationDate.after(deactivationDate)) {
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
            GeneralUtils.showSnackbar(rootLayout, getResources().getString(R.string.invalid_radius));
        }
        if(address != null) {
            LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
            String city = address.getLocality();
            city = address.getSubLocality() == null ? city : city + " - " + address.getSubLocality();
            savedInstance.setLocationContext(new LocationContext(latLng, city, radius, savedInstance.getId()));
        } else {
            GeneralUtils.showSnackbar(rootLayout, getResources().getString(R.string.fill_each_field_address_missing));
        }

        Intent intent = new Intent(NewContextActivity.this, ChoosePermissionsNewContextActivity.class);

        TmpContextKeeper.getInstance().setPhase(TmpContextKeeper.REVOKE_PHASE);
        intent.putExtra("TYPE", CurrentContext.REVOKE);
        startActivity(intent);
    }

    public void onDayItemClick(View v) {
        ((CheckedTextView) v).setChecked(!((CheckedTextView) v).isChecked());
        if (((CheckedTextView) v).isChecked()) {
            v.setBackground(getResources().getDrawable(R.drawable.circle));
            ((CheckedTextView) v).setTextColor(Color.WHITE);
        } else {
            v.setBackground(null);
            ((CheckedTextView) v).setTextColor(Color.BLACK);
        }
    }

    public void dateEndEditTextOnClick(View v) {
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

    public void onDateStartEditTextClick(View v) {
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

    public void onTimeEndEditTextClick(View v) {
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

    private void setTimeContextFieldsForContextInModification(CurrentContext toModify) {
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
            setDaysOfWeekForConextInModification(toModify);
        }
    }

    private void setDaysOfWeekForConextInModification(CurrentContext toModify) {
        if (toModify != null && toModify.getTimeContext() != null && toModify.getTimeContext().getDaysOfWeek() != null)
            for (int day : toModify.getTimeContext().getDaysOfWeek()) {
                day = (day - 2) % 7;
                if (day < 0)
                    day += 7;
                daysOfWeekCTV.get(day).setChecked(true);
                daysOfWeekCTV.get(day).setBackground(getResources().getDrawable(R.drawable.circle));
                daysOfWeekCTV.get(day).setTextColor(Color.WHITE);
            }
    }

    private void setLocationContextFieldsForContextInModification(CurrentContext toModify) {
        if(toModify != null && toModify.getLocationContext() != null) {
            LocationContext lc = toModify.getLocationContext();
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses;
            try {
                addresses = geocoder.getFromLocation(lc.getLatitude(), lc.getLongitude(), 1);
                addresses.forEach(address -> Timber.d("Found address %s", address.getAddressLine(0)));
                address = addresses.get(0);
            } catch (IOException e) {
                Timber.e("Error fetching address from location %s from LocatioContext", lc.getCity());
                GeneralUtils.showSnackbar(findViewById(R.id.activity_new_context_root_layout), getResources().getString(R.string.snackbar_error_get_address));
                e.printStackTrace();
            }

            radiusEdiText.setText(lc.getRadius() + "");
            addressEditText.setText(lc.getCity());
        }
    }

    private void setRadiusHint() {
        if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).
                getString(LocationContext.MEASURE, LocationContext.KM).equals(LocationContext.KM))
            radiusTextInputLayout.setHint(getResources().getString(R.string.radius) + "(" + getResources().getString(R.string.km) + ")");
        else
            radiusTextInputLayout.setHint(getResources().getString(R.string.radius) + "(" + getResources().getString(R.string.miles) + ")");
    }

    private void setNameForContextInModification(CurrentContext toModify) {
        if (toModify.getName() != null && !toModify.getName().equals("")) {
            contextNameEditText.setText(toModify.getName());
            setTitle(toModify.getName());
        } else {
            setTitle(getResources().getString(R.string.new_context));
        }
    }

    private void checkIfInModifyScenarioAndSetFields() {
        CurrentContext toModify = TmpContextKeeper.getInstance().getCurrentContext();
        if(toModify == null) return;
        setNameForContextInModification(toModify);
        setLocationContextFieldsForContextInModification(toModify);
        setTimeContextFieldsForContextInModification(toModify);
    }

    @Override
    public void onProVersionResult(boolean isPro) {
        Timber.d("IProVersionListener invoked for %s", getClass().toString());
        super.onProVersionResult(isPro);
        if(isPro) {
            try {
                if (scrollView == null)
                    scrollView = findViewById(R.id.activity_new_context_scroll_view);
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) scrollView.getLayoutParams();
                layoutParams.setMargins(0, 0, layoutParams.getMarginEnd(), layoutParams.getMarginEnd());
                scrollView.setLayoutParams(layoutParams);
            } catch(NullPointerException e) {
                Timber.w("ScrollView is null");
            }
        }
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_new_context;
    }
}