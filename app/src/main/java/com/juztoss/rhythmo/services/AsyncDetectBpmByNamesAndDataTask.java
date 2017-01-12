package com.juztoss.rhythmo.services;

import com.juztoss.rhythmo.audio.BpmDetector;
import com.juztoss.rhythmo.presenters.RhythmoApp;

/**
 * Created by JuzTosS on 5/27/2016.
 */

public class AsyncDetectBpmByNamesAndDataTask extends AsyncDetectBpmTaskAbstract
{
    private final boolean mNeedToGetBPMByNames;

    public AsyncDetectBpmByNamesAndDataTask(RhythmoApp app, int playlistIndex, boolean resetBpm, boolean needToGetBPMByNames)
    {
        super(app, playlistIndex, resetBpm);
        mNeedToGetBPMByNames = needToGetBPMByNames;
    }

    @Override
    public double detectBpm(double oldBpm, String fullPath, String name)
    {
        double newBpm = mNeedToGetBPMByNames ? BpmDetector.detectFromName(name) : 0;
        if(newBpm <= 0)
        {
            newBpm = BpmDetector.detectFromData(fullPath);
            if(newBpm <= 0 && oldBpm > 0)
                return oldBpm;
            else
                return newBpm;
        }
        else
            return newBpm;
    }
}
