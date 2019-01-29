package jp.co.shiratsuki.walkietalkie.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import jp.co.shiratsuki.walkietalkie.R;
import jp.co.shiratsuki.walkietalkie.bean.Language;
import jp.co.shiratsuki.walkietalkie.utils.LanguageUtil;
import jp.co.shiratsuki.walkietalkie.utils.ViewUtil;

import java.util.List;

/**
 * 选择语言的适配器
 * Created at 2018/11/28 13:38
 *
 * @author LiYuliang
 * @version 1.0
 */

public class ChooseLanguageAdapter extends RecyclerView.Adapter<ChooseLanguageAdapter.ListViewHolder> {

    private Context mContext;
    private List<Language> list;
    private OnItemClickListener mItemClickListener;

    public ChooseLanguageAdapter(Context context, List<Language> lv) {
        mContext = context;
        list = lv;
    }

    @Override
    public ListViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_language, viewGroup, false);
        ListViewHolder listViewHolder = new ListViewHolder(view);
        listViewHolder.tvLanguage = view.findViewById(R.id.tv_language);
        listViewHolder.ivSelect = view.findViewById(R.id.iv_select);
        return listViewHolder;
    }

    @Override
    public void onBindViewHolder(ListViewHolder holder, int position) {
        Language language = list.get(position);
        holder.tvLanguage.setText(language.getLanguageName());
        ViewUtil.updateViewLanguage(holder.tvLanguage);
        if (LanguageUtil.getLanguageLocal(mContext).equals(language.getLanguageCode())) {
            holder.ivSelect.setImageResource(R.drawable.checkbox_choose_language_selected);
        } else {
            holder.ivSelect.setImageResource(R.drawable.checkbox_choose_language_normal);
        }
        View itemView = holder.itemView;
        if (mItemClickListener != null) {
            itemView.setOnClickListener((v) -> mItemClickListener.onItemClick(holder.itemView, holder.getLayoutPosition()));
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ListViewHolder extends RecyclerView.ViewHolder {

        private TextView tvLanguage;
        private ImageView ivSelect;

        private ListViewHolder(View itemView) {
            super(itemView);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mItemClickListener = onItemClickListener;
    }

}
