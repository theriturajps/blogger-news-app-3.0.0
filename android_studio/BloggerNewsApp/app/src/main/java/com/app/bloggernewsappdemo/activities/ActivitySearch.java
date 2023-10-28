package com.app.bloggernewsappdemo.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.app.bloggernewsappdemo.Config;
import com.app.bloggernewsappdemo.R;
import com.app.bloggernewsappdemo.adapters.AdapterSearch;
import com.app.bloggernewsappdemo.adapters.AdapterSearchHistory;
import com.app.bloggernewsappdemo.callbacks.CallbackSearch;
import com.app.bloggernewsappdemo.database.prefs.AdsPref;
import com.app.bloggernewsappdemo.database.prefs.SharedPref;
import com.app.bloggernewsappdemo.models.Entry;
import com.app.bloggernewsappdemo.rests.RestAdapter;
import com.app.bloggernewsappdemo.utils.AdsManager;
import com.app.bloggernewsappdemo.utils.Constant;
import com.app.bloggernewsappdemo.utils.Tools;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivitySearch extends AppCompatActivity {

    private static final String TAG = "ActivitySearch";
    private EditText edtSearch;
    private RecyclerView recyclerView;
    private AdapterSearch adapterPost;
    RecyclerView recyclerSuggestion;
    private AdapterSearchHistory adapterSearchHistory;
    private LinearLayout lytSuggestion;
    private ImageButton btnClear;
    Call<CallbackSearch> callbackSearchCall = null;
    List<Entry> posts = new ArrayList<>();
    private ShimmerFrameLayout lytShimmer;
    CoordinatorLayout parentView;
    SharedPref sharedPref;
    AdsPref adsPref;
    AdsManager adsManager;
    Tools tools;
    LinearLayout lytAds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.getTheme(this);
        setContentView(R.layout.activity_search);
        sharedPref = new SharedPref(this);
        adsPref = new AdsPref(this);
        adsManager = new AdsManager(this);
        tools = new Tools(this);
        Tools.setNavigation(this);
        Tools.setupAppSearchBarLayout(this);
        parentView = findViewById(R.id.coordinatorLayout);
        initComponent();
        adsManager.loadBannerAd(adsPref.getIsBannerSearch());
        adsManager.loadInterstitialAd(adsPref.getIsInterstitialPostList(), adsPref.getInterstitialAdInterval());
        setupToolbar();
        initShimmerView();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initComponent() {
        lytSuggestion = findViewById(R.id.lyt_suggestion);
        edtSearch = findViewById(R.id.edt_search);
        btnClear = findViewById(R.id.bt_clear);
        btnClear.setVisibility(View.GONE);
        lytShimmer = findViewById(R.id.shimmer_view_container);
        lytAds = findViewById(R.id.lyt_ads);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));

        if (!Config.ENABLE_NEW_APP_DESIGN) {
            recyclerView.setPadding(
                    getResources().getDimensionPixelOffset(R.dimen.gnt_no_margin),
                    getResources().getDimensionPixelOffset(R.dimen.item_post_padding_small),
                    getResources().getDimensionPixelOffset(R.dimen.gnt_no_margin),
                    getResources().getDimensionPixelOffset(R.dimen.item_post_padding_small)
            );
        }

        adapterPost = new AdapterSearch(this, recyclerView, posts);
        recyclerView.setAdapter(adapterPost);

        recyclerSuggestion = findViewById(R.id.recyclerSuggestion);
        recyclerSuggestion.setLayoutManager(new LinearLayoutManager(this));

        edtSearch.addTextChangedListener(textWatcher);
        edtSearch.requestFocus();
        swipeProgress(false);

        adapterPost.setOnItemClickListener((view, obj, position) -> {
            String[] data = obj.id.$t.split(",");
            String[] _data = data[1].split("\\.");
            String postId = _data[1].replace("post-", "");

            Intent intent = new Intent(getApplicationContext(), ActivityPostDetail.class);
            intent.putExtra(Constant.EXTRA_ID, postId);
            startActivity(intent);
            sharedPref.savePostId(postId);

            adsManager.showInterstitialAd();
            adsManager.destroyBannerAd();
        });

        adapterPost.setOnItemOverflowClickListener((view, obj, position) -> {
            String[] data = obj.id.$t.split(",");
            String[] _data = data[1].split("\\.");
            String postId = _data[1].replace("post-", "");
            List<String> categories = Collections.singletonList(obj.category.get(0).term);
            tools.showBottomSheetDialog(parentView, postId, obj.title.$t, categories, obj.content.$t, obj.published.$t, obj.link.get(2).href, false, false);
        });

        adapterSearchHistory = new AdapterSearchHistory(this);
        recyclerSuggestion.setAdapter(adapterSearchHistory);
        showSuggestionSearch();
        adapterSearchHistory.setOnItemClickListener((view, viewModel, pos) -> {
            lytSuggestion.setVisibility(View.GONE);
            edtSearch.setText(viewModel);
            edtSearch.setSelection(viewModel.length());
            hideKeyboard();
            adapterPost.resetListData();
            requestAction();
        });

        adapterSearchHistory.setOnItemActionClickListener((view, viewModel, pos) -> {
            edtSearch.setText(viewModel);
            edtSearch.setSelection(viewModel.length());
        });

        btnClear.setOnClickListener(view -> edtSearch.setText(""));

        edtSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (edtSearch.getText().toString().equals("")) {
                    Snackbar.make(parentView, getString(R.string.msg_search_input), Snackbar.LENGTH_SHORT).show();
                    hideKeyboard();
                    swipeProgress(false);
                } else {
                    adapterPost.resetListData();
                    hideKeyboard();
                    requestAction();
                }
                return true;
            }
            return false;
        });

        edtSearch.setOnTouchListener((view, motionEvent) -> {
            showSuggestionSearch();
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            return false;
        });

    }

    public void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        Tools.setupToolbar(this, toolbar, "", true);
        if (Config.ENABLE_NEW_APP_DESIGN) {
            if (sharedPref.getIsDarkTheme()) {
                edtSearch.setTextColor(ContextCompat.getColor(this, R.color.color_white));
                btnClear.setColorFilter(ContextCompat.getColor(this, R.color.color_dark_icon), PorterDuff.Mode.SRC_IN);
                lytSuggestion.setBackgroundColor(ContextCompat.getColor(this, R.color.color_dark_background));
            } else {
                btnClear.setColorFilter(ContextCompat.getColor(this, R.color.color_light_icon), PorterDuff.Mode.SRC_IN);
                lytSuggestion.setBackgroundColor(ContextCompat.getColor(this, R.color.color_light_background));
            }
        }
    }

    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence c, int i, int i1, int i2) {
            if (c.toString().trim().length() == 0) {
                btnClear.setVisibility(View.GONE);
            } else {
                btnClear.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence c, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    };

    private void requestSearchAPI(final String query) {
        this.callbackSearchCall = RestAdapter.createApiCategory(sharedPref.getBloggerId()).getSearchPosts(query, 100);
        this.callbackSearchCall.enqueue(new Callback<CallbackSearch>() {
            public void onResponse(@NonNull Call<CallbackSearch> call, @NonNull Response<CallbackSearch> response) {
                CallbackSearch resp = response.body();
                if (resp == null) {
                    onFailRequest();
                    return;
                }
                displayApiResult(resp.feed.entry, resp.feed.entry.size());
            }

            public void onFailure(@NonNull Call<CallbackSearch> call, @NonNull Throwable th) {
                Log.e("onFailure", th.getMessage());
                if (!call.isCanceled()) {
                    onFailRequest();
                }
            }
        });
    }

    private void displayApiResult(final List<Entry> posts, int totalPosts) {
        if (adsPref.getAdStatus() && adsPref.getIsNativePostList()) {
            adapterPost.insertDataWithNativeAd(posts, totalPosts);
        } else {
            adapterPost.insertData(posts);
        }
        swipeProgress(false);
        if (posts.size() == 0) {
            showNotFoundView(true);
        }
        lytAds.setVisibility(View.VISIBLE);
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

    private void requestAction() {
        lytSuggestion.setVisibility(View.GONE);
        showFailedView(false, "");
        showNotFoundView(false);
        final String query = edtSearch.getText().toString().trim();
        if (!query.equals("")) {
            adapterPost.setLoading();
            requestSearchAPI(query);
            adapterSearchHistory.addSearchHistory(query);
            swipeProgress(true);
        } else {
            Snackbar.make(parentView, getString(R.string.msg_search_input), Snackbar.LENGTH_SHORT).show();
            swipeProgress(false);
        }
    }

    private void showSuggestionSearch() {
        adapterSearchHistory.refreshItems();
        lytSuggestion.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void showFailedView(boolean show, String message) {
        View lytFailed = findViewById(R.id.lyt_failed);
        ((TextView) findViewById(R.id.failed_message)).setText(message);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            lytFailed.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lytFailed.setVisibility(View.GONE);
        }
        findViewById(R.id.failed_retry).setOnClickListener(view -> requestAction());
    }

    private void showNotFoundView(boolean show) {
        View lytNoItem = findViewById(R.id.lyt_no_item);
        ((TextView) findViewById(R.id.no_item_message)).setText(R.string.no_search_found);
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
            lytShimmer.setVisibility(View.GONE);
            lytShimmer.stopShimmer();
            return;
        } else {
            lytShimmer.setVisibility(View.VISIBLE);
            lytShimmer.startShimmer();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        adsManager.resumeBannerAd(adsPref.getIsBannerSearch());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (callbackSearchCall != null && callbackSearchCall.isExecuted()) {
            callbackSearchCall.cancel();
        }
        lytShimmer.stopShimmer();
        adsManager.destroyBannerAd();
    }

    @Override
    public void onBackPressed() {
        if (edtSearch.length() > 0) {
            edtSearch.setText("");
        } else {
            super.onBackPressed();
            adsManager.destroyBannerAd();
        }
    }

    private void initShimmerView() {
        ViewStub shimmerPostList = findViewById(R.id.shimmer_view_post);
        if (Config.ENABLE_NEW_APP_DESIGN) {
            if (sharedPref.getIsShowPostListInLargeStyle()) {
                shimmerPostList.setLayoutResource(R.layout.shimmer_new_post_list_large);
            } else {
                shimmerPostList.setLayoutResource(R.layout.shimmer_new_post_list_default);
            }
        } else {
            if (sharedPref.getIsShowPostListInLargeStyle()) {
                shimmerPostList.setLayoutResource(R.layout.shimmer_post_list_large);
            } else {
                shimmerPostList.setLayoutResource(R.layout.shimmer_post_list_default);
            }
        }
        shimmerPostList.inflate();
    }

}
