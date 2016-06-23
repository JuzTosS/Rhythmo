package com.juztoss.bpmplayer.models;

import android.database.Cursor;
import android.support.annotation.Nullable;

import java.util.List;

/**
 * Created by JuzTosS on 4/25/2016.
 */
public class ParentLink extends BaseExplorerElement
{
    private BaseExplorerElement mSource;

    public ParentLink(BaseExplorerElement element)
    {
        mSource = element;
    }

    @Override
    public String name()
    {
        return "..";
    }

    @Override
    public ExplorerPriority priority()
    {
        return ExplorerPriority.PARENT_LINK;
    }

    @Override
    public List<BaseExplorerElement> getChildren()
    {
        return mSource.getChildren();
    }

    @Nullable
    @Override
    public Cursor getSongIds()
    {
        return mSource.getSongIds();
    }

    @Override
    public boolean hasChildren()
    {
        return mSource.hasChildren();
    }

    @Override
    public String getFileSystemPath()
    {
        return mSource.getFileSystemPath();
    }
}
