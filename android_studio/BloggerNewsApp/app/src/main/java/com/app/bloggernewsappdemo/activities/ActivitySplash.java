package com.app.bloggernewsappdemo.activities;

import static com.app.bloggernewsappdemo.Config.DELAY_SPLASH;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.app.bloggernewsappdemo.BuildConfig;
import com.app.bloggernewsappdemo.Config;
import com.app.bloggernewsappdemo.R;
import com.app.bloggernewsappdemo.callbacks.CallbackConfig;
import com.app.bloggernewsappdemo.callbacks.CallbackLabel;
import com.app.bloggernewsappdemo.database.prefs.AdsPref;
import com.app.bloggernewsappdemo.database.prefs.SharedPref;
import com.app.bloggernewsappdemo.database.sqlite.DbLabel;
import com.app.bloggernewsappdemo.models.Ads;
import com.app.bloggernewsappdemo.models.App;
import com.app.bloggernewsappdemo.models.Blog;
import com.app.bloggernewsappdemo.models.CustomCategory;
import com.app.bloggernewsappdemo.rests.RestAdapter;
import com.app.bloggernewsappdemo.utils.AdsManager;
import com.app.bloggernewsappdemo.utils.Tools;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivitySplash extends AppCompatActivity {

    public static final String TAG = "SplashActivity";
    Call<CallbackConfig> callbackConfigCall = null;
    Call<CallbackLabel> callbackLabelCall = null;
    ImageView imgSplash;
    AdsManager adsManager;
    SharedPref sharedPref;
    AdsPref adsPref;
    App app;
    CustomCategory customCategory;
    Blog blog;
    Ads ads;
    DbLabel dbLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.getTheme(this);
        setContentView(R.layout.activity_splash);
        dbLabel = new DbLabel(this);
        sharedPref = new SharedPref(this);
        adsPref = new AdsPref(this);
        adsManager = new AdsManager(this);
        adsManager.initializeAd();

        imgSplash = findViewById(R.id.img_splash);
        if (sharedPref.getIsDarkTheme()) {
            imgSplash.setImageResource(R.drawable.bg_splash_dark);
            Tools.darkNavigation(this);
        } else {
            imgSplash.setImageResource(R.drawable.bg_splash_default);
            Tools.lightNavigation(this);
        }

        requestConfig();
    }

    private void requestConfig() {
        String data = Tools.decode(Config.ACCESS_KEY);
        String[] results = data.split("_applicationId_");
        String remoteUrl = results[0];
        String applicationId = results[1];

        if (applicationId.equals(BuildConfig.APPLICATION_ID)) {
            requestAPI(remoteUrl);
        } else {
            new MaterialAlertDialogBuilder(this, R.style.Material3AlertDialog)
                    .setTitle("Error")
                    .setMessage("Whoops! invalid access key or applicationId, please check your configuration")
                    .setPositiveButton(getString(R.string.dialog_option_ok), (dialog, which) -> finish())
                    .setCancelable(false)
                    .show();
        }
        Log.d(TAG, "Start request config");
    }

    private void requestAPI(String remoteUrl) {
        if (remoteUrl.startsWith("http://") || remoteUrl.startsWith("https://")) {
            if (remoteUrl.contains("https://drive.google.com")) {
                String driveUrl = remoteUrl.replace("https://", "").replace("http://", "");
                List<String> data = Arrays.asList(driveUrl.split("/"));
                String googleDriveFileId = data.get(3);
                callbackConfigCall = RestAdapter.createApiGoogleDrive().getDriveJsonFileId(googleDriveFileId);
                Log.d(TAG, "Request API from Google Drive Share link");
                Log.d(TAG, "Google drive file id : " + data.get(3));
            } else {
                callbackConfigCall = RestAdapter.createApiJsonUrl().getJsonUrl(remoteUrl);
                Log.d(TAG, "Request API from Json Url");
            }
        } else {
            callbackConfigCall = RestAdapter.createApiGoogleDrive().getDriveJsonFileId(remoteUrl);
            Log.d(TAG, "Request API from Google Drive File ID");
        }
        callbackConfigCall.enqueue(new Callback<CallbackConfig>() {
            public void onResponse(@NonNull Call<CallbackConfig> call, @NonNull Response<CallbackConfig> response) {
                CallbackConfig resp = response.body();
                displayApiResults(resp);
            }

            public void onFailure(@NonNull Call<CallbackConfig> call, @NonNull Throwable th) {
                Log.e(TAG, "initialize failed");
                showAppOpenAdIfAvailable(false);
            }
        });
    }

    private void displayApiResults(CallbackConfig resp) {
        if (resp != null) {
            app = resp.app;
            ads = resp.ads;
            blog = resp.blog;
            customCategory = resp.custom_category;

            sharedPref.saveBlogCredentials(blog.blogger_id, blog.api_key);
            adsManager.saveConfig(sharedPref, app);
            adsManager.saveAds(adsPref, ads);
            adsManager.saveAdsPlacement(adsPref, ads.placement);

            if (!app.status) {
                startActivity(new Intent(getApplicationContext(), ActivityRedirect.class));
                finish();
                Log.d(TAG, "App status is suspended");
            } else {
                if (customCategory.status) {
                    dbLabel.truncateTableCategory(DbLabel.TABLE_LABEL);
                    dbLabel.addListCategory(customCategory.categories, DbLabel.TABLE_LABEL);
                    showAppOpenAdIfAvailable(true);
                } else {
                    requestLabel();
                }
                Log.d(TAG, "App status is live");
            }
            Log.d(TAG, "initialize success");
        } else {
            Log.d(TAG, "initialize failed");
            showAppOpenAdIfAvailable(false);
        }
    }

    private void requestLabel() {
        this.callbackLabelCall = RestAdapter.createApiCategory(sharedPref.getBloggerId()).getLabel();
        this.callbackLabelCall.enqueue(new Callback<CallbackLabel>() {
            public void onResponse(@NonNull Call<CallbackLabel> call, @NonNull Response<CallbackLabel> response) {
                CallbackLabel resp = response.body();
                if (resp == null) {
                    showAppOpenAdIfAvailable(false);
                    return;
                }
                dbLabel.truncateTableCategory(DbLabel.TABLE_LABEL);
                dbLabel.addListCategory(resp.feed.category, DbLabel.TABLE_LABEL);
                showAppOpenAdIfAvailable(true);
                Log.d(TAG, "Success initialize label with count " + resp.feed.category.size() + " items");
            }

            public void onFailure(@NonNull Call<CallbackLabel> call, @NonNull Throwable th) {
                Log.e("onFailure", th.getMessage());
                if (!call.isCanceled()) {
                    showAppOpenAdIfAvailable(false);
                }
            }
        });
    }

    private void showAppOpenAdIfAvailable(boolean showAd) {
        Tools.postDelayed(() -> {
            if (showAd) {
                adsManager.loadAppOpenAd(adsPref.getIsAppOpenAdOnStart(), this::startMainActivity);
            } else {
                startMainActivity();
            }
        }, 100);
    }

    private void startMainActivity() {
        Tools.postDelayed(() -> {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }, DELAY_SPLASH);
    }

}
