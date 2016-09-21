package com.juztoss.rhythmo.views.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.juztoss.rhythmo.services.PlaybackService;

/**
 * Created by JuzTosS on 7/26/2016.
 */
public class BasePlayerActivity extends AppCompatActivity
{

    private PlaybackService mPlaybackService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        Log.d(getClass().toString(), "onCreate()");
        super.onCreate(savedInstanceState);

        Intent serviceIntent = new Intent(this, PlaybackService.class);
        bindService(serviceIntent, mServiceConnection, BIND_AUTO_CREATE);
//        startService(serviceIntent);
    }

    @Override
    protected void onDestroy()
    {
        Log.d(getClass().toString(), "onDestroy()");
        unbindService(mServiceConnection);
        mPlaybackService = null;
        super.onDestroy();
    }

    private ServiceConnection mServiceConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            Log.d(BasePlayerActivity.this.getClass().toString(), "onServiceConnected");
            mPlaybackService = ((PlaybackService.PlaybackServiceBinder) service).getService();
            BasePlayerActivity.this.onServiceConnected();
        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            Log.d(BasePlayerActivity.this.getClass().toString(), "onServiceDisconnected");
            mPlaybackService = null;
            BasePlayerActivity.this.onServiceDisconnected();
        }
    };

    protected void onServiceConnected()
    {

    }

    protected void onServiceDisconnected()
    {

    }

    public PlaybackService playbackService()
    {
        return mPlaybackService;
    }
}
