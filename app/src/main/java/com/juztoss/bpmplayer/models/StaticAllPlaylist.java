package com.juztoss.bpmplayer.models;

import android.database.Cursor;

import com.juztoss.bpmplayer.DatabaseHelper;
import com.juztoss.bpmplayer.presenters.BPMPlayerApp;

public class StaticAllPlaylist extends Playlist
{
    public StaticAllPlaylist(BPMPlayerApp app)
    {
        super("All songs", app);
    }

    @Override
    protected void rebuild()
    {
        if (mList != null)
            mList.close();

        mList = mApp.getDatabaseHelper().getWritableDatabase().query(DatabaseHelper.TABLE_MUSIC_LIBRARY,
                new String[]{DatabaseHelper._ID},
                DatabaseHelper.MUSIC_LIBRARY_BPM_SHIFTEDX10 + " >= ?" + " AND " + DatabaseHelper.MUSIC_LIBRARY_BPM_SHIFTEDX10 + " <= ?"
                , new String[]{Integer.toString(mMinBPMX10), Integer.toString(mMaxBPMX10)},
                null, null,
                DatabaseHelper.MUSIC_LIBRARY_BPM_SHIFTEDX10 + " ASC");

        mNeedRebuild = false;
    }

    @Override
    public void add(Cursor songIds)
    {
        //Do nothing
    }

    @Override
    public boolean allowModify()
    {
        return false;
    }
}
