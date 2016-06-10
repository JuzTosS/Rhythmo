package com.juztoss.bpmplayer.presenters;

/**
 * Created by JuzTosS on 6/10/2016.
 */
public interface ISongsDataSource
{
    void goToItem(int index);
    int getCount();
    long getId();
    void close();
}
