package jp.co.shiratsuki.walkietalkie.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import jp.co.shiratsuki.walkietalkie.R;
import jp.co.shiratsuki.walkietalkie.bean.User;
import jp.co.shiratsuki.walkietalkie.constant.NetWork;

/**
 * 联系人列表适配器
 * Created at 2019/1/19 13:17
 *
 * @author Li Yuliang
 * @version 1.0
 */

public class ContactAdapter extends BaseExpandableListAdapter {

    private Context mContext;
    private ExpandableListView expandableListView;
    private List<String> departmentList;
    private List<List<User>> userList;

    public ContactAdapter(Context mContext, ExpandableListView expandableListView, List<String> departmentList, List<List<User>> userList) {
        this.mContext = mContext;
        this.expandableListView = expandableListView;
        this.departmentList = departmentList;
        this.userList = userList;
    }

    @Override
    public int getGroupCount() {
        return departmentList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return userList.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return departmentList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return userList.get(groupPosition).get(childPosition);
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
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        DepartmentViewHolder departmentViewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_list_contact_group, parent, false);
            departmentViewHolder = new DepartmentViewHolder(convertView);
            convertView.setTag(departmentViewHolder);
        } else {
            departmentViewHolder = (DepartmentViewHolder) convertView.getTag();
        }
        String departmentName = departmentList.get(groupPosition);
        departmentViewHolder.tvDepartment.setText(departmentName);

        if (isExpanded) {
            departmentViewHolder.ivExpand.setImageResource(R.drawable.ic_arrow_down);
        } else {
            departmentViewHolder.ivExpand.setImageResource(R.drawable.ic_arrow_right);
        }
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ContactViewHolder contactViewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_list_contact, parent, false);
            contactViewHolder = new ContactViewHolder(convertView);
            convertView.setTag(contactViewHolder);
        } else {
            contactViewHolder = (ContactViewHolder) convertView.getTag();
        }
        User user = userList.get(groupPosition).get(childPosition);
        contactViewHolder.tvUserInfo.setText(user.getUser_name());
        contactViewHolder.tvMessage.setText("");
        String iconUrl = ("http://" + NetWork.SERVER_HOST_MAIN + ":" + NetWork.SERVER_PORT_MAIN + user.getIcon_url()).replace("\\", "/");
        RequestOptions options = new RequestOptions().error(R.drawable.photo_user).placeholder(R.drawable.photo_user).dontAnimate();
        Glide.with(mContext).load(iconUrl).apply(options).into(contactViewHolder.ivUserIcon);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public boolean isEmpty() {
        return departmentList == null || departmentList.size() == 0;
    }

    private class DepartmentViewHolder {
        private TextView tvDepartment;
        private ImageView ivExpand;

        private DepartmentViewHolder(View itemView) {
            tvDepartment = itemView.findViewById(R.id.tvDepartment);
            ivExpand = itemView.findViewById(R.id.ivExpand);
        }
    }

    private class ContactViewHolder {
        private TextView tvUserInfo, tvMessage;
        private ImageView ivUserIcon;

        private ContactViewHolder(View itemView) {
            ivUserIcon = itemView.findViewById(R.id.ivUserIcon);
            tvUserInfo = itemView.findViewById(R.id.tvUserInfo);
            tvMessage = itemView.findViewById(R.id.tvMessage);
        }
    }

    @Override
    public void notifyDataSetChanged() {
        expandableListView.setVisibility(View.GONE);
        super.notifyDataSetChanged();
        for (int i = 0; i < getGroupCount(); i++) {
            expandableListView.collapseGroup(i);
            expandableListView.expandGroup(i);
        }
        expandableListView.setVisibility(View.VISIBLE);
    }
}
