package com.juztoss.bpmplayer.views;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;

/**
 * Created by JuzTosS on 6/30/2016.
 */
public class AdvancedFloatingActionButton extends FloatingActionButton
{
    private boolean mIsAlwaysShown;

    public AdvancedFloatingActionButton(Context context)
    {
        super(context);
    }

    public AdvancedFloatingActionButton(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public AdvancedFloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }

    public boolean isAlwaysShown()
    {
        return mIsAlwaysShown;
    }

    public void setAlwaysShown(boolean alwaysShown)
    {
        mIsAlwaysShown = alwaysShown;
    }
}
