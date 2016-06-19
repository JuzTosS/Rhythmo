package com.juztoss.bpmplayer.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by JuzTosS on 6/18/2016.
 */
public class ScrollingFABBehavior extends CoordinatorLayout.Behavior<FloatingActionButton>
{
    private int mToolbarHeight;

    public ScrollingFABBehavior(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        final TypedArray styledAttributes = context.getTheme().obtainStyledAttributes(
                new int[]{android.R.attr.actionBarSize});
        mToolbarHeight = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, FloatingActionButton fab, View dependency)
    {
        return dependency instanceof AppBarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, FloatingActionButton fab, View dependency)
    {
        if (dependency instanceof AppBarLayout)
        {
            float ratio = - dependency.getY() / (float) mToolbarHeight;
            if(ratio >= 1)
                ratio = 1;
            if(ratio <= 0)
                ratio = 0;
            fab.setAlpha(ratio);
            fab.setScaleX(ratio);
            fab.setScaleY(ratio);
        }
        return true;
    }



}