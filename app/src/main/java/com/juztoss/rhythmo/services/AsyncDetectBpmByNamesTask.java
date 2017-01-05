package com.juztoss.rhythmo.services;

import com.juztoss.rhythmo.audio.BpmDetector;
import com.juztoss.rhythmo.presenters.RhythmoApp;

/**
 * Created by JuzTosS on 5/27/2016.
 */

public class AsyncDetectBpmByNamesTask extends AsyncDetectBpmTaskAbstract <OnDetectBpmByNamesUpdate>
{
    public AsyncDetectBpmByNamesTask(RhythmoApp app, int playlistIndex, boolean resetBpm)
    {
        super(app, playlistIndex, resetBpm);
    }

    @Override
    double detectBpm(double oldBpm, String fullPath, String name)
    {
        double newBpm = BpmDetector.detectFromName(name);
        if(newBpm <= 0 && oldBpm > 0)
            return oldBpm;
        else
            return newBpm;
    }
}
