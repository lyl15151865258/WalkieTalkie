package jp.co.shiratsuki.walkietalkie.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import jp.co.shiratsuki.walkietalkie.R;
import jp.co.shiratsuki.walkietalkie.bean.User;
import jp.co.shiratsuki.walkietalkie.constant.NetWork;

/**
 * 房间联系人列表适配器
 * Created at 2019/1/19 13:17
 *
 * @author Li Yuliang
 * @version 1.0
 */

public class ChatRoomContactAdapter extends RecyclerView.Adapter<ChatRoomContactAdapter.ContactViewHolder> {

    private Context mContext;
    private List<User> userList;
    private OnItemClickListener mListener;

    public ChatRoomContactAdapter(Context mContext, List<User> userList) {
        this.mContext = mContext;
        this.userList = userList;
    }

    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_contact_room, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder viewHolder, int position) {
        User user = userList.get(position);
        viewHolder.tvUserName.setText(user.getUser_name());
        viewHolder.tvDepartment.setText(user.getDepartment_name());
        if (user.isSpeaking()) {
            viewHolder.llRoot.setBackgroundResource(R.drawable.border_gridview_green_gray);
        } else {
            viewHolder.llRoot.setBackgroundResource(R.drawable.border_gridview_white_gray);
        }
        String iconUrl = ("http://" + NetWork.SERVER_HOST_MAIN + ":" + NetWork.SERVER_PORT_MAIN + user.getIcon_url()).replace("\\", "/");
        RequestOptions options = new RequestOptions().error(R.drawable.photo_user).placeholder(R.drawable.photo_user).dontAnimate();
        Glide.with(mContext).load(iconUrl).apply(options).into(viewHolder.ivUserIcon);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class ContactViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout llRoot;
        private TextView tvUserName, tvDepartment;
        private ImageView ivUserIcon;

        private ContactViewHolder(View itemView) {
            super(itemView);
            llRoot = itemView.findViewById(R.id.llRoot);
            ivUserIcon = itemView.findViewById(R.id.ivUserIcon);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvDepartment = itemView.findViewById(R.id.tvDepartment);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

}
