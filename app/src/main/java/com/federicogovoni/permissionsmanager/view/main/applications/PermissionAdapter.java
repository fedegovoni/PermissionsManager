package com.federicogovoni.permissionsmanager.view.main.applications;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.federicogovoni.permissionsmanager.model.Permission;
import com.federicogovoni.permissionmanager.R;
import com.federicogovoni.permissionsmanager.utils.GeneralUtils;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.StringTokenizer;

import timber.log.Timber;

/**
 * Created by Federico on 28/02/2017.
 */

public class PermissionAdapter extends BaseAdapter {
    final private List<Permission> dataSet;
    Context mContext;
    final private LayoutInflater inflater;
    final private ApplicationInfo mApplicationInfo;
    final private FirebaseAnalytics firebaseAnalytics;

    public static final int ROW_HEADER = 0;
    public static final int ROW_NO_HEADER = 1;

    // View lookup cache
    private static class ViewHolder {
        TextView name;
        TextView group;
        SwitchMaterial mySwitch;
        ImageView icon;
        ImageView appIcon;
        TextView appSize;
        TextView appVersion;
        TextView appApi;
    }

    public PermissionAdapter(List<Permission> data, Context context, ApplicationInfo applicationInfo) {
        this.dataSet = data;
        this.mContext=context;
        this.mApplicationInfo = applicationInfo;
        this.inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        this.firebaseAnalytics = FirebaseAnalytics.getInstance(context);
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {
        // Get the data item for this position
        final Permission permission = getItem(position);
        int type = getItemViewType(position);

        ViewHolder viewHolder = null; // view lookup cache stored in tag

        if (convertView == null) {
            switch (type) {
                case ROW_HEADER:
                    convertView = inflater.inflate(R.layout.adapter_row_custom_permission_header, parent, false);
                    viewHolder = new ViewHolder();
                    viewHolder.name = (TextView) convertView.findViewById(R.id.adapter_row_custom_permission_name_text_view);
                    viewHolder.group = (TextView) convertView.findViewById(R.id.adapter_row_custom_permission_header_app_name_text_view);
                    viewHolder.mySwitch = (SwitchMaterial) convertView.findViewById(R.id.adapter_row_custom_permission_grant_or_revoke_switch);
                    viewHolder.icon = (ImageView) convertView.findViewById(R.id.adapter_row_custom_permission_icon_image_view);

                    viewHolder.appApi = (TextView) convertView.findViewById(R.id.adapter_row_custom_permission_header_app_api_text_view);
                    viewHolder.appIcon = (ImageView) convertView.findViewById(R.id.adapter_row_custom_permission_header_application_icon_image_view);
                    viewHolder.appSize = (TextView) convertView.findViewById(R.id.adapter_row_custom_permission_header_app_size_text_view);
                    viewHolder.appVersion = (TextView) convertView.findViewById(R.id.adapter_row_custom_permission_header_app_version_text_view);

                    convertView.setTag(viewHolder);
                    break;

                case ROW_NO_HEADER:
                    convertView = inflater.inflate(R.layout.adapter_row_custom_permission, parent, false);
                    viewHolder = new ViewHolder();
                    viewHolder.name = (TextView) convertView.findViewById(R.id.adapter_row_custom_permission_name_text_view);
                    viewHolder.mySwitch = (SwitchMaterial) convertView.findViewById(R.id.adapter_row_custom_permission_grant_or_revoke_switch);
                    viewHolder.icon = (ImageView) convertView.findViewById(R.id.adapter_row_custom_permission_icon_image_view);

                    convertView.setTag(viewHolder);
                    break;
            }

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        assert viewHolder != null;
        viewHolder.mySwitch.setOnClickListener(new Switch.OnClickListener() {
            public void onClick(View view) {
                boolean success;
                if (mContext.getPackageManager().checkPermission(permission.getName(), permission.getPackageName()) == PackageManager.PERMISSION_GRANTED) {
                    success = permission.revoke();
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.SOURCE, "Permission Manual Change");
                    bundle.putString(FirebaseAnalytics.Param.METHOD, "Revoke");
                    bundle.putString(FirebaseAnalytics.Param.SUCCESS, "" + success);
                    firebaseAnalytics.logEvent("PERMISSION_MANUAL_CHANGE", bundle);
                    Timber.d("Firebase - Logged analytics event");

                    if(!success) {
                        Timber.e("Manual revoke failed");
                        ((SwitchMaterial) view).setChecked(true);
                        GeneralUtils.showSnackbar(view, mContext.getResources().getString(R.string.snackbar_no_priviliges));
                    }

                } else {
                    success = permission.grant();
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.SOURCE, "Permission Manual Change");
                    bundle.putString(FirebaseAnalytics.Param.METHOD, "Grant");
                    bundle.putString(FirebaseAnalytics.Param.SUCCESS, "" + success);
                    firebaseAnalytics.logEvent("PERMISSION_MANUAL_CHANGE", bundle);
                    Timber.d("Firebase - Logged analytics event");

                    if(!success) {
                        Timber.e("Manual grant failed");
                        ((SwitchMaterial) view).setChecked(false);
                        GeneralUtils.showSnackbar(view, mContext.getResources().getString(R.string.snackbar_no_priviliges));
                    }
                }
            }
        });


        if(getItemViewType(position) == ROW_HEADER) {
            String headerText = "";
            StringTokenizer tokenizer = new StringTokenizer(permission.getGroup(), ".");
            while (tokenizer.hasMoreTokens())
                headerText = tokenizer.nextToken();
            viewHolder.group.setText(headerText);
        }

        PackageManager pm = mContext.getPackageManager();

        if(position == 0) {
            convertView.findViewById(R.id.adapter_row_custom_permission_header_relative_layout).setVisibility(View.VISIBLE);
            viewHolder.appIcon.setImageDrawable(mApplicationInfo.loadIcon(pm));
            viewHolder.appApi.setText("API: " + mApplicationInfo.targetSdkVersion);
            try {
                float fileSize = new FileInputStream(mApplicationInfo.sourceDir).getChannel().size()/(1024*1024);
                viewHolder.appSize.setText("Size: " + String.format("%.0f",fileSize) + " Mb");
            } catch (IOException e) {
                viewHolder.appSize.setText("Size:");
            }
            PackageInfo pInfo;
            try {
                pInfo= pm.getPackageInfo(permission.getPackageName(), 0);
                viewHolder.appVersion.setText("Version: " + pInfo.versionName);
            } catch (PackageManager.NameNotFoundException e) {
                viewHolder.appVersion.setText("Version: ");
            }
        } else if(getItemViewType(position) == ROW_HEADER){
            convertView.findViewById(R.id.adapter_row_custom_permission_header_relative_layout).setVisibility(View.GONE);
        }

        viewHolder.name.setText(permission.getName());
        viewHolder.mySwitch.setChecked(permission.check());
        viewHolder.icon.setImageResource(permission.getIconId());

        // Return the completed view to render on screen
        return convertView;
    }

    @Override
    public int getCount() {
        return dataSet.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Permission getItem(int position) {
        return dataSet.get(position);
    }

    @Override
    public int getViewTypeCount() {
        // TYPE_PERSON and TYPE_DIVIDER
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if(position == 0 || !getItem(position).getGroup().equals(getItem(position-1).getGroup()))
            return ROW_HEADER;
        return ROW_NO_HEADER;

    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }
}
