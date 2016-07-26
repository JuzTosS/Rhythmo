package com.juztoss.bpmplayer.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.juztoss.bpmplayer.services.PlaybackService;

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
            Intent i = new Intent(context, PlaybackService.class);
            i.setAction(PlaybackService.ACTION_COMMAND);
            i.putExtra(PlaybackService.ACTION_NAME, PlaybackService.PAUSE_PLAYBACK_ACTION);
            context.startService(i);
        }
    }
}
