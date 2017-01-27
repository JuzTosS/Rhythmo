package com.juztoss.rhythmo.models;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by JuzTosS on 5/8/2016.
 */
public class DatabaseHelper extends SQLiteOpenHelper implements BaseColumns
{
    private static final String DATABASE_NAME = "main.db";
    private static final int DATABASE_VERSION = 20;


    //TABLE SETTINGS
    public static final String TABLE_SETTINGS = "settings";

    public static final String SETTING_NAME = "name";
    public static final String SETTING_VALUE = "value";

    //TABLE MUSIC_LIBRARY_TABLE
    public static final String TABLE_MUSIC_LIBRARY = "music_library";

    public static final String MUSIC_LIBRARY_PATH = "path";
    public static final String MUSIC_LIBRARY_NAME = "name";
    public static final String MUSIC_LIBRARY_FULL_PATH = "full_path";
    public static final String MUSIC_LIBRARY_BPMX10 = "bpmX10";
    public static final String MUSIC_LIBRARY_BPM_SHIFTEDX10 = "bpmShiftedX10";
    public static final String MUSIC_LIBRARY_DELETED = "deleted";
    public static final String MUSIC_LIBRARY_DATE_ADDED = "date_added";

    //TABLE FOLDERS
    public static final String TABLE_FOLDERS = "folders";

