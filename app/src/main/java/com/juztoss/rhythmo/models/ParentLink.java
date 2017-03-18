package com.juztoss.rhythmo.models;

import android.database.Cursor;
import android.support.annotation.Nullable;

import com.juztoss.rhythmo.R;

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
    public int type()
    {
        return BaseExplorerElement.BACK_LINK;
    }

    @Override
    public List<BaseExplorerElement> getChildren(boolean onlyFolders)
    {
        return mSource.getChildren(onlyFolders);
    }

    @Override
    public int getIconResource()
    {
        return R.drawable.ic_navigate_before_black_24dp;
    }

    @Override
    public boolean isAddable()
    {
        return false;
    }

    @Override
    public AddState getAddState()
    {
        return AddState.NOT_ADDED;
    }

    @Override
    public void setAddState(AddState state)
    {

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

    @Override
    public String description()
    {
        return "";
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
