package com.juztoss.bpmplayer.models;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by JuzTosS on 5/8/2016.
 */
public class Playlist
{
    private OnChangedListener mListener;

    private int mMinBPM = 0;
    private int mMaxBPM = Integer.MAX_VALUE;

    private List<Composition> mOriginalList = new LinkedList<>();
    private List<Composition> mCurrentList = new LinkedList<>();

    public void setOnChangedListener(OnChangedListener listener)
    {
        mListener = listener;
    }

    public void add(List<Composition> songs)
    {
        mOriginalList.addAll(songs);
        applyFilter();

        if(mListener != null)
            mListener.onListChanged();
    }

    public List<Composition> songs()
    {
        return mCurrentList;
    }

    public void clear()
    {
        mOriginalList.clear();
        mOriginalList.clear();
    }

    public void setRange(int minBPM, int maxBPM)
    {
        mMinBPM = minBPM;
        mMaxBPM = maxBPM;
        applyFilter();

        if(mListener != null)
            mListener.onListChanged();
    }

    private void applyFilter()
    {
        mCurrentList.clear();
        for(Composition composition : mOriginalList)
        {
            if(composition.bpm() > mMinBPM && composition.bpm() < mMaxBPM)
            {
                mCurrentList.add(composition);
            }
        }
    }

    public interface OnChangedListener
    {
        void onListChanged();
    }
}
