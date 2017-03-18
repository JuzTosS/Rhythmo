package com.juztoss.rhythmo.models;

import android.database.Cursor;
import android.support.annotation.Nullable;

import com.juztoss.rhythmo.R;
import com.juztoss.rhythmo.presenters.RhythmoApp;

import java.io.File;
import java.util.List;

/**
 * Created by JuzTosS on 4/25/2016.
 */
public class SongFile extends BaseExplorerElement
{
    private File mFile;
    private RhythmoApp mApp;
    private final Composition mComposition;
    private BaseExplorerElement mParent;

    public SongFile(File source, RhythmoApp app, BaseExplorerElement parent, Composition composition)
    {
        mParent = parent;
        mFile = source;
        mApp = app;
        mComposition = composition;
    }

    public String name()
    {
        return mFile.getName();
    }

    @Override
    public int getIconResource()
    {
        return R.drawable.ic_music_note_black_24dp;
    }


    @Override
    public boolean isAddable()
    {
        return true;
    }

    @Override
    public AddState getAddState()
    {
        return mApp.getBrowserPresenter().getAddState(mFile.getAbsolutePath(), mParent.getChildren(false));
    }

    @Override
    public void setAddState(AddState state)
    {
        if(state == AddState.ADDED)
            mApp.getBrowserPresenter().add(mFile.getAbsolutePath());
        else if(state == AddState.NOT_ADDED)
            mApp.getBrowserPresenter().remove(mFile.getAbsolutePath(), mParent.getChildren(false));
    }

    @Override
    public ExplorerPriority priority()
    {
        return ExplorerPriority.SONG;
    }

    @Override
    public int type()
    {
        return BaseExplorerElement.SINGLE_LINK;
    }

    @Override
    public List<BaseExplorerElement> getChildren(boolean onlyFolders)
    {
        return null;
    }

    @Nullable
    @Override
    public Cursor getSongIds()
    {
        return mApp.getMusicLibraryHelper().getSongIdsCursor(mFile.getAbsolutePath());
    }

    @Override
    public boolean hasChildren()
    {
        return false;
    }

    @Override
    public String getFileSystemPath()
    {
        return mFile.getAbsolutePath();
    }

    @Override
    public String description()
    {
//        if(mFile.exists())
//        {
//            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
//            try
//            {
//                mmr.setDataSource(mFile.getAbsolutePath());
//                int duration = Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
//                return DateUtils.formatElapsedTime(duration / 1000);
//            }
//            catch (Exception e)
//            {
//                return "Unknown length";
//            }
//        }

        return "";
    }

    @Override
    public void dispose()
    {
        mApp = null;
    }

    @Override
    public BaseExplorerElement getChildFromPath(String path, boolean onlyFolders) {
        return null;
    }

    public Composition getComposition() {
        return mComposition;
    }
}
