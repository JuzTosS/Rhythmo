package com.juztoss.rhythmo.services;

import android.content.Context;
import android.content.Intent;

/**
 * The library service builder
 * start() method must be called last
 */

public class LibraryServiceBuilder
{
    private final Context mContext;
    private final Intent mIntent;

    public LibraryServiceBuilder(Context context)
    {
        mContext = context;
        mIntent = new Intent(context, BuildMusicLibraryService.class);
    }

    /**
     * Clear the information of bpm of a songs before find the new one
     */
    public LibraryServiceBuilder clearBpm()
    {
        mIntent.putExtra(BuildMusicLibraryService.CLEAR_BPM, true);
        return this;
    }

    /**
     * If any task is currently executing when stop them
     */
    public LibraryServiceBuilder stopCurrentlyExecuting()
    {
        mIntent.putExtra(BuildMusicLibraryService.STOP_CURRENTLY_ECECUTING, true);
        return this;
    }

    /**
     * Shows to a user notifications about progress
     */
    public LibraryServiceBuilder enableNotifications()
    {
        mIntent.putExtra(BuildMusicLibraryService.ENABLE_NOTIFICATIONS, true);
        return this;
    }

    /**
     * Detect bpm of songs
     */
    public LibraryServiceBuilder detectBpm()
    {
        if(mIntent.getExtras() != null && mIntent.getBooleanExtra(BuildMusicLibraryService.DETECT_BPM_IN_PLAYLIST, false))
        {
            throw new RuntimeException("Unable to detectBpm() in conjunction with detectBpmInAPlaylist()");
        }

        mIntent.putExtra(BuildMusicLibraryService.DETECT_BPM, true);
        return this;
    }

    /**
     * Finds all the songs in a media store
     */
    public LibraryServiceBuilder scanMediaStore()
    {
        mIntent.putExtra(BuildMusicLibraryService.SCAN_MEDIA_STORE, true);
        return this;
    }

    /**
     * Finds all the songs in a storage of the device
     */
    public LibraryServiceBuilder scanStorage()
    {
        mIntent.putExtra(BuildMusicLibraryService.SCAN_STORAGE, true);
        return this;
    }

    /**
     * Detect bpm for song only in the playlist
     */
    public LibraryServiceBuilder detectBpmInAPlaylist(int playlistIndex)
    {
        if(mIntent.getExtras() != null && mIntent.getBooleanExtra(BuildMusicLibraryService.DETECT_BPM, false))
        {
            throw new RuntimeException("Unable to detectBpmInAPlaylist() in conjunction with detectBpm()");
        }

        mIntent.putExtra(BuildMusicLibraryService.DETECT_BPM_IN_PLAYLIST, true);
        return this;
    }

    /**
     * Starts the service
     */
    public void start()
    {
        mContext.startService(mIntent);
    }
}
