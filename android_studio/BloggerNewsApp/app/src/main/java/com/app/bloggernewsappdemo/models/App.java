package com.app.bloggernewsappdemo.models;

import java.io.Serializable;

public class App implements Serializable {

    public boolean status;
    public String redirect_url = "";
    public String more_apps_url = "";
    public String privacy_policy_url = "";
    public String publisher_info_url = "";
    public String terms_conditions_url = "";
    public String email_feedback_and_report = "";
    public int category_column_count;
    public boolean show_page_menu;
    public boolean show_view_on_site_menu;
    public boolean show_post_list_in_large_style;
    public boolean show_post_list_header;
    public boolean show_post_list_short_description;
    public boolean show_post_date;
    public boolean show_related_post;
    public boolean open_link_inside_app;
    public boolean enable_exit_dialog;
    public boolean enable_rtl_mode;
    public boolean enable_dark_mode_as_default_theme;

}