package jp.co.shiratsuki.walkietalkie.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import jp.co.shiratsuki.walkietalkie.R;
import jp.co.shiratsuki.walkietalkie.bean.WebSocketData;

import java.util.List;

/**
 * 故障列表适配器
 * Created at 2018/11/28 13:39
 *
 * @author LiYuliang
 * @version 1.0
 */

public class MalfunctionAdapter extends RecyclerView.Adapter {

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
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        NewsViewHolder holder = (NewsViewHolder) viewHolder;
        WebSocketData malfunction = malfunctionList.get(position);
        holder.tvMalfunctionType.setText(malfunction.getText());
        holder.tvMalfunctionTime.setText(malfunction.getTime());

        holder.ivBack.setColorFilter(malfunction.getBackColor());
        if (malfunction.isPalying()) {
            holder.ivSpeaker.setVisibility(View.VISIBLE);
        } else {
            holder.ivSpeaker.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener((v) -> {
            if (mListener != null) {
                mListener.onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return malfunctionList.size();
    }

    private class NewsViewHolder extends RecyclerView.ViewHolder {
        private TextView tvMalfunctionType, tvMalfunctionTime;
        private ImageView ivBack, ivSpeaker;

        private NewsViewHolder(View itemView) {
            super(itemView);
            ivBack = itemView.findViewById(R.id.ivBack);
            ivSpeaker = itemView.findViewById(R.id.ivSpeaker);
            tvMalfunctionType = itemView.findViewById(R.id.tvMalfunctionType);
            tvMalfunctionTime = itemView.findViewById(R.id.tvMalfunctionTime);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

}
