package juztoss.com.bpmplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by JuzTosS on 5/3/2016.
 */
public class PlaybackActionReceiver extends BroadcastReceiver {
    @Override

    public void onReceive(Context context, Intent intent) {
//        BPMPlayerApp mApp = (BPMPlayerApp) context.getApplicationContext();
        int action = intent.getExtras().getInt("action");
        Log.i(getClass().toString(), "onReceive, action: " + action);
//        if (mApp.isServiceRunning())
//            mApp.getPlaybackService().
    }
}
