package com.juztoss.bpmplayer.models;

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
    private List<Composition> mCompositions = new ArrayList<>();

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
    public List<Composition> getCompositions()
    {
        return mCompositions;
    }
}
