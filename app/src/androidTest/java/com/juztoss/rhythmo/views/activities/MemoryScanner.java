package com.juztoss.rhythmo.views.activities;

/**
 * Created by JuzTosS on 1/10/2017.
 */

public class MemoryScanner
{
    public void startScan()
    {

    }

    public void cancel()
    {

    }

    public void setListener(MemoryScannerListener listener)
    {

    }

    public interface MemoryScannerListener
    {
        void onProgress(int progress, int maxValue);
        void onComplete();
    }
}
