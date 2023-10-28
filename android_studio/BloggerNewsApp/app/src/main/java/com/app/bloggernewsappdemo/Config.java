package com.app.bloggernewsappdemo;

import com.app.bloggernewsappdemo.utils.Constant;

public class Config {

    //please check the documentation for the guide to generate your access key
    public static final String ACCESS_KEY = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";

    //app UI design
    public static final boolean ENABLE_NEW_APP_DESIGN = true;

    //"published": Order by the date the post was published
    //"updated": Order by the date the post was last updated
    public static final String DISPLAY_POST_ORDER = "published";

    //if it's true the first image in the post details will be the main image
    public static final boolean FIRST_POST_IMAGE_AS_MAIN_IMAGE = true;

    //label sorting, supported value : Constant.LABEL_NAME_ASCENDING, Constant.LABEL_NAME_DESCENDING or Constant.LABEL_DEFAULT
    public static final String LABELS_SORTING = Constant.LABEL_NAME_ASCENDING;

    //category layout style, supported value : Constant.CATEGORY_LIST, Constant.CATEGORY_GRID_SMALL or Constant..CATEGORY_GRID_MEDIUM
    public static final String CATEGORY_LAYOUT_STYLE = Constant.CATEGORY_GRID_SMALL;

    //category image style, supported value : Constant.CIRCULAR or Constant.ROUNDED
    public static final String CATEGORY_IMAGE_STYLE = Constant.CIRCULAR;

    //enable copy text in the story content
    public static final boolean ENABLE_TEXT_SELECTION = false;

    //GDPR EU Consent
    public static final boolean LEGACY_GDPR = false;

    //delay splash when remote config finish loading in millisecond
    public static final int DELAY_SPLASH = 1500;

}