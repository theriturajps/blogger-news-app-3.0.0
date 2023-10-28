package com.app.bloggernewsappdemo.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CustomCategory implements Serializable {

    public boolean status;
    public List<Category> categories = new ArrayList<>();

}
