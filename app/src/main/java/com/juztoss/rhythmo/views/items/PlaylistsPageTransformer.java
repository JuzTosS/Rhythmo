package com.juztoss.rhythmo.views.items;

import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * Created by JuzTosS on 1/22/2017.
 */

public class PlaylistsPageTransformer implements ViewPager.PageTransformer
{
    /**
     * Apply a property transformation to the given page.
     *
     * @param page     Apply the transformation to this page
     * @param position Position of page relative to the current front-and-center
     *                 position of the pager. 0 is front and center. 1 is one full
     *                 page position to the right, and -1 is one page position to the left.
     */
    public void transformPage(View page, float position)
    {
        ((PlaylistAnimatedLayout) page).animateBackground(position);
    }
}