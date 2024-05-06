package com.juztoss.rhythmo.views.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.juztoss.rhythmo.R;
import com.juztoss.rhythmo.services.PlaybackService;
import com.juztoss.rhythmo.utils.SystemHelper;

/**
 * Created by JuzTosS on 7/26/2016.
 */
public class BasePlayerActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener
{

    private PlaybackService mPlaybackService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        SystemHelper.updateTheme(this);
        super.onCreate(savedInstanceState);

        Intent serviceIntent = new Intent(this, PlaybackService.class);
        bindService(serviceIntent, mServiceConnection, BIND_AUTO_CREATE);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        if(key.equals(getResources().getString(R.string.pref_theme)))
            recreate();
    }

    @Override
    protected void onDestroy()
    {
        unbindService(mServiceConnection);
        mPlaybackService = null;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    private ServiceConnection mServiceConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            mPlaybackService = ((PlaybackService.PlaybackServiceBinder) service).getService();
            BasePlayerActivity.this.onServiceConnected();
        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
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
