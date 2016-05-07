package juztoss.com.bpmplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import juztoss.com.bpmplayer.presenters.BPMPlayerApp;
import juztoss.com.bpmplayer.views.MainActivity;

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
            Intent activityIntent = new Intent(context, MainActivity.class);
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
