package com.juztoss.bpmplayer.models;

import android.database.Cursor;

import com.juztoss.bpmplayer.presenters.BPMPlayerApp;

/**
 * Created by JuzTosS on 6/11/2016.
 */
public class StaticFolderPlaylist extends Playlist
{
    public StaticFolderPlaylist(BPMPlayerApp app)
    {
        super("Folder", app);
        mNeedRebuild = false;
    }

    @Override
    public void add(Cursor songIds)
    {
        clear();
        super.add(songIds);
    }

    @Override
    public boolean allowModify()
    {
        return false;
    }
}
