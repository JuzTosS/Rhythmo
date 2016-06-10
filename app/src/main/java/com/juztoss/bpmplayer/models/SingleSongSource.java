package com.juztoss.bpmplayer.models;

import com.juztoss.bpmplayer.presenters.ISongsDataSource;

/**
 * Created by JuzTosS on 6/10/2016.
 */
public class SingleSongSource implements ISongsDataSource
{
    public SingleSongSource(String absolutePath)
    {
    }

    @Override
    public void goToItem(int index)
    {

    }

    @Override
    public int getCount()
    {
        return 0;
    }

    @Override
    public long getId()
    {
        return 0;
    }
}
