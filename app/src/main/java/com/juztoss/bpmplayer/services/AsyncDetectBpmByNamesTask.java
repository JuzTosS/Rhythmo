package com.juztoss.bpmplayer.services;

import com.juztoss.bpmplayer.audio.BpmDetector;
import com.juztoss.bpmplayer.presenters.BPMPlayerApp;

/**
 * Created by JuzTosS on 5/27/2016.
 */

public class AsyncDetectBpmByNamesTask extends AsyncDetectBpmTaskAbstract <OnDetectBpmByNamesUpdate>
{
    public AsyncDetectBpmByNamesTask(BPMPlayerApp app)
    {
        super(app);
    }

    @Override
    double detectBpm(String fullPath, String name)
    {
        return BpmDetector.detectFromName(name);
    }
}
