package com.juztoss.rhythmo.presenters;

import android.app.Application;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.juztoss.rhythmo.R;
import com.juztoss.rhythmo.audio.AdvancedMediaPlayer;
import com.juztoss.rhythmo.models.Composition;
import com.juztoss.rhythmo.models.DatabaseHelper;
import com.juztoss.rhythmo.models.Playlist;
import com.juztoss.rhythmo.models.songsources.SourcesFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JuzTosS on 4/16/2016.
 */
public class RhythmoApp extends Application
{
    static {
        System.loadLibrary(AdvancedMediaPlayer.LIBRARY_NAME);
    }

    public static final String FIRST_RUN = "FirstRun";
    public static final String LIBRARY_BUILD_STARTED = "LibraryBuildStarted";
    public static final String ALL_SONGS_SORT = "AllSongsSort";

    public static float MIN_BPM = 25;
    public static float MAX_BPM = 250;

    public static float MAX_BPM_SHIFT = 30;

    private BrowserPresenter mBrowserPresenter;
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
        mSharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        mMusicLibraryHelper = new MusicLibraryHelper(this);
        mDatabaseHelper = new DatabaseHelper(this);
        mBrowserPresenter = new BrowserPresenter(this);
        mPlaylists = loadPlaylists();
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);
    }

    private SharedPreferences.OnSharedPreferenceChangeListener mOnSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener()
    {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
        {
            if(key.equals(getResources().getString(R.string.pref_bpm_auto_shift_range)))
                notifyPlaylistsRepresentationUpdated();
        }
    };

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

    public void setIsBuildingLibrary(boolean isBuildingLibrary)
    {
        mIsBuildingLibrary = isBuildingLibrary;
    }

    public void notifyPlaylistsRepresentationUpdated()
    {
        for (Playlist playlist : mPlaylists)
        {
            playlist.setNeedRebuild();
        }
    }

    public List<Playlist> getPlaylists()
    {
        return mPlaylists;
    }

    public float getAvailableToPlayBPM(float bpm)
    {
        if (isBPMInRange(bpm))
            return bpm;
        else
        {
            if (mMinBPM - bpm >= 0 && mMinBPM - bpm <= getBPMFilterAdditionWindowSize())
                return mMinBPM;
            else if (bpm - mMaxBPM >= 0 && bpm - mMaxBPM <= getBPMFilterAdditionWindowSize())
                return mMaxBPM;
            else
                return bpm;
        }
    }

    public int getBPMFilterAdditionWindowSize()
    {
        String key = getResources().getString(R.string.pref_bpm_auto_shift_range);
        return PreferenceManager.getDefaultSharedPreferences(this).getInt(key, 0);
    }

    public boolean isBPMInRange(float bpm)
    {
        if (mMinBPM > 0 && mMaxBPM > 0)
            return bpm >= mMinBPM && bpm <= mMaxBPM;
        else
            return true;
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
        mPlaylists.add(new Playlist(this, SourcesFactory.createLocalPlaylistSongSource(this)));
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
