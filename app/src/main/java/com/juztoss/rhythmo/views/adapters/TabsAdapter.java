package com.juztoss.rhythmo.views.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.ViewGroup;

import com.juztoss.rhythmo.views.fragments.HierarchyPlaylistFragment;
import com.juztoss.rhythmo.views.fragments.IPlaylistFragment;
import com.juztoss.rhythmo.views.fragments.PlaylistFragment;

/**
 * Created by JuzTosS on 6/4/2016.
 */
public class TabsAdapter extends FragmentStatePagerAdapter implements ViewPager.OnPageChangeListener
{
    private boolean mIsBrowserMode;
    private int mNumOfLists;
    private int mCurrentPosition = 0;
    private ViewGroup mContainer;

    @Override
    public void startUpdate(ViewGroup container)
    {
        super.startUpdate(container);
        mContainer = container;
    }

    public TabsAdapter(FragmentManager supportFragmentManager, int numOfLists)
    {
        super(supportFragmentManager);
        setNumOfLists(numOfLists);
    }

    public void setNumOfLists(int numOfLists)
    {
        mNumOfLists = numOfLists;
        notifyDataSetChanged();
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

        IPlaylistFragment fragmentToShow = (IPlaylistFragment) instantiateItem(mContainer, newPosition);
        fragmentToShow.onScreen();

        IPlaylistFragment fragmentToHide = (IPlaylistFragment) instantiateItem(mContainer, mCurrentPosition);
        fragmentToHide.offScreen();

        mCurrentPosition = newPosition;
    }

    @Override
    public Fragment getItem(int position)
    {
        if (position == 0 && mIsBrowserMode)
            return HierarchyPlaylistFragment.newInstance(0);
        else
            return PlaylistFragment.newInstance(position);
    }

    @Override
    public int getCount()
    {
        return mNumOfLists;
    }

    public IPlaylistFragment getFragmentAt(int position)
    {
        return (IPlaylistFragment) instantiateItem(mContainer, position);
    }

    public IPlaylistFragment getCurrentFragment()
    {
        return (IPlaylistFragment) instantiateItem(mContainer, mCurrentPosition);
    }

    public void setIsBrowserMode(boolean browserMode) {
        mIsBrowserMode = browserMode;
    }
}