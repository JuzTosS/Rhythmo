package com.juztoss.rhythmo.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.juztoss.rhythmo.R;
import com.juztoss.rhythmo.presenters.RhythmoApp;
import com.juztoss.rhythmo.views.activities.SettingsActivity;

/**
 * Created by JuzTosS on 5/27/2016.
 */
public class BuildMusicLibraryService extends Service
{
    public static final String UPDATE_PROGRESS_ACTION = "com.juztoss.rhythmo.action.UPDATE_PROGRESS";
    public static final String PROGRESS_ACTION_OVERALL_PROGRESS = "OverallProgress";
    public static final String PROGRESS_ACTION_MAX_PROGRESS = "MaxProgress";
    public static final String PROGRESS_ACTION_HEADER = "Header";

    /**
     * Clear all the data in the library
     */
    public static final String CLEAR = "Clear";
    /**
     * Stops executing the currently running tasks and clear the library
     * The other flags are ignored
     */
    public static final String STOP_AND_CLEAR = "StopAndClear";
    /**
     * Disable any interaction with user
     */
    public static final String SILENT_MODE = "SilentMode";
    /**
     * Disable bpm detection
     */
    public static final String DONT_DETECT_BPM = "DontDetectBPM";
    /**
     * If any task is executed don't interrupt them.
     * The new task WON'T be started
     */
    public static final String DONT_INTERRUPT_EXIST_TASKS = "DontInterruptExistTasks";

    /**
     * Detect bpm for song only in the playlist
     */
    public static final String PLAYLIST_INDEX = "PlaylistIndex";

    /**
     * Reset BPM values before scanning
     */
    public static final String RESET_BPM = "ResetBpm";


    private RhythmoApp mApp;
    private NotificationCompat.Builder mBuilder;
    private Notification mNotification;
    private NotificationManager mNotifyManager;
    public static final int NOTIFICATION_ID = 43;
    private volatile boolean mSilentMode;
    private volatile boolean mDontDetectBPM;

    private AsyncBuildLibraryTask mTaskBuildLib;
    private AsyncDetectBpmByNamesTask mTaskDetectBpmByNames;
    private AsyncDetectBpmByDataTask mTaskDetectBpmByData;

    @Override
    public void onCreate()
    {
        mApp = (RhythmoApp) this.getApplicationContext();
        mBuilder = new NotificationCompat.Builder(mApp);
        mBuilder.setSmallIcon(R.drawable.ic_play_arrow_black_36dp);
        mBuilder.setContentTitle(getResources().getString(R.string.building_music_library));
        mBuilder.setTicker(getResources().getString(R.string.building_music_library));
        mBuilder.setContentText("");
        mBuilder.setProgress(0, 0, true);
        mBuilder.setShowWhen(false);

        mNotifyManager = (NotificationManager) mApp.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotification = mBuilder.build();
        mNotification.flags |= Notification.FLAG_INSISTENT | Notification.FLAG_NO_CLEAR;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if(intent == null)
        {
            stopSelf();
            return START_NOT_STICKY;
        }



        boolean stopAndClear = false;
        boolean clear = false;
        boolean resetBpm = false;
        int playlistIndex = 0;
        if(intent.getExtras() != null)
        {
            boolean dontInterruptExistTasks = intent.getExtras().getBoolean(DONT_INTERRUPT_EXIST_TASKS, false);
            if(dontInterruptExistTasks && isInProgress())
                return START_NOT_STICKY;
            else
                cancelTasks();

            stopAndClear = intent.getExtras().getBoolean(STOP_AND_CLEAR, false);
            clear = intent.getExtras().getBoolean(CLEAR, false);
            playlistIndex = intent.getExtras().getInt(PLAYLIST_INDEX, -1);
            resetBpm = intent.getExtras().getBoolean(RESET_BPM, false);
            mSilentMode = intent.getExtras().getBoolean(SILENT_MODE, false);
            mDontDetectBPM = intent.getExtras().getBoolean(DONT_DETECT_BPM, false);
        }

        if(stopAndClear)
        {
            mTaskBuildLib = new AsyncBuildLibraryTask(mApp, true);
            mTaskBuildLib.setOnBuildLibraryProgressUpdate(mOnClearLibraryUpdate);
            mTaskBuildLib.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);

            return START_STICKY;
        }

