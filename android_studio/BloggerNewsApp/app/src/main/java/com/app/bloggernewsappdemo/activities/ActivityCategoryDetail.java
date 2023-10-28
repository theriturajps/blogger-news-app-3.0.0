package com.app.bloggernewsappdemo.activities;

import static com.app.bloggernewsappdemo.utils.Constant.POST_ORDER;
import static com.app.bloggernewsappdemo.utils.Constant.POST_PER_PAGE;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.app.bloggernewsappdemo.Config;
import com.app.bloggernewsappdemo.R;
import com.app.bloggernewsappdemo.adapters.AdapterPost;
import com.app.bloggernewsappdemo.callbacks.CallbackPost;
import com.app.bloggernewsappdemo.database.prefs.AdsPref;
import com.app.bloggernewsappdemo.database.prefs.SharedPref;
import com.app.bloggernewsappdemo.models.Post;
import com.app.bloggernewsappdemo.rests.RestAdapter;
import com.app.bloggernewsappdemo.utils.AdsManager;
import com.app.bloggernewsappdemo.utils.Constant;
import com.app.bloggernewsappdemo.utils.Tools;
import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityCategoryDetail extends AppCompatActivity {

    private static final String TAG = "ActivityCategoryDetail";
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private AdapterPost adapterPost;
    private ShimmerFrameLayout lytShimmer;
    private Call<CallbackPost> callbackCall = null;
    private List<Post> posts = new ArrayList<>();
    View lytShimmerHead;
    SharedPref sharedPref;
    String category;
    CoordinatorLayout lytParent;
    AdsPref adsPref;
    AdsManager adsManager;
    Tools tools;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.getTheme(this);
        setContentView(R.layout.activity_category_detail);
        sharedPref = new SharedPref(this);
        adsPref = new AdsPref(this);
        tools = new Tools(this);

        Tools.setupAppBarLayout(this);

        adsManager = new AdsManager(this);
        adsManager.loadBannerAd(adsPref.getIsBannerCategoryDetails());
        adsManager.loadInterstitialAd(adsPref.getIsInterstitialPostList(), adsPref.getInterstitialAdInterval());

        Tools.setNavigation(this);
        sharedPref.resetCategoryDetailToken();

        category = getIntent().getStringExtra(Constant.EXTRA_OBJC);

        lytParent = findViewById(R.id.coordinatorLayout);
        recyclerView = findViewById(R.id.recycler_view);
        lytShimmer = findViewById(R.id.shimmer_view_container);
        lytShimmerHead = findViewById(R.id.lyt_shimmer_head);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.color_light_primary);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));

        if (!sharedPref.showPostHeader()) {
            lytShimmerHead.setVisibility(View.GONE);
            recyclerView.setPadding(0, getResources().getDimensionPixelSize(R.dimen.item_post_padding_small), 0, getResources().getDimensionPixelSize(R.dimen.item_post_padding_small));
        }

        //set data and list adapter
        adapterPost = new AdapterPost(this, recyclerView, posts, sharedPref.showPostHeader());
        recyclerView.setAdapter(adapterPost);

        adapterPost.setOnItemClickListener((view, obj, position) -> {
            Intent intent = new Intent(getApplicationContext(), ActivityPostDetail.class);
            intent.putExtra(Constant.EXTRA_ID, obj.id);
            startActivity(intent);
            sharedPref.savePostId(obj.id);
            adsManager.showInterstitialAd();
            adsManager.destroyBannerAd();
        });

        adapterPost.setOnLoadMoreListener(current_page -> {
            if (sharedPref.getCategoryDetailToken() != null) {
                requestAction();
            } else {
                adapterPost.setLoaded();
            }
        });

        // on swipe list
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (callbackCall != null && callbackCall.isExecuted()) callbackCall.cancel();
            adapterPost.resetListData();
            sharedPref.resetCategoryDetailToken();
            requestAction();
        });

        requestAction();
        setupToolbar();
        initShimmerView();

    }

    private void requestAction() {
        showFailedView(false, "");
        showNoItemView(false);
        if (sharedPref.getCategoryDetailToken() == null) {
            swipeProgress(true);
        } else {
            adapterPost.setLoading();
        }
        new Handler(Looper.getMainLooper()).postDelayed(this::requestPostAPI, Constant.DELAY_REFRESH);
    }

    private void requestPostAPI() {
        List<String> apiKeys = Arrays.asList(sharedPref.getAPIKey().replace(", ", ",").split(","));
        int totalKeys = (apiKeys.size() - 1);
        String apiKey;
        if (sharedPref.getApiKeyPosition() > totalKeys) {
            apiKey = apiKeys.get(0);
            sharedPref.updateApiKeyPosition(0);
        } else {
            apiKey = apiKeys.get(sharedPref.getApiKeyPosition());
        }
        this.callbackCall = RestAdapter.createApiPosts(sharedPref.getBloggerId()).getCategoryDetail(category, POST_ORDER, apiKey, POST_PER_PAGE, sharedPref.getCategoryDetailToken());
        this.callbackCall.enqueue(new Callback<CallbackPost>() {
            public void onResponse(@NonNull Call<CallbackPost> call, @NonNull Response<CallbackPost> response) {
                CallbackPost resp = response.body();
                if (resp != null) {
                    displayApiResult(resp.items);
                    String token = resp.nextPageToken;
                    if (token != null) {
                        sharedPref.updateCategoryDetailToken(token);
                    } else {
                        sharedPref.resetCategoryDetailToken();
                    }
                    sharedPref.updateRetryToken(0);
                    adapterPost.setOnItemOverflowClickListener((view, obj, position) -> tools.showBottomSheetDialog(lytParent, obj.id, obj.title, obj.labels, obj.content, obj.published, obj.url, false, false));
                    //adapterPost.setOnItemOverflowClickListener((view, obj, position) -> tools.showBottomSheetDialog(lytParent, obj));
                } else {
                    if (sharedPref.getRetryToken() < Constant.MAX_RETRY_TOKEN) {
                        if (sharedPref.getApiKeyPosition() >= totalKeys) {
                            sharedPref.updateApiKeyPosition(0);
                        } else {
                            sharedPref.updateApiKeyPosition(sharedPref.getApiKeyPosition() + 1);
                        }
                        new Handler().postDelayed(() -> requestPostAPI(), 100);
                        sharedPref.updateRetryToken(sharedPref.getRetryToken() + 1);
                    } else {
                        onFailRequest();
                        sharedPref.updateRetryToken(0);
                    }
                }
            }

            public void onFailure(@NonNull Call<CallbackPost> call, @NonNull Throwable th) {
                Log.e("onFailure", "" + th.getMessage());
                if (!call.isCanceled()) {
                    onFailRequest();
                }
            }
        });
    }

    private void displayApiResult(final List<Post> posts) {
        if (adsPref.getAdStatus() && adsPref.getIsNativePostList()) {
            adapterPost.insertDataWithNativeAd(posts);
        } else {
            adapterPost.insertData(posts);
        }
        swipeProgress(false);
        if (posts.size() == 0) {
            showNoItemView(true);
        }
    }

    private void onFailRequest() {
        adapterPost.setLoaded();
        swipeProgress(false);
        if (Tools.isConnect(this)) {
            showFailedView(true, getString(R.string.failed_text));
        } else {
            showFailedView(true, getString(R.string.failed_text));
        }
    }

    private void showFailedView(boolean flag, String message) {
        View lyt_failed = findViewById(R.id.lyt_failed);
        ((TextView) findViewById(R.id.failed_message)).setText(message);
        if (flag) {
            recyclerView.setVisibility(View.GONE);
            lyt_failed.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_failed.setVisibility(View.GONE);
        }
        findViewById(R.id.failed_retry).setOnClickListener(view -> requestAction());
    }

    private void showNoItemView(boolean show) {
        View lyt_no_item = findViewById(R.id.lyt_no_item);
        ((TextView) findViewById(R.id.no_item_message)).setText(R.string.no_category_found);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            lyt_no_item.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_no_item.setVisibility(View.GONE);
        }
    }

    private void swipeProgress(final boolean show) {
        if (!show) {
            swipeRefreshLayout.setRefreshing(show);
            lytShimmer.setVisibility(View.GONE);
            lytShimmer.stopShimmer();
            return;
        }
        swipeRefreshLayout.post(() -> {
            swipeRefreshLayout.setRefreshing(show);
            lytShimmer.setVisibility(View.VISIBLE);
            lytShimmer.startShimmer();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        adsManager.resumeBannerAd(adsPref.getIsBannerCategoryDetails());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (callbackCall != null && callbackCall.isExecuted()) {
            callbackCall.cancel();
        }
        lytShimmer.stopShimmer();
        adsManager.destroyBannerAd();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        adsManager.destroyBannerAd();
    }

    public void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        Tools.setupToolbar(this, toolbar, category, true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_category_detail, menu);
        if (Config.ENABLE_NEW_APP_DESIGN) {
            for (int i = 0; i < menu.size(); i++) {
                if (sharedPref.getIsDarkTheme()) {
                    menu.getItem(i).getIcon().setColorFilter(ContextCompat.getColor(this, R.color.color_dark_icon), PorterDuff.Mode.SRC_IN);
                } else {
                    menu.getItem(i).getIcon().setColorFilter(ContextCompat.getColor(this, R.color.color_light_icon), PorterDuff.Mode.SRC_IN);
                }
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (menuItem.getItemId() == R.id.action_search) {
            Intent intent = new Intent(getApplicationContext(), ActivitySearch.class);
            startActivity(intent);
            adsManager.destroyBannerAd();
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private void initShimmerView() {
        ViewStub shimmerPostHead = findViewById(R.id.shimmer_view_head);
        ViewStub shimmerPostList = findViewById(R.id.shimmer_view_post);
        if (Config.ENABLE_NEW_APP_DESIGN) {
            if (sharedPref.showPostHeader()) {
                shimmerPostHead.setLayoutResource(R.layout.shimmer_new_post_head);
            } else {
                shimmerPostHead.setLayoutResource(R.layout.shimmer_new_post_list_default);
            }
            if (sharedPref.getIsShowPostListInLargeStyle()) {
                shimmerPostList.setLayoutResource(R.layout.shimmer_new_post_list_large);
            } else {
                shimmerPostList.setLayoutResource(R.layout.shimmer_new_post_list_default);
            }
        } else {
            if (sharedPref.showPostHeader()) {
                shimmerPostHead.setLayoutResource(R.layout.shimmer_post_head);
            } else {
                shimmerPostHead.setLayoutResource(R.layout.shimmer_post_list_default);
            }
            if (sharedPref.getIsShowPostListInLargeStyle()) {
                shimmerPostList.setLayoutResource(R.layout.shimmer_post_list_large);
            } else {
                shimmerPostList.setLayoutResource(R.layout.shimmer_post_list_default);
            }
        }
        shimmerPostHead.inflate();
        shimmerPostList.inflate();
    }

}
