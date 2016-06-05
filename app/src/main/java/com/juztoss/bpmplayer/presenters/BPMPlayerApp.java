package com.juztoss.bpmplayer.presenters;

import android.app.Application;
import android.content.res.Configuration;
import android.support.annotation.Nullable;

import com.juztoss.bpmplayer.DatabaseHelper;
import com.juztoss.bpmplayer.models.Playlist;
import com.juztoss.bpmplayer.services.PlaybackService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JuzTosS on 4/16/2016.
 */
public class BPMPlayerApp extends Application
{

    private BrowserPresenter mBrowserPresenter;
    private PlaybackService mPlaybackService;
    private boolean mIsBuildingLibrary;
    private boolean mIsScanFinished;
    private List<Playlist> mPlaylists;
    private float mMinBPM;
    private float mMaxBPM;

    private List<OnBPMChangedListener> mOnBPMChangedListeners;

    @Override
    public void onCreate()
    {
        super.onCreate();
        new DatabaseHelper(this);
        mBrowserPresenter = new BrowserPresenter(this);
        mPlaylists = Playlist.loadPlaylists();
    }

    @Override
    public void onTerminate()
    {
        super.onTerminate();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
    }

    public BrowserPresenter getBrowserPresenter()
    {
        return mBrowserPresenter;
    }

    public void setPlaybackService(@Nullable PlaybackService playbackService)
    {
        this.mPlaybackService = playbackService;
    }

    public PlaybackService getPlaybackService()
    {
        return mPlaybackService;
    }

    public boolean isPlaybackServiceRunning()
    {
        return mPlaybackService != null;
    }

    public void setIsBuildingLibrary(boolean isBuildingLibrary)
    {
        mIsBuildingLibrary = isBuildingLibrary;
    }

    public void setIsScanFinished(boolean isScanFinished)
    {
        mIsScanFinished = isScanFinished;
    }

    public List<Playlist> getPlaylists()
    {
        return mPlaylists;
    }

    public void setBPMRange(float minBPM, float maxBPM)
    {
        mMinBPM = minBPM;
        mMaxBPM = maxBPM;

        for(Playlist playlist : mPlaylists)
        {
            playlist.setBPMFilter(mMinBPM, mMaxBPM);
        }

        callBPMListeners();
    }

    private void callBPMListeners()
    {
        if(mOnBPMChangedListeners == null) return;
        for(OnBPMChangedListener listener : mOnBPMChangedListeners)
        {
            listener.onBPMChanged(mMinBPM, mMaxBPM);
        }
    }

    public void addOnRangeChangedListener(OnBPMChangedListener listener)
    {
        if(mOnBPMChangedListeners == null)
            mOnBPMChangedListeners = new ArrayList<>();

        if(!mOnBPMChangedListeners.contains(listener))
            mOnBPMChangedListeners.add(listener);
    }

    public void removeOnRangeChangedListener(OnBPMChangedListener listener)
    {
        mOnBPMChangedListeners.remove(listener);
    }

    public interface OnBPMChangedListener
    {
        void onBPMChanged(float minBPM, float maxBPM);
    }
}
