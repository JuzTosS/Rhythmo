package com.juztoss.bpmplayer.models;

import com.juztoss.bpmplayer.presenters.ISongsDataSource;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JuzTosS on 4/25/2016.
 */
public class CustomExplorerElement extends BaseExplorerElement
{
    private String mName;
    private ExplorerPriority mPriority;
    private List<BaseExplorerElement> mChildren;
    private ISongsDataSource mSource = new EmptySongsSource();

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

    @Override
    public ISongsDataSource getSource()
    {
        return mSource;
    }
}
