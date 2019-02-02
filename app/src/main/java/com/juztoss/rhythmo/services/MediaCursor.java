package com.juztoss.rhythmo.services;

import android.database.Cursor;
import android.database.CursorWrapper;
import android.provider.MediaStore;

/**
 * Created by JuzTosS on 1/27/2017.
 */

public class MediaCursor extends CursorWrapper
{

    final int mFilePathColIndex;
    final int mDateAddedColIndex;
    final int mDurationColIndex;

    public MediaCursor(Cursor cursor)
    {
        super(cursor);
        mFilePathColIndex = getColumnIndex(MediaStore.Audio.Media.DATA);
        mDateAddedColIndex = getColumnIndex(MediaStore.Audio.Media.DATE_ADDED);
        mDurationColIndex = getColumnIndex(MediaStore.Audio.Media.DURATION);
    }

    public String fullName()
    {
        return getString(mFilePathColIndex);
    }

    public int dateAdded()
    {
        return getInt(mDateAddedColIndex);
    }

    public int duration() {
        return getInt(mDurationColIndex);
    }
}
