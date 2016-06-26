package com.juztoss.bpmplayer.presenters;

import android.database.Cursor;

import com.juztoss.bpmplayer.DatabaseHelper;

/**
 * Created by JuzTosS on 6/10/2016.
 */
public class MusicLibraryHelper
{
    BPMPlayerApp mApp;
    public MusicLibraryHelper(BPMPlayerApp bpmPlayerApp)
    {
        mApp = bpmPlayerApp;
    }

    public Cursor getSongIdsCursor(String absolutePath, boolean doCheckFileSystem)
    {
        //TODO: Implement doCheckFileSystem logic
        Cursor cursor = mApp.getDatabaseHelper().getReadableDatabase().query(DatabaseHelper.TABLE_MUSIC_LIBRARY,
                new String[]{DatabaseHelper._ID},
                DatabaseHelper.MUSIC_LIBRARY_FULL_PATH + " LIKE '" + absolutePath + "%'",
                null,
                null, null, null);

        return cursor;
    }

    public Cursor getSongIdsCursor(String absolutePath, float minBPM, float maxBPM, boolean doCheckFileSystem)
    {
        //TODO: Implement doCheckFileSystem logic
        Cursor cursor = mApp.getDatabaseHelper().getReadableDatabase().query(DatabaseHelper.TABLE_MUSIC_LIBRARY,
                new String[]{DatabaseHelper._ID},
                DatabaseHelper.MUSIC_LIBRARY_FULL_PATH + " LIKE '" + absolutePath + "%' AND " +
                DatabaseHelper.MUSIC_LIBRARY_BPMX10 + " >= ? AND " + DatabaseHelper.MUSIC_LIBRARY_BPMX10 + " <= ?",
                new String[]{Integer.toString((int)(minBPM * 10)), Integer.toString((int)(maxBPM * 10))},
                null, null, null);

        return cursor;
    }
}
