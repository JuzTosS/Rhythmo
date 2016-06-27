package com.juztoss.bpmplayer.models;

import android.database.Cursor;
import android.support.annotation.Nullable;

import java.util.List;

/**
 * Created by JuzTosS on 4/25/2016.
 */
public abstract class BaseExplorerElement implements Comparable<BaseExplorerElement>
{
    public abstract String name();

    abstract ExplorerPriority priority();

    public abstract List<BaseExplorerElement> getChildren();

    @Nullable
    public abstract Cursor getSongIds();

    @Override
    public int compareTo(BaseExplorerElement another)
    {
        if (!priority().equals(another.priority()))
            return priority().compareTo(another.priority());
        else
            return name().compareTo(another.name());
    }

    public abstract boolean hasChildren();

    public String getFileSystemPath()
    {
        return null;
    }

    public abstract String description();

    public abstract int getIconResource();
}
