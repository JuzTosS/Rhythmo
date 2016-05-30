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
    private static final int DATABASE_VERSION = 6;


    //TABLE SETTINGS
    public static final String TABLE_SETTINGS = "settings";

    public static final String SETTING_NAME = "name";
    public static final String SETTING_VALUE = "value";

    public static final String SETTINGS_SONG_FOLDER = "song_folder";


    //TABLE MUSIC_LIBRARY_TABLE
    public static final String TABLE_MUSIC_LIBRARY = "music_library";

    public static final String MUSIC_LIBRARY_MEDIA_ID = "media_id";
    public static final String MUSIC_LIBRARY_PATH = "path";
    public static final String MUSIC_LIBRARY_NAME = "name";
    public static final String MUSIC_LIBRARY_BPMX10 = "bpmX10";

    //TABLE FOLDERS
    public static final String TABLE_FOLDERS = "folders";

    public static final String FOLDERS_NAME = "name";
    public static final String FOLDERS_PARENT_ID = "parent_id";
    public static final String FOLDERS_HAS_SONGS = "has_songs";

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

        db.execSQL("create table "
                + TABLE_MUSIC_LIBRARY + " (" + BaseColumns._ID + " integer primary key autoincrement, "
                + MUSIC_LIBRARY_MEDIA_ID + " integer key, "
                + MUSIC_LIBRARY_PATH + " text key, "
                + MUSIC_LIBRARY_BPMX10 + " integer key, "
                + MUSIC_LIBRARY_NAME + " text); ");

        db.execSQL("create table "
                + TABLE_FOLDERS + " (" +  BaseColumns._ID + " integer primary key autoincrement, "
                + FOLDERS_NAME + " text, "
                + FOLDERS_PARENT_ID + " integer key, "
                + FOLDERS_HAS_SONGS + " boolean); ");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE IF EXISTS '" + TABLE_SETTINGS + "';");
        db.execSQL("DROP TABLE IF EXISTS '" + TABLE_MUSIC_LIBRARY + "';");
        db.execSQL("DROP TABLE IF EXISTS '" + TABLE_FOLDERS + "';");

        onCreate(db);
    }

    public static SQLiteDatabase db()
    {
        return mDb;
    }
}
