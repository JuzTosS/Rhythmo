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
    public Cursor getNewCompositionsIds()
    {
        return mApp.getDatabaseHelper().getWritableDatabase().query(DatabaseHelper.TABLE_MUSIC_LIBRARY,
                new String[]{DatabaseHelper._ID},
                DatabaseHelper.MUSIC_LIBRARY_BPMX10 + " >= ?" + " AND " + DatabaseHelper.MUSIC_LIBRARY_BPMX10 + " <= ?"
                , new String[]{Integer.toString(mMinBPMX10), Integer.toString(mMaxBPMX10)},
                null, null,
                DatabaseHelper.MUSIC_LIBRARY_BPMX10 + " ASC");
    }

}
