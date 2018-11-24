package cn.edu.scnu.ljh.chinesepoetry.entity;

import android.database.Cursor;

public class Star {
    private int id;
    private String title;
    private String author;
    private int type;

    public static Star getStarFromCursor(Cursor cursor) {
        Star star = new Star();
        star.id = cursor.getInt(0);
        star.title = cursor.getString(1);
        star.author = cursor.getString(2);
        star.type = cursor.getInt(3);
        return star;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public int getType() {
        return type;
    }

    @Override
    public String toString() {
        return title + "(" + author + ")";
    }
}
