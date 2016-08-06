package com.juztoss.bpmplayer.presenters;

import android.database.Cursor;
import android.database.DatabaseUtils;

import com.juztoss.bpmplayer.models.DatabaseHelper;

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
                DatabaseHelper.MUSIC_LIBRARY_FULL_PATH + " LIKE " + DatabaseUtils.sqlEscapeString(absolutePath + "%"),
                null,
                null, null, DatabaseHelper.MUSIC_LIBRARY_FULL_PATH);

        return cursor;
    }

    public Cursor getSongIdsCursor(String absolutePath, float minBPM, float maxBPM, boolean doCheckFileSystem)
    {
        //TODO: Implement doCheckFileSystem logic

        Cursor cursor;
        if (minBPM > 0 && maxBPM > 0)//BPM Filter is enabled
        {
            int add = mApp.getBPMFilterAdditionWindowSize();
            cursor = mApp.getDatabaseHelper().getReadableDatabase().query(DatabaseHelper.TABLE_MUSIC_LIBRARY,
                    new String[]{DatabaseHelper._ID},
                    DatabaseHelper.MUSIC_LIBRARY_FULL_PATH + " LIKE " + DatabaseUtils.sqlEscapeString(absolutePath + "%") + " AND " +
                            DatabaseHelper.MUSIC_LIBRARY_BPM_SHIFTEDX10 + " >= ? AND " + DatabaseHelper.MUSIC_LIBRARY_BPM_SHIFTEDX10 + " <= ?",
                    new String[]{Integer.toString((int) (minBPM * 10) - add * 10), Integer.toString((int) (maxBPM * 10) + add * 10)},
                    null, null, DatabaseHelper.MUSIC_LIBRARY_BPM_SHIFTEDX10);
        }
        else
        {
            cursor = mApp.getDatabaseHelper().getReadableDatabase().query(DatabaseHelper.TABLE_MUSIC_LIBRARY,
                    new String[]{DatabaseHelper._ID},
                    DatabaseHelper.MUSIC_LIBRARY_FULL_PATH + " LIKE " + DatabaseUtils.sqlEscapeString(absolutePath + "%"),
                    null,
                    null, null, DatabaseHelper.MUSIC_LIBRARY_BPM_SHIFTEDX10);
        }

        return cursor;
    }
}
