package com.app.bloggernewsappdemo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.bloggernewsappdemo.Config;
import com.app.bloggernewsappdemo.R;
import com.app.bloggernewsappdemo.database.prefs.SharedPref;
import com.app.bloggernewsappdemo.models.Category;
import com.app.bloggernewsappdemo.utils.Constant;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;
import java.util.Random;

public class AdapterCategory extends RecyclerView.Adapter<AdapterCategory.ViewHolder> {

    Context context;
    private List<Category> items;
    private OnItemClickListener mOnItemClickListener;
    SharedPref sharedPref;

    public interface OnItemClickListener {
        void onItemClick(View view, Category obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public AdapterCategory(Context context, List<Category> items) {
        this.items = items;
        this.context = context;
        this.sharedPref = new SharedPref(context);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView categoryName;
        public TextView txtAlphabet;
        public ImageView imgAlphabet;
        public ImageView imgCategory;
        public LinearLayout lytParent;

        public ViewHolder(View v) {
            super(v);
            categoryName = v.findViewById(R.id.txt_label_name);
            txtAlphabet = v.findViewById(R.id.txt_alphabet);
            imgAlphabet = v.findViewById(R.id.img_alphabet);
            imgCategory = v.findViewById(R.id.img_category);
            lytParent = v.findViewById(R.id.lyt_parent);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (sharedPref.getCategoryColumnCount() > 1) {
            if (Config.CATEGORY_LAYOUT_STYLE.equals(Constant.CATEGORY_GRID_MEDIUM)) {
                if (Config.CATEGORY_IMAGE_STYLE.equals("circular")) {
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_label_medium_circle, parent, false);
                } else {
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_label_medium_round, parent, false);
                }
            } else {
                if (Config.CATEGORY_IMAGE_STYLE.equals("circular")) {
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_label_small_circle, parent, false);
                } else {
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_label_small_round, parent, false);
                }
            }
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_label_list, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final Category c = items.get(position);

        holder.categoryName.setText(c.term);

        holder.txtAlphabet.setText(c.term.substring(0, 1));

        int[] colorArr = {R.color.color_red, R.color.color_pink, R.color.color_purple, R.color.color_deep_purple, R.color.color_indigo, R.color.color_blue, R.color.color_cyan, R.color.color_teal, R.color.color_green, R.color.color_lime, R.color.color_orange, R.color.color_brown, R.color.color_grey, R.color.color_blue_gray, R.color.color_black};
        int rnd = new Random().nextInt(colorArr.length);
        holder.imgAlphabet.setImageResource(colorArr[rnd]);

        if (!c.image.equals("")) {
            Glide.with(context)
                    .load(c.image.replace(" ", "%20"))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.bg_button_transparent)
                    .centerCrop()
                    .into(holder.imgCategory);
            holder.imgCategory.setVisibility(View.VISIBLE);
        } else {
            holder.imgCategory.setVisibility(View.GONE);
        }

        holder.lytParent.setOnClickListener(view -> {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(view, c, position);
            }
        });
    }

    public void setListData(List<Category> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    public void resetListData() {
        this.items.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

}