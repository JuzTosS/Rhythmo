package com.juztoss.rhythmo.models;

import android.database.Cursor;
import android.support.annotation.Nullable;

import com.juztoss.rhythmo.models.songsources.ISongsSource;
import com.juztoss.rhythmo.presenters.RhythmoApp;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JuzTosS on 5/8/2016.
 */
public class Playlist implements ISongsSource.ISourceUpdatedListener
{
    protected RhythmoApp mApp;
    protected float mMinBPM = 0;
    protected float mMaxBPM = 0;
    protected Cursor mList;
    protected boolean mNeedRebuild = true;
    private List<IUpdateListener> mUpdateListeners;
    private ISongsSource mSource;
    protected String mWordFilter;

    public Playlist(RhythmoApp app, ISongsSource source)
    {
        mApp = app;
        setSource(source);
    }

    @Override
    public void onSourceUpdated()
    {
        setNeedRebuild();
    }

    public void setSource(ISongsSource source)
    {
        if(mSource != null)
            mSource.setUpdateListener(null);

        mSource = source;
        mSource.setUpdateListener(this);
        setNeedRebuild();
    }

    public ISongsSource getSource()
    {
        return mSource;
    }

    protected void rebuild()
    {
        if (mList != null)
            mList.close();

        mList = mSource.getIds(mMinBPM, mMaxBPM, mWordFilter);

        mNeedRebuild = false;
    }

    public void setNeedRebuild()
    {
        mNeedRebuild = true;
        notifyUpdateListeners();
    }

    public void setWordFilter(@Nullable String wordFilter)
    {
        mWordFilter = wordFilter;
        setNeedRebuild();
    }

    public int findPositionById(long id) //TODO: Implement smarter and faster search
    {
        Cursor cursor = getList();
        if(cursor == null) return -1;
        while (cursor.moveToNext())
        {
            if(cursor.getLong(0) == id)
                return cursor.getPosition();
        }
        return -1;
    }

    @Nullable
    public Cursor getList()
    {
        if (mNeedRebuild || mList == null || mList.isClosed())
            rebuild();

        return mList;
    }

    public String getName()
    {
        return mSource.getName();
    }

    public void setBPMFilter(float minBPM, float maxBPM)
    {
        mMinBPM = minBPM;
        mMaxBPM = maxBPM;
        setNeedRebuild();
    }

    protected void notifyUpdateListeners()
    {
        if (mUpdateListeners == null) return;
        for (IUpdateListener listener : mUpdateListeners)
        {
            listener.onPlaylistUpdated();
        }
    }

    public void addUpdateListener(IUpdateListener listener)
    {
        if (mUpdateListeners == null)
            mUpdateListeners = new ArrayList<>();

        if (!mUpdateListeners.contains(listener))
            mUpdateListeners.add(listener);
    }

    public void removeUpdateListener(IUpdateListener listener)
    {
        if (mUpdateListeners != null && mUpdateListeners.contains(listener))
            mUpdateListeners.remove(listener);
    }

    public interface IUpdateListener
    {
        void onPlaylistUpdated();
    }
}

