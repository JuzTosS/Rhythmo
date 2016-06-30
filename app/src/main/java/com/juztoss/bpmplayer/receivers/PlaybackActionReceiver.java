package com.juztoss.bpmplayer.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.juztoss.bpmplayer.presenters.BPMPlayerApp;
import com.juztoss.bpmplayer.services.PlaybackService;
import com.juztoss.bpmplayer.views.activities.PlayerActivity;

/**
 * Created by JuzTosS on 5/3/2016.
 */
public class PlaybackActionReceiver extends BroadcastReceiver
{
    @Override

    public void onReceive(Context context, Intent intent)
    {
        if (intent.getAction().equals(PlaybackService.LAUNCH_NOW_PLAYING_ACTION))
        {
            Intent activityIntent = new Intent(context, PlayerActivity.class);
            activityIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(activityIntent);
        }
        else if (intent.getAction().equals(PlaybackService.SWITCH_PLAYBACK_ACTION))
        {
            BPMPlayerApp app = (BPMPlayerApp) context.getApplicationContext();
            if (app.isPlaybackServiceRunning())
                app.getPlaybackService().togglePlaybackState();
        }
    }
}
