package com.juztoss.bpmplayer.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.juztoss.bpmplayer.R;
import com.juztoss.bpmplayer.presenters.BPMPlayerApp;

/**
 * Created by JuzTosS on 5/27/2016.
 */
public class BuildMusicLibraryService extends Service
{
    public static final String REBUILD = "Rebuild";
    private BPMPlayerApp mApp;
    private NotificationCompat.Builder mBuilder;
    private Notification mNotification;
    private NotificationManager mNotifyManager;
    public static final int NOTIFICATION_ID = 43;

    @Override
    public void onCreate()
    {
        mApp = (BPMPlayerApp) this.getApplicationContext();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
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
        if(intent.getExtras() != null)
            clear = intent.getExtras().getBoolean(REBUILD);

        AsyncBuildLibraryTask taskBuildLib = new AsyncBuildLibraryTask(mApp, clear);
        taskBuildLib.setOnBuildLibraryProgressUpdate(mOnBuildLibraryUpdate);
        taskBuildLib.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);

        AsyncDetectBpmByNamesTask taskDetectBpmByNames = new AsyncDetectBpmByNamesTask(mApp);
        taskDetectBpmByNames.setOnBuildLibraryProgressUpdate(mOnDetectBpmByNamesUpdate);
        taskDetectBpmByNames.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);

        AsyncDetectBpmByDataTask taskDetectBpmByData = new AsyncDetectBpmByDataTask(mApp);
        taskDetectBpmByData.setOnBuildLibraryProgressUpdate(mOnDetectBpmByDataUpdate);
        taskDetectBpmByData.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
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
            mApp.notifyDatabaseUpdated();
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
            mApp.notifyDatabaseUpdated();
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
            mApp.notifyDatabaseUpdated();

            Toast.makeText(mApp, R.string.building_music_library_finished, Toast.LENGTH_LONG).show();
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
        mNotification = mBuilder.build();

        mNotification.flags |= Notification.FLAG_INSISTENT | Notification.FLAG_NO_CLEAR;
        mNotifyManager.notify(NOTIFICATION_ID, mNotification);
    }
}
