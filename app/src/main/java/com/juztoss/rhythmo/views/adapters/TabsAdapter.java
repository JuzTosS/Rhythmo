package com.juztoss.rhythmo.views.adapters;

import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

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