package jp.co.shiratsuki.walkietalkie.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import jp.co.shiratsuki.walkietalkie.R;
import jp.co.shiratsuki.walkietalkie.activity.MainActivity;
import jp.co.shiratsuki.walkietalkie.adapter.ChatRoomContactAdapter;
import jp.co.shiratsuki.walkietalkie.bean.User;
import jp.co.shiratsuki.walkietalkie.utils.LogUtils;

/**
 * 聊天室页面
 * Created at 2019/1/21 13:57
 *
 * @author Li Yuliang
 * @version 1.0
 */

public class ChatRoomFragment extends BaseFragment {

    private String TAG = "ChatRoomFragment";
    private Context mContext;
    private List<User> userList;
    private ChatRoomContactAdapter chatRoomContactAdapter;
    private boolean sIsScrolling = false;
    private EditText etRoomId;
    private Button btnEnterExitRoom;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();
        View view = inflater.inflate(R.layout.fragment_contacts_chatroom, container, false);
        RecyclerView rvContacts = view.findViewById(R.id.rvContacts);
        //纵向线性布局
        GridLayoutManager layoutManager = new GridLayoutManager(mContext, 3);
        rvContacts.setLayoutManager(layoutManager);
        userList = new ArrayList<>();
        chatRoomContactAdapter = new ChatRoomContactAdapter(mContext, userList);
        chatRoomContactAdapter.setOnItemClickListener(onItemClickListener);
        rvContacts.setAdapter(chatRoomContactAdapter);
        rvContacts.addOnScrollListener(onScrollListener);
        etRoomId = view.findViewById(R.id.etRoomId);
        btnEnterExitRoom = view.findViewById(R.id.btnEnterExitRoom);
        btnEnterExitRoom.setOnClickListener(onClickListener);
        return view;
    }

    private View.OnClickListener onClickListener = (v) -> {
        switch (v.getId()) {
            case R.id.btnEnterExitRoom:
                FragmentActivity activity = getActivity();
                if (activity instanceof MainActivity) {
                    MainActivity mainActivity = (MainActivity) activity;
                    mainActivity.clickEnterExitBtn();
                }
                break;
            default:
                break;
        }
    };

    private ChatRoomContactAdapter.OnItemClickListener onItemClickListener = (position) -> {

    };

    private RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            // 先判断mContext是否为空，防止Activity已经onDestroy导致的java.lang.IllegalArgumentException: You cannot start a load for a destroyed activity
            if (mContext != null) {
                // 如果快速滑动，停止Glide的加载，停止滑动后恢复加载
                if (newState == RecyclerView.SCROLL_STATE_SETTLING) {
                    sIsScrolling = true;
                    Glide.with(mContext).pauseRequests();
                } else if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (sIsScrolling) {
                        Glide.with(mContext).resumeRequests();
                    }
                    sIsScrolling = false;
                }
            }
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
        }
    };

    @Override
    public void lazyLoad() {

    }

    /**
     * 刷新房间内联系人列表
     *
     * @param userList 房间内联系人列表
     */
    public void refreshList(List<User> userList) {
        LogUtils.d(TAG, "刷新联系人列表");
        this.userList.clear();
        this.userList.addAll(userList);
        chatRoomContactAdapter.notifyDataSetChanged();
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
            chatRoomContactAdapter.notifyItemRemoved(position);
        }
    }

    /**
     * 清空房间联系人
     */
    public void clearUserList() {
        userList.clear();
        LogUtils.d(TAG, "走了这里，清空房间联系人，联系人数量：" + userList.size());
        chatRoomContactAdapter.notifyDataSetChanged();
    }

    /**
     * 加入了房间后
     */
    public void enterRoom() {
        btnEnterExitRoom.setText(getString(R.string.releaseToExitChat));
        etRoomId.setFocusable(false);
        etRoomId.setFocusableInTouchMode(false);
    }

    /**
     * 离开了房间后
     */
    public void exitRoom() {
        btnEnterExitRoom.setText(getString(R.string.pressToJoinChat));
        etRoomId.setFocusable(true);
        etRoomId.setFocusableInTouchMode(true);
    }

}
