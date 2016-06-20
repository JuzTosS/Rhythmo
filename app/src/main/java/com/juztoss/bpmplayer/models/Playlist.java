package com.juztoss.bpmplayer.models;

import android.content.ContentValues;
import android.database.Cursor;

import com.juztoss.bpmplayer.DatabaseHelper;
import com.juztoss.bpmplayer.presenters.BPMPlayerApp;
import com.juztoss.bpmplayer.views.PlaylistAdapter;

import java.util.ArrayList;
import java.util.List;

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
    private List<IUpdateListener> mUpdateListeners;

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

        setNeedRebuild();
    }

    protected void rebuild()
    {
        if (mList != null)
            mList.close();

        if(mMinBPMX10 > 0 && mMaxBPMX10 > 0)//BPM Filter is enabled
        {
            mList = mApp.getDatabaseHelper().getWritableDatabase().rawQuery(
                    "select " + DatabaseHelper.SONGS_SONG_ID + " as " + DatabaseHelper._ID + " from " + DatabaseHelper.TABLE_SONGS +
                            " inner join " + DatabaseHelper.TABLE_MUSIC_LIBRARY + " on " + DatabaseHelper.TABLE_SONGS + "." + DatabaseHelper.SONGS_SONG_ID + " = " + DatabaseHelper.TABLE_MUSIC_LIBRARY + "." + DatabaseHelper._ID +
                            " where " + DatabaseHelper.TABLE_MUSIC_LIBRARY + "." + DatabaseHelper.MUSIC_LIBRARY_BPM_SHIFTEDX10 + " >= ?" +
                            " AND " + DatabaseHelper.TABLE_MUSIC_LIBRARY + "." + DatabaseHelper.MUSIC_LIBRARY_BPM_SHIFTEDX10 + " <= ?" +
                            " AND " + DatabaseHelper.TABLE_SONGS + "." + DatabaseHelper.SONGS_PLAYLIST_ID + " = ? " +
                            " order by " + DatabaseHelper.TABLE_MUSIC_LIBRARY + "." + DatabaseHelper.MUSIC_LIBRARY_BPM_SHIFTEDX10,
                    new String[]{Integer.toString(mMinBPMX10), Integer.toString(mMaxBPMX10), Long.toString(mId)}
            );
        }
        else
        {
            mList = mApp.getDatabaseHelper().getWritableDatabase().rawQuery(
                    "select " + DatabaseHelper.SONGS_SONG_ID + " as " + DatabaseHelper._ID + " from " + DatabaseHelper.TABLE_SONGS +
                            " inner join " + DatabaseHelper.TABLE_MUSIC_LIBRARY + " on " + DatabaseHelper.TABLE_SONGS + "." + DatabaseHelper.SONGS_SONG_ID + " = " + DatabaseHelper.TABLE_MUSIC_LIBRARY + "." + DatabaseHelper._ID +
                            " where " + DatabaseHelper.TABLE_SONGS + "." + DatabaseHelper.SONGS_PLAYLIST_ID + " = ? " +
                            " order by " + DatabaseHelper.TABLE_MUSIC_LIBRARY + "." + DatabaseHelper.MUSIC_LIBRARY_BPM_SHIFTEDX10,
                    new String[]{Long.toString(mId)}
            );
        }

        mNeedRebuild = false;
    }

    protected void setNeedRebuild()
    {
        mNeedRebuild = true;
        notifyUpdateListners();
    }

    public Cursor getList()
    {
        if (mNeedRebuild || mList.isClosed())
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
        setNeedRebuild();
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
        mUpdateListeners.clear();
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

    protected void notifyUpdateListners()
    {
        if(mUpdateListeners == null) return;
        for(IUpdateListener listener : mUpdateListeners)
        {
            listener.onPlaylistUpdated();
        }
    }

    public void addUpdateListener(IUpdateListener listener)
    {
        if(mUpdateListeners == null)
            mUpdateListeners = new ArrayList<>();

        if(!mUpdateListeners.contains(listener))
            mUpdateListeners.add(listener);
    }

    public void removeUpdateListener(IUpdateListener listener)
    {
        if(mUpdateListeners != null && mUpdateListeners.contains(listener))
            mUpdateListeners.remove(listener);
    }

    public boolean allowModify()
    {
        return true;
    }

    public void rename(String name)
    {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.PLAYLISTS_NAME, name);
        long result = mApp.getDatabaseHelper().getWritableDatabase().update(DatabaseHelper.TABLE_PLAYLISTS, values, DatabaseHelper._ID + " = ?", new String[]{Long.toString(mId)});
        if(result > 0)
        {
            mName = name;
        }
    }

    public interface IUpdateListener
    {
        void onPlaylistUpdated();
    }
}

