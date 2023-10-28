package com.app.bloggernewsappdemo.database.sqlite;

import static com.app.bloggernewsappdemo.Config.LABELS_SORTING;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.app.bloggernewsappdemo.models.Category;

import java.util.ArrayList;
import java.util.List;

public class DbLabel extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "label.db";
    public static final String TABLE_LABEL = "label";
    public static final String ID = "id";
    public static final String LABEL_NAME = "term";
    public static final String LABEL_IMAGE = "image";
    private final SQLiteDatabase db;

    public DbLabel(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTableCategory(db, TABLE_LABEL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LABEL);
        createTableCategory(db, TABLE_LABEL);
    }

    public void truncateTableCategory(String table) {
        db.execSQL("DROP TABLE IF EXISTS " + table);
        createTableCategory(db, table);
    }

    private void createTableCategory(SQLiteDatabase db, String table) {
        String CREATE_TABLE = "CREATE TABLE " + table + "("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + LABEL_NAME + " TEXT,"
                + LABEL_IMAGE + " TEXT"
                + ")";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        db.disableWriteAheadLogging();
    }

    public void addListCategory(List<Category> categories, String table) {
        for (Category category : categories) {
            addOneCategory(db, category, table);
        }
        getAllCategory(table);
    }

    public void addOneCategory(SQLiteDatabase db, Category category, String table) {
        ContentValues values = new ContentValues();
        values.put(LABEL_NAME, category.term);
        values.put(LABEL_IMAGE, category.image);
        db.insert(table, null, values);
    }

    public List<Category> getAllCategory(String table) {
        return getAllCategories(table);
    }

    private List<Category> getAllCategories(String table) {
        List<Category> list;
        Cursor cursor;
        cursor = db.rawQuery("SELECT * FROM " + table + " ORDER BY " + LABELS_SORTING, null);
        list = getAllCategoryFormCursor(cursor);
        return list;
    }

    @SuppressLint("Range")
    private List<Category> getAllCategoryFormCursor(Cursor cursor) {
        List<Category> list = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                Category category = new Category();
                category.term = cursor.getString(cursor.getColumnIndex(LABEL_NAME));
                category.image = cursor.getString(cursor.getColumnIndex(LABEL_IMAGE));
                list.add(category);
            } while (cursor.moveToNext());
        }
        return list;
    }

}
