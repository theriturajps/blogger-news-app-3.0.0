package com.app.bloggernewsappdemo.database.prefs;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPref {

    Context context;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    public SharedPref(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences("blog_settings", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void saveBlogCredentials(String bloggerId, String apiKey) {
        editor.putString("blogger_id", bloggerId);
        editor.putString("api_key", apiKey);
        editor.apply();
    }

    public Boolean getIsDarkTheme() {
        return sharedPreferences.getBoolean("theme", getIsEnableDarkModeAsDefaultTheme());
    }

    public void setIsDarkTheme(Boolean isDarkTheme) {
        editor.putBoolean("theme", isDarkTheme);
        editor.apply();
    }

    public void saveConfig(String redirectUrl, String moreAppsUrl, String privacyPolicyUrl, String publisherInfoUrl, String termsConditionsUrl, String email_feedback_and_report, int category_column_count, boolean show_page_menu, boolean show_view_on_site_menu, boolean show_post_list_in_large_style, boolean show_post_list_header, boolean shortDescription, boolean postDate, boolean relatedPosts, boolean openLinkInsideApp, boolean enableExitDialog, boolean enableRtlMode, boolean enableDarkModeAsDefaultTheme) {
        editor.putString("redirect_url", redirectUrl);
        editor.putString("more_apps_url", moreAppsUrl);
        editor.putString("privacy_policy_url", privacyPolicyUrl);
        editor.putString("publisher_info_url", publisherInfoUrl);
        editor.putString("terms_conditions_url", termsConditionsUrl);
        editor.putString("email_feedback_and_report", email_feedback_and_report);
        editor.putInt("category_column_count", category_column_count);
        editor.putBoolean("show_page_menu", show_page_menu);
        editor.putBoolean("show_view_on_site_menu", show_view_on_site_menu);
        editor.putBoolean("show_post_list_in_large_style", show_post_list_in_large_style);
        editor.putBoolean("show_post_list_header", show_post_list_header);
        editor.putBoolean("show_post_list_short_description", shortDescription);
        editor.putBoolean("show_post_date", postDate);
        editor.putBoolean("show_related_post", relatedPosts);
        editor.putBoolean("open_link_inside_app", openLinkInsideApp);
        editor.putBoolean("enable_exit_dialog", enableExitDialog);
        editor.putBoolean("enable_rtl_mode", enableRtlMode);
        editor.putBoolean("enable_dark_mode_as_default_theme", enableDarkModeAsDefaultTheme);
        editor.apply();
    }

    public String getRedirectUrl() {
        return sharedPreferences.getString("redirect_url", "");
    }

    public String getMoreAppsUrl() {
        return sharedPreferences.getString("more_apps_url", "");
    }

    public String getPrivacyPolicyUrl() {
        return sharedPreferences.getString("privacy_policy_url", "");
    }

    public String getPublisherInfoUrl() {
        return sharedPreferences.getString("publisher_info_url", "");
    }

    public String getTermsConditionsUrl() {
        return sharedPreferences.getString("terms_conditions_url", "");
    }

    public String getEmailFeedbackAndReport() {
        return sharedPreferences.getString("email_feedback_and_report", "");
    }

    public int getCategoryColumnCount() {
        return sharedPreferences.getInt("category_column_count", 3);
    }

    public boolean getIsShowPageMenu() {
        return sharedPreferences.getBoolean("show_page_menu", true);
    }

    public boolean getIsShowViewOnSiteMenu() {
        return sharedPreferences.getBoolean("show_view_on_site_menu", true);
    }

    public boolean getIsShowPostListInLargeStyle() {
        return sharedPreferences.getBoolean("show_post_list_in_large_style", false);
    }

    public boolean showPostHeader() {
        return sharedPreferences.getBoolean("show_post_list_header", true);
    }

    public boolean showShortDescription() {
        return sharedPreferences.getBoolean("show_post_list_short_description", true);
    }

    public boolean showPostDate() {
        return sharedPreferences.getBoolean("show_post_date", true);
    }

    public boolean showRelatedPosts() {
        return sharedPreferences.getBoolean("show_related_post", true);
    }

    public boolean openLinkInsideApp() {
        return sharedPreferences.getBoolean("open_link_inside_app", true);
    }

    public boolean getIsEnableExitDialog() {
        return sharedPreferences.getBoolean("enable_exit_dialog", true);
    }

    public boolean getIsEnableRtlMode() {
        return sharedPreferences.getBoolean("enable_rtl_mode", false);
    }

    public boolean getIsEnableDarkModeAsDefaultTheme() {
        return sharedPreferences.getBoolean("enable_dark_mode_as_default_theme", false);
    }

    public String getBloggerId() {
        return sharedPreferences.getString("blogger_id", "0");
    }

    public String getAPIKey() {
        return sharedPreferences.getString("api_key", "0");
    }

    public String getPostId() {
        return sharedPreferences.getString("post_id", "0");
    }

    public void savePostId(String post_id) {
        editor.putString("post_id", post_id);
        editor.apply();
    }

    public void resetPostId() {
        sharedPreferences.edit().remove("post_id").apply();
    }

    //post
    public String getPostToken() {
        return sharedPreferences.getString("post_token", null);
    }

    public void updatePostToken(String post_token) {
        editor.putString("post_token", post_token);
        editor.apply();
    }

    public void resetPostToken() {
        sharedPreferences.edit().remove("post_token").apply();
    }

    //category detail
    public String getCategoryDetailToken() {
        return sharedPreferences.getString("category_detail_token", null);
    }

    public void updateCategoryDetailToken(String category_detail_token) {
        editor.putString("category_detail_token", category_detail_token);
        editor.apply();
    }

    public void resetCategoryDetailToken() {
        sharedPreferences.edit().remove("category_detail_token").apply();
    }

    //search post
    public String getSearchToken() {
        return sharedPreferences.getString("search_token", null);
    }

    public void updateSearchToken(String search_token) {
        editor.putString("search_token", search_token);
        editor.apply();
    }

    public void resetSearchToken() {
        sharedPreferences.edit().remove("search_token").apply();
    }

    //page
    public String getPageToken() {
        return sharedPreferences.getString("page_token", null);
    }

    public void updatePageToken(String page_token) {
        editor.putString("page_token", page_token);
        editor.apply();
    }

    public void resetPageToken() {
        sharedPreferences.edit().remove("page_token").apply();
    }

    public Integer getFontSize() {
        return sharedPreferences.getInt("font_size", 2);
    }

    public void updateFontSize(int font_size) {
        editor.putInt("font_size", font_size);
        editor.apply();
    }

    public Integer getInAppReviewToken() {
        return sharedPreferences.getInt("in_app_review_token", 0);
    }

    public void updateInAppReviewToken(int value) {
        editor.putInt("in_app_review_token", value);
        editor.apply();
    }

    public Integer getApiKeyPosition() {
        return sharedPreferences.getInt("api_key_position", 0);
    }

    public void updateApiKeyPosition(int position) {
        editor.putInt("api_key_position", position);
        editor.apply();
    }

    public Integer getRetryToken() {
        return sharedPreferences.getInt("retry_token", 0);
    }

    public void updateRetryToken(int token) {
        editor.putInt("retry_token", token);
        editor.apply();
    }

}
