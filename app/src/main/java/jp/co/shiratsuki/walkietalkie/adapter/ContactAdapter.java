package jp.co.shiratsuki.walkietalkie.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import jp.co.shiratsuki.walkietalkie.R;
import jp.co.shiratsuki.walkietalkie.bean.Contact;

/**
 * 联系人列表适配器
 * Created at 2019/1/19 13:17
 *
 * @author Li Yuliang
 * @version 1.0
 */

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {

    private Context mContext;
    private List<Contact> contactList;
    private OnItemClickListener mListener;

    public ContactAdapter(Context mContext, List<Contact> contactList) {
        this.mContext = mContext;
        this.contactList = contactList;
    }

    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder viewHolder, int position) {
        Contact con = contactList.get(position);
        viewHolder.tvUserIP.setText(con.getUserIP());
        viewHolder.tvUserName.setText(con.getUserName());
        if (con.isSpeaking()) {
            viewHolder.ivSpeaking.setVisibility(View.VISIBLE);
        } else {
            viewHolder.ivSpeaking.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    public class ContactViewHolder extends RecyclerView.ViewHolder {
        private TextView tvUserName, tvUserIP;
        private ImageView ivUserIcon, ivSpeaking;

        private ContactViewHolder(View itemView) {
            super(itemView);
            ivUserIcon = itemView.findViewById(R.id.ivUserIcon);
            ivSpeaking = itemView.findViewById(R.id.ivSpeaking);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserIP = itemView.findViewById(R.id.tvUserIP);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

}
