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
import android.util.Log;
import android.widget.Toast;

import com.juztoss.rhythmo.R;
import com.juztoss.rhythmo.presenters.RhythmoApp;
import com.juztoss.rhythmo.views.activities.SettingsActivity;

/**
 * Created by JuzTosS on 5/27/2016.
 */
public class BuildMusicLibraryService extends Service
{
    public static final boolean DEBUG = false;

    public static final String UPDATE_PROGRESS_ACTION = "com.juztoss.rhythmo.action.UPDATE_PROGRESS";
    public static final String PROGRESS_ACTION_OVERALL_PROGRESS = "OverallProgress";
    public static final String PROGRESS_ACTION_MAX_PROGRESS = "MaxProgress";
    public static final String PROGRESS_ACTION_HEADER = "Header";

    /**
     * Clear the information of bpm of a songs before find the new one
     */
    public static final String CLEAR_BPM = "ClearBpm";

    /**
     * If any task is currently executing when stop them
     */
    public static final String STOP_CURRENTLY_ECECUTING = "StopCurrentlyExecuting";

    /**
     * Shows to a user notifications about progress
     */
    public static final String ENABLE_NOTIFICATIONS = "EnableNotification";

    /**
     * Detect bpm of songs
     */
    public static final String DETECT_BPM = "DetectBpm";

    /**
     * Finds all the songs in a media store
     */
    public static final String SCAN_MEDIA_STORE = "ScanMediaStore";

    /**
     * Finds all the songs in a storage of the device
     */
    public static final String SCAN_STORAGE = "ScanStorage";

    /**
     * Detect bpm for song only in the playlist
     */
    public static final String DETECT_BPM_IN_PLAYLIST = "PlaylistIndex";


    public static final int NOTIFICATION_ID = 43;
    private RhythmoApp mApp;
    private NotificationCompat.Builder mBuilder;
    private Notification mNotification;
    private NotificationManager mNotifyManager;
    private boolean mEnableNotifications = false;
    private int mTasksCounter = 0;

    private AsyncBuildLibraryTask mTaskBuildLib;
    private AsyncDetectBpmByNamesTask mTaskDetectBpmByNames;
    private AsyncDetectBpmTaskAbstract mTaskDetectBpmByData;
    private AsyncDetectBpmTaskAbstract mTaskDetectBpmByNamesAndData;
    private AsyncClearLibraryTask mTaskClear;

    @Override
    public void onCreate()
    {
        mApp = (RhythmoApp) this.getApplicationContext();
        mBuilder = new NotificationCompat.Builder(mApp);
        mNotifyManager = (NotificationManager) mApp.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if(DEBUG) Log.d(BuildMusicLibraryService.class.toString(), "onStartCommand");

        if(intent == null || intent.getExtras() == null)
        {
            stopSelf();
            return START_NOT_STICKY;
        }

        boolean clearBpm = intent.getExtras().getBoolean(CLEAR_BPM, false);
        boolean stopCurrentlyExecuting = intent.getExtras().getBoolean(STOP_CURRENTLY_ECECUTING, false);
        boolean enableNoticications = intent.getExtras().getBoolean(ENABLE_NOTIFICATIONS, false);
        boolean detectBpm = intent.getExtras().getBoolean(DETECT_BPM, false);
        boolean scanMediaStore = intent.getExtras().getBoolean(SCAN_MEDIA_STORE, false);
        boolean scanStorage = intent.getExtras().getBoolean(SCAN_STORAGE, false);
        int detectBpmInPlaylist = intent.getExtras().getInt(DETECT_BPM_IN_PLAYLIST, -1);

        if(DEBUG) Log.d(BuildMusicLibraryService.class.toString(), clearBpm + ", " + stopCurrentlyExecuting + ", " + mEnableNotifications + ", " + detectBpm + ", " + scanMediaStore + ", " + scanStorage  + ", " + detectBpmInPlaylist);

        if(!stopCurrentlyExecuting && isInProgress())
            return START_NOT_STICKY;
        else
            cancelTasks();

        mApp.setIsBuildingLibrary(true);

        mEnableNotifications = enableNoticications;
        if(mEnableNotifications)
        {
            Toast.makeText(this, getString(R.string.build_library_started), Toast.LENGTH_LONG).show();
            showNotification(getResources().getString(R.string.speeding_up), 0, 0);
        }

        if(scanMediaStore)
        {
            mTaskBuildLib = new AsyncBuildLibraryTask(mApp);
            mTaskBuildLib.setOnBuildLibraryProgressUpdate(mOnBuildLibraryUpdate);
            mTasksCounter++;
            mTaskBuildLib.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);

            if(scanStorage)
            {
//                mTasksCounter++;
            }
        }

        if(clearBpm)
        {
            mTaskClear = new AsyncClearLibraryTask(mApp);
            mTaskClear.setListener(mClearLibraryListener);
            mTasksCounter++;
            mTaskClear.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
        }

