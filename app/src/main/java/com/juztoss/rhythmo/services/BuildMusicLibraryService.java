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
import com.juztoss.rhythmo.views.activities.PlayerActivity;
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
    public static final String REBUILD = "Rebuild";
    private RhythmoApp mApp;
    private NotificationCompat.Builder mBuilder;
    private Notification mNotification;
    private NotificationManager mNotifyManager;
    public static final int NOTIFICATION_ID = 43;

    @Override
    public void onCreate()
    {
        mApp = (RhythmoApp) this.getApplicationContext();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Toast.makeText(this, getString(R.string.build_library_started), Toast.LENGTH_LONG).show();

        mBuilder = new NotificationCompat.Builder(mApp);
        mBuilder.setSmallIcon(R.drawable.ic_play_arrow_black_36dp);
        mBuilder.setContentTitle(getResources().getString(R.string.building_music_library));
        mBuilder.setTicker(getResources().getString(R.string.building_music_library));
        mBuilder.setContentText("");
        mBuilder.setProgress(0, 0, true);

        mNotifyManager = (NotificationManager) mApp.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotification = mBuilder.build();
        mNotification.flags |= Notification.FLAG_INSISTENT | Notification.FLAG_NO_CLEAR;
        mNotifyManager.notify(NOTIFICATION_ID, mNotification);


        boolean clear = false;
        if(intent != null && intent.getExtras() != null)
            clear = intent.getExtras().getBoolean(REBUILD);

        AsyncBuildLibraryTask taskBuildLib = new AsyncBuildLibraryTask(mApp, clear);
        taskBuildLib.setOnBuildLibraryProgressUpdate(mOnBuildLibraryUpdate);
        taskBuildLib.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);

        String key = getResources().getString(R.string.pref_recognize_bpm_from_name);
        boolean needToGetBPMByNames =  PreferenceManager.getDefaultSharedPreferences(this).getBoolean(key, true);

        if(needToGetBPMByNames)
        {
            AsyncDetectBpmByNamesTask taskDetectBpmByNames = new AsyncDetectBpmByNamesTask(mApp);
            taskDetectBpmByNames.setOnBuildLibraryProgressUpdate(mOnDetectBpmByNamesUpdate);
            taskDetectBpmByNames.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
        }

        AsyncDetectBpmByDataTask taskDetectBpmByData = new AsyncDetectBpmByDataTask(mApp);
        taskDetectBpmByData.setOnBuildLibraryProgressUpdate(mOnDetectBpmByDataUpdate);
        taskDetectBpmByData.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return new Binder();
    }

    private AsyncBuildLibraryTask.OnBuildLibraryProgressUpdate mOnBuildLibraryUpdate = new AsyncBuildLibraryTask.OnBuildLibraryProgressUpdate()
    {
        @Override
        public void onStartBuildingLibrary(AsyncBuildLibraryTask task)
        {
            mApp.setIsBuildingLibrary(true);
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
            String header = getResources().getString(R.string.detect_bpm_by_data);
            showNotification(header, overallProgress, maxProgress);
        }

        @Override
        public void onFinishBuildingLibrary(AsyncDetectBpmByDataTask task)
        {
            mNotifyManager.cancel(NOTIFICATION_ID);
            stopSelf();
            mApp.setIsBuildingLibrary(false);
            mApp.notifyPlaylistsRepresentationUpdated();

            Intent intent = new Intent(UPDATE_PROGRESS_ACTION);
            intent.putExtra(PROGRESS_ACTION_HEADER, "debug header");
            intent.putExtra(PROGRESS_ACTION_OVERALL_PROGRESS, 0);
            intent.putExtra(PROGRESS_ACTION_MAX_PROGRESS, 0);
            LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(mApp);
            broadcastManager.sendBroadcast(intent);

            Toast.makeText(mApp, R.string.build_library_finished, Toast.LENGTH_LONG).show();
        }
    };

    private void showNotification(String header, int overallProgress, int maxProgress)
    {
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
