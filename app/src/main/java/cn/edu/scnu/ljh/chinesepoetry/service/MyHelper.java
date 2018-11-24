package cn.edu.scnu.ljh.chinesepoetry.service;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyHelper extends SQLiteOpenHelper {
    public MyHelper(Context context) {
        super(context, "star.db", null, 2);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE `data` (\n" +
                " `id` INTEGER,\n" +
                " `title` TEXT,\n" +
                " `author` TEXT,\n" +
                " `type` INTEGER\n" +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
