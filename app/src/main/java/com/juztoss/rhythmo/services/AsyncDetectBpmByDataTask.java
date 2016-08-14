package com.juztoss.rhythmo.services;

import com.juztoss.rhythmo.audio.BpmDetector;
import com.juztoss.rhythmo.presenters.RhythmoApp;

/**
 * Created by JuzTosS on 5/27/2016.
 */

public class AsyncDetectBpmByDataTask extends AsyncDetectBpmTaskAbstract<OnDetectBpmByDataUpdate>
{
    public AsyncDetectBpmByDataTask(RhythmoApp app)
    {
        super(app);
    }

    @Override
    double detectBpm(String fullPath, String name)
    {
        return BpmDetector.detect(fullPath);
    }
}
