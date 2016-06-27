package com.juztoss.bpmplayer.services;

import com.juztoss.bpmplayer.audio.BpmDetector;
import com.juztoss.bpmplayer.presenters.BPMPlayerApp;

/**
 * Created by JuzTosS on 5/27/2016.
 */

public class AsyncDetectBpmByDataTask extends AsyncDetectBpmTaskAbstract<OnDetectBpmByDataUpdate>
{
    public AsyncDetectBpmByDataTask(BPMPlayerApp app)
    {
        super(app);
    }

    @Override
    double detectBpm(String fullPath, String name)
    {
        return BpmDetector.detect(fullPath);
    }
}
