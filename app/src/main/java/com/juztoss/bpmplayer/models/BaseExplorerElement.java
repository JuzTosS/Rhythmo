package com.juztoss.bpmplayer.models;

import android.database.Cursor;
import android.support.annotation.Nullable;

import java.util.List;

/**
 * Created by JuzTosS on 4/25/2016.
 * Override if you want to display item in the browser
 */
public abstract class BaseExplorerElement implements Comparable<BaseExplorerElement>
{
    /**
     * Dispalyed name of the element
     */
    public abstract String name();

    /**
     * Priorty for sorting elements
     */
    abstract ExplorerPriority priority();

    /**
     * Must return children of this element
     */
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

    /**
     * Return true if getChildren method returns children
     */
    public abstract boolean hasChildren();

    /**
     * Return file system path associated with that element to be used as songs source
     */
    public String getFileSystemPath()
    {
        return null;
    }

    /**
     * Dispalyed description of the element
     */
    public abstract String description();

    /**
     * Return resource id of image to be displayed as an icon of the element
     */
    public abstract int getIconResource();

    /**
     * Return true if it is possible to add this element to a playlist as a songs source.
     */
    public abstract boolean isAddable();

    /**
     * Return current element state (If is added or not)
     */
    public abstract AddState getAddState();

    /**
     * Set current element state (If is added or not)
     */
    public abstract void setAddState(AddState state);

    public enum AddState
    {
        NOT_ADDED,
        ADDED,
        PARTLY_ADDED //Means that only part of children of that element are added
    }
}
