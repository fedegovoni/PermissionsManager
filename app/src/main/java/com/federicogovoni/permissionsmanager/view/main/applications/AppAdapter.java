package com.federicogovoni.permissionsmanager.view.main.applications;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.federicogovoni.permissionmanager.R;

import java.util.List;

/**
 * Created by Federico on 20/02/2017.
 */

public class AppAdapter extends ArrayAdapter<ApplicationInfo> {

    public AppAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public AppAdapter(Context context, int resource, List<ApplicationInfo> items) {
        super(context, resource, items);
    }

    @NonNull
    @Override
    public View getView(int position, View v, @NonNull ViewGroup parent) {


        if (v == null) {
            LayoutInflater vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.adapter_row_custom_application, parent, false);
        }

        ApplicationInfo p = getItem(position);


        if (p != null) {
            ImageView iv = (ImageView) v.findViewById(R.id.nav_header_main_image_view);
            TextView tt = (TextView) v.findViewById(R.id.adapter_row_custom_application_app_name_text_view);

            if (tt != null) {
                tt.setText(p.loadLabel(getContext().getPackageManager()));
            }

            if (iv != null) {
                iv.setImageDrawable(p.loadIcon(getContext().getPackageManager()));
            }
        }

        return v;
    }

}
