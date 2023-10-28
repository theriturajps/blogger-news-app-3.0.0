package com.app.bloggernewsappdemo.models;

import com.app.bloggernewsappdemo.models.entries.Link;
import com.app.bloggernewsappdemo.models.entries.Object;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Entry implements Serializable {

    public Object id = null;
    public Object published = null;
    public Object updated = null;
    public List<Category> category = new ArrayList<>();
    public Object title = null;
    public Object content = null;
    public List<Link> link = new ArrayList<>();

}