        if(!mSilentMode)
        {
            Toast.makeText(this, getString(R.string.build_library_started), Toast.LENGTH_LONG).show();
            mNotifyManager.notify(NOTIFICATION_ID, mNotification);
        }

        mApp.setIsBuildingLibrary(true);
        if(playlistIndex < 0)
        {
            mTaskBuildLib = new AsyncBuildLibraryTask(mApp, clear);
            mTaskBuildLib.setOnBuildLibraryProgressUpdate(mOnBuildLibraryUpdate);
            mTaskBuildLib.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
        }

        String key = getResources().getString(R.string.pref_recognize_bpm_from_name);
        boolean needToGetBPMByNames =  PreferenceManager.getDefaultSharedPreferences(this).getBoolean(key, true);

        if(!mDontDetectBPM)
        {
            if (needToGetBPMByNames)
            {
                mTaskDetectBpmByNames = new AsyncDetectBpmByNamesTask(mApp, playlistIndex, resetBpm);
                mTaskDetectBpmByNames.setOnBuildLibraryProgressUpdate(mOnDetectBpmByNamesUpdate);
                mTaskDetectBpmByNames.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
                resetBpm = false;
            }

            mTaskDetectBpmByData = new AsyncDetectBpmByDataTask(mApp, playlistIndex, resetBpm);
            mTaskDetectBpmByData.setOnBuildLibraryProgressUpdate(mOnDetectBpmByDataUpdate);
            mTaskDetectBpmByData.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
        }

