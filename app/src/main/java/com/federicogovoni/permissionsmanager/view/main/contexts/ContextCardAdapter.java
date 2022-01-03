package com.federicogovoni.permissionsmanager.view.main.contexts;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.federicogovoni.permissionsmanager.model.CurrentContext;
import com.federicogovoni.permissionsmanager.model.LocationContext;
import com.federicogovoni.permissionsmanager.view.main.contexts.newcontext.NewContextActivity;
import com.federicogovoni.permissionmanager.R;
import com.federicogovoni.permissionsmanager.model.TimeContext;
import com.federicogovoni.permissionsmanager.utils.TmpContextKeeper;
import com.federicogovoni.permissionsmanager.controller.ContextManager;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import static com.federicogovoni.permissionsmanager.model.CurrentContext.RUNNING_STATE;

import timber.log.Timber;

/**
 * Created by Federico on 12/03/2017.
 */

public class ContextCardAdapter extends BaseAdapter {

    private static final String TAG = "ContextCardAdapter";

    private List<CurrentContext> contexts;
    private final Context mContext;
    private final LayoutInflater inflater;
    private final FirebaseAnalytics firebaseAnalytics;

    private static class ViewHolder {
        CardView cardView;
        TextView contextName;
        TextView frequencyDetail;
        TextView applyAndDisableDateDetail;
        TextView addressDetail;
        TextView radiusDetail;
        SwitchMaterial mySwitch;
        ImageView running;
    }

    public ContextCardAdapter(Context context) {
        super();
        contexts = ContextManager.getInstance(context).getContexts();
        this.mContext = context;
        this.inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        this.firebaseAnalytics = FirebaseAnalytics.getInstance(context);
    }

    @Override
    public int getCount() {
        return contexts.size();
    }

    @Override
    public Object getItem(int position) {
        return contexts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder = null;
        final View result;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.context_layout, parent, false);
            if(position == contexts.size() - 1) {
                int currentPadding = convertView.getPaddingTop();
                convertView.setPadding(currentPadding, currentPadding, currentPadding, 10*currentPadding);
            }
            viewHolder = new ViewHolder();
            viewHolder.contextName = (TextView) convertView.findViewById(R.id.context_layout_context_name);
            viewHolder.mySwitch = (SwitchMaterial) convertView.findViewById(R.id.context_layout_enable_switch);
            viewHolder.frequencyDetail = (TextView) convertView.findViewById(R.id.context_layout_details_first_row_frequency_text_view);
            viewHolder.applyAndDisableDateDetail = (TextView) convertView.findViewById(R.id.context_layout_details_first_row_apply_disable_times_text_view);
            viewHolder.addressDetail = (TextView) convertView.findViewById(R.id.context_layout_details_second_row_address_text_view);
            viewHolder.radiusDetail = (TextView) convertView.findViewById(R.id.context_layout_details_second_row_radius_text_view);
            viewHolder.cardView = (CardView) convertView.findViewById(R.id.context_layout_card_view);
            viewHolder.running = (ImageView) convertView.findViewById(R.id.context_layout_running_icon_image_view);

