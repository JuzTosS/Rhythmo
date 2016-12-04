package com.juztoss.rhythmo.models.songsources;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;

import com.juztoss.rhythmo.R;
import com.juztoss.rhythmo.models.Composition;
import com.juztoss.rhythmo.models.DatabaseHelper;
import com.juztoss.rhythmo.presenters.RhythmoApp;

/**
 * Created by JuzTosS on 6/22/2016.
 */
public class LocalPlaylistSongsSource extends AbstractSongsSource
{
    private long mId;
    private String mName;
    private SortType mSortType;
    private RhythmoApp mApp;
    private String mGeneratedName;

    LocalPlaylistSongsSource(long id, RhythmoApp app, String name, SortType sortType)
    {
        mGeneratedName = app.getString(R.string.default_playlist_name);
        mName = name;
        mSortType = sortType;
        mId = id;
        mApp = app;
        notifyUpdated();
    }

    @Override
    public String getName()
    {
        if (mName == null || mName.isEmpty())
            return mGeneratedName;
        else
            return mName;
    }

    @Override
    public Cursor getIds(float minBPM, float maxBPM, String wordFilter)
    {
        Cursor cursor;
        int mMinBPMX10 = (int) (minBPM * 10);
        int mMaxBPMX10 = (int) (maxBPM * 10);
        int add = mApp.getBPMFilterAdditionWindowSize();

        String order;
        if (mSortType == SortType.NAME)
            order = " order by " + DatabaseHelper.TABLE_MUSIC_LIBRARY + "." + DatabaseHelper.MUSIC_LIBRARY_NAME;
        else if (mSortType == SortType.BPM)
            order = " order by " + DatabaseHelper.TABLE_MUSIC_LIBRARY + "." + DatabaseHelper.MUSIC_LIBRARY_BPM_SHIFTEDX10;
        else//mSortType = SortType.DIRECTORY
            order = " order by " + DatabaseHelper.TABLE_MUSIC_LIBRARY + "." + DatabaseHelper.MUSIC_LIBRARY_PATH;

        if (mMinBPMX10 > 0 && mMaxBPMX10 > 0)//BPM Filter is enabled
        {
            cursor = mApp.getDatabaseHelper().getWritableDatabase().rawQuery(
//                    DatabaseHelper._ID, DatabaseHelper.MUSIC_LIBRARY_PATH, DatabaseHelper.MUSIC_LIBRARY_NAME, DatabaseHelper.MUSIC_LIBRARY_BPMX10, DatabaseHelper.MUSIC_LIBRARY_BPM_SHIFTEDX10
                    "select " + DatabaseHelper.PLAYLIST_SONG_ID + " as " + DatabaseHelper._ID + ", " + DatabaseHelper.MUSIC_LIBRARY_PATH + ", " + DatabaseHelper.MUSIC_LIBRARY_NAME + ", " + DatabaseHelper.MUSIC_LIBRARY_BPMX10 + ", " + DatabaseHelper.MUSIC_LIBRARY_BPM_SHIFTEDX10
                            + " from " + DatabaseHelper.TABLE_PLAYLISTS +
                            " inner join " + DatabaseHelper.TABLE_MUSIC_LIBRARY + " on " + DatabaseHelper.TABLE_PLAYLISTS + "." + DatabaseHelper.PLAYLIST_SONG_ID + " = " + DatabaseHelper.TABLE_MUSIC_LIBRARY + "." + DatabaseHelper._ID +
                            " where " + DatabaseHelper.TABLE_MUSIC_LIBRARY + "." + DatabaseHelper.MUSIC_LIBRARY_BPM_SHIFTEDX10 + " >= ?" +
                            " AND " + DatabaseHelper.TABLE_MUSIC_LIBRARY + "." + DatabaseHelper.MUSIC_LIBRARY_BPM_SHIFTEDX10 + " <= ?" +
                            " AND " + DatabaseHelper.TABLE_PLAYLISTS + "." + DatabaseHelper.PLAYLIST_SOURCE_ID + " = ? " +
                            ((wordFilter == null) ? "" : " AND " + DatabaseHelper.MUSIC_LIBRARY_NAME + " LIKE " + DatabaseUtils.sqlEscapeString("%" + wordFilter + "%")) +
                            order,
                    new String[]{Integer.toString(mMinBPMX10 - add * 10), Integer.toString(mMaxBPMX10 + add * 10), Long.toString(mId)}
            );
        }
        else
        {
            cursor = mApp.getDatabaseHelper().getWritableDatabase().rawQuery(
                    "select " + DatabaseHelper.PLAYLIST_SONG_ID + " as " + DatabaseHelper._ID + ", " + DatabaseHelper.MUSIC_LIBRARY_PATH + ", " + DatabaseHelper.MUSIC_LIBRARY_NAME + ", " + DatabaseHelper.MUSIC_LIBRARY_BPMX10 + ", " + DatabaseHelper.MUSIC_LIBRARY_BPM_SHIFTEDX10
                            + " from " + DatabaseHelper.TABLE_PLAYLISTS +
                            " inner join " + DatabaseHelper.TABLE_MUSIC_LIBRARY + " on " + DatabaseHelper.TABLE_PLAYLISTS + "." + DatabaseHelper.PLAYLIST_SONG_ID + " = " + DatabaseHelper.TABLE_MUSIC_LIBRARY + "." + DatabaseHelper._ID +
                            " where " + DatabaseHelper.TABLE_PLAYLISTS + "." + DatabaseHelper.PLAYLIST_SOURCE_ID + " = ? " +
                            ((wordFilter == null) ? "" : " AND " + DatabaseHelper.MUSIC_LIBRARY_NAME + " LIKE " + DatabaseUtils.sqlEscapeString("%" + wordFilter + "%")) +
                            order,
                    new String[]{Long.toString(mId)}
            );
        }

        return cursor;
    }

