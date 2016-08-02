package com.juztoss.bpmplayer.services;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.util.Log;

import com.juztoss.bpmplayer.audio.AdvancedMediaPlayer;
import com.juztoss.bpmplayer.models.DatabaseHelper;
import com.juztoss.bpmplayer.presenters.BPMPlayerApp;
import com.juztoss.bpmplayer.views.activities.PlayerActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JuzTosS on 6/26/2016.
 */
public abstract class AsyncDetectBpmTaskAbstract<T extends AsyncDetectBpmTaskAbstract.OnDetectBpmUpdate> extends AsyncTask<String, String, Void>
{
    protected final String UPDATE_COMPLETE = "UpdateComplete";
    protected final int MAX_PROGRESS_VALUE = 1000000;
    protected BPMPlayerApp mApp;

    public List<T> mBuildLibraryProgressUpdate;


    private PowerManager.WakeLock wakeLock;

    public interface OnDetectBpmUpdate<X extends AsyncDetectBpmTaskAbstract>
    {
        void onStartBuildingLibrary(X task);

        void onProgressUpdate(X task,
                              int overallProgress, int maxProgress,
                              boolean mediaStoreTransferDone);

        void onFinishBuildingLibrary(X task);

    }


    protected int mOverallProgress = 0;

    public AsyncDetectBpmTaskAbstract(BPMPlayerApp context)
    {
        mApp = context;
        mBuildLibraryProgressUpdate = new ArrayList<>();
    }

    @Override
    protected Void doInBackground(String... params)
    {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        detectSongsBpm();
        publishProgress(UPDATE_COMPLETE);
        return null;
    }

    abstract double detectBpm(String fullPath, String name);

    private void detectSongsBpm()
    {
        Cursor songsCursor = mApp.getDatabaseHelper().getReadableDatabase().query(DatabaseHelper.TABLE_MUSIC_LIBRARY,
                new String[]{DatabaseHelper._ID, DatabaseHelper.MUSIC_LIBRARY_PATH, DatabaseHelper.MUSIC_LIBRARY_NAME},
                DatabaseHelper.MUSIC_LIBRARY_BPM_SHIFTEDX10 + " <= ?",
                new String[]{"0"}, null, null, null);

        if (songsCursor.getCount() <= 0)
        {
            songsCursor.close();
            return;
        }

        try
        {
            int idIndex = songsCursor.getColumnIndex(DatabaseHelper._ID);
            int nameIndex = songsCursor.getColumnIndex(DatabaseHelper.MUSIC_LIBRARY_NAME);
            int pathIndex = songsCursor.getColumnIndex(DatabaseHelper.MUSIC_LIBRARY_PATH);
            final int subProgress = (MAX_PROGRESS_VALUE - mOverallProgress) / songsCursor.getCount();

            long lastUpdated = System.currentTimeMillis();
            for (int i = 0; i < songsCursor.getCount(); i++)
            {
                songsCursor.moveToPosition(i);
                String path = songsCursor.getString(pathIndex);
                String name = songsCursor.getString(nameIndex);
                String songId = songsCursor.getString(idIndex);
                String fullPath = path + "/" + name;
                double bpm = detectBpm(fullPath, name);
                if (bpm >= BPMPlayerApp.MAX_BPM)
                {
                    bpm = bpm / 2;
                    if (bpm >= BPMPlayerApp.MAX_BPM)
                    {
                        bpm = bpm / 2;
                        if (bpm >= BPMPlayerApp.MAX_BPM)
                        {
                            bpm = 0;//Too big value for real BPM, unset BPM for the song.
                        }
                    }
                }

                int bpmX10 = (int) (bpm * 10);
                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.MUSIC_LIBRARY_BPMX10, bpmX10);
                values.put(DatabaseHelper.MUSIC_LIBRARY_BPM_SHIFTEDX10, bpmX10);
                int rowsAffected = mApp.getDatabaseHelper().getWritableDatabase().update(DatabaseHelper.TABLE_MUSIC_LIBRARY, values, DatabaseHelper._ID + "= ?", new String[]{songId});

                Log.e("DEBUG affected=", rowsAffected + ", " + fullPath + " : " + bpmX10);
                mOverallProgress += subProgress;
                long now = System.currentTimeMillis();
                if (now - lastUpdated > 1000)
                {
                    lastUpdated = now;
                    publishProgress();
                }
            }
        }
        finally
        {
            songsCursor.close();
        }
    }

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();


        if (mBuildLibraryProgressUpdate != null)
            for (int i = 0; i < mBuildLibraryProgressUpdate.size(); i++)
                if (mBuildLibraryProgressUpdate.get(i) != null)
                    mBuildLibraryProgressUpdate.get(i).onStartBuildingLibrary(this);

        PowerManager pm = (PowerManager) mApp.getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                getClass().getName());
        wakeLock.acquire();

    }

    @Override
    protected void onProgressUpdate(String... progressParams)
    {
        super.onProgressUpdate(progressParams);

        if (progressParams.length > 0 && progressParams[0].equals(UPDATE_COMPLETE))
        {
            for (int i = 0; i < mBuildLibraryProgressUpdate.size(); i++)
                if (mBuildLibraryProgressUpdate.get(i) != null)
                    mBuildLibraryProgressUpdate.get(i).onProgressUpdate(this, mOverallProgress,
                            MAX_PROGRESS_VALUE, true);

            return;
        }

        if (mBuildLibraryProgressUpdate != null)
            for (int i = 0; i < mBuildLibraryProgressUpdate.size(); i++)
                if (mBuildLibraryProgressUpdate.get(i) != null)
                    mBuildLibraryProgressUpdate.get(i).onProgressUpdate(this, mOverallProgress, MAX_PROGRESS_VALUE, false);

    }

    @Override
    protected void onPostExecute(Void arg0)
    {
        wakeLock.release();

        if (mBuildLibraryProgressUpdate != null)
            for (int i = 0; i < mBuildLibraryProgressUpdate.size(); i++)
                if (mBuildLibraryProgressUpdate.get(i) != null)
                    mBuildLibraryProgressUpdate.get(i).onFinishBuildingLibrary(this);

    }

    /**
     * Setter methods.
     */
    public void setOnBuildLibraryProgressUpdate(T buildLibraryProgressUpdate)
    {
        if (buildLibraryProgressUpdate != null)
            mBuildLibraryProgressUpdate.add(buildLibraryProgressUpdate);
    }
}
