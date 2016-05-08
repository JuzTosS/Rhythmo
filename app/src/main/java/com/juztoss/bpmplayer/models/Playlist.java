package com.juztoss.bpmplayer.models;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by JuzTosS on 5/8/2016.
 */
public class Playlist
{
    private List<Song> mSongs = new LinkedList<>();
    public void add(List<Song> songs)
    {
        mSongs.addAll(songs);
    }

    public List<Song> songs()
    {
        return mSongs;
    }


    public void clear()
    {
        mSongs.clear();
    }
}
