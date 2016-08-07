package com.juztoss.bpmplayer.models.songsources;

import android.database.Cursor;
import android.database.DatabaseUtils;
import android.support.annotation.Nullable;

import com.juztoss.bpmplayer.R;
import com.juztoss.bpmplayer.models.DatabaseHelper;
import com.juztoss.bpmplayer.presenters.BPMPlayerApp;

/**
 * Created by JuzTosS on 6/22/2016.
 */
public class AllSongsSource extends ISongsSource
{
    private SortType mSortType;
    private BPMPlayerApp mApp;

    AllSongsSource(BPMPlayerApp app)
    {
        mApp = app;
        mSortType = SortType.values()[mApp.getSharedPreferences().getInt(BPMPlayerApp.ALL_SONGS_SORT, 0)];
    }

    @Override
    public Cursor getIds(float minBPM, float maxBPM)
    {
        int mMinBPMX10 = (int)(minBPM * 10);
        int mMaxBPMX10 = (int)(maxBPM * 10);
        Cursor mList;

        String order;
        if(mSortType == SortType.NAME)
            order = DatabaseHelper.MUSIC_LIBRARY_NAME;
        else if(mSortType == SortType.BPM)
            order = DatabaseHelper.MUSIC_LIBRARY_BPM_SHIFTEDX10;
        else//mSortType = SortType.DIRECTORY
            order = DatabaseHelper.MUSIC_LIBRARY_PATH;

        int add = mApp.getBPMFilterAdditionWindowSize();
        if (mMinBPMX10 > 0 && mMaxBPMX10 > 0)//BPM Filter is enabled
        {
            mList = mApp.getDatabaseHelper().getWritableDatabase().query(DatabaseHelper.TABLE_MUSIC_LIBRARY,
                    new String[]{DatabaseHelper._ID},
                    DatabaseHelper.MUSIC_LIBRARY_BPM_SHIFTEDX10 + " >= ?" + " AND " + DatabaseHelper.MUSIC_LIBRARY_BPM_SHIFTEDX10 + " <= ?"
                        + ((mWordFilter == null) ? "" : " AND " + DatabaseHelper.MUSIC_LIBRARY_NAME + " LIKE " + DatabaseUtils.sqlEscapeString("%" + mWordFilter + "%"))
                    , new String[]{Integer.toString(mMinBPMX10 - add * 10), Integer.toString(mMaxBPMX10 + add * 10)},
                    null, null,
                    order);
        }
        else
        {
            mList = mApp.getDatabaseHelper().getWritableDatabase().query(DatabaseHelper.TABLE_MUSIC_LIBRARY,
                    new String[]{DatabaseHelper._ID},
                    (mWordFilter == null) ? null : DatabaseHelper.MUSIC_LIBRARY_NAME + " LIKE " + DatabaseUtils.sqlEscapeString("%" + mWordFilter + "%")
                    , null,
                    null, null,
                    order);
        }
        return mList;
    }

    @Override
    public String getName()
    {
        return mApp.getString(R.string.main_playlist_name);
    }

    @Override
    public void rename(String newName)
    {

    }

    @Override
    public boolean isRenameAvailable()
    {
        return false;
    }

    @Override
    public boolean isDeleteAvailable()
    {
        return false;
    }

    @Override
    public boolean isModifyAvailable()
    {
        return false;
    }

    @Override
    public void add(Cursor songIdsCursor)
    {

    }

    @Override
    public void remove(long songId)
    {

    }

    @Override
    public void setSortType(SortType sortType)
    {
        mSortType = sortType;
        mApp.getSharedPreferences().edit().putInt(BPMPlayerApp.ALL_SONGS_SORT, sortType.ordinal()).apply();
        notifyUpdated();
    }

    @Override
    public SortType getSortType()
    {
        return mSortType;
    }

    @Override
    public void delete()
    {

    }
}
