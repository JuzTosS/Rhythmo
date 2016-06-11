package com.juztoss.bpmplayer.presenters;

import android.app.Application;
import android.content.res.Configuration;
import android.database.Cursor;
import android.support.annotation.Nullable;

import com.juztoss.bpmplayer.DatabaseHelper;
import com.juztoss.bpmplayer.models.Composition;
import com.juztoss.bpmplayer.models.Playlist;
import com.juztoss.bpmplayer.models.StaticAllPlaylist;
import com.juztoss.bpmplayer.models.StaticFolderPlaylist;
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
    private DatabaseHelper mDatabaseHelper;

    private List<OnBPMChangedListener> mOnBPMChangedListeners;

    private MusicLibraryHelper mMusicLibraryHelper;

    @Override
    public void onCreate()
    {
        super.onCreate();
        mMusicLibraryHelper = new MusicLibraryHelper(this);
        mDatabaseHelper = new DatabaseHelper(this);
        mBrowserPresenter = new BrowserPresenter(this);
        mPlaylists = loadPlaylists();
    }

    public List<Playlist> loadPlaylists()
    {
        List<Playlist> result = new ArrayList<>();

        Cursor playlists = getDatabaseHelper().getReadableDatabase().query(DatabaseHelper.TABLE_PLAYLISTS,
                new String[]{DatabaseHelper._ID, DatabaseHelper.PLAYLISTS_NAME},
                null, null, null, null, null);

        result.add(new StaticAllPlaylist(this));
        result.add(new StaticFolderPlaylist(this));
        int idIndex = playlists.getColumnIndex(DatabaseHelper._ID);
        int nameIndex = playlists.getColumnIndex(DatabaseHelper.PLAYLISTS_NAME);

        try
        {
            while (playlists.moveToNext())
            {
                result.add(new Playlist(playlists.getInt(idIndex), playlists.getString(nameIndex), this));
            }

        }
        finally
        {
            playlists.close();
        }

        return result;
    }

    public Composition getComposition(long id)
    {
        Cursor cursor = getDatabaseHelper().getReadableDatabase().query(DatabaseHelper.TABLE_MUSIC_LIBRARY,
                new String[]{DatabaseHelper._ID, DatabaseHelper.MUSIC_LIBRARY_PATH, DatabaseHelper.MUSIC_LIBRARY_NAME, DatabaseHelper.MUSIC_LIBRARY_BPMX10},
                DatabaseHelper._ID + "= ?",
                new String[]{Long.toString(id)},
                null, null, null);

        Composition composition;
        try
        {
            cursor.moveToFirst();

            composition = new Composition(
                    id,
                    cursor.getString(cursor.getColumnIndex(DatabaseHelper.MUSIC_LIBRARY_PATH)),
                    cursor.getString(cursor.getColumnIndex(DatabaseHelper.MUSIC_LIBRARY_NAME)),
                    (float) cursor.getInt(cursor.getColumnIndex(DatabaseHelper.MUSIC_LIBRARY_BPMX10)) / (float) 10);

        }
        finally
        {
            cursor.close();
        }

        return composition;
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

        for (Playlist playlist : mPlaylists)
        {
            playlist.setBPMFilter(mMinBPM, mMaxBPM);
        }

        callBPMListeners();
    }

    private void callBPMListeners()
    {
        if (mOnBPMChangedListeners == null) return;
        for (OnBPMChangedListener listener : mOnBPMChangedListeners)
        {
            listener.onBPMChanged(mMinBPM, mMaxBPM);
        }
    }

    public void addOnRangeChangedListener(OnBPMChangedListener listener)
    {
        if (mOnBPMChangedListeners == null)
            mOnBPMChangedListeners = new ArrayList<>();

        if (!mOnBPMChangedListeners.contains(listener))
            mOnBPMChangedListeners.add(listener);
    }

    public void removeOnRangeChangedListener(OnBPMChangedListener listener)
    {
        mOnBPMChangedListeners.remove(listener);
    }

    public void createNewPlaylist(String name)
    {
        mPlaylists.add(Playlist.create(name, this));
    }

    public DatabaseHelper getDatabaseHelper()
    {
        return mDatabaseHelper;
    }

    public void removePlaylist(int playlistIndex)
    {
        Playlist playlist = mPlaylists.get(playlistIndex);
        if(playlist != null)
        {
            playlist.delete();
            mPlaylists.remove(playlistIndex);
        }
    }

    public MusicLibraryHelper getMusicLibraryHelper()
    {
        return mMusicLibraryHelper;
    }

    public interface OnBPMChangedListener
    {
        void onBPMChanged(float minBPM, float maxBPM);
    }
}
