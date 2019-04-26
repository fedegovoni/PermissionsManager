package com.federicogovoni.permissionmanager.view.adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.federicogovoni.permissionmanager.model.CurrentContext;
import com.federicogovoni.permissionmanager.model.LocationContext;
import com.federicogovoni.permissionmanager.view.NewContextActivity;
import com.federicogovoni.permissionmanager.R;
import com.federicogovoni.permissionmanager.model.TimeContext;
import com.federicogovoni.permissionmanager.utils.TmpContextKeeper;
import com.federicogovoni.permissionmanager.controller.ContextManager;
import com.federicogovoni.permissionmanager.view.fragment.SettingsFragment;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import static com.federicogovoni.permissionmanager.model.CurrentContext.ENABLED;
import static com.federicogovoni.permissionmanager.model.CurrentContext.IN_LOCATION;
import static com.federicogovoni.permissionmanager.model.CurrentContext.IN_TIME;
import static com.federicogovoni.permissionmanager.model.CurrentContext.RUNNING_STATE;

/**
 * Created by Federico on 12/03/2017.
 */

public class ContextCardAdapter extends BaseAdapter {

    private static final String TAG = "ContextCardAdapter";

    private List<CurrentContext> contexts;
    private final Context context;
    private LayoutInflater inflater;

    public ContextCardAdapter(Context context) {
        super();
        contexts = ContextManager.getInstance(context).getContexts();
        this.context = context;
        this.inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
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
            viewHolder.mySwitch = (Switch) convertView.findViewById(R.id.context_layout_enable_switch);
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
                Log.d(TAG, "Invoked onSharedPreferenceChangeListener");
                if (key.contains(RUNNING_STATE)) {
                    String idS = key.substring(RUNNING_STATE.length());
                    int id = Integer.parseInt(idS);
                    if (!(finalCurrentContexts.get(position).getId() == id))
                        return;
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
                    boolean isRunning = sp.getBoolean(RUNNING_STATE + id, false);
                    if (isRunning) {
                        finalViewHolder.running.setVisibility(View.VISIBLE);
                    } else {
                        finalViewHolder.running.setVisibility(View.GONE);
                    }
                }
            }
        };

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.registerOnSharedPreferenceChangeListener(listener);

        viewHolder.contextName.setText(contexts.get(position).getName());
        if (contexts.get(position).getTimeContext() != null) {
            TimeContext p = contexts.get(position).getTimeContext();
            viewHolder.frequencyDetail.setText(p.getFrequency() == 0 ? R.string.none : p.getFrequency() == 1 ? R.string.daily : R.string.weekly);
            String dateDetail = "";
            SimpleDateFormat sdf = new SimpleDateFormat(context.getResources().getString(R.string.date_format_full), Locale.getDefault());
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
            viewHolder.radiusDetail.setText(p.getRadius() + " " + PreferenceManager.getDefaultSharedPreferences(context).getString(LocationContext.MEASURE, LocationContext.KM));
        } else {
            convertView.findViewById(R.id.context_layout_details_second_row_linear_layout).setVisibility(View.GONE);
        }
        viewHolder.cardView.setLongClickable(true);

        final View rl = ((AppCompatActivity) context).findViewById(R.id.fragment_contexts_main_list_view);
        final View noContext = ((AppCompatActivity) context).findViewById(R.id.fragment_contexts_empty_relative_layout);

        viewHolder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final Dialog modifyDeleteDialog = new Dialog(context);
                modifyDeleteDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                modifyDeleteDialog.setContentView(R.layout.dialog_context_long_press);

                TextView modify = (TextView) modifyDeleteDialog.findViewById(R.id.dialog_context_long_press_modify_text_view);
                TextView delete = (TextView) modifyDeleteDialog.findViewById(R.id.dialog_context_long_press_delete_text_view);

                modify.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CurrentContext current = ContextManager.getInstance(context).getContexts().get(position);
                        TmpContextKeeper.getInstance().setCurrentContext(current);
                        Intent intent = new Intent(context, NewContextActivity.class);
                        context.startActivity(intent);
                        modifyDeleteDialog.dismiss();
                    }
                });

                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ContextManager.getInstance(context).removeContext(position);
                        contexts = ContextManager.getInstance(context).getContexts();
                        notifyDataSetChanged();
                        modifyDeleteDialog.dismiss();
                        if(ContextManager.getInstance(context).getContexts().size() == 0) {
                            rl.setVisibility(View.GONE);
                            noContext.setVisibility(View.VISIBLE);
                        }
                    }
                });

                modifyDeleteDialog.show();
                return false;
            }
        });

        if(contexts.get(position).isRunning(context))
            viewHolder.running.setVisibility(View.VISIBLE);
        else
            viewHolder.running.setVisibility(View.GONE);

        final Switch mySwitch = viewHolder.mySwitch;
        final ImageView running = viewHolder.running;
        viewHolder.mySwitch.setChecked(contexts.get(position).isEnabled(context));
        viewHolder.mySwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CurrentContext p = contexts.get(position);
                if (p.getTimeContext() != null && p.getTimeContext().getFrequency() == TimeContext.WEEKLY &&
                        (p.getTimeContext().getDaysOfWeek() == null || p.getTimeContext().getDaysOfWeek().isEmpty())) {
                    mySwitch.setChecked(false);
                    Toast.makeText(v.getContext(), R.string.toast_timecontext_weekly_noweekdays, Toast.LENGTH_SHORT).show();
                }
                else {
                    contexts.get(position).setEnabled(mySwitch.isChecked(), context);
                }
            }
        });



        return result;
    }

    private static class ViewHolder {
        CardView cardView;
        TextView contextName;
        TextView frequencyDetail;
        TextView applyAndDisableDateDetail;
        TextView addressDetail;
        TextView radiusDetail;
        Switch mySwitch;
        ImageView running;
    }
}
