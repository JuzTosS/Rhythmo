package com.juztoss.rhythmo.models;

import android.database.Cursor;
import android.support.annotation.Nullable;

import java.util.List;

/**
 * Created by JuzTosS on 12/17/2016.
 */

public class MockExplorerElement extends BaseExplorerElement
{
    private String mPath;
    public MockExplorerElement(String path)
    {
        mPath = path;
    }

    @Override
    public String name()
    {
        return null;
    }

    @Override
    public ExplorerPriority priority()
    {
        return null;
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
        return null;
    }

    @Override
    public int compareTo(BaseExplorerElement another)
    {
        return super.compareTo(another);
    }

    @Override
    public boolean hasChildren()
    {
        return false;
    }

    @Override
    public String getFileSystemPath()
    {
        return mPath;
    }

    @Override
    public String description()
    {
        return null;
    }

    @Override
    public int getIconResource()
    {
        return 0;
    }

    @Override
    public boolean isAddable()
    {
        return false;
    }

    @Override
    public AddState getAddState()
    {
        return null;
    }

    @Override
    public void setAddState(AddState state)
    {

    }

    @Override
    public int type()
    {
        return BaseExplorerElement.FOLDER_LINK;
    }

    @Override
    public void dispose()
    {

    }

    @Override
    public BaseExplorerElement getChildFromPath(String path, boolean onlyFolders) {
        return null;
    }
}
