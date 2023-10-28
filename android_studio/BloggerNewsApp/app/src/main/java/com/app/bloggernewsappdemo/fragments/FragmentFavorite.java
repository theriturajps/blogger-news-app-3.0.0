package com.app.bloggernewsappdemo.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.bloggernewsappdemo.Config;
import com.app.bloggernewsappdemo.R;
import com.app.bloggernewsappdemo.activities.ActivityFavoriteDetail;
import com.app.bloggernewsappdemo.activities.ActivityPostDetail;
import com.app.bloggernewsappdemo.activities.MainActivity;
import com.app.bloggernewsappdemo.adapters.AdapterFavorite;
import com.app.bloggernewsappdemo.database.prefs.SharedPref;
import com.app.bloggernewsappdemo.database.sqlite.DbFavorite;
import com.app.bloggernewsappdemo.models.Post;
import com.app.bloggernewsappdemo.utils.Constant;
import com.app.bloggernewsappdemo.utils.Tools;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class FragmentFavorite extends Fragment {

    List<Post> posts = new ArrayList<>();
    private View rootView;
    LinearLayout lytNoFavorite;
    private RecyclerView recyclerView;
    private AdapterFavorite adapterFavorite;
    DbFavorite dbFavorite;
    private BottomSheetDialog mBottomSheetDialog;
    SharedPref sharedPref;
    Activity activity;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (Activity) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_favorite, container, false);
        sharedPref = new SharedPref(activity);
        recyclerView = rootView.findViewById(R.id.recycler_view);
        lytNoFavorite = rootView.findViewById(R.id.lyt_no_favorite);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));

        if (!Config.ENABLE_NEW_APP_DESIGN) {
            recyclerView.setPadding(
                    activity.getResources().getDimensionPixelOffset(R.dimen.gnt_no_margin),
                    activity.getResources().getDimensionPixelOffset(R.dimen.item_post_padding_small),
                    activity.getResources().getDimensionPixelOffset(R.dimen.gnt_no_margin),
                    activity.getResources().getDimensionPixelOffset(R.dimen.item_post_padding_small)
            );
        }
        loadDataFromDatabase();
        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();
        loadDataFromDatabase();
    }

    public void loadDataFromDatabase() {
        dbFavorite = new DbFavorite(activity);
        posts = dbFavorite.getAllData();

        adapterFavorite = new AdapterFavorite(activity, recyclerView, posts);
        recyclerView.setAdapter(adapterFavorite);

        showNoItemView(posts.size() == 0);

        adapterFavorite.setOnItemClickListener((v, obj, position) -> {
            if (Tools.isConnect(activity)) {
                Intent intent = new Intent(activity, ActivityPostDetail.class);
                intent.putExtra(Constant.EXTRA_ID, obj.id);
                startActivity(intent);
                if (activity != null) {
                    ((MainActivity) activity).showInterstitialAd();
                }
            } else {
                Intent intent = new Intent(activity, ActivityFavoriteDetail.class);
                intent.putExtra(Constant.EXTRA_OBJC, obj);
                startActivity(intent);
            }
            sharedPref.savePostId(obj.id);
            ((MainActivity) activity).destroyBannerAd();
        });

        adapterFavorite.setOnItemOverflowClickListener((view, obj, position) -> {
            showBottomSheetDialog(obj);
        });

    }

    private void showNoItemView(boolean show) {
        ((TextView) rootView.findViewById(R.id.no_item_message)).setText(R.string.msg_no_favorite);
        if (show) {
            //recyclerView.setVisibility(View.GONE);
            lytNoFavorite.setVisibility(View.VISIBLE);
        } else {
            //recyclerView.setVisibility(View.VISIBLE);
            lytNoFavorite.setVisibility(View.GONE);
        }
    }

    private void showBottomSheetDialog(Post post) {
        @SuppressLint("InflateParams") final View view = getLayoutInflater().inflate(R.layout.dialog_more_options, null);
        FrameLayout lytBottomSheet = view.findViewById(R.id.bottom_sheet);
        TextView txtFavorite = view.findViewById(R.id.txt_favorite);

        ImageView imgFavorite = view.findViewById(R.id.img_favorite);
        ImageView imgLaunch = view.findViewById(R.id.img_launch);
        ImageView imgShare = view.findViewById(R.id.img_share);
        ImageView imgReport = view.findViewById(R.id.img_report);
        ImageView imgFeedback = view.findViewById(R.id.img_feedback);
        ImageView btnClose = view.findViewById(R.id.btn_close);

        if (sharedPref.getIsDarkTheme()) {
            lytBottomSheet.setBackground(ContextCompat.getDrawable(activity, R.drawable.bg_rounded_dark));
            imgFavorite.setColorFilter(ContextCompat.getColor(activity, R.color.color_dark_icon));
            imgLaunch.setColorFilter(ContextCompat.getColor(activity, R.color.color_dark_icon));
            imgShare.setColorFilter(ContextCompat.getColor(activity, R.color.color_dark_icon));
            imgReport.setColorFilter(ContextCompat.getColor(activity, R.color.color_dark_icon));
            imgFeedback.setColorFilter(ContextCompat.getColor(activity, R.color.color_dark_icon));
            btnClose.setColorFilter(ContextCompat.getColor(activity, R.color.color_dark_icon));
        } else {
            lytBottomSheet.setBackground(ContextCompat.getDrawable(activity, R.drawable.bg_rounded_default));
            imgFavorite.setColorFilter(ContextCompat.getColor(activity, R.color.color_light_icon));
            imgLaunch.setColorFilter(ContextCompat.getColor(activity, R.color.color_light_icon));
            imgShare.setColorFilter(ContextCompat.getColor(activity, R.color.color_light_icon));
            imgReport.setColorFilter(ContextCompat.getColor(activity, R.color.color_light_icon));
            imgFeedback.setColorFilter(ContextCompat.getColor(activity, R.color.color_light_icon));
            btnClose.setColorFilter(ContextCompat.getColor(activity, R.color.color_light_icon));
        }

        LinearLayout btnFavorite = view.findViewById(R.id.btn_favorite);
        LinearLayout btnLaunch = view.findViewById(R.id.btn_launch);
        LinearLayout btnShare = view.findViewById(R.id.btn_share);
        LinearLayout btnReport = view.findViewById(R.id.btn_report);
        LinearLayout btnFeedback = view.findViewById(R.id.btn_feedback);

        if (!sharedPref.getIsShowViewOnSiteMenu()) {
            btnLaunch.setVisibility(View.GONE);
        }

        btnFavorite.setOnClickListener(action -> {
            List<Post> posts = dbFavorite.getFavRow(post.id);
            if (posts.size() == 0) {
                dbFavorite.AddToFavorite(new Post(post.id, post.title, post.labels, post.content, post.published));
                Snackbar.make(activity.findViewById(R.id.tab_coordinator_layout), getString(R.string.msg_favorite_added), Snackbar.LENGTH_SHORT).show();
                imgFavorite.setImageResource(R.drawable.ic_favorite_grey);
            } else {
                if (posts.get(0).getId().equals(post.id)) {
                    dbFavorite.RemoveFav(new Post(post.id));
                    Snackbar.make(activity.findViewById(R.id.tab_coordinator_layout), getString(R.string.msg_favorite_removed), Snackbar.LENGTH_SHORT).show();
                    imgFavorite.setImageResource(R.drawable.ic_favorite_outline_grey);
                    refreshFragment();
                }
            }
            mBottomSheetDialog.dismiss();
        });

        btnShare.setOnClickListener(action -> {
            Tools.shareArticle(activity, post.title, post.url);
            mBottomSheetDialog.dismiss();
        });

        btnLaunch.setOnClickListener(action -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(post.url)));
            mBottomSheetDialog.dismiss();
        });

        btnReport.setOnClickListener(v -> {
            Tools.sendReport(activity, sharedPref.getEmailFeedbackAndReport(), post.title, "");
            mBottomSheetDialog.dismiss();
        });

        btnFeedback.setOnClickListener(v -> {
            Tools.sendFeedback(activity, sharedPref.getEmailFeedbackAndReport());
            mBottomSheetDialog.dismiss();
        });

        btnClose.setOnClickListener(v -> mBottomSheetDialog.dismiss());

        if (sharedPref.getIsDarkTheme()) {
            mBottomSheetDialog = new BottomSheetDialog(activity, R.style.SheetDialogDark);
        } else {
            mBottomSheetDialog = new BottomSheetDialog(activity, R.style.SheetDialogLight);
        }
        mBottomSheetDialog.setContentView(view);

        mBottomSheetDialog.show();
        mBottomSheetDialog.setOnDismissListener(dialog -> mBottomSheetDialog = null);

        dbFavorite = new DbFavorite(activity);
        List<Post> posts = dbFavorite.getFavRow(post.id);
        if (posts.size() == 0) {
            txtFavorite.setText(getString(R.string.favorite_add));
            imgFavorite.setImageResource(R.drawable.ic_favorite_outline_grey);
        } else {
            if (posts.get(0).id.equals(post.id)) {
                txtFavorite.setText(getString(R.string.favorite_remove));
                imgFavorite.setImageResource(R.drawable.ic_favorite_grey);
            }
        }

    }

    public void refreshFragment() {
        adapterFavorite.resetListData();
        loadDataFromDatabase();
    }

}
