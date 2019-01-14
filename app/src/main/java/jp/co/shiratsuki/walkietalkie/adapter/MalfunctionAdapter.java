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

import jp.co.shiratsuki.walkietalkie.R;
import jp.co.shiratsuki.walkietalkie.bean.MusicList;
import jp.co.shiratsuki.walkietalkie.bean.WebSocketData;
import jp.co.shiratsuki.walkietalkie.utils.LogUtils;
import jp.co.shiratsuki.walkietalkie.voice.MusicPlay;
import jp.co.shiratsuki.walkietalkie.widget.SwipeItemLayout;

import java.util.List;

/**
 * 故障列表适配器
 * Created at 2018/11/28 13:39
 *
 * @author LiYuliang
 * @version 1.0
 */

public class MalfunctionAdapter extends RecyclerView.Adapter<MalfunctionAdapter.NewsViewHolder> {

    private Context mContext;
    private List<WebSocketData> malfunctionList;
    private OnItemClickListener mListener;

    public MalfunctionAdapter(Context mContext, List<WebSocketData> malfunctionList) {
        this.mContext = mContext;
        this.malfunctionList = malfunctionList;
    }

    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_malfunction, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder viewHolder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            onBindViewHolder(viewHolder, position);
        } else if (payloads.get(0) instanceof Boolean) {
            if ((boolean) payloads.get(0)) {
                viewHolder.ivSpeaker.setVisibility(View.VISIBLE);
            } else {
                viewHolder.ivSpeaker.setVisibility(View.GONE);
            }
        } else if (payloads.get(0) instanceof Integer) {
            LogUtils.d("MainActivity", "Adapter中List的长度：" + malfunctionList.size() + "，不再播放位置：" + payloads.get(0));
            viewHolder.llMain.setBackgroundColor(mContext.getResources().getColor(R.color.gray_6));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder viewHolder, int position) {
        WebSocketData malfunction = malfunctionList.get(position);
        viewHolder.tvMalfunctionType.setText(malfunction.getText());
        viewHolder.tvMalfunctionTime.setText(malfunction.getTime());

        viewHolder.ivBack.setColorFilter(malfunction.getBackColor());
        if (malfunction.isPalying()) {
            viewHolder.ivSpeaker.setVisibility(View.VISIBLE);
        } else {
            viewHolder.ivSpeaker.setVisibility(View.GONE);
        }

        viewHolder.llMain.setBackgroundColor(mContext.getResources().getColor(R.color.gray_7));

        viewHolder.itemView.setOnClickListener((v) -> {
            if (mListener != null) {
                mListener.onItemClick(position);
            }
        });

        //标记已读，不再播放音乐
        viewHolder.tvConfirm.setOnClickListener((view) -> {
            List<MusicList> musicLists = MusicPlay.with(mContext).getMusicListList();
            for (int i = 0; i < musicLists.size(); i++) {
                if (musicLists.get(i).getListNo() == malfunctionList.get(viewHolder.getAdapterPosition()).getListNo()) {
                    musicLists.get(i).setAlreadyPlayCount(musicLists.get(i).getPlayCount());
                    break;
                }
            }
            viewHolder.llMain.setBackgroundColor(mContext.getResources().getColor(R.color.gray_6));
            viewHolder.slRootView.close();
        });

        //删除Item
        viewHolder.tvDelete.setOnClickListener((view) -> {
            List<MusicList> musicLists = MusicPlay.with(mContext).getMusicListList();
            for (int i = 0; i < musicLists.size(); i++) {
                if (musicLists.get(i).getListNo() == malfunctionList.get(viewHolder.getAdapterPosition()).getListNo()) {
                    musicLists.get(i).setAlreadyPlayCount(musicLists.get(i).getPlayCount());
                    break;
                }
            }
            removeData(viewHolder.getAdapterPosition());
        });
    }

    @Override
    public int getItemCount() {
        return malfunctionList.size();
    }

    public class NewsViewHolder extends RecyclerView.ViewHolder {
        private SwipeItemLayout slRootView;
        private LinearLayout llMain;
        private TextView tvMalfunctionType, tvMalfunctionTime, tvConfirm, tvDelete;
        private ImageView ivBack, ivSpeaker;

        private NewsViewHolder(View itemView) {
            super(itemView);
            slRootView = itemView.findViewById(R.id.slRootView);
            llMain = itemView.findViewById(R.id.llMain);
            ivBack = itemView.findViewById(R.id.ivBack);
            ivSpeaker = itemView.findViewById(R.id.ivSpeaker);
            tvMalfunctionType = itemView.findViewById(R.id.tvMalfunctionType);
            tvMalfunctionTime = itemView.findViewById(R.id.tvMalfunctionTime);
            tvConfirm = itemView.findViewById(R.id.tvConfirm);
            tvDelete = itemView.findViewById(R.id.tvDelete);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    /**
     * 删除单条数据
     *
     * @param position
     */
    public void removeData(int position) {
        malfunctionList.remove(position);
        notifyItemRemoved(position);
    }

}
