package com.juztoss.bpmplayer.models;

import android.database.Cursor;

import com.juztoss.bpmplayer.presenters.BPMPlayerApp;
import com.juztoss.bpmplayer.utils.SystemHelper;

/**
 * Created by JuzTosS on 6/22/2016.
 */
public class FolderSongsSource implements ISongsSource
{

    private BPMPlayerApp mApp;
    private String mFolderFullPath;

    public FolderSongsSource(BPMPlayerApp app, String folderFullPath)
    {
        mApp = app;
        mFolderFullPath = folderFullPath;
    }

    @Override
    public Cursor getIds(float minBPM, float maxBPM)
    {
        return mApp.getMusicLibraryHelper().getSongIdsCursor(mFolderFullPath, minBPM, maxBPM, true);
    }

    @Override
    public void delete()
    {
        //Do nothing
    }

    @Override
    public String getName()
    {
        return SystemHelper.getLastSegmentOfPath(mFolderFullPath);
    }

    @Override
    public void rename(String newName)
    {

    }

    @Override
    public boolean isRenameAvailable()
    {
        return false;
    }

    @Override
    public boolean isDeleteAvailable()
    {
        return false;
    }

    @Override
    public boolean isModifyAvailable()
    {
        return false;
    }
}
