package com.juztoss.bpmplayer.models;

import com.juztoss.bpmplayer.presenters.ISongsDataSource;

import java.util.List;

/**
 * Created by JuzTosS on 4/25/2016.
 */
public abstract class BaseExplorerElement implements Comparable<BaseExplorerElement>
{
    public abstract String name();

    abstract ExplorerPriority priority();

    public abstract List<BaseExplorerElement> getChildren();
    public abstract ISongsDataSource getSource();

    @Override
    public int compareTo(BaseExplorerElement another)
    {
        if (!priority().equals(another.priority()))
            return priority().compareTo(another.priority());
        else
            return name().compareTo(another.name());
    }

}