    public void add(Cursor songIds)
    {
        try
        {
            if (songIds.getCount() > 0)
            {
                songIds.moveToFirst();
                mApp.getDatabaseHelper().getWritableDatabase().beginTransaction();
                do
                {
                    long songId = songIds.getLong(0);
                    ContentValues values = new ContentValues();
                    values.put(DatabaseHelper.PLAYLIST_SOURCE_ID, mId);
                    values.put(DatabaseHelper.PLAYLIST_SONG_ID, songId);
                    mApp.getDatabaseHelper().getWritableDatabase().insert(DatabaseHelper.TABLE_PLAYLISTS, null, values);

                } while (songIds.moveToNext());

                mApp.getDatabaseHelper().getWritableDatabase().setTransactionSuccessful();
                mApp.getDatabaseHelper().getWritableDatabase().endTransaction();
            }
        }
        finally
        {
            songIds.close();
        }

        notifyUpdated();
    }

    @Override
    public void remove(long songId)
    {
        mApp.getDatabaseHelper().getWritableDatabase().delete(DatabaseHelper.TABLE_PLAYLISTS, DatabaseHelper.PLAYLIST_SOURCE_ID + " = ? AND " + DatabaseHelper.PLAYLIST_SONG_ID + " = ? ", new String[]{Long.toString(mId), Long.toString(songId)});
        notifyUpdated();
    }

    @Override
    protected void notifyUpdated()
    {
        //Update generated name
        String newGeneratedName = mGeneratedName;
        Cursor cursor = mApp.getDatabaseHelper().getWritableDatabase().rawQuery(
                "select " + DatabaseHelper.PLAYLIST_SONG_ID + " as " + DatabaseHelper._ID + ", " + DatabaseHelper.MUSIC_LIBRARY_PATH + ", " + DatabaseHelper.MUSIC_LIBRARY_NAME + ", " + DatabaseHelper.MUSIC_LIBRARY_BPMX10 + ", " + DatabaseHelper.MUSIC_LIBRARY_BPM_SHIFTEDX10
                        + " from " + DatabaseHelper.TABLE_PLAYLISTS +
                        " inner join " + DatabaseHelper.TABLE_MUSIC_LIBRARY + " on " + DatabaseHelper.TABLE_PLAYLISTS + "." + DatabaseHelper.PLAYLIST_SONG_ID + " = " + DatabaseHelper.TABLE_MUSIC_LIBRARY + "." + DatabaseHelper._ID +
                        " where " + DatabaseHelper.TABLE_PLAYLISTS + "." + DatabaseHelper.PLAYLIST_SOURCE_ID + " = ? " +
                        " LIMIT 1",
                new String[]{Long.toString(mId)}
        );

        try
        {
            if (cursor.getCount() > 0)
            {
                cursor.moveToFirst();
                Composition composition = Composition.fromCursor(cursor);
                if (composition != null)
                    newGeneratedName = composition.getFolder();
            }
        }
        finally
        {
            cursor.close();
        }
        mGeneratedName = newGeneratedName;

        super.notifyUpdated();
    }

    @Override
    public void setSortType(SortType sortType)
    {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.SOURCE_SORT, sortType.ordinal());
        long result = mApp.getDatabaseHelper().getWritableDatabase().update(DatabaseHelper.TABLE_SOURCES, values, DatabaseHelper._ID + " = ?", new String[]{Long.toString(mId)});
        if (result > 0)
        {
            mSortType = sortType;
        }

        notifyUpdated();
    }

    @Override
    public SortType getSortType()
    {
        return mSortType;
    }

    public void rename(String name)
    {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.SOURCE_NAME, name);
        long result = mApp.getDatabaseHelper().getWritableDatabase().update(DatabaseHelper.TABLE_SOURCES, values, DatabaseHelper._ID + " = ?", new String[]{Long.toString(mId)});
        if (result > 0)
        {
            mName = name;
        }

        notifyUpdated();
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
        mApp.getDatabaseHelper().getWritableDatabase().delete(DatabaseHelper.TABLE_SOURCES, DatabaseHelper._ID + " = ?", new String[]{Long.toString(mId)});
    }

    protected void clear()
    {
        mApp.getDatabaseHelper().getWritableDatabase().delete(DatabaseHelper.TABLE_PLAYLISTS, DatabaseHelper.PLAYLIST_SOURCE_ID + " = ?", new String[]{Long.toString(mId)});
    }
}
