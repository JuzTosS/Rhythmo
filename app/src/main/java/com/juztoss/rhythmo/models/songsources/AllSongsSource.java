package com.juztoss.rhythmo.models.songsources;

import android.database.Cursor;
import android.database.DatabaseUtils;
import android.support.annotation.Nullable;

import com.juztoss.rhythmo.R;
import com.juztoss.rhythmo.models.BaseExplorerElement;
import com.juztoss.rhythmo.models.DatabaseHelper;
import com.juztoss.rhythmo.presenters.RhythmoApp;

import static com.juztoss.rhythmo.presenters.RhythmoApp.BROWSER_MODE_IN_PLAYLIST_ENABLED;

/**
 * Created by JuzTosS on 6/22/2016.
 */
public class AllSongsSource extends AbstractSongsSource
{
    private SortType mSortType;
    private RhythmoApp mApp;

    AllSongsSource(RhythmoApp app)
    {
        mApp = app;
        mSortType = SortType.values()[mApp.getSharedPreferences().getInt(RhythmoApp.ALL_SONGS_SORT, 0)];
    }

    private String getSelectedFolder() {
        boolean isBrowserMode = mApp.getSharedPreferences().getBoolean(BROWSER_MODE_IN_PLAYLIST_ENABLED, false);
        if (!isBrowserMode) return "";

        BaseExplorerElement current = mApp.getBrowserPresenter().getCurrent();
        if (current == null) return "";

        return current.getFileSystemPath();
    }

    @Override
    public Cursor getIds(float minBPM, float maxBPM, String wordFilter)
    {
        int mMinBPMX10 = (int)(minBPM * 10);
        int mMaxBPMX10 = (int)(maxBPM * 10);
        Cursor mList;

        String selectedFolder = getSelectedFolder();
        String folderFilter = "";
        if(!selectedFolder.isEmpty())
        {
            folderFilter = DatabaseHelper.MUSIC_LIBRARY_PATH + " = " +
                    DatabaseUtils.sqlEscapeString(selectedFolder);
        }

        if(wordFilter == null)
            wordFilter = "";
        else
            wordFilter = DatabaseHelper.MUSIC_LIBRARY_NAME + " LIKE " + DatabaseUtils.sqlEscapeString("%" + wordFilter + "%");

        String filter = "";

        if(!wordFilter.isEmpty()) {
            filter += wordFilter;

            if(!folderFilter.isEmpty())
                filter += " AND " + folderFilter;
        }
        else
            filter += folderFilter;


        String order;
        if(mSortType == SortType.NAME)
            order = DatabaseHelper.MUSIC_LIBRARY_NAME;
        else if(mSortType == SortType.BPM)
            order = DatabaseHelper.MUSIC_LIBRARY_BPM_SHIFTEDX10;
        else if(mSortType == SortType.LAST)
            order = DatabaseHelper.MUSIC_LIBRARY_DATE_ADDED + " DESC";
        else//mSortType = SortType.DIRECTORY
            order = DatabaseHelper.MUSIC_LIBRARY_PATH;

        int add = mApp.getBPMFilterAdditionWindowSize();
        if (mMinBPMX10 > 0 && mMaxBPMX10 > 0)//BPM Filter is enabled
        {
            mList = mApp.getDatabaseHelper().getWritableDatabase().query(DatabaseHelper.TABLE_MUSIC_LIBRARY,
                    new String[]{DatabaseHelper._ID, DatabaseHelper.MUSIC_LIBRARY_PATH, DatabaseHelper.MUSIC_LIBRARY_NAME, DatabaseHelper.MUSIC_LIBRARY_BPMX10, DatabaseHelper.MUSIC_LIBRARY_BPM_SHIFTEDX10, DatabaseHelper.MUSIC_LIBRARY_DATE_ADDED},
                    DatabaseHelper.MUSIC_LIBRARY_BPM_SHIFTEDX10 + " >= ?" + " AND " + DatabaseHelper.MUSIC_LIBRARY_BPM_SHIFTEDX10 + " <= ?"
                            + (filter.isEmpty() ? "" : (" AND " + filter))
                    , new String[]{Integer.toString(mMinBPMX10 - add * 10), Integer.toString(mMaxBPMX10 + add * 10)},
                    null, null,
                    order + ", " + DatabaseHelper.MUSIC_LIBRARY_NAME + ", " + DatabaseHelper._ID);
        }
        else
        {
            mList = mApp.getDatabaseHelper().getWritableDatabase().query(DatabaseHelper.TABLE_MUSIC_LIBRARY,
                    new String[]{DatabaseHelper._ID, DatabaseHelper.MUSIC_LIBRARY_PATH, DatabaseHelper.MUSIC_LIBRARY_NAME, DatabaseHelper.MUSIC_LIBRARY_BPMX10, DatabaseHelper.MUSIC_LIBRARY_BPM_SHIFTEDX10, DatabaseHelper.MUSIC_LIBRARY_DATE_ADDED},
                            filter
                    , null,
                    null, null,
                    order + ", " + DatabaseHelper.MUSIC_LIBRARY_NAME + ", " + DatabaseHelper._ID);
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
        mApp.getSharedPreferences().edit().putInt(RhythmoApp.ALL_SONGS_SORT, sortType.ordinal()).apply();
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
