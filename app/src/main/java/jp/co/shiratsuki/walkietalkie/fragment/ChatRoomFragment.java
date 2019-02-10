package jp.co.shiratsuki.walkietalkie.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import jp.co.shiratsuki.walkietalkie.R;
import jp.co.shiratsuki.walkietalkie.adapter.ContactAdapter;
import jp.co.shiratsuki.walkietalkie.bean.Contact;
import jp.co.shiratsuki.walkietalkie.widget.RecyclerViewDivider;

/**
 * 聊天室页面
 * Created at 2019/1/21 13:57
 *
 * @author Li Yuliang
 * @version 1.0
 */

public class ChatRoomFragment extends BaseFragment {

    private Context mContext;
    public List<Contact> contactList;
    public ContactAdapter contactAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();
        View view = inflater.inflate(R.layout.fragment_chatroom, container, false);
        RecyclerView rvContacts = view.findViewById(R.id.rvContacts);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvContacts.setLayoutManager(linearLayoutManager);
        rvContacts.addItemDecoration(new RecyclerViewDivider(mContext, LinearLayoutManager.HORIZONTAL, 1, ContextCompat.getColor(mContext, R.color.gray_slight)));
        contactList = new ArrayList<>();
        contactAdapter = new ContactAdapter(mContext, contactList);
        rvContacts.setAdapter(contactAdapter);
        return view;
    }

    @Override
    public void lazyLoad() {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

}