        String key = getResources().getString(R.string.pref_recognize_bpm_from_name);
        boolean needToGetBPMByNames =  PreferenceManager.getDefaultSharedPreferences(this).getBoolean(key, true);
        if(detectBpm)
        {
            if (needToGetBPMByNames)
            {
                mTaskDetectBpmByNames = new AsyncDetectBpmByNamesTask(mApp, detectBpmInPlaylist, false);
                mTaskDetectBpmByNames.setOnBuildLibraryProgressUpdate(mOnDetectBpmByNamesUpdate);
                mTasksCounter++;
                mTaskDetectBpmByNames.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
            }

            mTaskDetectBpmByData = new AsyncDetectBpmByDataTask(mApp, detectBpmInPlaylist, false);
            mTaskDetectBpmByData.setOnBuildLibraryProgressUpdate(mOnDetectBpmByDataUpdate);
            mTasksCounter++;
            mTaskDetectBpmByData.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
        }
        else if(detectBpmInPlaylist >= 0)
        {
            mTaskDetectBpmByNamesAndData = new AsyncDetectBpmByNamesAndDataTask(mApp, detectBpmInPlaylist, false, needToGetBPMByNames);
            mTaskDetectBpmByNamesAndData.setOnBuildLibraryProgressUpdate(mOnDetectBpmByDataUpdate);
            mTasksCounter++;
            mTaskDetectBpmByNamesAndData.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
        }

        return START_REDELIVER_INTENT;
    }

    private void onTaskComplete()
    {
        mTasksCounter--;
        if(mTasksCounter <= 0)
        {
            if(mEnableNotifications)
                Toast.makeText(mApp, R.string.build_library_finished, Toast.LENGTH_LONG).show();

            stopSelf();
        }
    }

    private boolean isInProgress()
    {
        return mTaskBuildLib != null
                || mTaskDetectBpmByNames != null
                || mTaskDetectBpmByData != null
                || mTaskDetectBpmByNamesAndData != null
                || mTaskClear != null;
    }

    private void cancelTasks()
    {
        if(mTaskBuildLib != null)
            mTaskBuildLib.cancel(true);

        if(mTaskDetectBpmByNames != null)
            mTaskDetectBpmByNames.cancel(true);

        if(mTaskDetectBpmByData != null)
            mTaskDetectBpmByData.cancel(true);

        if(mTaskClear != null)
            mTaskClear.cancel(true);

        if(mTaskDetectBpmByNamesAndData != null)
            mTaskDetectBpmByNamesAndData.cancel(true);

        mTaskBuildLib = null;
        mTaskDetectBpmByData = null;
        mTaskDetectBpmByNames = null;
        mTaskClear = null;
        mTaskDetectBpmByNamesAndData = null;

        mTasksCounter = 0;
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

    private AsyncClearLibraryTask.Listener mClearLibraryListener = new AsyncClearLibraryTask.Listener()
    {
        @Override
        public void onFinish()
        {
            mApp.notifyPlaylistsRepresentationUpdated();
            onTaskComplete();
        }
    };

    private AsyncBuildLibraryTask.OnBuildLibraryProgressUpdate mOnBuildLibraryUpdate = new AsyncBuildLibraryTask.OnBuildLibraryProgressUpdate()
    {
        @Override
        public void onProgressUpdate(int overallProgress, int maxProgress, boolean mediaStoreTransferDone)
        {
            if(!mEnableNotifications) return;

            String header = getResources().getString(R.string.looking_for_new_songs);
            showNotification(header, overallProgress, maxProgress);
        }

        @Override
        public void onFinish(boolean wasDatabaseChanged)
        {
            if(wasDatabaseChanged)
                mApp.notifyPlaylistsRepresentationUpdated();

            onTaskComplete();
        }
    };

    private AsyncDetectBpmTaskAbstract.Listener mOnDetectBpmByNamesUpdate = new AsyncDetectBpmTaskAbstract.Listener()
    {
        @Override
        public void onProgressUpdate(int overallProgress, int maxProgress, boolean mediaStoreTransferDone)
        {
            String header = getResources().getString(R.string.detect_bpm);
            showNotification(header, overallProgress, maxProgress);
        }

        @Override
        public void onFinish()
        {
            mApp.notifyPlaylistsRepresentationUpdated();
            onTaskComplete();
        }
    };

    private AsyncDetectBpmTaskAbstract.Listener mOnDetectBpmByDataUpdate = new AsyncDetectBpmTaskAbstract.Listener()
    {
        @Override
        public void onProgressUpdate(int overallProgress, int maxProgress, boolean mediaStoreTransferDone)
        {
            String header = getResources().getString(R.string.detect_bpm_progress_notification, overallProgress, maxProgress);
            showNotification(header, overallProgress, maxProgress);
        }

        @Override
        public void onFinish()
        {
            mApp.notifyPlaylistsRepresentationUpdated();
            onTaskComplete();
        }
    };

    private void showNotification(String header, int overallProgress, int maxProgress)
    {
        if(!mEnableNotifications) return;

        mBuilder = new NotificationCompat.Builder(mApp);
        mBuilder.setSmallIcon(R.drawable.ic_notification);
        mBuilder.setContentTitle(header);
        mBuilder.setTicker(header);
        mBuilder.setContentText("");
        mBuilder.setShowWhen(false);
        mBuilder.setProgress(maxProgress, overallProgress, maxProgress == 0 && overallProgress == 0);

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
