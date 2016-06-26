package com.juztoss.bpmplayer.models.songsources;

import android.database.Cursor;
import android.support.annotation.Nullable;

import com.juztoss.bpmplayer.DatabaseHelper;
import com.juztoss.bpmplayer.presenters.BPMPlayerApp;
import com.juztoss.bpmplayer.utils.SystemHelper;

/**
 * Created by JuzTosS on 6/22/2016.
 */
public class FolderSongsSource implements ISongsSource
{

    private long mId;
    private BPMPlayerApp mApp;
    private String mFolderFullPath;

    FolderSongsSource(long id, BPMPlayerApp app, String folderFullPath)
    {
        mId = id;
        mApp = app;
        mFolderFullPath = folderFullPath;
    }

    @Nullable
    @Override
    public Cursor getIds(float minBPM, float maxBPM)
    {
        if(mFolderFullPath == null || mFolderFullPath.length() <= 0)
            return null;

        return mApp.getMusicLibraryHelper().getSongIdsCursor(mFolderFullPath, minBPM, maxBPM, true);
    }

    @Override
    public void delete()
    {
        mApp.getDatabaseHelper().getWritableDatabase().delete(DatabaseHelper.TABLE_SOURCES, DatabaseHelper._ID + " = ?", new String[]{Long.toString(mId)});
    }

    @Override
    public String getName()
    {
        if(mFolderFullPath == null || mFolderFullPath.isEmpty())
            return "Empty";
        else
            return SystemHelper.getLastSegmentOfPath(mFolderFullPath);
    }

    @Override
    public void rename(String newName)
    {

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
}
