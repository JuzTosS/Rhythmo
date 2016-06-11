package com.juztoss.bpmplayer.models;

import android.content.ContentValues;
import android.database.Cursor;

import com.juztoss.bpmplayer.DatabaseHelper;
import com.juztoss.bpmplayer.presenters.BPMPlayerApp;

/**
 * Created by JuzTosS on 5/8/2016.
 */
public class Playlist
{
    protected BPMPlayerApp mApp;
    protected String mName;
    protected int mMinBPMX10 = 0;
    protected int mMaxBPMX10 = Integer.MAX_VALUE;
    private long mId;
    protected Cursor mList;
    protected boolean mNeedRebuild = true;

    Playlist(String name, BPMPlayerApp app)
    {
        this(-1, name, app);
    }

    public Playlist(long playlistId, String name, BPMPlayerApp app)
    {
        mId = playlistId;
        mName = name;
        mApp = app;
    }

    public void add(Cursor songIds)
    {
        try
        {
            songIds.moveToFirst();
            mApp.getDatabaseHelper().getWritableDatabase().beginTransaction();
            do
            {
                long songId = songIds.getLong(0);
                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.SONGS_PLAYLIST_ID, mId);
                values.put(DatabaseHelper.SONGS_SONG_ID, songId);
                mApp.getDatabaseHelper().getWritableDatabase().insert(DatabaseHelper.TABLE_SONGS, null, values);

            } while (songIds.moveToNext());
        }
        finally
        {
            mApp.getDatabaseHelper().getWritableDatabase().setTransactionSuccessful();
            mApp.getDatabaseHelper().getWritableDatabase().endTransaction();
            songIds.close();
        }

        mNeedRebuild = true;
    }

    protected void rebuild()
    {
        if (mList != null)
            mList.close();

        mList = mApp.getDatabaseHelper().getWritableDatabase().rawQuery(
                "select " + DatabaseHelper.SONGS_SONG_ID + " as " + DatabaseHelper._ID + " from " + DatabaseHelper.TABLE_SONGS +
                        " inner join " + DatabaseHelper.TABLE_MUSIC_LIBRARY + " on " + DatabaseHelper.TABLE_SONGS + "." + DatabaseHelper.SONGS_SONG_ID + " = " + DatabaseHelper.TABLE_MUSIC_LIBRARY + "." + DatabaseHelper._ID +
                        " where " + DatabaseHelper.TABLE_MUSIC_LIBRARY + "." + DatabaseHelper.MUSIC_LIBRARY_BPMX10 + " >= ?" +
                        " AND " + DatabaseHelper.TABLE_MUSIC_LIBRARY + "." + DatabaseHelper.MUSIC_LIBRARY_BPMX10 + " <= ?" +
                        " AND " + DatabaseHelper.TABLE_SONGS + "." + DatabaseHelper.SONGS_PLAYLIST_ID + " = ? " +
                        " order by " + DatabaseHelper.TABLE_MUSIC_LIBRARY + "." + DatabaseHelper.MUSIC_LIBRARY_BPMX10,
                new String[]{Integer.toString(mMinBPMX10), Integer.toString(mMaxBPMX10), Long.toString(mId)}
        );

        mNeedRebuild = false;
    }

    public Cursor getList()
    {
        if (mNeedRebuild)
            rebuild();

        return mList;
    }

    public String getName()
    {
        return mName;
    }

    public void setBPMFilter(float minBPM, float maxBPM)
    {
        mMinBPMX10 = (int) (minBPM * 10);
        mMaxBPMX10 = (int) (maxBPM * 10);
        mNeedRebuild = true;
    }

    public static Playlist create(String name, BPMPlayerApp app)
    {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.PLAYLISTS_NAME, name);
        long id = app.getDatabaseHelper().getWritableDatabase().insert(DatabaseHelper.TABLE_PLAYLISTS, null, values);
        return new Playlist(id, name, app);
    }

    public void delete()
    {
        clear();
        if (getId() >= 0)
            mApp.getDatabaseHelper().getWritableDatabase().delete(DatabaseHelper.TABLE_PLAYLISTS, DatabaseHelper._ID + " = ?", new String[]{Long.toString(getId())});
    }

    protected void clear()
    {
        mApp.getDatabaseHelper().getWritableDatabase().delete(DatabaseHelper.TABLE_SONGS, DatabaseHelper.SONGS_PLAYLIST_ID + " = ?", new String[]{Long.toString(getId())});

    }

    public long getId()
    {
        return mId;
    }
}

