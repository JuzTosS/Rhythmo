package com.juztoss.bpmplayer.views;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

/**
 * Created by JuzTosS on 6/4/2016.
 */
public class PagerAdapter extends FragmentStatePagerAdapter implements ViewPager.OnPageChangeListener
{
    private final int mNumOfLists;
    private int mCurrentPosition = 0;

    public PagerAdapter(FragmentManager supportFragmentManager, int numOfLists)
    {
        super(supportFragmentManager);
        mNumOfLists = numOfLists;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
    {

    }

    @Override
    public void onPageScrollStateChanged(int state)
    {

    }

    @Override
    public void onPageSelected(int newPosition)
    {
        PlaylistFragment fragmentToShow = (PlaylistFragment)getItem(newPosition);
        fragmentToShow.onResumeFragment();

        PlaylistFragment fragmentToHide = (PlaylistFragment)getItem(mCurrentPosition);
        fragmentToHide.onPauseFragment();

        mCurrentPosition = newPosition;
    }

    @Override
    public Fragment getItem(int position)
    {
        return PlaylistFragment.newInstance(position);

    }

    @Override
    public int getCount()
    {
        return mNumOfLists;
    }


}