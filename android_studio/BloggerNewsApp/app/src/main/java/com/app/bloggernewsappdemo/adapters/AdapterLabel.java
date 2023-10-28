package com.app.bloggernewsappdemo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.bloggernewsappdemo.R;
import com.app.bloggernewsappdemo.database.prefs.SharedPref;
import com.app.bloggernewsappdemo.models.Category;

import java.util.List;

public class AdapterLabel extends RecyclerView.Adapter<AdapterLabel.ViewHolder> {

    Context context;
    List<Category> items;
    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view,  List<Category> items, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public AdapterLabel(Context context, List<Category> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_label_chips, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Category c = items.get(position);
        holder.title.setText(c.term);
        SharedPref sharedPref = new SharedPref(context);

        if (sharedPref.getIsDarkTheme()) {
            holder.lytLabel.setBackgroundResource(R.drawable.bg_chips_dark);
        } else {
            holder.lytLabel.setBackgroundResource(R.drawable.bg_chips_default);
        }

        holder.title.setOnClickListener(view -> {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(view, items, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        LinearLayout lytLabel;

        public ViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.txt_label);
            lytLabel = view.findViewById(R.id.lyt_label);
        }
    }

}