package com.juztoss.rhythmo.models;

import android.database.Cursor;

import com.juztoss.rhythmo.models.songsources.AbstractSongsSource;
import com.juztoss.rhythmo.utils.SystemHelper;

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

    public static Composition fromCursor(Cursor cursor)
    {
        long id = cursor.getLong(AbstractSongsSource.I_ID);
        if(id >= 0)
        {
            return new Composition(cursor);
        }
        else
        {
            return null;
        }
    }

    private Composition(Cursor cursor)
    {
        mId = cursor.getLong(AbstractSongsSource.I_ID);
        mFolderName = cursor.getString(AbstractSongsSource.I_FOLDER);
        mSongName = cursor.getString(AbstractSongsSource.I_NAME);
        mAbsolutePath = mFolderName + SystemHelper.SEPARATOR + mSongName;
        mBPM = cursor.getInt(AbstractSongsSource.I_BPM) / 10f;
        mShiftedBPM = cursor.getInt(AbstractSongsSource.I_BPM_SHIFT) / 10f;
    }
    public Composition(long songId, String folderName, String songName, float bpm, float bpmShifted)
    {
        mId = songId;
        mFolderName = folderName;
        mSongName = songName;
        mAbsolutePath = folderName + SystemHelper.SEPARATOR + songName;
        mShiftedBPM = bpmShifted;
        mBPM = bpm;
    }

    public String getAbsolutePath()
    {
        return mAbsolutePath;
    }
    public String getFolderPath()
    {
        return mFolderName;
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

    public String getFolder()
    {
        return SystemHelper.getLastSegmentOfPath(mFolderName);
    }
}
