package com.federicogovoni.permissionsmanager.view.main.contexts.newcontext;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.federicogovoni.permissionsmanager.model.Permission;
import com.federicogovoni.permissionmanager.R;
import com.federicogovoni.permissionsmanager.utils.TmpContextKeeper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Federico on 10/03/2017.
 */

public class AppPermissionsExpandableListAdapter extends BaseExpandableListAdapter {

    private Context mContext;
    private List<ApplicationInfo> mListDataHeader;

    private Map<ApplicationInfo, List<Permission>> mMapDataChild;

    private List<Permission> mGrantList;
    private List<Permission> mRevokeList;

    private ChildViewHolder mChildViewHolder;
    private GroupViewHolder mGroupViewHolder;

    private final class GroupViewHolder {
        ImageView mIcon;
        TextView mLabel;
    }

    private static class ChildViewHolder {
        ImageView mIcon;
        TextView mLabel;
        CheckBox mCheckBox;
    }

    public AppPermissionsExpandableListAdapter(Context context, List<ApplicationInfo> listDataHeader, Map<ApplicationInfo, List<Permission>> mapDataChild) {
        mContext = context;
        mListDataHeader = listDataHeader;
        mMapDataChild = mapDataChild;
        mGrantList = new ArrayList<>();
        mRevokeList = new ArrayList<>();
        mGrantList.addAll(TmpContextKeeper.getInstance().getCurrentContext().getGrantList());
        mRevokeList.addAll(TmpContextKeeper.getInstance().getCurrentContext().getRevokeList());
    }

    @Override
    public int getGroupCount() {
        return mListDataHeader.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mMapDataChild.get(mListDataHeader.get(groupPosition)).size();
    }

    @Override
    public ApplicationInfo getGroup(int groupPosition) {
        return mListDataHeader.get(groupPosition);
    }

    @Override
    public Permission getChild(int groupPosition, int childPosition) {
        try {
            return mMapDataChild.get(mListDataHeader.get(groupPosition)).get(childPosition);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        String appName = getGroup(groupPosition).loadLabel(mContext.getPackageManager()).toString();
        Drawable appIcon = getGroup(groupPosition).loadIcon(mContext.getPackageManager());

        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.adapter_expandable_list_header, null);
            mGroupViewHolder = new GroupViewHolder();
            mGroupViewHolder.mIcon = (ImageView) convertView.findViewById(R.id.adapter_expandable_list_header_app_logo_image_view);
            mGroupViewHolder.mLabel = (TextView) convertView.findViewById(R.id.adapter_expandable_list_header_label_text_view);
            convertView.setTag(mGroupViewHolder);
        } else {
            mGroupViewHolder = (GroupViewHolder) convertView.getTag();
        }

        mGroupViewHolder.mLabel.setText(appName);
        mGroupViewHolder.mIcon.setImageDrawable(appIcon);

        return convertView;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        String permissionName = getChild(groupPosition, childPosition).getName();
        int permissionIcon = getChild(groupPosition, childPosition).getIconId();

        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.adapter_expandable_list_item, null);

            mChildViewHolder = new ChildViewHolder();
            mChildViewHolder.mIcon = (ImageView) convertView.findViewById(R.id.adapter_expandable_list_view_item_permission_logo_image_view);
            mChildViewHolder.mLabel = (TextView) convertView.findViewById(R.id.adapter_expandable_list_view_item_permission_name_text_view);
            mChildViewHolder.mCheckBox = (CheckBox) convertView.findViewById(R.id.adapter_expandable_list_view_item_selection_check_box);

            convertView.setTag(mChildViewHolder);
        } else {
            mChildViewHolder = (ChildViewHolder) convertView.getTag();
        }

        mChildViewHolder.mLabel.setText(permissionName);
        mChildViewHolder.mIcon.setImageResource(permissionIcon);
        mChildViewHolder.mCheckBox.setOnCheckedChangeListener(null);

        final int currentPhase = TmpContextKeeper.getInstance().getPhase();

        if(getChild(groupPosition, childPosition) == null)
            Log.e("CHILD", "è NULL");

        if(getChild(groupPosition, childPosition) != null &&
                (mGrantList.contains(getChild(groupPosition, childPosition)) ||
                        mRevokeList.contains(getChild(groupPosition, childPosition)))) {
            mChildViewHolder.mCheckBox.setChecked(true);
            Log.e("CHILD VIEW", "Carico gruppo " + groupPosition + " child " + childPosition + " checked_state = true");
        } else {
            mChildViewHolder.mCheckBox.setChecked(false);
        }

        if(getChild(groupPosition, childPosition) != null &&((mGrantList.contains(getChild(groupPosition, childPosition)) && currentPhase == TmpContextKeeper.REVOKE_PHASE) ||
                (mRevokeList.contains(getChild(groupPosition, childPosition)) && currentPhase == TmpContextKeeper.GRANT_PHASE))) {
            mChildViewHolder.mCheckBox.setEnabled(false);
            Log.e("CHILD VIEW", "Carico gruppo " + groupPosition + " child " + childPosition + " enabled_state = false");
        } else {
            mChildViewHolder.mCheckBox.setEnabled(true);
        }

        mChildViewHolder.mCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("CLICK", "Group " + groupPosition + " Child " + childPosition);
                if(getChild(groupPosition, childPosition) != null && mGrantList.contains(getChild(groupPosition, childPosition))) {
                    mGrantList.remove(getChild(groupPosition, childPosition));
                    Log.e("RIMOZIONE", "Group " + groupPosition + " Child " + childPosition + " rimosso da GrantList");
                }
                else if (getChild(groupPosition, childPosition) != null && mRevokeList.contains(getChild(groupPosition, childPosition))) {
                    mRevokeList.remove(getChild(groupPosition, childPosition));
                    Log.e("RIMOZIONE", "Group " + groupPosition + " Child " + childPosition + " rimosso da RevokeList");
                }
                else if (getChild(groupPosition, childPosition) != null && currentPhase == TmpContextKeeper.GRANT_PHASE) {
                    mGrantList.add(getChild(groupPosition, childPosition));
                    Log.e("RIMOZIONE", "Group " + groupPosition + " Child " + childPosition + " aggiunto a GrantList");
                }
                else if(getChild(groupPosition, childPosition) != null && currentPhase == TmpContextKeeper.REVOKE_PHASE) {
                    mRevokeList.add(getChild(groupPosition, childPosition));
                    Log.e("RIMOZIONE", "Group " + groupPosition + " Child " + childPosition + " aggiunto a RevokeList");
                }
                else
                    Log.e("ERROR", "Qualcosa è null");
            }
        });

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    public List<Permission> getRevokeSelectedPermissions() {
        List<Permission> res = new ArrayList<>();
        res.addAll(mRevokeList);
        return res;
    }

    public List<Permission> getGrantSelectedPermission() {
        List<Permission> res = new ArrayList<>();
        res.addAll(mGrantList);
        return res;
    }

    public List<ApplicationInfo> getGroups() {
        return mListDataHeader;
    }
}

