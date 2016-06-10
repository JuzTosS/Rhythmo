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
}
