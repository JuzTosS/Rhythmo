package com.juztoss.bpmplayer.models;

import android.database.Cursor;
import android.support.annotation.Nullable;

import java.util.List;

/**
 * Created by JuzTosS on 4/25/2016.
 */
public class CustomExplorerElement extends BaseExplorerElement
{
    private String mName;
    private ExplorerPriority mPriority;
    private List<BaseExplorerElement> mChildren;

    public CustomExplorerElement(String name, List<BaseExplorerElement> children, ExplorerPriority priority)
    {
        mName = name;
        mChildren = children;
        mPriority = priority;
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

    @Override
    public String name()
    {
        return mName;
    }

    @Override
    ExplorerPriority priority()
    {
        return mPriority;
    }

    @Override
    public List<BaseExplorerElement> getChildren()
    {
        return mChildren;
    }

    public void add(BaseExplorerElement element)
    {
        mChildren.add(element);
    }

    @Nullable
    @Override
    public Cursor getSongIds()
    {
        return null;
    }

    @Override
    public boolean hasChildren()
    {
        return true;
    }

    @Override
    public String description()
    {
        return "";
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
}
