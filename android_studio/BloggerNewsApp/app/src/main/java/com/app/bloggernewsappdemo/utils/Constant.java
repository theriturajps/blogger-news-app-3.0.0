package com.app.bloggernewsappdemo.utils;

import static com.app.bloggernewsappdemo.Config.DISPLAY_POST_ORDER;

public class Constant {

    public static final int POST_PER_PAGE = 10;

    public static final int PAGER_NUMBER_DEFAULT = 4;
    public static final int PAGER_NUMBER_NO_PAGE = 3;

    //"published": Order by the date the post was published
    //"updated": Order by the date the post was last updated
    public static final String POST_ORDER = DISPLAY_POST_ORDER;

    public static final String LABEL_DEFAULT = "id ASC";
    public static final String LABEL_NAME_ASCENDING = "term ASC";
    public static final String LABEL_NAME_DESCENDING = "term DESC";

    public static final int GRID_2_COLUMNS = 2;
    public static final int GRID_3_COLUMNS = 3;

    public static final String CATEGORY_GRID_SMALL = "sm_grid";
    public static final String CATEGORY_GRID_MEDIUM = "md_grid";

    public static final String CIRCULAR = "circular";
    public static final String ROUNDED = "rounded";

    public static final int HEADER_WIDTH = 720;
    public static final int HEADER_HEIGHT = 400;
    public static final int THUMBNAIL_WIDTH = 300;
    public static final int THUMBNAIL_HEIGHT = 300;

    public static final int FONT_SIZE_XSMALL = 12;
    public static final int FONT_SIZE_SMALL = 14;
    public static final int FONT_SIZE_MEDIUM = 16;
    public static final int FONT_SIZE_LARGE = 18;
    public static final int FONT_SIZE_XLARGE = 20;

    public static final int IMMEDIATE_APP_UPDATE_REQ_CODE = 124;
    public static final int DELAY_REFRESH = 10;
    public static final int MAX_RETRY_TOKEN = 10;

    public static final String DATE_FORMATTED = "dd MMMM yyyy, HH:mm";

    public static final String EXTRA_OBJC = "key.EXTRA_OBJC";
    public static final String EXTRA_ID = "post_id";

}