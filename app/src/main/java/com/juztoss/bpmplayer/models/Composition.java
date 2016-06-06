package com.juztoss.bpmplayer.models;

/**
 * Created by JuzTosS on 5/30/2016.
 */
public class Composition
{

    private long mId;
    private String mFolderName;
    private String mSongName;
    private String mAbsolutePath;
    private float mBPM;

    public Composition(long songId, String folderName, String songName, float bpm)
    {
        mId = songId;
        mFolderName = folderName;
        mSongName = songName;
        mAbsolutePath = folderName + "/" + songName;
        mBPM = bpm;
    }

    public String getAbsolutePath()
    {
        return mAbsolutePath;
    }

    public String name()
    {
        return mSongName;
    }

    public float bpm()
    {
        return mBPM;
    }
}
