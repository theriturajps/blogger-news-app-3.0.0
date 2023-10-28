package com.app.bloggernewsappdemo.activities;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.bloggernewsappdemo.Config;
import com.app.bloggernewsappdemo.R;
import com.app.bloggernewsappdemo.adapters.AdapterCategoryList;
import com.app.bloggernewsappdemo.database.prefs.SharedPref;
import com.app.bloggernewsappdemo.database.sqlite.DbFavorite;
import com.app.bloggernewsappdemo.models.Post;
import com.app.bloggernewsappdemo.utils.Constant;
import com.app.bloggernewsappdemo.utils.Tools;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Arrays;
import java.util.List;

public class ActivityFavoriteDetail extends AppCompatActivity {

    Post post;
    WebView webView;
    FrameLayout customViewContainer;
    String htmlText;
    SharedPref sharedPref;
    DbFavorite dbFavorite;
    private String singleChoiceSelected;
    ImageButton btnFavorite;
    ImageButton btnFontSize;
    ImageButton btnOverflow;
    CoordinatorLayout parentView;
    LinearLayout lytImage;
    ImageView primaryImage;
    TextView txtDate;
    LinearLayout lytDate;
    ImageView imgDate;
    Tools tools;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.getTheme(this);
        setContentView(R.layout.activity_post_detail);
        tools = new Tools(this);
        post = (Post) getIntent().getSerializableExtra(Constant.EXTRA_OBJC);
        sharedPref = new SharedPref(this);
        Tools.setNavigation(this);
        Tools.setupAppDetailBarLayout(this);
        dbFavorite = new DbFavorite(this);
        parentView = findViewById(R.id.coordinatorLayout);
        initView();
        displayData();
        setupToolbar();
    }

    private void initView() {
        btnFavorite = findViewById(R.id.btn_favorite);
        btnFontSize = findViewById(R.id.btn_font_size);
        btnOverflow = findViewById(R.id.btn_overflow);
        lytImage = findViewById(R.id.lyt_image);
        primaryImage = findViewById(R.id.primary_image);
    }

    public void displayData() {
        if (Config.ENABLE_NEW_APP_DESIGN) {
            lytImage.setPadding(
                    getResources().getDimensionPixelSize(R.dimen.item_post_padding_medium),
                    getResources().getDimensionPixelSize(R.dimen.item_post_padding_medium),
                    getResources().getDimensionPixelSize(R.dimen.item_post_padding_medium),
                    getResources().getDimensionPixelSize(R.dimen.gnt_no_margin)
            );
            CardView cardView = findViewById(R.id.card_view);
            cardView.setRadius(getResources().getDimensionPixelSize(R.dimen.corner_radius));
        } else {
            lytImage.setPadding(
                    getResources().getDimensionPixelSize(R.dimen.gnt_no_margin),
                    getResources().getDimensionPixelSize(R.dimen.gnt_no_margin),
                    getResources().getDimensionPixelSize(R.dimen.gnt_no_margin),
                    getResources().getDimensionPixelSize(R.dimen.gnt_no_margin)
            );
        }

        Document htmlData = Jsoup.parse(post.content);
        Elements elements = htmlData.select("img");
        if (elements.hasAttr("src")) {
            Glide.with(this)
                    .load(elements.get(0).attr("src").replace(" ", "%20"))
                    .transition(withCrossFade())
                    .thumbnail(0.1f)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.bg_button_transparent)
                    .centerCrop()
                    .into(primaryImage);
            primaryImage.setVisibility(View.VISIBLE);
            primaryImage.setOnClickListener(v -> {
                Intent intent = new Intent(getApplicationContext(), ActivityImageDetail.class);
                intent.putExtra("image", elements.get(0).attr("src"));
                startActivity(intent);
            });
        } else {
            primaryImage.setVisibility(View.GONE);
        }

        if (htmlData.select("img").first() != null) {
            Element element = htmlData.select("img").first();
            assert element != null;
            if (element.hasAttr("src")) {
                element.remove();
            }
            htmlText = htmlData.toString();
        } else {
            htmlText = htmlData.toString();
        }

        ((TextView) findViewById(R.id.txt_title)).setText(post.title);

        imgDate = findViewById(R.id.ic_date);
        txtDate = findViewById(R.id.txt_date);
        lytDate = findViewById(R.id.lyt_date);

        if (sharedPref.showPostDate()) {
            if (Config.ENABLE_NEW_APP_DESIGN) {
                imgDate.setVisibility(View.GONE);
            } else {
                imgDate.setVisibility(View.VISIBLE);
            }
            txtDate.setText(Tools.convertDateTime(post.published, Constant.DATE_FORMATTED));
            lytDate.setVisibility(View.VISIBLE);
        } else {
            lytDate.setVisibility(View.GONE);
        }

        webView = findViewById(R.id.content);
        customViewContainer = findViewById(R.id.customViewContainer);
        Tools.displayPostDescription(this, webView, htmlText, customViewContainer, sharedPref);

        String labels = String.valueOf(post.labels).replace("[[", "").replace("]]", "").replace(", ", ",");
        List<String> arrayListLabels = Arrays.asList((labels.split(",")));
        RecyclerView recyclerView = findViewById(R.id.recycler_view_category);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        AdapterCategoryList adapter = new AdapterCategoryList(this, arrayListLabels);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener((view, items, position) -> {
            Intent intent = new Intent(getApplicationContext(), ActivityCategoryDetail.class);
            intent.putExtra(Constant.EXTRA_OBJC, items.get(position));
            startActivity(intent);
        });

        addToFavorite();

    }

    public void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        Tools.setupToolbar(this, toolbar, "", true);
        ((TextView) findViewById(R.id.toolbar_title)).setText("");
        if (Config.ENABLE_NEW_APP_DESIGN) {
            if (sharedPref.getIsDarkTheme()) {
                btnFontSize.setColorFilter(ContextCompat.getColor(this, R.color.color_dark_icon), PorterDuff.Mode.SRC_IN);
                btnFavorite.setColorFilter(ContextCompat.getColor(this, R.color.color_dark_icon), PorterDuff.Mode.SRC_IN);
                btnOverflow.setColorFilter(ContextCompat.getColor(this, R.color.color_dark_icon), PorterDuff.Mode.SRC_IN);
            } else {
                btnFontSize.setColorFilter(ContextCompat.getColor(this, R.color.color_light_icon), PorterDuff.Mode.SRC_IN);
                btnFavorite.setColorFilter(ContextCompat.getColor(this, R.color.color_light_icon), PorterDuff.Mode.SRC_IN);
                btnOverflow.setColorFilter(ContextCompat.getColor(this, R.color.color_light_icon), PorterDuff.Mode.SRC_IN);
            }
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(final Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_post_detail, menu);
//        this.menu = menu;
//        addToFavorite();
//        if (!sharedPref.getIsShowViewOnSiteMenu()) {
//            MenuItem viewOnSiteItem = menu.findItem(R.id.action_launch);
//            viewOnSiteItem.setVisible(false);
//        }
//        return true;
//    }

//    public void addToFavorite() {
//        List<Post> data = dbFavorite.getFavRow(post.id);
//        if (data.size() == 0) {
//            menu.getItem(2).setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_menu_favorite_outline));
//        } else {
//            if (data.get(0).getId().equals(post.id)) {
//                menu.getItem(2).setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_menu_favorite));
//            }
//        }
//    }

    public void addToFavorite() {
        List<Post> data = dbFavorite.getFavRow(post.id);
        if (data.size() == 0) {
            btnFavorite.setImageResource(R.drawable.ic_menu_favorite_outline);
        } else {
            if (data.get(0).getId().equals(post.id)) {
                btnFavorite.setImageResource(R.drawable.ic_menu_favorite);
            }
        }

        btnFavorite.setOnClickListener(v -> onFavoriteClicked(post.id, post.title, post.labels, post.content, post.published));

        btnFontSize.setOnClickListener(v -> {
            String[] items = getResources().getStringArray(R.array.dialog_font_size);
            singleChoiceSelected = items[sharedPref.getFontSize()];
            int itemSelected = sharedPref.getFontSize();
            new MaterialAlertDialogBuilder(ActivityFavoriteDetail.this, R.style.Material3AlertDialog)
                    .setTitle(getString(R.string.title_dialog_font_size))
                    .setSingleChoiceItems(items, itemSelected, (dialogInterface, i) -> singleChoiceSelected = items[i])
                    .setPositiveButton(R.string.dialog_option_ok, (dialogInterface, i) -> {
                        WebSettings webSettings = webView.getSettings();
                        if (singleChoiceSelected.equals(getResources().getString(R.string.font_size_xsmall))) {
                            sharedPref.updateFontSize(0);
                            webSettings.setDefaultFontSize(Constant.FONT_SIZE_XSMALL);
                        } else if (singleChoiceSelected.equals(getResources().getString(R.string.font_size_small))) {
                            sharedPref.updateFontSize(1);
                            webSettings.setDefaultFontSize(Constant.FONT_SIZE_SMALL);
                        } else if (singleChoiceSelected.equals(getResources().getString(R.string.font_size_medium))) {
                            sharedPref.updateFontSize(2);
                            webSettings.setDefaultFontSize(Constant.FONT_SIZE_MEDIUM);
                        } else if (singleChoiceSelected.equals(getResources().getString(R.string.font_size_large))) {
                            sharedPref.updateFontSize(3);
                            webSettings.setDefaultFontSize(Constant.FONT_SIZE_LARGE);
                        } else if (singleChoiceSelected.equals(getResources().getString(R.string.font_size_xlarge))) {
                            sharedPref.updateFontSize(4);
                            webSettings.setDefaultFontSize(Constant.FONT_SIZE_XLARGE);
                        } else {
                            sharedPref.updateFontSize(2);
                            webSettings.setDefaultFontSize(Constant.FONT_SIZE_MEDIUM);
                        }
                        dialogInterface.dismiss();
                    })
                    .show();
        });

        btnOverflow.setOnClickListener(view -> tools.showBottomSheetDialog(parentView, post.id, post.title, post.labels, post.content, post.published, post.url, true, true));

    }

    public void onFavoriteClicked(String id, String title, List<String> category, String content, String published) {
        List<Post> posts = dbFavorite.getFavRow(post.id);
        if (posts.size() == 0) {
            dbFavorite.AddToFavorite(new Post(id, title, category, content, published));
            Snackbar.make(parentView, R.string.msg_favorite_added, Snackbar.LENGTH_SHORT).show();
            btnFavorite.setImageResource(R.drawable.ic_menu_favorite);
        } else {
            if (posts.get(0).getId().equals(post.id)) {
                dbFavorite.RemoveFav(new Post(id));
                Snackbar.make(parentView, R.string.msg_favorite_removed, Snackbar.LENGTH_SHORT).show();
                btnFavorite.setImageResource(R.drawable.ic_menu_favorite_outline);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
        return true;
    }

}
