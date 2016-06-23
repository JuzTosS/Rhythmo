package com.juztoss.bpmplayer.models;

import android.content.ContentValues;
import android.database.Cursor;

import com.juztoss.bpmplayer.DatabaseHelper;
import com.juztoss.bpmplayer.presenters.BPMPlayerApp;

/**
 * Created by JuzTosS on 6/22/2016.
 */
public class LocalPlaylistSongsSource implements ISongsSource
{
    private long mId;
    private String mName;
    private BPMPlayerApp mApp;

    public LocalPlaylistSongsSource(long id, BPMPlayerApp app, String name)
    {
        mName = name;
        mId = id;
        mApp = app;
    }

    public static LocalPlaylistSongsSource create(String name, BPMPlayerApp app)
    {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.PLAYLISTS_NAME, name);
        long id = app.getDatabaseHelper().getWritableDatabase().insert(DatabaseHelper.TABLE_PLAYLISTS, null, values);
        return new LocalPlaylistSongsSource(id, app, name);
    }

    @Override
    public String getName()
    {
        return mName;
    }

    @Override
    public Cursor getIds(float minBPM, float maxBPM)
    {
        Cursor cursor;
        int mMinBPMX10 = (int) (minBPM * 10);
        int mMaxBPMX10 = (int) (maxBPM * 10);
        if (mMinBPMX10 > 0 && mMaxBPMX10 > 0)//BPM Filter is enabled
        {
            cursor = mApp.getDatabaseHelper().getWritableDatabase().rawQuery(
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
            cursor = mApp.getDatabaseHelper().getWritableDatabase().rawQuery(
                    "select " + DatabaseHelper.SONGS_SONG_ID + " as " + DatabaseHelper._ID + " from " + DatabaseHelper.TABLE_SONGS +
                            " inner join " + DatabaseHelper.TABLE_MUSIC_LIBRARY + " on " + DatabaseHelper.TABLE_SONGS + "." + DatabaseHelper.SONGS_SONG_ID + " = " + DatabaseHelper.TABLE_MUSIC_LIBRARY + "." + DatabaseHelper._ID +
                            " where " + DatabaseHelper.TABLE_SONGS + "." + DatabaseHelper.SONGS_PLAYLIST_ID + " = ? " +
                            " order by " + DatabaseHelper.TABLE_MUSIC_LIBRARY + "." + DatabaseHelper.MUSIC_LIBRARY_BPM_SHIFTEDX10,
                    new String[]{Long.toString(mId)}
            );
        }

        return cursor;
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
    }


    public void rename(String name)
    {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.PLAYLISTS_NAME, name);
        long result = mApp.getDatabaseHelper().getWritableDatabase().update(DatabaseHelper.TABLE_PLAYLISTS, values, DatabaseHelper._ID + " = ?", new String[]{Long.toString(mId)});
        if (result > 0)
        {
            mName = name;
        }
    }

    @Override
    public boolean isRenameAvailable()
    {
        return true;
    }

    @Override
    public boolean isDeleteAvailable()
    {
        return true;
    }

    @Override
    public boolean isModifyAvailable()
    {
        return true;
    }

    public void delete()
    {
        clear();
        if (mId >= 0)
            mApp.getDatabaseHelper().getWritableDatabase().delete(DatabaseHelper.TABLE_PLAYLISTS, DatabaseHelper._ID + " = ?", new String[]{Long.toString(mId)});
    }

    protected void clear()
    {
        mApp.getDatabaseHelper().getWritableDatabase().delete(DatabaseHelper.TABLE_SONGS, DatabaseHelper.SONGS_PLAYLIST_ID + " = ?", new String[]{Long.toString(mId)});
    }
}
