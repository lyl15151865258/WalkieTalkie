package jp.co.shiratsuki.walkietalkie.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import jp.co.shiratsuki.walkietalkie.R;
import jp.co.shiratsuki.walkietalkie.activity.version.VersionsActivity;
import jp.co.shiratsuki.walkietalkie.bean.version.Version;
import jp.co.shiratsuki.walkietalkie.utils.ApkUtils;

/**
 * 软件版本更新日志的适配器
 * Created at 2019/3/1 12:37
 *
 * @author Li Yuliang
 * @version 1.0
 */

public class VersionLogAdapter extends RecyclerView.Adapter {

    private ButtonInterface buttonInterface;
    private VersionsActivity versionsActivity;
    private List<Version> list;

    public VersionLogAdapter(VersionsActivity versionsActivity, List<Version> lv) {
        this.versionsActivity = versionsActivity;
        list = lv;
    }

    @Override
    @NotNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NotNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_list_version, viewGroup, false);
        VersionLogViewHolder versionLogViewHolder = new VersionLogViewHolder(view);
        versionLogViewHolder.tvVersionName = view.findViewById(R.id.tv_versionName);
        versionLogViewHolder.tvVersionTime = view.findViewById(R.id.tv_versionTime);
        versionLogViewHolder.tvVersionLog = view.findViewById(R.id.tv_versionLog);
        versionLogViewHolder.btnDownloadApk = view.findViewById(R.id.btn_downloadApk);
        return versionLogViewHolder;
    }

    @Override
    public void onBindViewHolder(@NotNull RecyclerView.ViewHolder viewHolder, int position) {
        VersionLogViewHolder holder = (VersionLogViewHolder) viewHolder;
        Version version = list.get(position);
        holder.tvVersionName.setText(version.getVersionName());
        holder.tvVersionTime.setText(version.getCreateTime());
        holder.tvVersionLog.setText(version.getVersionLog());
        if (version.getVersionCode() < ApkUtils.getVersionCode(versionsActivity)) {
            //如果版本号小于等于当前软件版本号则隐藏下载按钮
            holder.btnDownloadApk.setVisibility(View.GONE);
        }
        holder.btnDownloadApk.setOnClickListener((v) -> {
            if (buttonInterface != null) {
                //接口实例化后的对象，调用重写后的方法
                buttonInterface.onclick(v, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private class VersionLogViewHolder extends RecyclerView.ViewHolder {

        private TextView tvVersionName, tvVersionTime, tvVersionLog;
        private Button btnDownloadApk;

        private VersionLogViewHolder(View itemView) {
            super(itemView);
        }
    }

    /**
     * 按钮点击事件需要的方法
     */
    public void buttonSetOnclick(ButtonInterface buttonInterface) {
        this.buttonInterface = buttonInterface;
    }

    /**
     * 按钮点击事件对应的接口
     */
    public interface ButtonInterface {
        /**
         * 点击事件
         *
         * @param view     被点击的控件
         * @param position 点击的位置
         */
        void onclick(View view, int position);
    }
}
