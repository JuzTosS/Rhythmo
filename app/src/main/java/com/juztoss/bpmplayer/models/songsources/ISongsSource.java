package com.juztoss.bpmplayer.models.songsources;

import android.database.Cursor;
import android.support.annotation.Nullable;

/**
 * Created by JuzTosS on 6/22/2016.
 */
public abstract class ISongsSource
{
    private ISourceUpdatedListener mListener;
    protected String mWordFilter;

    @Nullable
    public abstract Cursor getIds(float minBPM, float maxBPM);

    public abstract String getName();

    public abstract void delete();

    public abstract void rename(String newName);

    public abstract boolean isRenameAvailable();

    public abstract boolean isDeleteAvailable();

    public abstract boolean isModifyAvailable();

    public abstract void add(Cursor songIdsCursor);

    public abstract void remove(long songId);

    public void setUpdateListener(ISourceUpdatedListener listener)
    {
        mListener = listener;
    }

    protected void notifyUpdated()
    {
        if(mListener != null)
            mListener.onSourceUpdated();
    }

    public void setWordFilter(@Nullable String wordFilter)
    {
        mWordFilter = wordFilter;
        notifyUpdated();
    }

    abstract public void setSortType(SortType sortType);

    abstract public SortType getSortType();

    public interface ISourceUpdatedListener
    {
        void onSourceUpdated();
    }
}