        return START_REDELIVER_INTENT;
    }

    private boolean isInProgress()
    {
        return mTaskBuildLib != null || mTaskDetectBpmByNames != null || mTaskDetectBpmByData != null;
    }

    private void cancelTasks()
    {
        if(mTaskBuildLib != null)
            mTaskBuildLib.cancel(true);

        if(mTaskDetectBpmByNames != null)
            mTaskDetectBpmByNames.cancel(true);

        if(mTaskDetectBpmByData != null)
            mTaskDetectBpmByData.cancel(true);

        mTaskBuildLib = null;
        mTaskDetectBpmByData = null;
        mTaskDetectBpmByNames = null;
    }

    @Override
    public void onDestroy()
    {
        cancelTasks();
        if(mNotifyManager != null)
            mNotifyManager.cancel(NOTIFICATION_ID);

        mApp.setIsBuildingLibrary(false);
        mApp.notifyPlaylistsRepresentationUpdated();

        Intent intent = new Intent(UPDATE_PROGRESS_ACTION);
        intent.putExtra(PROGRESS_ACTION_HEADER, mApp.getString(R.string.build_library_desc));
        intent.putExtra(PROGRESS_ACTION_OVERALL_PROGRESS, 0);
        intent.putExtra(PROGRESS_ACTION_MAX_PROGRESS, 0);
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(mApp);
        broadcastManager.sendBroadcast(intent);

        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return new Binder();
    }

    private AsyncBuildLibraryTask.OnBuildLibraryProgressUpdate mOnClearLibraryUpdate = new AsyncBuildLibraryTask.OnBuildLibraryProgressUpdate()
    {
        @Override
        public void onStartBuildingLibrary(AsyncBuildLibraryTask task)
        {

        }

        @Override
        public void onProgressUpdate(AsyncBuildLibraryTask task, int overallProgress, int maxProgress, boolean mediaStoreTransferDone)
        {

        }

        @Override
        public void onFinishBuildingLibrary(AsyncBuildLibraryTask task)
        {
            if(!mSilentMode)
                Toast.makeText(mApp, R.string.clear_library_finished, Toast.LENGTH_LONG).show();

            stopSelf();
        }
    };

    private AsyncBuildLibraryTask.OnBuildLibraryProgressUpdate mOnBuildLibraryUpdate = new AsyncBuildLibraryTask.OnBuildLibraryProgressUpdate()
    {
        @Override
        public void onStartBuildingLibrary(AsyncBuildLibraryTask task)
        {

        }

        @Override
        public void onProgressUpdate(AsyncBuildLibraryTask task, int overallProgress, int maxProgress, boolean mediaStoreTransferDone)
        {
            String header = getResources().getString(R.string.building_music_library);
            showNotification(header, overallProgress, maxProgress);
        }

        @Override
        public void onFinishBuildingLibrary(AsyncBuildLibraryTask task)
        {
            mApp.notifyPlaylistsRepresentationUpdated();

            if(mDontDetectBPM)
            {
                if(!mSilentMode)
                    Toast.makeText(mApp, R.string.build_library_finished, Toast.LENGTH_LONG).show();

                stopSelf();
            }
        }
    };

    private OnDetectBpmByNamesUpdate mOnDetectBpmByNamesUpdate = new OnDetectBpmByNamesUpdate()
    {
        @Override
        public void onStartBuildingLibrary(AsyncDetectBpmByNamesTask task)
        {

        }

        @Override
        public void onProgressUpdate(AsyncDetectBpmByNamesTask task, int overallProgress, int maxProgress, boolean mediaStoreTransferDone)
        {
            String header = getResources().getString(R.string.detect_bpm_by_name);
            showNotification(header, overallProgress, maxProgress);
        }

        @Override
        public void onFinishBuildingLibrary(AsyncDetectBpmByNamesTask task)
        {
            mApp.notifyPlaylistsRepresentationUpdated();
        }
    };

    private OnDetectBpmByDataUpdate mOnDetectBpmByDataUpdate = new OnDetectBpmByDataUpdate()
    {
        @Override
        public void onStartBuildingLibrary(AsyncDetectBpmByDataTask task)
        {

        }

        @Override
        public void onProgressUpdate(AsyncDetectBpmByDataTask task, int overallProgress, int maxProgress, boolean mediaStoreTransferDone)
        {
            String header = getResources().getString(R.string.detect_bpm_by_data, overallProgress, maxProgress);
            showNotification(header, overallProgress, maxProgress);
        }

        @Override
        public void onFinishBuildingLibrary(AsyncDetectBpmByDataTask task)
        {
            mApp.notifyPlaylistsRepresentationUpdated();
            if(!mSilentMode)
                Toast.makeText(mApp, R.string.build_library_finished, Toast.LENGTH_LONG).show();

            stopSelf();
        }
    };

    private void showNotification(String header, int overallProgress, int maxProgress)
    {
        if(mSilentMode) return;

        mBuilder = new NotificationCompat.Builder(mApp);
        mBuilder.setSmallIcon(R.drawable.ic_play_arrow_black_36dp);
        mBuilder.setContentTitle(header);
        mBuilder.setTicker(header);
        mBuilder.setContentText("");
        mBuilder.setProgress(maxProgress, overallProgress, false);

        Intent launchNowPlayingIntent = new Intent(this, SettingsActivity.class);
        launchNowPlayingIntent.putExtra(PROGRESS_ACTION_HEADER, header);
        launchNowPlayingIntent.putExtra(PROGRESS_ACTION_OVERALL_PROGRESS, overallProgress);
        launchNowPlayingIntent.putExtra(PROGRESS_ACTION_MAX_PROGRESS, maxProgress);
        PendingIntent launchNowPlayingPendingIntent = PendingIntent.getActivity(this, 0, launchNowPlayingIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(launchNowPlayingPendingIntent);

        mNotification = mBuilder.build();

        mNotification.flags |= Notification.FLAG_INSISTENT | Notification.FLAG_NO_CLEAR;
        mNotifyManager.notify(NOTIFICATION_ID, mNotification);

        Intent intent = new Intent(UPDATE_PROGRESS_ACTION);
        intent.putExtra(PROGRESS_ACTION_HEADER, header);
        intent.putExtra(PROGRESS_ACTION_OVERALL_PROGRESS, overallProgress);
        intent.putExtra(PROGRESS_ACTION_MAX_PROGRESS, maxProgress);
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(mApp);
        broadcastManager.sendBroadcast(intent);
    }
}
