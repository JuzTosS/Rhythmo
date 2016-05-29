package com.juztoss.bpmplayer.presenters;

import android.app.Application;
import android.content.res.Configuration;
import android.support.annotation.Nullable;

import com.juztoss.bpmplayer.DatabaseHelper;
import com.juztoss.bpmplayer.services.PlaybackService;

/**
 * Created by JuzTosS on 4/16/2016.
 */
public class BPMPlayerApp extends Application
{

    private BrowserPresenter mBrowserPresenter;
    private PlaybackService mPlaybackService;
    private boolean mIsBuildingLibrary;
    private boolean mIsScanFinished;

    @Override
    public void onCreate()
    {
        super.onCreate();
        new DatabaseHelper(this);
        mBrowserPresenter = new BrowserPresenter(this);
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
//
//    public void setLastFolder(ISongSource songsFolder)
//    {
//        ContentValues values = new ContentValues();
//        values.put(DatabaseHelper.SETTING_NAME, DatabaseHelper.SETTINGS_SONG_FOLDER);
//        values.put(DatabaseHelper.SETTING_VALUE, songsFolder.source().getPath());
//        if (DatabaseHelper.db().update(DatabaseHelper.TABLE_SETTINGS,
//                values, DatabaseHelper.SETTING_NAME + "= ?",
//                new String[]{DatabaseHelper.SETTINGS_SONG_FOLDER}) <= 0)
//        {
//            DatabaseHelper.db().insert(DatabaseHelper.TABLE_SETTINGS, null, values);
//        }
//    }
//
//    public File getLastFolder()
//    {
//        Cursor cursor = DatabaseHelper.db().query(DatabaseHelper.TABLE_SETTINGS,
//                new String[]{DatabaseHelper.SETTING_VALUE},
//                DatabaseHelper.SETTING_NAME + "= ?",
//                new String[]{DatabaseHelper.SETTINGS_SONG_FOLDER},
//                null, null, null);
//        if (cursor.getCount() > 0)
//        {
//            cursor.moveToFirst();
//            return new Folder(new File(cursor.getString(0)));
//        }
//        else
//            return null;
//    }

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
}
