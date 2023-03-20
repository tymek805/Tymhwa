package com.tl.tymhwa.DataManagement;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class DatabaseManager extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "appDB";
    public static final int DATABASE_VERSION = 2;
    private static final String TABLE_NAME = "ManhuaStorage";
    private static final String ID_COL = "ID";
    private static final String TITLE_COL = "Title  ";
    private static final String URL_COL = "URL";
    private static final String CHAPTER_COL = "LastChapterRead";

    public DatabaseManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        String query = "CREATE TABLE " + TABLE_NAME + " ("
                + ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + TITLE_COL + " TEXT,"
                + URL_COL + " TEXT,"
                + CHAPTER_COL + " INTEGER)";

        db.execSQL(query);
    }

    public void addItem(String title, String link, int lastChapter) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(TITLE_COL, title);
        values.put(URL_COL, link);
        values.put(CHAPTER_COL, lastChapter);

        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public ArrayList<Item> readItems() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursorCourses
                = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);

        ArrayList<Item> items = new ArrayList<>();
        if (cursorCourses.moveToFirst()) {
            do {
                items.add(new Item(
                        cursorCourses.getString(1),
                        cursorCourses.getString(2),
                        cursorCourses.getInt(3)));
                Log.i("SQLite", cursorCourses.getString(1));
            } while (cursorCourses.moveToNext());
        }
        cursorCourses.close();
        return items;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
