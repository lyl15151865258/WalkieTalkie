package jp.co.shiratsuki.walkietalkie.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.co.shiratsuki.walkietalkie.R;
import jp.co.shiratsuki.walkietalkie.activity.MainActivity;
import jp.co.shiratsuki.walkietalkie.adapter.ContactAdapter;
import jp.co.shiratsuki.walkietalkie.bean.User;
import jp.co.shiratsuki.walkietalkie.contentprovider.SPHelper;
import jp.co.shiratsuki.walkietalkie.utils.GsonUtils;
import jp.co.shiratsuki.walkietalkie.utils.LogUtils;

/**
 * 联系人页面
 * Created at 2019/1/21 13:57
 *
 * @author Li Yuliang
 * @version 1.0
 */

public class ContactsFragment extends BaseFragment {

    private String TAG = "ContactsFragment";
    private Context mContext;
    private MainActivity mainActivity;
    private List<User> userList;
    private List<String> departmentList;
    private List<List<User>> departmentUserList;
    private ContactAdapter contactAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();
        mainActivity = (MainActivity) getActivity();
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        ExpandableListView rvContacts = view.findViewById(R.id.elvContacts);
        rvContacts.setGroupIndicator(null);
        userList = new ArrayList<>();
        departmentList = new ArrayList<>();
        departmentUserList = new ArrayList<>();
        contactAdapter = new ContactAdapter(mContext, rvContacts, departmentList, departmentUserList);
        rvContacts.setAdapter(contactAdapter);
        rvContacts.setOnChildClickListener(onChildClickListener);
        return view;
    }

    @Override
    public void lazyLoad() {

    }

    private ExpandableListView.OnChildClickListener onChildClickListener = (parent, view, groupPosition, childPosition, id) -> {
        String userId = departmentUserList.get(groupPosition).get(childPosition).getUser_id();
        User user = GsonUtils.parseJSON(SPHelper.getString("User", GsonUtils.convertJSON(new User())), User.class);
        if (!user.getUser_id().equals(userId)) {
            mainActivity.callOthers(userId);
        }
        return true;
    };

    /**
     * 刷新所有联系人列表
     *
     * @param userList 房间内联系人列表
     */
    public void refreshList(List<User> userList) {
        LogUtils.d(TAG, "刷新联系人列表");
        this.userList.clear();
        this.userList.addAll(userList);

        generateList();
        contactAdapter.notifyDataSetChanged();
    }

    /**
     * 移除用户
     *
     * @param userId 用户ID
     */
    public void removeUser(String userId) {
        LogUtils.d(TAG, "移除用户：" + userId);
        int position = -1;
        for (int i = 0; i < userList.size(); i++) {
            if (userList.get(i).getUser_id().equals(userId)) {
                position = i;
                break;
            }
        }
        if (position != -1) {
            userList.remove(position);
            generateList();
            contactAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 清空所有联系人
     */
    public void clearUserList() {
        LogUtils.d(TAG, "清空联系人列表");
        userList.clear();
        generateList();
        contactAdapter.notifyDataSetChanged();
    }

    private void generateList() {
        departmentList.clear();
        departmentUserList.clear();
        HashMap<String, List<User>> map = new HashMap<>();
        for (int i = 0; i < userList.size(); i++) {
            String departmentName = userList.get(i).getDepartment_name();
            if (!departmentList.contains(departmentName)) {
                departmentList.add(departmentName);
            }
            if (map.containsKey(departmentName)) {
                List<User> users = map.get(departmentName);
                if (users != null) {
                    users.add(userList.get(i));
                } else {
                    users = new ArrayList<>();
                    users.add(userList.get(i));
                }
                map.put(departmentName, users);
            } else {
                List<User> users = new ArrayList<>();
                users.add(userList.get(i));
                map.put(departmentName, users);
            }
        }
        for (Map.Entry<String, List<User>> entry : map.entrySet()) {
            departmentUserList.add(entry.getValue());
        }
    }

}
