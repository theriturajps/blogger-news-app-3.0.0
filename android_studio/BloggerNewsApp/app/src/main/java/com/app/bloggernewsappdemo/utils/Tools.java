package com.app.bloggernewsappdemo.utils;

import static com.app.bloggernewsappdemo.utils.Constant.PAGER_NUMBER_DEFAULT;
import static com.app.bloggernewsappdemo.utils.Constant.PAGER_NUMBER_NO_PAGE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.viewpager.widget.ViewPager;

import com.app.bloggernewsappdemo.BuildConfig;
import com.app.bloggernewsappdemo.Config;
import com.app.bloggernewsappdemo.R;
import com.app.bloggernewsappdemo.activities.ActivityFavoriteDetail;
import com.app.bloggernewsappdemo.activities.ActivityImageDetail;
import com.app.bloggernewsappdemo.activities.ActivityPostDetail;
import com.app.bloggernewsappdemo.activities.ActivityWebView;
import com.app.bloggernewsappdemo.database.prefs.SharedPref;
import com.app.bloggernewsappdemo.database.sqlite.DbFavorite;
import com.app.bloggernewsappdemo.models.Post;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.solodroid.ads.sdk.ui.BannerAdView;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

@SuppressWarnings("deprecation")
public class Tools {

    private Activity activity;
    MenuItem prevMenuItem;
    SharedPref sharedPref;
    DbFavorite dbFavorite;
    private BottomSheetDialog mBottomSheetDialog;

    public Tools(Activity activity) {
        this.activity = activity;
        this.sharedPref = new SharedPref(activity);
    }

    public static void getTheme(Activity activity) {
        SharedPref sharedPref = new SharedPref(activity);
        if (sharedPref.getIsDarkTheme()) {
            activity.setTheme(R.style.AppDarkTheme);
        } else {
            activity.setTheme(R.style.AppTheme);
        }
    }

    public static void setupAppBarLayout(AppCompatActivity activity) {
        ViewStub viewStub = activity.findViewById(R.id.header_view);
        if (Config.ENABLE_NEW_APP_DESIGN) {
            viewStub.setLayoutResource(R.layout.include_appbar_light);
        } else {
            viewStub.setLayoutResource(R.layout.include_appbar_default);
        }
        viewStub.inflate();
    }

    public static void setupAppSearchBarLayout(AppCompatActivity activity) {
        ViewStub viewStub = activity.findViewById(R.id.header_view);
        if (Config.ENABLE_NEW_APP_DESIGN) {
            viewStub.setLayoutResource(R.layout.include_appbar_search_light);
        } else {
            viewStub.setLayoutResource(R.layout.include_appbar_search_default);
        }
        viewStub.inflate();
    }

    public static void setupAppDetailBarLayout(AppCompatActivity activity) {
        ViewStub viewStub = activity.findViewById(R.id.header_view);
        if (Config.ENABLE_NEW_APP_DESIGN) {
            viewStub.setLayoutResource(R.layout.include_appbar_detail_light);
        } else {
            viewStub.setLayoutResource(R.layout.include_appbar_detail_default);
        }
        viewStub.inflate();
    }

