package com.juztoss.rhythmo.models;

import android.database.Cursor;
import android.support.annotation.Nullable;
import android.util.Log;

import com.juztoss.rhythmo.models.songsources.AbstractSongsSource;
import com.juztoss.rhythmo.models.songsources.SortType;
import com.juztoss.rhythmo.presenters.RhythmoApp;
import com.juztoss.rhythmo.utils.CursorList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by JuzTosS on 5/8/2016.
 */
public class Playlist implements AbstractSongsSource.AbstractSourceUpdatedListener
{
    protected RhythmoApp mApp;
    protected float mMinBPM = 0;
    protected float mMaxBPM = 0;
//    protected Cursor mList;
//    protected boolean mNeedRebuild = true;
    private List<IUpdateListener> mUpdateListeners;
    private AbstractSongsSource mSource;
    protected String mWordFilter;

    public Playlist(RhythmoApp app, AbstractSongsSource source)
    {
        mApp = app;
        setSource(source);
    }

    @Override
    public void onSourceUpdated()
    {
        setNeedRebuild();
    }

    public void setSource(AbstractSongsSource source)
    {
        if(mSource != null)
            mSource.setUpdateListener(null);

        mSource = source;
        mSource.setUpdateListener(this);
        setNeedRebuild();
    }

    public AbstractSongsSource getSource()
    {
        return mSource;
    }

//    protected void rebuild()
//    {
//        if (mList != null)
//            mList.close();
//
//        mList = mSource.getIds(mMinBPM, mMaxBPM, mWordFilter);
//
//        mNeedRebuild = false;
//    }

    public void setNeedRebuild()
    {
//        mNeedRebuild = true;
        notifyUpdateListeners();
    }

    public void setWordFilter(@Nullable String wordFilter)
    {
        mWordFilter = wordFilter;
        setNeedRebuild();
    }

    public static int findPositionById(final Cursor cursor, Composition needle, final SortType sort)
    {
        if(cursor == null || cursor.getCount() <= 0) return -1;
        CursorList cursorList = new CursorList(cursor);
        try
        {
            return Collections.binarySearch(cursorList, needle, new Comparator<Composition>()
            {
                @Override
                public int compare(Composition left, Composition right)
                {
                    if (left == null || right == null) return 0;

                    int result;
                    if (sort == SortType.NAME)
                    {
                        return left.name().compareToIgnoreCase(right.name());
                    }
                    else if (sort == SortType.BPM)
                    {
                        Integer leftInt = (Integer) (int) (left.bpmShifted() * 10);
                        Integer rightInt = (Integer) (int) (right.bpmShifted() * 10);
                        result = leftInt.compareTo(rightInt);
                        if (result != 0)//Return only if the songs in different folder
                            return result;
                    }
                    else if (sort == SortType.DIRECTORY)
                    {
                        result = left.getFolderPath().compareToIgnoreCase(right.getFolderPath());
                        if (result != 0)//Return only if the songs in different folder
                            return result;
                    }
                    else// if(sort == SortType.LAST)
                    {
                        result = ((Integer) right.getDateAdded()).compareTo(left.getDateAdded());
                        if (result != 0)//Return only if the songs in different folder
                            return result;
                    }

                    //If both songs in the same folder compare by name;
                    result = left.name().compareToIgnoreCase(right.name());
                    return result;
                }
            });

        }
        catch (Exception e)
        {
            Log.e(Playlist.class.toString(), "Unable to find element");
            e.printStackTrace();
            return -1;
        }
    }

    @Nullable
    public Cursor getCursor()
    {
//        if (mNeedRebuild || mList == null || mList.isClosed())
//            rebuild();

        return mSource.getIds(mMinBPM, mMaxBPM, mWordFilter);
    }

    public String getName()
    {
        return mSource.getName();
    }

    public void setBPMFilter(float minBPM, float maxBPM)
    {
        mMinBPM = minBPM;
        mMaxBPM = maxBPM;
        setNeedRebuild();
    }

    protected void notifyUpdateListeners()
    {
        if (mUpdateListeners == null) return;
        for (IUpdateListener listener : mUpdateListeners)
        {
            listener.onPlaylistUpdated();
        }
    }

    public void addUpdateListener(IUpdateListener listener)
    {
        if (mUpdateListeners == null)
            mUpdateListeners = new ArrayList<>();

        if (!mUpdateListeners.contains(listener))
            mUpdateListeners.add(listener);
    }

    public void removeUpdateListener(IUpdateListener listener)
    {
        if (mUpdateListeners != null && mUpdateListeners.contains(listener))
            mUpdateListeners.remove(listener);
    }

    public void clearListeners()
    {
        if(mUpdateListeners == null) return;
        mUpdateListeners.clear();
    }

    public interface IUpdateListener
    {
        void onPlaylistUpdated();
    }
}

