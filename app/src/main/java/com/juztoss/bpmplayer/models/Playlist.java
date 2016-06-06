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

    public Cursor getNewCompositionsIds()
    {
//        return null;
        return mApp.getDatabaseHelper().getWritableDatabase().query(DatabaseHelper.TABLE_MUSIC_LIBRARY,
            new String[]{DatabaseHelper._ID},
            DatabaseHelper.MUSIC_LIBRARY_BPMX10 + " >= ?" + " AND " + DatabaseHelper.MUSIC_LIBRARY_BPMX10 + " <= ?"
            , new String[]{Integer.toString(mMinBPMX10), Integer.toString(mMaxBPMX10)},
            null, null,
            DatabaseHelper.MUSIC_LIBRARY_BPMX10 + " ASC");
    }

    public String getName()
    {
        return mName;
    }

    public void setBPMFilter(float minBPM, float maxBPM)
    {
        mMinBPMX10 = (int)(minBPM * 10);
        mMaxBPMX10 = (int)(maxBPM * 10);
    }

    public static Playlist create(String name, BPMPlayerApp app)
    {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.PLAYLISTS_NAME, name);
        long id = app.getDatabaseHelper().getWritableDatabase().insert(DatabaseHelper.TABLE_PLAYLISTS, null, values);
        return new Playlist(id, name, app);
    }

    public static void remove(Playlist playlist, BPMPlayerApp app)
    {
        if(playlist.getId() >= 0)
            app.getDatabaseHelper().getWritableDatabase().delete(DatabaseHelper.TABLE_PLAYLISTS, DatabaseHelper._ID + " = ?", new String[]{Long.toString(playlist.getId())});
    }

    public long getId()
    {
        return mId;
    }
}