    public static void setupToolbar(AppCompatActivity activity, Toolbar toolbar, String title, boolean backButton) {
        SharedPref sharedPref = new SharedPref(activity);
        activity.setSupportActionBar(toolbar);
        if (sharedPref.getIsDarkTheme()) {
            toolbar.setBackgroundColor(activity.getResources().getColor(R.color.color_dark_toolbar));
        } else {
            if (Config.ENABLE_NEW_APP_DESIGN) {
                toolbar.setBackgroundColor(activity.getResources().getColor(R.color.color_white));
            } else {
                toolbar.setBackgroundColor(activity.getResources().getColor(R.color.color_light_primary));
            }
        }
        final ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(backButton);
            activity.getSupportActionBar().setHomeButtonEnabled(backButton);
            activity.getSupportActionBar().setTitle(title);
        }
    }

    public void setupViewPager(AppCompatActivity activity, ViewPager viewPager, BottomNavigationView navigation, Toolbar toolbar, TextView titleToolbar, SharedPref sharedPref) {
        viewPager.setVisibility(View.VISIBLE);
        if (sharedPref.getIsShowPageMenu()) {
            viewPager.setAdapter(new NavigationAdapter.BottomNavigationAdapterDefault(activity.getSupportFragmentManager()));
            viewPager.setOffscreenPageLimit(PAGER_NUMBER_DEFAULT);
            navigation.setOnNavigationItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.navigation_recent) {
                    viewPager.setCurrentItem(0);
                } else if (itemId == R.id.navigation_category) {
                    viewPager.setCurrentItem(1);
                } else if (itemId == R.id.navigation_page) {
                    viewPager.setCurrentItem(2);
                } else if (itemId == R.id.navigation_favorite) {
                    viewPager.setCurrentItem(3);
                } else {
                    viewPager.setCurrentItem(0);
                }
                return false;
            });
            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    if (prevMenuItem != null) {
                        prevMenuItem.setChecked(false);
                    } else {
                        navigation.getMenu().getItem(0).setChecked(false);
                    }
                    navigation.getMenu().getItem(position).setChecked(true);
                    prevMenuItem = navigation.getMenu().getItem(position);

                    int currentItem = viewPager.getCurrentItem();
                    if (currentItem == 0) {
                        toolbar.setTitle(activity.getResources().getString(R.string.app_name));
                        titleToolbar.setText(activity.getResources().getString(R.string.app_name));
                    } else if (currentItem == 1) {
                        toolbar.setTitle(activity.getResources().getString(R.string.title_nav_category));
                        titleToolbar.setText(activity.getResources().getString(R.string.title_nav_category));
                    } else if (currentItem == 2) {
                        toolbar.setTitle(activity.getResources().getString(R.string.title_nav_page));
                        titleToolbar.setText(activity.getResources().getString(R.string.title_nav_page));
                    } else if (currentItem == 3) {
                        toolbar.setTitle(activity.getResources().getString(R.string.title_nav_favorite));
                        titleToolbar.setText(activity.getResources().getString(R.string.title_nav_favorite));
                    } else {
                        toolbar.setTitle(activity.getResources().getString(R.string.app_name));
                        titleToolbar.setText(activity.getResources().getString(R.string.app_name));
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        } else {
            viewPager.setAdapter(new NavigationAdapter.BottomNavigationAdapterNoPage(activity.getSupportFragmentManager()));
            viewPager.setOffscreenPageLimit(PAGER_NUMBER_NO_PAGE);
            navigation.setOnNavigationItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.navigation_recent) {
                    viewPager.setCurrentItem(0);
                } else if (itemId == R.id.navigation_category) {
                    viewPager.setCurrentItem(1);
                } else if (itemId == R.id.navigation_favorite) {
                    viewPager.setCurrentItem(2);
                } else {
                    viewPager.setCurrentItem(0);
                }
                return false;
            });
            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    if (prevMenuItem != null) {
                        prevMenuItem.setChecked(false);
                    } else {
                        navigation.getMenu().getItem(0).setChecked(false);
                    }
                    navigation.getMenu().getItem(position).setChecked(true);
                    prevMenuItem = navigation.getMenu().getItem(position);

                    int currentItem = viewPager.getCurrentItem();
                    if (currentItem == 0) {
                        toolbar.setTitle(activity.getResources().getString(R.string.app_name));
                        titleToolbar.setText(activity.getResources().getString(R.string.app_name));
                    } else if (currentItem == 1) {
                        toolbar.setTitle(activity.getResources().getString(R.string.title_nav_category));
                        titleToolbar.setText(activity.getResources().getString(R.string.title_nav_category));
                    } else if (currentItem == 2) {
                        toolbar.setTitle(activity.getResources().getString(R.string.title_nav_favorite));
                        titleToolbar.setText(activity.getResources().getString(R.string.title_nav_favorite));
                    } else {
                        toolbar.setTitle(activity.getResources().getString(R.string.app_name));
                        titleToolbar.setText(activity.getResources().getString(R.string.app_name));
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        }
    }

    public void setupViewPagerRTL(AppCompatActivity activity, RtlViewPager viewPager, BottomNavigationView navigation, Toolbar toolbar, TextView titleToolbar, SharedPref sharedPref) {
        viewPager.setVisibility(View.VISIBLE);
        if (sharedPref.getIsShowPageMenu()) {
            viewPager.setAdapter(new NavigationAdapter.BottomNavigationAdapterDefault(activity.getSupportFragmentManager()));
            viewPager.setOffscreenPageLimit(PAGER_NUMBER_DEFAULT);
            navigation.setOnNavigationItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.navigation_recent) {
                    viewPager.setCurrentItem(0);
                } else if (itemId == R.id.navigation_category) {
                    viewPager.setCurrentItem(1);
                } else if (itemId == R.id.navigation_page) {
                    viewPager.setCurrentItem(2);
                } else if (itemId == R.id.navigation_favorite) {
                    viewPager.setCurrentItem(3);
                } else {
                    viewPager.setCurrentItem(0);
                }
                return false;
            });
            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    if (prevMenuItem != null) {
                        prevMenuItem.setChecked(false);
                    } else {
                        navigation.getMenu().getItem(0).setChecked(false);
                    }
                    navigation.getMenu().getItem(position).setChecked(true);
                    prevMenuItem = navigation.getMenu().getItem(position);

                    int currentItem = viewPager.getCurrentItem();
                    if (currentItem == 0) {
                        toolbar.setTitle(activity.getResources().getString(R.string.app_name));
                        titleToolbar.setText(activity.getResources().getString(R.string.app_name));
                    } else if (currentItem == 1) {
                        toolbar.setTitle(activity.getResources().getString(R.string.title_nav_category));
                        titleToolbar.setText(activity.getResources().getString(R.string.title_nav_category));
                    } else if (currentItem == 2) {
                        toolbar.setTitle(activity.getResources().getString(R.string.title_nav_page));
                        titleToolbar.setText(activity.getResources().getString(R.string.title_nav_page));
                    } else if (currentItem == 3) {
                        toolbar.setTitle(activity.getResources().getString(R.string.title_nav_favorite));
                        titleToolbar.setText(activity.getResources().getString(R.string.title_nav_favorite));
                    } else {
                        toolbar.setTitle(activity.getResources().getString(R.string.app_name));
                        titleToolbar.setText(activity.getResources().getString(R.string.app_name));
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        } else {
            viewPager.setAdapter(new NavigationAdapter.BottomNavigationAdapterNoPage(activity.getSupportFragmentManager()));
            viewPager.setOffscreenPageLimit(PAGER_NUMBER_NO_PAGE);
            navigation.setOnNavigationItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.navigation_recent) {
                    viewPager.setCurrentItem(0);
                } else if (itemId == R.id.navigation_category) {
                    viewPager.setCurrentItem(1);
                } else if (itemId == R.id.navigation_favorite) {
                    viewPager.setCurrentItem(2);
                } else {
                    viewPager.setCurrentItem(0);
                }
                return false;
            });
            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    if (prevMenuItem != null) {
                        prevMenuItem.setChecked(false);
                    } else {
                        navigation.getMenu().getItem(0).setChecked(false);
                    }
                    navigation.getMenu().getItem(position).setChecked(true);
                    prevMenuItem = navigation.getMenu().getItem(position);

                    int currentItem = viewPager.getCurrentItem();
                    if (currentItem == 0) {
                        toolbar.setTitle(activity.getResources().getString(R.string.app_name));
                        titleToolbar.setText(activity.getResources().getString(R.string.app_name));
                    } else if (currentItem == 1) {
                        toolbar.setTitle(activity.getResources().getString(R.string.title_nav_category));
                        titleToolbar.setText(activity.getResources().getString(R.string.title_nav_category));
                    } else if (currentItem == 2) {
                        toolbar.setTitle(activity.getResources().getString(R.string.title_nav_favorite));
                        titleToolbar.setText(activity.getResources().getString(R.string.title_nav_favorite));
                    } else {
                        toolbar.setTitle(activity.getResources().getString(R.string.app_name));
                        titleToolbar.setText(activity.getResources().getString(R.string.app_name));
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        }
    }

    public void showBottomSheetDialog(View parentView, String id, String title, List<String> category, String content, String published, String url, boolean isDetailView, boolean isOffline) {
        @SuppressLint("InflateParams") View view = activity.getLayoutInflater().inflate(R.layout.dialog_more_options, null);
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

        btnFavorite.setOnClickListener(v -> {
            if (isDetailView) {
                if (isOffline) {
                    ((ActivityFavoriteDetail) activity).onFavoriteClicked(id, title, category, content, published);
                } else {
                    ((ActivityPostDetail) activity).onFavoriteClicked(id, title, category, content, published);
                }
            } else {
                List<Post> posts = dbFavorite.getFavRow(id);
                if (posts.size() == 0) {
                    dbFavorite.AddToFavorite(new Post(id, title, category, content, published));
                    Snackbar.make(parentView, activity.getString(R.string.msg_favorite_added), Snackbar.LENGTH_SHORT).show();
                    imgFavorite.setImageResource(R.drawable.ic_favorite_grey);

                } else {
                    if (posts.get(0).getId().equals(id)) {
                        dbFavorite.RemoveFav(new Post(id));
                        Snackbar.make(parentView, activity.getString(R.string.msg_favorite_removed), Snackbar.LENGTH_SHORT).show();
                        imgFavorite.setImageResource(R.drawable.ic_favorite_outline_grey);
                    }
                }
            }
            mBottomSheetDialog.dismiss();
        });

        btnShare.setOnClickListener(v -> {
            Tools.shareArticle(activity, title, url);
            mBottomSheetDialog.dismiss();
        });

        btnLaunch.setOnClickListener(v -> {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            mBottomSheetDialog.dismiss();
        });

        btnReport.setOnClickListener(v -> {
            Tools.sendReport(activity, sharedPref.getEmailFeedbackAndReport(), title, "");
            mBottomSheetDialog.dismiss();
        });

        btnFeedback.setOnClickListener(v -> {
            Tools.sendFeedback(activity, sharedPref.getEmailFeedbackAndReport());
            mBottomSheetDialog.dismiss();
        });

        btnClose.setOnClickListener(v -> mBottomSheetDialog.dismiss());

        if (this.sharedPref.getIsDarkTheme()) {
            this.mBottomSheetDialog = new BottomSheetDialog(activity, R.style.SheetDialogDark);
        } else {
            this.mBottomSheetDialog = new BottomSheetDialog(activity, R.style.SheetDialogLight);
        }
        this.mBottomSheetDialog.setContentView(view);

        mBottomSheetDialog.show();
        mBottomSheetDialog.setOnDismissListener(dialog -> mBottomSheetDialog = null);

        dbFavorite = new DbFavorite(activity);
        List<Post> posts = dbFavorite.getFavRow(id);
        if (posts.size() == 0) {
            txtFavorite.setText(activity.getString(R.string.favorite_add));
            imgFavorite.setImageResource(R.drawable.ic_favorite_outline_grey);
        } else {
            if (posts.get(0).id.equals(id)) {
                txtFavorite.setText(activity.getString(R.string.favorite_remove));
                imgFavorite.setImageResource(R.drawable.ic_favorite_grey);
            }
        }

    }

    public static void displayPostDescription(Activity activity, WebView webView, String htmlData, FrameLayout viewContainer, SharedPref sharedPref) {
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.getSettings().setDefaultTextEncodingName("UTF-8");
        webView.setFocusableInTouchMode(false);
        webView.setFocusable(false);

        if (!Config.ENABLE_TEXT_SELECTION) {
            webView.setOnLongClickListener(v -> true);
            webView.setLongClickable(false);
        }

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        WebSettings webSettings = webView.getSettings();
        if (sharedPref.getFontSize() == 0) {
            webSettings.setDefaultFontSize(Constant.FONT_SIZE_XSMALL);
        } else if (sharedPref.getFontSize() == 1) {
            webSettings.setDefaultFontSize(Constant.FONT_SIZE_SMALL);
        } else if (sharedPref.getFontSize() == 2) {
            webSettings.setDefaultFontSize(Constant.FONT_SIZE_MEDIUM);
        } else if (sharedPref.getFontSize() == 3) {
            webSettings.setDefaultFontSize(Constant.FONT_SIZE_LARGE);
        } else if (sharedPref.getFontSize() == 4) {
            webSettings.setDefaultFontSize(Constant.FONT_SIZE_XLARGE);
        } else {
            webSettings.setDefaultFontSize(Constant.FONT_SIZE_MEDIUM);
        }

        String bgParagraph;
        String mimeType = "text/html; charset=UTF-8";
        String encoding = "utf-8";

        if (sharedPref.getIsDarkTheme()) {
            bgParagraph = "<style type=\"text/css\">body{color: #eeeeee;} a{color:#ffffff; font-weight:bold;}";
        } else {
            bgParagraph = "<style type=\"text/css\">body{color: #000000;} a{color:#1e88e5; font-weight:bold;}";
        }

        String fontStyleDefault = "<style type=\"text/css\">@font-face {font-family: MyFont;src: url(\"file:///android_asset/font/custom_font.ttf\")}body {font-family: MyFont; font-size: medium; overflow-wrap: break-word; word-wrap: break-word; -ms-word-break: break-all; word-break: break-all; word-break: break-word; -ms-hyphens: auto; -moz-hyphens: auto; -webkit-hyphens: auto; hyphens: auto;}</style>";

        String textDefault = "<html><head>"
                + fontStyleDefault
                + "<style>img{max-width:100%;height:auto;border-radius:8px;margin-top:8px;margin-bottom:8px;} figure{max-width:100%;height:auto;} iframe{width:100%;}</style> "
                + bgParagraph
                + "</style></head>"
                + "<body>"
                + Tools.parseHtml(htmlData)
                + "</body></html>";

        String textRtl = "<html dir='rtl'><head>"
                + fontStyleDefault
                + "<style>img{max-width:100%;height:auto;border-radius:8px;margin-top:8px;margin-bottom:8px;} figure{max-width:100%;height:auto;} iframe{width:100%;}</style> "
                + bgParagraph
                + "</style></head>"
                + "<body>"
                + Tools.parseHtml(htmlData)
                + "</body></html>";

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                final boolean isImage = url.endsWith(".jpg") || url.endsWith(".jpeg") || url.endsWith(".png") || url.endsWith(".gif");
                if (sharedPref.openLinkInsideApp()) {
                    if (url.startsWith("http://") || url.startsWith("https://")) {
                        Intent intent;
                        if (isImage) {
                            intent = new Intent(activity, ActivityImageDetail.class);
                            intent.putExtra("image", url);
                        } else {
                            if (url.contains("play.google.com") || url.contains("?target=external")) {
                                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                            } else {
                                intent = new Intent(activity, ActivityWebView.class);
                                intent.putExtra("title", "");
                                intent.putExtra("url", url);
                            }
                        }
                        activity.startActivity(intent);
                    }
                } else {
                    Intent intent;
                    if (isImage) {
                        intent = new Intent(activity, ActivityImageDetail.class);
                        intent.putExtra("image", url);
                    } else {
                        intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(url));
                    }
                    activity.startActivity(intent);
                }

                return true;
            }
        });

        BannerAdView bannerAdView = activity.findViewById(R.id.bannerAdView);

        webView.setWebChromeClient(new WebChromeClient() {
            public void onShowCustomView(View view, CustomViewCallback callback) {
                super.onShowCustomView(view, callback);
                webView.setVisibility(View.INVISIBLE);
                bannerAdView.setVisibility(View.GONE);
                viewContainer.setVisibility(View.VISIBLE);
                viewContainer.addView(view);
                Tools.darkNavigation(activity);
            }

            public void onHideCustomView() {
                super.onHideCustomView();
                webView.setVisibility(View.VISIBLE);
                bannerAdView.setVisibility(View.VISIBLE);
                viewContainer.setVisibility(View.GONE);
                Tools.lightNavigation(activity);
            }
        });

        webView.getSettings().setMediaPlaybackRequiresUserGesture(false);

        if (sharedPref.getIsEnableRtlMode()) {
            webView.loadDataWithBaseURL(null, textRtl, mimeType, encoding, null);
        } else {
            webView.loadDataWithBaseURL(null, textDefault, mimeType, encoding, null);
        }
    }

    public static void shareArticle(Activity activity, String title, String url) {
        SharedPref sharedPref = new SharedPref(activity);
        String content = Html.fromHtml(activity.getResources().getString(R.string.app_share)).toString();
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        if (sharedPref.getIsShowViewOnSiteMenu()) {
            intent.putExtra(Intent.EXTRA_TEXT, title + "\n" + url + "\n\n" + content + "\n" + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID);
        } else {
            intent.putExtra(Intent.EXTRA_TEXT, title + "\n\n" + content + "\n" + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID);
        }
        intent.setType("text/plain");
        activity.startActivity(intent);
    }

    public static void sendReport(Context activity, String email, String title, String reason) {
        String str;
        try {
            str = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0).versionName;
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:"));
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
            intent.putExtra(Intent.EXTRA_SUBJECT, "Report " + title + " in the " + activity.getResources().getString(R.string.app_name));
            intent.putExtra(Intent.EXTRA_TEXT, "Device OS : Android \n Device OS version : " +
                    Build.VERSION.RELEASE + "\n App Version : " + str + "\n Device Brand : " + Build.BRAND +
                    "\n Device Model : " + Build.MODEL + "\n Device Manufacturer : " + Build.MANUFACTURER + "\n" + "Reason : " + reason);
            try {
                activity.startActivity(Intent.createChooser(intent, "Report"));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(activity.getApplicationContext(), "There are no email clients installed.", Toast.LENGTH_SHORT).show();
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void sendFeedback(Context activity, String email) {
        String str;
        try {
            str = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0).versionName;
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:"));
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
            intent.putExtra(Intent.EXTRA_SUBJECT, "Feedback for " + activity.getResources().getString(R.string.app_name));
            intent.putExtra(Intent.EXTRA_TEXT, "Device OS : Android \n Device OS version : " +
                    Build.VERSION.RELEASE + "\n App Version : " + str + "\n Device Brand : " + Build.BRAND +
                    "\n Device Model : " + Build.MODEL + "\n Device Manufacturer : " + Build.MANUFACTURER + "\n" + "Message : ");
            try {
                activity.startActivity(Intent.createChooser(intent, "Send feedback"));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(activity.getApplicationContext(), "There are no email clients installed.", Toast.LENGTH_SHORT).show();
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void openAssetActivity(Context context, String title, String fileName) {
        Intent intent = new Intent(context, ActivityWebView.class);
        intent.putExtra("title", title);
        intent.putExtra("file_name", fileName);
        context.startActivity(intent);
    }

    public static String parseHtml(String htmlData) {
        if (htmlData != null && !htmlData.trim().equals("")) {
            return htmlData.replace("", "");
        } else {
            return "";
        }
    }

    public static void notificationOpenHandler(Context context, Intent getIntent) {

        String uniqueId = getIntent.getStringExtra("unique_id");
        String postId = getIntent.getStringExtra("post_id");
        String title = getIntent.getStringExtra("title");
        String link = getIntent.getStringExtra("link");

        if (getIntent.hasExtra("unique_id")) {

            if (postId != null && !postId.equals("")) {
                if (!postId.equals("0")) {
                    Intent intent = new Intent(context, ActivityPostDetail.class);
                    intent.putExtra("post_id", postId);
                    context.startActivity(intent);
                    new SharedPref(context).savePostId(postId);
                }
            }

            if (link != null && !link.equals("")) {
                if (link.contains("play.google.com") || link.contains("?target=external")) {
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link)));
                } else {
                    Intent intent = new Intent(context, ActivityWebView.class);
                    intent.putExtra("title", title);
                    intent.putExtra("url", link);
                    context.startActivity(intent);
                }
            }

        }

    }

    public static void setNavigation(Activity activity) {
        SharedPref sharedPref = new SharedPref(activity);
        if (sharedPref.getIsDarkTheme()) {
            Tools.darkNavigation(activity);
        } else {
            Tools.lightNavigation(activity);
        }
        setLayoutDirection(activity);
    }

    public static void setLayoutDirection(Activity activity) {
        SharedPref sharedPref = new SharedPref(activity);
        if (sharedPref.getIsEnableRtlMode()) {
            activity.getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
    }

    public static void darkNavigation(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity.getWindow().setNavigationBarColor(ContextCompat.getColor(activity, R.color.color_dark_bottom_navigation));
            activity.getWindow().setStatusBarColor(ContextCompat.getColor(activity, R.color.color_dark_status_bar));
            activity.getWindow().getDecorView().setSystemUiVisibility(0);
        }
    }

    public static void lightNavigation(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity.getWindow().setNavigationBarColor(activity.getResources().getColor(R.color.color_light_bottom_navigation));
            if (!Config.ENABLE_NEW_APP_DESIGN) {
                activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
                activity.getWindow().setStatusBarColor(activity.getResources().getColor(R.color.color_light_status_bar));
            } else {
                activity.getWindow().setStatusBarColor(activity.getResources().getColor(R.color.color_white));
            }
        }
    }

    public static void transparentStatusBarNavigation(Activity activity) {
        activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        setWindowFlag(activity, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, false);
        activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
        activity.getWindow().setNavigationBarColor(Color.TRANSPARENT);
    }

    public static void setWindowFlag(Activity activity, final int bits, boolean on) {
        Window win = activity.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    @SuppressLint("SourceLockedOrientationActivity")
    public static void fullScreenMode(AppCompatActivity activity, boolean show) {
        SharedPref sharedPref = new SharedPref(activity);
        if (show) {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().hide();
            }
            //activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            if (!sharedPref.getIsDarkTheme()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                }
            }
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().show();
            }
            //activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    public static void startExternalApplication(Context context, String url) {
        try {
            String[] results = url.split("package=");
            String packageName = results[1];
            boolean isAppInstalled = appInstalledOrNot(context, packageName);
            if (isAppInstalled) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                intent.setPackage(packageName);
                intent.setData(Uri.parse(url));
                context.startActivity(intent);
            } else {
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packageName)));
            }
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(context, "Whoops! cannot handle this url.", Toast.LENGTH_SHORT).show();
        }
    }

    private static boolean appInstalledOrNot(Context context, String uri) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            Log.d("Error", "NameNotFoundException");
        }
        return false;
    }

    public static void postDelayed(OnCompleteListener onCompleteListener, int millisecond) {
        new Handler(Looper.getMainLooper()).postDelayed(onCompleteListener::onComplete, millisecond);
    }

    public static void nestedScrollTo(final NestedScrollView nested, final View targetView) {
        //nested.post(() -> nested.scrollTo(500, targetView.getBottom()));
    }

    public static String decode(String code) {
        return decodeBase64(decodeBase64(decodeBase64(code)));
    }

    public static String decodeBase64(String code) {
        byte[] valueDecoded = Base64.decode(code.getBytes(StandardCharsets.UTF_8), Base64.DEFAULT);
        return new String(valueDecoded);
    }

    public static String withSuffix(long count) {
        if (count < 1000) return "" + count;
        int exp = (int) (Math.log(count) / Math.log(1000));
        return String.format("%.1f%c", count / Math.pow(1000, exp), "KMGTPE".charAt(exp - 1));
    }

    public static long timeStringtoMilis(String time) {
        long milis = 0;
        try {
            SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = sd.parse(time);
            milis = date.getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return milis;
    }

    public static int dpToPx(Context c, int dp) {
        Resources r = c.getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    public static boolean isConnect(Context context) {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if (activeNetworkInfo != null) {
                return activeNetworkInfo.isConnected() || activeNetworkInfo.isConnectedOrConnecting();
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public static String convertDateTime(String str, String str2) {
        if (str2 == null) {
            str2 = "dd MMM yyyy";
        }
        try {
            return new SimpleDateFormat(str2).format(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(str));
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static CharSequence getTimeAgo(String dateStr) {
        if (dateStr != null && !dateStr.trim().equals("")) {
            //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
            sdf.setTimeZone(TimeZone.getTimeZone("CET"));
            try {
                long time = sdf.parse(dateStr).getTime();
                long now = System.currentTimeMillis();
                return DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS);
            } catch (ParseException e) {
                return "";
            }
        } else {
            return "";
        }
    }

    public static String getFormattedDate(String dateStr) {
        if (dateStr != null && !dateStr.trim().equals("")) {
            SimpleDateFormat oldFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
            oldFormat.setTimeZone(TimeZone.getTimeZone("CET"));
            SimpleDateFormat newFormat = new SimpleDateFormat("MMMM dd, yyyy HH:mm");
            try {
                String newStr = newFormat.format(oldFormat.parse(dateStr));
                return newStr;
            } catch (ParseException e) {
                return "";
            }
        } else {
            return "";
        }
    }

    public static void openWebPage(Activity context, String title, String url) {
        Intent intent = new Intent(context, ActivityWebView.class);
        intent.putExtra("title", title);
        intent.putExtra("url", url);
        context.startActivity(intent);
    }

    public static void downloadImage(Activity activity, String filename, String downloadUrlOfImage, String mimeType) {
        try {
            DownloadManager dm = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
            Uri downloadUri = Uri.parse(downloadUrlOfImage);
            DownloadManager.Request request = new DownloadManager.Request(downloadUri);
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                    .setAllowedOverRoaming(false)
                    .setTitle(filename)
                    .setMimeType(mimeType) // Your file type. You can use this code to download other file types also.
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, File.separator + filename + ".jpg");
            dm.enqueue(request);
            //Toast.makeText(activity, "Image download started.", Toast.LENGTH_SHORT).show();
            Snackbar.make(activity.findViewById(android.R.id.content), "Image download started.", Snackbar.LENGTH_SHORT).show();
        } catch (Exception e) {
            //Toast.makeText(activity, "", Toast.LENGTH_SHORT).show();
            Snackbar.make(activity.findViewById(android.R.id.content), "Image download failed.", Snackbar.LENGTH_SHORT).show();
        }
    }

    public static void setNativeAdStyle(Context context, LinearLayout nativeAdView, String style) {
        switch (style) {
            case "small":
                nativeAdView.addView(View.inflate(context, com.solodroid.ads.sdk.R.layout.view_native_ad_radio, null));
                break;
            case "medium":
                nativeAdView.addView(View.inflate(context, com.solodroid.ads.sdk.R.layout.view_native_ad_news, null));
                break;
            default:
                nativeAdView.addView(View.inflate(context, com.solodroid.ads.sdk.R.layout.view_native_ad_medium, null));
                break;
        }
    }

}
