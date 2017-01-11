package com.juztoss.rhythmo.services;

import android.content.ContentValues;
import android.os.AsyncTask;

import com.juztoss.rhythmo.models.DatabaseHelper;
import com.juztoss.rhythmo.presenters.RhythmoApp;

import java.util.ArrayList;

/**
 * Created by JuzTosS on 5/27/2016.
 */

public class AsyncClearLibraryTask extends AsyncTask<Void, Void, Void>
{
    private ArrayList<Listener> mListener;
    private RhythmoApp mApp;

    public AsyncClearLibraryTask(RhythmoApp app)
    {
        mApp = app;
        mListener = new ArrayList<>();
    }

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... params)
    {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.MUSIC_LIBRARY_BPMX10, 0);
        values.put(DatabaseHelper.MUSIC_LIBRARY_BPM_SHIFTEDX10, 0);
        mApp.getDatabaseHelper().getWritableDatabase().update(DatabaseHelper.TABLE_MUSIC_LIBRARY, values, null, null);
        return null;
    }

    @Override
    protected void onPostExecute(Void arg0)
    {
        if (mListener != null)
            for (int i = 0; i < mListener.size(); i++)
                if (mListener.get(i) != null)
                    mListener.get(i).onFinish();

    }

    public void setListener(Listener buildLibraryProgressUpdate)
    {
        if (buildLibraryProgressUpdate != null)
            mListener.add(buildLibraryProgressUpdate);
    }

    public interface Listener
    {
        void onFinish();
    }
}
