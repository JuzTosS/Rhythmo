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
    private float mShiftedBPM;

    public Composition(long songId, String folderName, String songName, float bpm, float bpmShifted)
    {
        mId = songId;
        mFolderName = folderName;
        mSongName = songName;
        mAbsolutePath = folderName + "/" + songName;
        mShiftedBPM = bpmShifted;
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

    public long id()
    {
        return mId;
    }

    public void setBPM(float BPM)
    {
        mBPM = BPM;
    }

    public float bpmShifted()
    {
        return mShiftedBPM;
    }

    public void setShiftedBPM(float shiftedBPM)
    {
        mShiftedBPM = shiftedBPM;
    }
}
