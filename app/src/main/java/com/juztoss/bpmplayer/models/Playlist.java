package com.juztoss.bpmplayer.models;

import android.database.Cursor;
import android.support.annotation.Nullable;

import com.juztoss.bpmplayer.models.songsources.ISongsSource;
import com.juztoss.bpmplayer.presenters.BPMPlayerApp;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JuzTosS on 5/8/2016.
 */
public class Playlist
{
    protected BPMPlayerApp mApp;
    protected float mMinBPM = 0;
    protected float mMaxBPM = 0;
    protected Cursor mList;
    protected boolean mNeedRebuild = true;
    private List<IUpdateListener> mUpdateListeners;
    private ISongsSource mSource;

    public Playlist(BPMPlayerApp app, ISongsSource source)
    {
        mApp = app;
        mSource = source;
    }

    public void setSource(ISongsSource source)
    {
        mSource = source;
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

        mList = mSource.getIds(mMinBPM, mMaxBPM);

        mNeedRebuild = false;
    }

    protected void setNeedRebuild()
    {
        mNeedRebuild = true;
        notifyUpdateListeners();
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

