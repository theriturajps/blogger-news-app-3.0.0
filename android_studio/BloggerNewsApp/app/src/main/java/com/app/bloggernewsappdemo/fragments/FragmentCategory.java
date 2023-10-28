package com.app.bloggernewsappdemo.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.app.bloggernewsappdemo.Config;
import com.app.bloggernewsappdemo.R;
import com.app.bloggernewsappdemo.activities.ActivityCategoryDetail;
import com.app.bloggernewsappdemo.activities.MainActivity;
import com.app.bloggernewsappdemo.adapters.AdapterCategory;
import com.app.bloggernewsappdemo.callbacks.CallbackLabel;
import com.app.bloggernewsappdemo.database.prefs.SharedPref;
import com.app.bloggernewsappdemo.database.sqlite.DbLabel;
import com.app.bloggernewsappdemo.models.Category;
import com.app.bloggernewsappdemo.utils.Constant;
import com.app.bloggernewsappdemo.utils.Tools;
import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;

@SuppressWarnings("ConstantConditions")
public class FragmentCategory extends Fragment {

    private static final String TAG = "FragmentCategory";
    private View rootView;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private AdapterCategory adapterLabel;
    private ShimmerFrameLayout lytShimmer;
    SharedPref sharedPref;
    DbLabel dbLabel;
    Activity activity;
    int categoryColumnCount;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (Activity) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_category, container, false);
        sharedPref = new SharedPref(activity);
        dbLabel = new DbLabel(activity);

        recyclerView = rootView.findViewById(R.id.recycler_view);
        lytShimmer = rootView.findViewById(R.id.shimmer_view_container);

        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.color_light_primary);

        categoryColumnCount = Math.min(sharedPref.getCategoryColumnCount(), 3);
        recyclerView.setLayoutManager(new GridLayoutManager(activity, categoryColumnCount));

        //recyclerView.setLayoutManager(new GridLayoutManager(activity, CATEGORY_COLUMN_COUNT));

        adapterLabel = new AdapterCategory(activity, new ArrayList<>());
        recyclerView.setAdapter(adapterLabel);

        displayData();

        swipeRefreshLayout.setOnRefreshListener(this::refreshData);

        initShimmerLayout();
        setRecyclerViewPadding();

        return rootView;
    }

    private void displayData() {
        if (Tools.isConnect(activity)) {
            loadLabelFromDatabase();
            showFailedView(false);
        } else {
            showFailedView(true);
            swipeProgress(false);
        }
    }

    private void refreshData() {
        showFailedView(false);
        swipeProgress(true);
        adapterLabel.resetListData();

        if (Tools.isConnect(activity)) {
            new Handler().postDelayed(this::loadLabelFromDatabase, 1000);
        } else {
            new Handler().postDelayed(()-> {
                showFailedView(true);
                swipeProgress(false);
            }, 10);
        }
    }

    public void loadLabelFromDatabase() {

        swipeProgress(false);

        List<Category> categories = dbLabel.getAllCategory(DbLabel.TABLE_LABEL);
        adapterLabel.setListData(categories);

        showNoItemView(categories.size() == 0);

        adapterLabel.setOnItemClickListener((v, obj, position) -> {
            Intent intent = new Intent(activity, ActivityCategoryDetail.class);
            intent.putExtra(Constant.EXTRA_OBJC, obj.term);
            startActivity(intent);
            ((MainActivity) activity).showInterstitialAd();
            ((MainActivity) activity).destroyBannerAd();
        });

    }

    private void showFailedView(boolean show) {
        View lytFailed = rootView.findViewById(R.id.lyt_failed);
        ((TextView) rootView.findViewById(R.id.failed_message)).setText( getString(R.string.failed_text));
        if (show) {
            recyclerView.setVisibility(View.GONE);
            lytFailed.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lytFailed.setVisibility(View.GONE);
        }
        rootView.findViewById(R.id.failed_retry).setOnClickListener(view -> refreshData());
    }

    private void showNoItemView(boolean show) {
        View lytNoItem = rootView.findViewById(R.id.lyt_no_item);
        ((TextView) rootView.findViewById(R.id.no_item_message)).setText(R.string.no_category_found);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            lytNoItem.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lytNoItem.setVisibility(View.GONE);
        }
    }

    private void swipeProgress(final boolean show) {
        if (!show) {
            swipeRefreshLayout.setRefreshing(show);
            recyclerView.setVisibility(View.VISIBLE);
            lytShimmer.setVisibility(View.GONE);
            lytShimmer.stopShimmer();
            return;
        }
        swipeRefreshLayout.post(() -> {
            swipeRefreshLayout.setRefreshing(show);
            recyclerView.setVisibility(View.GONE);
            lytShimmer.setVisibility(View.VISIBLE);
            lytShimmer.startShimmer();
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        lytShimmer.stopShimmer();
    }

    private void initShimmerLayout() {
        ViewStub stub = rootView.findViewById(R.id.lytShimmerView);
        if (sharedPref.getCategoryColumnCount() > 1) {
            if (Config.CATEGORY_IMAGE_STYLE.equals(Constant.ROUNDED)) {
                if (categoryColumnCount == 2) {
                    if (Config.CATEGORY_LAYOUT_STYLE.equals(Constant.CATEGORY_GRID_MEDIUM)) {
                        stub.setLayoutResource(R.layout.shimmer_category_grid2_round_md);
                    } else {
                        stub.setLayoutResource(R.layout.shimmer_category_grid2_round_sm);
                    }
                } else {
                    if (Config.CATEGORY_LAYOUT_STYLE.equals(Constant.CATEGORY_GRID_MEDIUM)) {
                        stub.setLayoutResource(R.layout.shimmer_category_grid3_round_md);
                    } else {
                        stub.setLayoutResource(R.layout.shimmer_category_grid3_round_sm);
                    }
                }
            } else {
                if (categoryColumnCount == 2) {
                    if (Config.CATEGORY_LAYOUT_STYLE.equals(Constant.CATEGORY_GRID_MEDIUM)) {
                        stub.setLayoutResource(R.layout.shimmer_category_grid2_circle_md);
                    } else {
                        stub.setLayoutResource(R.layout.shimmer_category_grid2_circle_sm);
                    }
                } else {
                    if (Config.CATEGORY_LAYOUT_STYLE.equals(Constant.CATEGORY_GRID_MEDIUM)) {
                        stub.setLayoutResource(R.layout.shimmer_category_grid3_circle_md);
                    } else {
                        stub.setLayoutResource(R.layout.shimmer_category_grid3_circle_sm);
                    }
                }
            }
        } else {
            stub.setLayoutResource(R.layout.shimmer_category_list);
        }
        stub.inflate();
    }

    private void setRecyclerViewPadding() {
        if (sharedPref.getCategoryColumnCount() > 1) {
            if (Config.CATEGORY_LAYOUT_STYLE.equals(Constant.CATEGORY_GRID_MEDIUM)) {
                recyclerView.setPadding(
                        getResources().getDimensionPixelSize(R.dimen.corner_radius),
                        getResources().getDimensionPixelSize(R.dimen.corner_radius),
                        getResources().getDimensionPixelSize(R.dimen.corner_radius),
                        getResources().getDimensionPixelSize(R.dimen.corner_radius)
                );
            } else {
                recyclerView.setPadding(0, 0, 0, 0);
            }
        }
    }

}
