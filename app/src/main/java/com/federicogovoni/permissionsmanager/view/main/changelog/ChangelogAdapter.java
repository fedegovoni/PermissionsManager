package com.federicogovoni.permissionsmanager.view.main.changelog;

import android.content.Context;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.federicogovoni.permissionmanager.R;

/**
 * Created by Federico on 03/05/2017.
 */

public class ChangelogAdapter extends BaseAdapter {

    private final Context context;
    private final String[][] changelog;


    // COSTRUTTORE CHANGELOG ADAPTER
    public ChangelogAdapter(Context context, int rootArray) {
        // Save the context
        this.context = context;
        // Populate the two-dimensional array
        TypedArray typedArray = this.context.getResources().obtainTypedArray(rootArray);
        changelog = new String[typedArray.length()][];
        for (int i = 0; i < typedArray.length(); i++) {
            int id = typedArray.getResourceId(i, 0);
            if (id > 0) {
                changelog[i] = this.context.getResources().getStringArray(id);
            }
        }
        typedArray.recycle();
    }


    // METODO CHE RESTITUISCE LA LUNGHEZZA DEL CHANGELOG
    @Override
    public int getCount() {
        return changelog.length;
    }


    // METODO CHE RESTITUISCE UN ITEM IN BASE ALLA POSIZIONE
    @Override
    public String[] getItem(int position) {
        return changelog[position];
    }


    // METODO CHE RESTITUISCE L'ITEM ID IN BASE ALLA POSIZIONE
    @Override
    public long getItemId(int position) {
        return 0;
    }


    // METODO CHE RESTITUISCE UNA VIEW
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.dialog_changelog_content, parent, false);
        }

        TextView versionName = (TextView) convertView.findViewById(R.id.dialog_changelog_content_version_name_text_view);
        TextView versionContent = (TextView) convertView.findViewById(R.id.dialog_changelog_content_version_content_text_view);
        String nameStr = changelog[position][0];
        String contentStr = "";

        for (int i = 1; i < changelog[position].length; i++) {
            if (i > 1) {
                contentStr += "\n";
            }
            contentStr += "\u2022 ";
            contentStr += changelog[position][i];
        }

        versionName.setText(nameStr);
        versionContent.setText(contentStr);
        return convertView;
    }
}