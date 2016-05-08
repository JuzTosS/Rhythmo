package com.juztoss.bpmplayer;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by JuzTosS on 5/8/2016.
 */
public class DatabaseHelper extends SQLiteOpenHelper implements BaseColumns
{
    private static SQLiteDatabase mDb;
    private static final String DATABASE_NAME = "main.db";
    private static final int DATABASE_VERSION = 1;
    public static final String TABLE_SETTINGS = "settings";

    public static final String SETTING_NAME = "name";
    public static final String SETTING_VALUE = "value";

    public static final String SETTINGS_SONG_FOLDER = "song_folder";

    public DatabaseHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mDb = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL("create table "
                + TABLE_SETTINGS + " (" + BaseColumns._ID + " integer primary key autoincrement, "
                + SETTING_NAME + " text not null, "
                + SETTING_VALUE + " text); ");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE IF EXISTS '" + TABLE_SETTINGS + "';");
        onCreate(db);
    }

    public static SQLiteDatabase db()
    {
        return mDb;
    }
}
