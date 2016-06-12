package com.juztoss.bpmplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.juztoss.bpmplayer.presenters.BPMPlayerApp;

/**
 * Created by JuzTosS on 6/12/2016.
 */
public class NoisyIntentReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (intent.getAction().equals(android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY))
        {
            BPMPlayerApp app = (BPMPlayerApp) context.getApplicationContext();
            if (app.isPlaybackServiceRunning())
                app.getPlaybackService().pausePlayback();
        }
    }
}
