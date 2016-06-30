package com.juztoss.bpmplayer.presenters;

import android.app.Application;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.support.annotation.Nullable;

import com.juztoss.bpmplayer.DatabaseHelper;
import com.juztoss.bpmplayer.models.Composition;
import com.juztoss.bpmplayer.models.Playlist;
import com.juztoss.bpmplayer.models.songsources.LocalPlaylistSongsSource;
import com.juztoss.bpmplayer.models.songsources.SourcesFactory;
import com.juztoss.bpmplayer.services.PlaybackService;
import com.vk.sdk.VKSdk;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JuzTosS on 4/16/2016.
 */
public class BPMPlayerApp extends Application
{
    public static final String FIRST_RUN = "FirstRun";

    public static float MIN_BPM = 50;
    public static float MAX_BPM = 150;

    public static float MAX_BPM_SHIFT = 30;

    private BrowserPresenter mBrowserPresenter;
    private PlaybackService mPlaybackService;
    private boolean mIsBuildingLibrary;
    private List<Playlist> mPlaylists;
    private float mMinBPM;
    private float mMaxBPM;
    private DatabaseHelper mDatabaseHelper;

    private MusicLibraryHelper mMusicLibraryHelper;
    private SharedPreferences mSharedPreferences;

    @Override
    public void onCreate()
    {
        super.onCreate();
        VKSdk.initialize(this);
        mSharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        mMusicLibraryHelper = new MusicLibraryHelper(this);
        mDatabaseHelper = new DatabaseHelper(this);
        mBrowserPresenter = new BrowserPresenter(this);
        mPlaylists = loadPlaylists();
    }

    public List<Playlist> loadPlaylists()
    {
        List<Playlist> result = new ArrayList<>();

        Cursor playlists = getDatabaseHelper().getReadableDatabase().query(DatabaseHelper.TABLE_SOURCES,
                new String[]{DatabaseHelper._ID},
                null, null, null, null, null);

        result.add(new Playlist(this, SourcesFactory.createAllSongsSource(this)));
        int idIndex = playlists.getColumnIndex(DatabaseHelper._ID);

        try
        {
            while (playlists.moveToNext())
            {
                result.add(new Playlist(this, SourcesFactory.loadExist(this, playlists.getLong(idIndex))));
            }
        }
        finally
        {
            playlists.close();
        }

        return result;
    }

    @Nullable
    public Composition getComposition(long id)
    {
        Cursor cursor = getDatabaseHelper().getReadableDatabase().query(DatabaseHelper.TABLE_MUSIC_LIBRARY,
                new String[]{DatabaseHelper._ID, DatabaseHelper.MUSIC_LIBRARY_PATH, DatabaseHelper.MUSIC_LIBRARY_NAME, DatabaseHelper.MUSIC_LIBRARY_BPMX10, DatabaseHelper.MUSIC_LIBRARY_BPM_SHIFTEDX10},
                DatabaseHelper._ID + "= ?",
                new String[]{Long.toString(id)},
                null, null, null);


        Composition composition = null;
        try
        {
            if (cursor.getCount() > 0)
            {
                cursor.moveToFirst();

                composition = new Composition(
                        id,
                        cursor.getString(cursor.getColumnIndex(DatabaseHelper.MUSIC_LIBRARY_PATH)),
                        cursor.getString(cursor.getColumnIndex(DatabaseHelper.MUSIC_LIBRARY_NAME)),
                        (float) cursor.getInt(cursor.getColumnIndex(DatabaseHelper.MUSIC_LIBRARY_BPMX10)) / (float) 10,
                        (float) cursor.getInt(cursor.getColumnIndex(DatabaseHelper.MUSIC_LIBRARY_BPM_SHIFTEDX10)) / (float) 10);
            }
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

    public void notifyDatabaseUpdated()
    {
        for(Playlist playlist : mPlaylists)
        {
            playlist.setNeedRebuild();
        }
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

    }

    public void createNewPlaylist()
    {
        mPlaylists.add(new Playlist(this, SourcesFactory.createFolderSongSource("", this)));
    }

    public DatabaseHelper getDatabaseHelper()
    {
        return mDatabaseHelper;
    }

    public void removePlaylist(int playlistIndex)
    {
        Playlist playlist = mPlaylists.get(playlistIndex);
        if (playlist != null)
        {
            playlist.getSource().delete();
            mPlaylists.remove(playlistIndex);
        }
    }

    public MusicLibraryHelper getMusicLibraryHelper()
    {
        return mMusicLibraryHelper;
    }

    public boolean isBuildingLibrary()
    {
        return mIsBuildingLibrary;
    }

    public void updateBpm(Composition composition)
    {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.MUSIC_LIBRARY_BPMX10, (int) (composition.bpm() * 10));
        values.put(DatabaseHelper.MUSIC_LIBRARY_BPM_SHIFTEDX10, (int) (composition.bpmShifted() * 10));
        getDatabaseHelper().getWritableDatabase().update(DatabaseHelper.TABLE_MUSIC_LIBRARY, values, DatabaseHelper._ID + " = ?", new String[]{Long.toString(composition.id())});
    }

    public SharedPreferences getSharedPreferences()
    {
        return mSharedPreferences;
    }
}
