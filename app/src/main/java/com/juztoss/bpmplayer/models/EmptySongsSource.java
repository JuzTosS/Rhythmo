package com.juztoss.bpmplayer.models;

import com.juztoss.bpmplayer.presenters.ISongsDataSource;

/**
 * Created by JuzTosS on 6/10/2016.
 */
public class EmptySongsSource implements ISongsDataSource
{
    @Override
    public void goToItem(int index)
    {
        //Do nothing
    }

    @Override
    public int getCount()
    {
        return 0;
    }

    @Override
    public long getId()
    {
        return -1;
    }
}
