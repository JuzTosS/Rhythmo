package com.juztoss.rhythmo.models;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.juztoss.rhythmo.presenters.RhythmoApp;

/**
 * Created by JuzTosS on 5/8/2016.
 */
public class DatabaseHelper extends SQLiteOpenHelper implements BaseColumns
{
    private static final String DATABASE_NAME = "main.db";
    private static final int DATABASE_VERSION = 15;


    //TABLE SETTINGS
    public static final String TABLE_SETTINGS = "settings";

    public static final String SETTING_NAME = "name";
    public static final String SETTING_VALUE = "value";

    //TABLE MUSIC_LIBRARY_TABLE
    public static final String TABLE_MUSIC_LIBRARY = "music_library";

    public static final String MUSIC_LIBRARY_MEDIA_ID = "media_id";
    public static final String MUSIC_LIBRARY_PATH = "path";
    public static final String MUSIC_LIBRARY_NAME = "name";
    public static final String MUSIC_LIBRARY_FULL_PATH = "full_path";
    public static final String MUSIC_LIBRARY_BPMX10 = "bpmX10";
    public static final String MUSIC_LIBRARY_BPM_SHIFTEDX10 = "bpmShiftedX10";
    public static final String MUSIC_LIBRARY_DELETED = "deleted";

    //TABLE FOLDERS
    public static final String TABLE_FOLDERS = "folders";

    public static final String FOLDERS_NAME = "name";
    public static final String FOLDERS_PARENT_ID = "parent_id";
    public static final String FOLDERS_HAS_SONGS = "has_songs";

    //TABLE PLAYLISTS
    public static final String TABLE_SOURCES = "sources";
    public static final String SOURCE_OPTIONS = "options";
    public static final String SOURCE_SORT = "sort";
    public static final String SOURCE_TYPE = "type";
    public static final String SOURCE_NAME = "name";

    //TABLE SONGS
    public static final String TABLE_PLAYLISTS = "playlists";
    public static final String PLAYLIST_SONG_ID = "song_id";
    public static final String PLAYLIST_SOURCE_ID = "source_id";
    public static final String PLAYLIST_POSITION = "position";

    private Context mContext;

    public DatabaseHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
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
                + MUSIC_LIBRARY_MEDIA_ID + " integer key not null default -1, "
                + MUSIC_LIBRARY_PATH + " text key, "
                + MUSIC_LIBRARY_NAME + " text key, "
                + MUSIC_LIBRARY_FULL_PATH + " text key, "
                + MUSIC_LIBRARY_BPMX10 + " integer key not null default 0, "
                + MUSIC_LIBRARY_BPM_SHIFTEDX10 + " integer key not null default 0, "
                + MUSIC_LIBRARY_DELETED + " boolean key, "
                + "unique(" + MUSIC_LIBRARY_FULL_PATH + ")); ");

        db.execSQL("create table "
                + TABLE_FOLDERS + " (" +  BaseColumns._ID + " integer primary key autoincrement, "
                + FOLDERS_NAME + " text, "
                + FOLDERS_PARENT_ID + " integer key, "
                + FOLDERS_HAS_SONGS + " boolean); ");

        db.execSQL("create table "
                + TABLE_SOURCES + " (" +  BaseColumns._ID + " integer primary key autoincrement, "
                + SOURCE_TYPE + " integer, "
                + SOURCE_OPTIONS + " text, "
                + SOURCE_SORT + " integer not null default 0, "
                + SOURCE_NAME + " text); ");

        db.execSQL("create table "
                + TABLE_PLAYLISTS + " (" +  BaseColumns._ID + " integer primary key autoincrement, "
                + PLAYLIST_SOURCE_ID + " integer key, "
                + PLAYLIST_POSITION + " integer, "
                + PLAYLIST_SONG_ID + " integer); ");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        clearAll(db);
    }

    public void clearAll(SQLiteDatabase db)
    {
        db.execSQL("DROP TABLE IF EXISTS '" + TABLE_SETTINGS + "';");
        db.execSQL("DROP TABLE IF EXISTS '" + TABLE_MUSIC_LIBRARY + "';");
        db.execSQL("DROP TABLE IF EXISTS '" + TABLE_FOLDERS + "';");
        db.execSQL("DROP TABLE IF EXISTS '" + TABLE_SOURCES + "';");
        db.execSQL("DROP TABLE IF EXISTS '" + TABLE_PLAYLISTS + "';");

        onCreate(db);

        //TODO: Remove it after beta release
        {
            ((RhythmoApp) mContext).getSharedPreferences().edit().putBoolean(RhythmoApp.FIRST_RUN, true).commit();
            ((RhythmoApp) mContext).getSharedPreferences().edit().putBoolean(RhythmoApp.LIBRARY_BUILD_STARTED, true).commit();
        }
    }
}