    public static final String FOLDERS_NAME = "name";
    public static final String FOLDERS_PARENT_ID = "parent_id";
    public static final String FOLDERS_HAS_SONGS = "has_songs";
    public static final String FOLDERS_DELETED = "deleted";

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
                + MUSIC_LIBRARY_PATH + " text key, "
                + MUSIC_LIBRARY_NAME + " text key, "
                + MUSIC_LIBRARY_FULL_PATH + " text key unique, "
                + MUSIC_LIBRARY_BPMX10 + " integer key not null default 0, "
                + MUSIC_LIBRARY_BPM_SHIFTEDX10 + " integer key not null default 0, "
                + MUSIC_LIBRARY_DELETED + " boolean key,"
                + MUSIC_LIBRARY_DATE_ADDED + " int key);");

        db.execSQL("create table "
                + TABLE_FOLDERS + " (" +  BaseColumns._ID + " integer primary key autoincrement, "
                + FOLDERS_NAME + " text, "
                + FOLDERS_DELETED + " boolean key,"
                + FOLDERS_PARENT_ID + " integer key, "
                + FOLDERS_HAS_SONGS + " boolean, "
                + "unique(" + FOLDERS_NAME + ", " + FOLDERS_PARENT_ID + ")" +
                "); ");

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
                + PLAYLIST_SONG_ID + " integer, " +
                "unique(" + PLAYLIST_SOURCE_ID + ", " + PLAYLIST_SONG_ID + ")" +
                "); ");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        if(oldVersion <=18)
        {
            migrateTo19(db);
        }

        if(oldVersion <= 19)
        {
            migrateTo20(db);
        }
    }

    private void migrateTo20(SQLiteDatabase db)
    {
        db.beginTransaction();
        //TABLE_MUSIC_LIBRARY
        db.execSQL("ALTER TABLE " + TABLE_MUSIC_LIBRARY + " RENAME TO " + TABLE_MUSIC_LIBRARY + "_temp" + ";");

        db.execSQL("create table "
                + TABLE_MUSIC_LIBRARY + " (" + BaseColumns._ID + " integer primary key autoincrement, "
                + MUSIC_LIBRARY_PATH + " text key, "
                + MUSIC_LIBRARY_NAME + " text key, "
                + MUSIC_LIBRARY_FULL_PATH + " text key unique, "
                + MUSIC_LIBRARY_BPMX10 + " integer key not null default 0, "
                + MUSIC_LIBRARY_BPM_SHIFTEDX10 + " integer key not null default 0, "
                + MUSIC_LIBRARY_DELETED + " boolean key,"
                + MUSIC_LIBRARY_DATE_ADDED + " int key);");

        db.execSQL("INSERT OR IGNORE INTO " + TABLE_MUSIC_LIBRARY +
                "(" + BaseColumns._ID + ", " + MUSIC_LIBRARY_PATH + ", " + MUSIC_LIBRARY_NAME + ", " + MUSIC_LIBRARY_FULL_PATH + ", " + MUSIC_LIBRARY_BPMX10 + ", "
                    + MUSIC_LIBRARY_BPM_SHIFTEDX10 + ", " + MUSIC_LIBRARY_DELETED + ", " + MUSIC_LIBRARY_DATE_ADDED + ")" +
                " SELECT " + BaseColumns._ID + ", " + MUSIC_LIBRARY_PATH + ", " + MUSIC_LIBRARY_NAME + ", " + MUSIC_LIBRARY_FULL_PATH + ", " + MUSIC_LIBRARY_BPMX10 + ", "
                        + MUSIC_LIBRARY_BPM_SHIFTEDX10 + ", " + MUSIC_LIBRARY_DELETED + ", " + MUSIC_LIBRARY_DATE_ADDED +
                " FROM " + TABLE_MUSIC_LIBRARY + "_temp;");

        db.execSQL("DROP TABLE " + TABLE_MUSIC_LIBRARY + "_temp");

        //TABLE_FOLDERS
        db.execSQL("DROP TABLE " + TABLE_FOLDERS);
        db.execSQL("create table "
                + TABLE_FOLDERS + " (" +  BaseColumns._ID + " integer primary key autoincrement, "
                + FOLDERS_NAME + " text, "
                + FOLDERS_DELETED + " boolean key,"
                + FOLDERS_PARENT_ID + " integer key, "
                + FOLDERS_HAS_SONGS + " boolean, "
                + "unique(" + FOLDERS_NAME + ", " + FOLDERS_PARENT_ID + ")" +
                "); ");

        db.setTransactionSuccessful();
        db.endTransaction();
    }

    private void migrateTo19(SQLiteDatabase db)
    {
        db.beginTransaction();

        db.execSQL("ALTER TABLE " + TABLE_PLAYLISTS + " RENAME TO " + TABLE_PLAYLISTS + "_temp" + ";");

        db.execSQL("create table "
                + TABLE_PLAYLISTS + " (" +  BaseColumns._ID + " integer primary key autoincrement, "
                + PLAYLIST_SOURCE_ID + " integer key, "
                + PLAYLIST_POSITION + " integer, "
                + PLAYLIST_SONG_ID + " integer, " +
                "unique(" + PLAYLIST_SOURCE_ID + ", " + PLAYLIST_SONG_ID + ")" +
                "); ");

        //Copy playlists and remove duplicates
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_PLAYLISTS +
                "(" + BaseColumns._ID + ", " + PLAYLIST_SOURCE_ID + ", " + PLAYLIST_POSITION + ", " + PLAYLIST_SONG_ID + ")" +
                " SELECT " + BaseColumns._ID + ", " + PLAYLIST_SOURCE_ID + ", " + PLAYLIST_POSITION + ", " + PLAYLIST_SONG_ID +
                " FROM " + TABLE_PLAYLISTS + "_temp;");

        db.execSQL("DROP TABLE " + TABLE_PLAYLISTS + "_temp");

        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public void clearAll(SQLiteDatabase db)
    {
        db.execSQL("DROP TABLE IF EXISTS '" + TABLE_SETTINGS + "';");
        db.execSQL("DROP TABLE IF EXISTS '" + TABLE_MUSIC_LIBRARY + "';");
        db.execSQL("DROP TABLE IF EXISTS '" + TABLE_FOLDERS + "';");
        db.execSQL("DROP TABLE IF EXISTS '" + TABLE_SOURCES + "';");
        db.execSQL("DROP TABLE IF EXISTS '" + TABLE_PLAYLISTS + "';");

        onCreate(db);
    }

    /**
     * Return a row id of the first selected row
     * @param selection
     * @param selectionArgs
     * @return
     */
    public long getRowId(String selection, String[] selectionArgs)
    {
        Cursor c = getReadableDatabase().query(
                DatabaseHelper.TABLE_FOLDERS,
                new String[]{DatabaseHelper._ID},
                selection,
                selectionArgs,
                null, null, null, "1"
        );

        try
        {
            if (c.getCount() > 0)
            {
                c.moveToPosition(0);
                return c.getLong(0);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            c.close();
        }
        return -1;
    }
}
