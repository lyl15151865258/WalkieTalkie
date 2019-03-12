package jp.co.shiratsuki.walkietalkie.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.LocaleList;
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
import jp.co.shiratsuki.walkietalkie.utils.LanguageUtil;
import jp.co.shiratsuki.walkietalkie.utils.LogUtils;
import jp.co.shiratsuki.walkietalkie.message.MusicPlay;
import jp.co.shiratsuki.walkietalkie.widget.SwipeItemLayout;

import java.util.List;
import java.util.Locale;

/**
 * 故障列表适配器
 * Created at 2018/11/28 13:39
 *
 * @author LiYuliang
 * @version 1.0
 */

public class MalfunctionAdapter extends RecyclerView.Adapter<MalfunctionAdapter.MalfunctionViewHolder> {

    private static final String TAG = "MalfunctionAdapter";
    private Context mContext;
    private List<WebSocketData> malfunctionList;
    private OnItemClickListener mListener;

    public MalfunctionAdapter(Context mContext, List<WebSocketData> malfunctionList) {
        this.mContext = mContext;
        this.malfunctionList = malfunctionList;
    }

    @Override
    public MalfunctionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_malfunction_swipe, parent, false);
        return new MalfunctionViewHolder(view);
    }

//    @Override
//    public void onBindViewHolder(@NonNull MalfunctionViewHolder viewHolder, int position, @NonNull List<Object> payloads) {
//        if (payloads.isEmpty()) {
//            onBindViewHolder(viewHolder, position);
//        } else if (payloads.get(0) instanceof Boolean) {
//            if ((boolean) payloads.get(0)) {
//                viewHolder.ivSpeaker.setVisibility(View.VISIBLE);
//            } else {
//                viewHolder.ivSpeaker.setVisibility(View.GONE);
//            }
//        } else if (payloads.get(0) instanceof Integer) {
//            LogUtils.d("MainActivity", "Adapter中List的长度：" + malfunctionList.size() + "，不再播放位置：" + payloads.get(0));
//            viewHolder.llMain.setBackgroundColor(mContext.getResources().getColor(R.color.gray_6));
//        }
//    }

    @Override
    public void onBindViewHolder(@NonNull MalfunctionViewHolder viewHolder, int position) {
        WebSocketData malfunction = malfunctionList.get(position);

        String text = "";
        switch (LanguageUtil.getLanguageLocal(mContext)) {
            case "":
                // 手机设置的语言是跟随系统
                Locale locale;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    locale = LocaleList.getDefault().get(0);
                } else {
                    locale = Locale.getDefault();
                }
                String language = locale.getLanguage();
                switch (language) {
                    case "zh":
                        text = malfunction.getText_Chinese();
                        break;
                    case "ja":
                        text = malfunction.getText_Japanese();
                        break;
                    default:
                        text = malfunction.getText_English();
                        break;
                }
                break;
            case "zh":
                text = malfunction.getText_Chinese();
                break;
            case "ja":
                text = malfunction.getText_Japanese();
                break;
            case "en":
                text = malfunction.getText_English();
                break;
            default:
                break;
        }

        viewHolder.tvMalfunctionType.setText(text);
        viewHolder.tvMalfunctionTime.setText(malfunction.getTime());

        viewHolder.ivBack.setColorFilter(malfunction.getBackColor());
        if (malfunction.isPlaying()) {
            viewHolder.ivSpeaker.setVisibility(View.VISIBLE);
        } else {
            viewHolder.ivSpeaker.setVisibility(View.GONE);
        }

        // 根据播放次数修改背景，0表示不播放，直接改为灰色底
        if (malfunction.getPlayCount() == 0) {
            LogUtils.d(TAG,"播放次数为0，直接为灰色底");
            viewHolder.tvConfirm.setVisibility(View.GONE);
            viewHolder.llMain.setBackgroundColor(mContext.getResources().getColor(R.color.gray_1));
        } else {
            // 根据是否停止播放修改背景
            if (malfunction.isFinishPlay()) {
                LogUtils.d(TAG,"播放次数不为0，但是已经播放结束，灰色底");
                viewHolder.tvConfirm.setVisibility(View.GONE);
                viewHolder.llMain.setBackgroundColor(mContext.getResources().getColor(R.color.gray_1));
            } else {
                LogUtils.d(TAG,"播放次数不为0，但是正在播放，白色底");
                viewHolder.tvConfirm.setVisibility(View.VISIBLE);
                viewHolder.llMain.setBackgroundColor(mContext.getResources().getColor(R.color.gray_7));
            }
        }

        viewHolder.itemView.setOnClickListener((v) -> {
            if (mListener != null) {
                mListener.onItemClick(position);
            }
        });

        //标记已读，不再播放音乐
        viewHolder.tvConfirm.setOnClickListener((view) -> {
            List<MusicList> musicLists = MusicPlay.with(mContext.getApplicationContext()).getMusicListList();
            for (int i = 0; i < musicLists.size(); i++) {
                if (musicLists.get(i).getListNo() == malfunctionList.get(viewHolder.getAdapterPosition()).getListNo()) {
                    musicLists.get(i).setAlreadyPlayCount(musicLists.get(i).getPlayCount());
                    malfunctionList.get(viewHolder.getAdapterPosition()).setFinishPlay(true);
                    break;
                }
            }
            viewHolder.llMain.setBackgroundColor(mContext.getResources().getColor(R.color.gray_1));
            viewHolder.tvConfirm.setVisibility(View.GONE);
            viewHolder.slRootView.close();
            notifyItemChanged(position);
        });

        //删除Item
        viewHolder.tvDelete.setOnClickListener((view) -> {
            List<MusicList> musicLists = MusicPlay.with(mContext.getApplicationContext()).getMusicListList();
            for (int i = 0; i < musicLists.size(); i++) {
                if (musicLists.get(i).getListNo() == malfunctionList.get(viewHolder.getAdapterPosition()).getListNo()) {
                    musicLists.get(i).setAlreadyPlayCount(musicLists.get(i).getPlayCount());
                    break;
                }
            }

            Intent intent = new Intent();
            intent.setAction("USER_DELETE_MALFUNCTION");
            intent.putExtra("ListNo", malfunctionList.get(viewHolder.getAdapterPosition()).getListNo());
            mContext.sendBroadcast(intent);

            removeData(viewHolder.getAdapterPosition());
        });
    }

    @Override
    public int getItemCount() {
        return malfunctionList.size();
    }

    public class MalfunctionViewHolder extends RecyclerView.ViewHolder {
        private SwipeItemLayout slRootView;
        private LinearLayout llMain;
        private TextView tvMalfunctionType, tvMalfunctionTime, tvConfirm, tvDelete;
        private ImageView ivBack, ivSpeaker;

        private MalfunctionViewHolder(View itemView) {
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
