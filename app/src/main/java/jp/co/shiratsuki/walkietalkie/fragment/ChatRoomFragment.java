package jp.co.shiratsuki.walkietalkie.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import jp.co.shiratsuki.walkietalkie.R;
import jp.co.shiratsuki.walkietalkie.adapter.ChatRoomContactAdapter;
import jp.co.shiratsuki.walkietalkie.bean.User;

/**
 * 聊天室页面
 * Created at 2019/1/21 13:57
 *
 * @author Li Yuliang
 * @version 1.0
 */

public class ChatRoomFragment extends BaseFragment {

    private Context mContext;
    public List<User> userList;
    public ChatRoomContactAdapter chatRoomContactAdapter;
    private boolean sIsScrolling = false;

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
        return view;
    }

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

}
