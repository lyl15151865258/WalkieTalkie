package jp.co.shiratsuki.walkietalkie.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import jp.co.shiratsuki.walkietalkie.R;
import jp.co.shiratsuki.walkietalkie.adapter.MalfunctionAdapter;
import jp.co.shiratsuki.walkietalkie.bean.WebSocketData;
import jp.co.shiratsuki.walkietalkie.utils.LogUtils;
import jp.co.shiratsuki.walkietalkie.widget.RecyclerViewDivider;
import jp.co.shiratsuki.walkietalkie.widget.SwipeItemLayout;

/**
 * 异常信息页面
 * Created at 2019/1/21 14:14
 *
 * @author Li Yuliang
 * @version 1.0
 */

public class MalfunctionFragment extends BaseFragment {

    private String TAG = "MalfunctionFragment";
    private Context mContext;
    private RecyclerView rvMalfunction;
    private List<WebSocketData> malfunctionList;
    private MalfunctionAdapter malfunctionAdapter;
    private boolean sIsScrolling = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();
        View view = inflater.inflate(R.layout.fragment_malfunction, container, false);
        rvMalfunction = view.findViewById(R.id.rvMalfunction);
        LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(mContext);
        linearLayoutManager1.setOrientation(LinearLayoutManager.VERTICAL);
        rvMalfunction.setLayoutManager(linearLayoutManager1);
        rvMalfunction.addItemDecoration(new RecyclerViewDivider(mContext, LinearLayoutManager.HORIZONTAL, 1, ContextCompat.getColor(mContext, R.color.gray_slight)));

        rvMalfunction.addOnItemTouchListener(new SwipeItemLayout.OnSwipeItemTouchListener(mContext));

        malfunctionList = new ArrayList<>();
        malfunctionAdapter = new MalfunctionAdapter(mContext, malfunctionList);
        malfunctionAdapter.setOnItemClickListener(onItemClickListener);
        rvMalfunction.setAdapter(malfunctionAdapter);
        rvMalfunction.addOnScrollListener(onScrollListener);

        return view;
    }

    @Override
    public void lazyLoad() {

    }

    private MalfunctionAdapter.OnItemClickListener onItemClickListener = (position) -> {

    };

    private RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
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
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
        }
    };

    /**
     * 刷新某一条异常信息
     *
     * @param listNo 异常信息的序号
     */
    public void refreshMalfunction(int listNo, boolean playing, boolean noLongerPlay) {
        int position = -1;
        for (int i = 0; i < malfunctionList.size(); i++) {
            if (listNo == malfunctionList.get(i).getListNo()) {
                position = i;
                malfunctionList.get(i).setPlaying(playing);
                LogUtils.d(TAG, "不再播放：" + listNo);
                malfunctionList.get(i).setFinishPlay(noLongerPlay);
                break;
            }
        }
        if (position != -1) {
            int newPosition = malfunctionList.size() - 1;
            // 更改这一条异常信息的背景
            malfunctionAdapter.notifyItemChanged(position);
            // 将这一条异常信息移动至列表末尾
            malfunctionAdapter.notifyItemMoved(position, newPosition);
            // 更新List数据
            malfunctionList.add(malfunctionList.get(position));
            malfunctionList.remove(position);
            // 重新绑定部分数据
            if (position != newPosition) {
                malfunctionAdapter.notifyItemRangeChanged(Math.min(position, newPosition), Math.abs(position - newPosition) + 1);
            }
        }
    }

    /**
     * 改变某一条异常信息的播放状态
     *
     * @param listNo 异常信息的序号
     */
    public void setCurrentPlaying(int listNo) {
        if (listNo == -1) {
            for (int i = 0; i < malfunctionList.size(); i++) {
                if (malfunctionList.get(i).isPlaying()) {
                    malfunctionList.get(i).setPlaying(false);
                    malfunctionAdapter.notifyItemChanged(i, false);
                    break;
                }
            }
        } else {
            for (int i = 0; i < malfunctionList.size(); i++) {
                if (listNo == malfunctionList.get(i).getListNo()) {
                    if (!malfunctionList.get(i).isPlaying()) {
                        malfunctionList.get(i).setPlaying(true);
                        malfunctionAdapter.notifyItemChanged(i, true);
                        rvMalfunction.smoothScrollToPosition(i);
                    }
                } else {
                    if (malfunctionList.get(i).isPlaying()) {
                        malfunctionList.get(i).setPlaying(false);
                        malfunctionAdapter.notifyItemChanged(i, false);
                    }
                }
            }
        }
    }

    /**
     * 刷新异常信息列表
     *
     * @param malfunctionList 异常信息列表
     */
    public void refreshList(List<WebSocketData> malfunctionList) {
        LogUtils.d(TAG, "刷新异常信息列表");
        this.malfunctionList.clear();
        this.malfunctionList.addAll(malfunctionList);
        malfunctionAdapter.notifyDataSetChanged();
    }

    /**
     * 刷新异常信息列表（用于切换语言后重新加载语言）
     */
    public void refreshList() {
        LogUtils.d(TAG, "走了更新语言的方法，刷新异常信息列表的语言");
        malfunctionAdapter.notifyDataSetChanged();
    }

    /**
     * 收到异常
     *
     * @param webSocketData 异常信息实体
     */
    public void receiveMalfunction(WebSocketData webSocketData) {
        // 判断状态，是添加新的异常还是取消已有的异常
        if (webSocketData.isStatus()) {
            // 检查是否包含了这个ListNo，如果不包含这条异常信息
            if (!malfunctionList.contains(webSocketData)) {
                // 检查优先级进行插入，默认认为优先级最低（插入位置是列表第一个播放完毕的位置），然后循环比较优先级
                int position = -1;
                for (int i = 0; i < malfunctionList.size(); i++) {
                    // 与未播放完毕的条目进行比较，播放完毕的就不用比较了
                    LogUtils.d(TAG, "!malfunctionList.get(i).isFinishPlay()：" + !malfunctionList.get(i).isFinishPlay());
                    LogUtils.d(TAG, "webSocketData.getPriority() <= malfunctionList.get(i).getPriority()：" + (webSocketData.getPriority() <= malfunctionList.get(i).getPriority()));
                    if (!malfunctionList.get(i).isFinishPlay() && webSocketData.getPriority() <= malfunctionList.get(i).getPriority()) {
                        position = i;
                        break;
                    }
                    // 找到第一条播放完毕的位置
                    if (malfunctionList.get(i).isFinishPlay() && position == -1) {
                        position = i;
                    }
                }

                if (position != -1) {
                    // 如果优先级比某一条未播放完毕的异常高，则插入在这条未播放完毕的异常的位置
                    malfunctionList.add(position, webSocketData);
                    malfunctionAdapter.notifyItemInserted(position);
                } else {
                    // 如果当前一条异常信息都没有，则直接插入在最一条的位置
                    position = malfunctionList.size();
                    malfunctionList.add(position, webSocketData);
                    malfunctionAdapter.notifyItemInserted(position);
                }
            }
        } else {
            int position = -1;
            for (int i = 0; i < malfunctionList.size(); i++) {
                if (malfunctionList.get(i).getListNo() == webSocketData.getListNo()) {
                    position = i;
                    break;
                }
            }
            if (position != -1) {
                malfunctionList.remove(position);
                malfunctionAdapter.notifyItemRemoved(position);
            }
        }
    }

    /**
     * 清空异常信息
     */
    public void clearMalfunctionList() {
        LogUtils.d(TAG, "清空异常信息列表");
        malfunctionList.clear();
        malfunctionAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

}