            convertView.setTag(viewHolder);
            result = convertView;
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result = convertView;
        }

        final ViewHolder finalViewHolder = viewHolder;
        final List<CurrentContext> finalCurrentContexts = contexts;
        SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                Timber.d("Invoked onSharedPreferenceChangeListener");
                if (key.contains(RUNNING_STATE)) {
                    String idS = key.substring(RUNNING_STATE.length());
                    int id = Integer.parseInt(idS);
                    if (!(finalCurrentContexts.get(position).getId() == id))
                        return;
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
                    boolean isRunning = sp.getBoolean(RUNNING_STATE + id, false);
                    if (isRunning) {
                        finalViewHolder.running.setVisibility(View.VISIBLE);
                    } else {
                        finalViewHolder.running.setVisibility(View.GONE);
                    }
                }
            }
        };

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        prefs.registerOnSharedPreferenceChangeListener(listener);

        viewHolder.contextName.setText(contexts.get(position).getName());
        if (contexts.get(position).getTimeContext() != null) {
            TimeContext p = contexts.get(position).getTimeContext();
            viewHolder.frequencyDetail.setText(p.getFrequency() == 0 ? R.string.none : p.getFrequency() == 1 ? R.string.daily : R.string.weekly);
            String dateDetail = "";
            SimpleDateFormat sdf = new SimpleDateFormat(mContext.getResources().getString(R.string.date_format_full), Locale.getDefault());
            if(p.getFrequency() == TimeContext.NONE) {
                dateDetail = sdf.format(p.getApplyDate()) + "\n" + sdf.format(p.getDeactivationDate());
            } else {
                dateDetail = p.getApplyTime() + " - " + p.getDeactivationTime();
            }
            viewHolder.applyAndDisableDateDetail.setText(dateDetail);
        } else {
            convertView.findViewById(R.id.context_layout_details_first_row_linear_layout).setVisibility(View.GONE);
        }
        if (contexts.get(position).getLocationContext() != null){
            LocationContext p = contexts.get(position).getLocationContext();
            viewHolder.addressDetail.setText(p.getCity());
            viewHolder.radiusDetail.setText(p.getRadius() + " " + PreferenceManager.getDefaultSharedPreferences(mContext).getString(LocationContext.MEASURE, LocationContext.KM));
        } else {
            convertView.findViewById(R.id.context_layout_details_second_row_linear_layout).setVisibility(View.GONE);
        }
        viewHolder.cardView.setLongClickable(true);

        final View mainListView = ((AppCompatActivity) mContext).findViewById(R.id.fragment_contexts_main_list_view);
        final View noContextView = ((AppCompatActivity) mContext).findViewById(R.id.fragment_contexts_empty_relative_layout);

        viewHolder.cardView.setOnClickListener(v -> {
            final Dialog modifyDeleteDialog = new Dialog(mContext);
            modifyDeleteDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            modifyDeleteDialog.setContentView(R.layout.dialog_context_long_press);

            TextView modify = (TextView) modifyDeleteDialog.findViewById(R.id.dialog_context_long_press_modify_text_view);
            TextView delete = (TextView) modifyDeleteDialog.findViewById(R.id.dialog_context_long_press_delete_text_view);

            modify.setOnClickListener(view -> {modifyContext(position, modifyDeleteDialog);});

            delete.setOnClickListener(view -> {deleteContext(position, modifyDeleteDialog, mainListView, noContextView);});

            modifyDeleteDialog.show();
            return;
        });

        if (contexts.get(position).isRunning(mContext)) {
            viewHolder.running.setVisibility(View.VISIBLE);
        } else {
            viewHolder.running.setVisibility(View.GONE);
        }

        final SwitchMaterial mySwitch = viewHolder.mySwitch;
        final ImageView running = viewHolder.running;
        viewHolder.mySwitch.setChecked(contexts.get(position).isEnabled(mContext));
        viewHolder.mySwitch.setOnClickListener(v -> {
            CurrentContext p = contexts.get(position);
            if (p.getTimeContext() != null && p.getTimeContext().getFrequency() == TimeContext.WEEKLY &&
                    (p.getTimeContext().getDaysOfWeek() == null || p.getTimeContext().getDaysOfWeek().isEmpty())) {
                mySwitch.setChecked(false);
                Toast.makeText(v.getContext(), R.string.toast_timecontext_weekly_noweekdays, Toast.LENGTH_SHORT).show();
            }
            else {
                contexts.get(position).setEnabled(mySwitch.isChecked(), mContext);
            }
        });
        return result;
    }

    private void modifyContext(int position, Dialog modifyDeleteDialog) {
        CurrentContext current = ContextManager.getInstance(mContext).getContexts().get(position);
        TmpContextKeeper.getInstance().setCurrentContext(current);
        Intent intent = new Intent(mContext, NewContextActivity.class);
        mContext.startActivity(intent);
        modifyDeleteDialog.dismiss();
    }

    private void deleteContext(int position, Dialog modifyDeleteDialog, View mainListView, View noContextView) {
        ContextManager.getInstance(mContext).removeContext(position);
        contexts = ContextManager.getInstance(mContext).getContexts();
        notifyDataSetChanged();
        modifyDeleteDialog.dismiss();
        if(ContextManager.getInstance(mContext).getContexts().size() == 0) {
            mainListView.setVisibility(View.GONE);
            noContextView.setVisibility(View.VISIBLE);
        }
        Bundle bundle = new Bundle();
        bundle.putString("DELETE_CONTEXT", "Deleted existing context");
        firebaseAnalytics.logEvent("DELETE_CONTEXT", bundle);

    }
}
