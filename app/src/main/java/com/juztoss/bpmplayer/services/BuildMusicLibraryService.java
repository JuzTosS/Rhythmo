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

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by JuzTosS on 5/27/2016.
 */
public class BuildMusicLibraryService extends Service implements AsyncBuildLibraryTask.OnBuildLibraryProgressUpdate
{
    private Context mContext;
    private NotificationCompat.Builder mBuilder;
    private Notification mNotification;
    private NotificationManager mNotifyManager;
    public static final int NOTIFICATION_ID = 43;

    @Override
    public void onCreate()
    {
        mContext = this.getApplicationContext();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        mBuilder = new NotificationCompat.Builder(mContext);
        mBuilder.setSmallIcon(R.drawable.ic_play_arrow_black_36dp);
        mBuilder.setContentTitle(getResources().getString(R.string.building_music_library));
        mBuilder.setTicker(getResources().getString(R.string.building_music_library));
        mBuilder.setContentText("");
        mBuilder.setProgress(0, 0, true);

        mNotifyManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotification = mBuilder.build();
        mNotification.flags |= Notification.FLAG_INSISTENT | Notification.FLAG_NO_CLEAR;
        mNotifyManager.notify(NOTIFICATION_ID, mNotification);

        AsyncBuildLibraryTask task = new AsyncBuildLibraryTask(mContext);
        task.setOnBuildLibraryProgressUpdate(this);
        task.execute();

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onStartBuildingLibrary()
    {

    }

    @Override
    public void onProgressUpdate(AsyncBuildLibraryTask task, int overallProgress, int maxProgress, boolean mediaStoreTransferDone)
    {
        mBuilder = new NotificationCompat.Builder(mContext);
        mBuilder.setSmallIcon(R.drawable.ic_play_arrow_black_36dp);
        String header = getResources().getString(R.string.building_music_library);
        mBuilder.setContentTitle(header);
        mBuilder.setTicker(header);
        mBuilder.setContentText("");
        mBuilder.setProgress(maxProgress, overallProgress, false);
        mNotification = mBuilder.build();

        mNotification.flags |= Notification.FLAG_INSISTENT | Notification.FLAG_NO_CLEAR;
        mNotifyManager.notify(NOTIFICATION_ID, mNotification);
    }

    @Override
    public void onFinishBuildingLibrary(AsyncBuildLibraryTask task)
    {
        mNotifyManager.cancel(NOTIFICATION_ID);
        stopSelf();

        Toast.makeText(mContext, R.string.building_music_library_finished, Toast.LENGTH_LONG).show();
    }
}
