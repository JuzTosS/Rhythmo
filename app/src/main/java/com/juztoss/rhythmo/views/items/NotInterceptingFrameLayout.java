package com.juztoss.rhythmo.views.items;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.IdRes;

import com.juztoss.rhythmo.R;

/**
 * Created by JuzTosS on 1/19/2017.
 */

public class NotInterceptingFrameLayout extends FrameLayout
{
    private View mClickableChild;
    @IdRes
    private int mClickableChildId;
    private GestureDetector mGestureDetector;

    public NotInterceptingFrameLayout(Context context)
    {
        super(context, null);
        init(context, null, 0, 0);
    }

    public NotInterceptingFrameLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }


    public NotInterceptingFrameLayout(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public NotInterceptingFrameLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        mGestureDetector = new GestureDetector(context, new YScrollDetector());

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.NotInterceptingFrameLayout,
                defStyleAttr, defStyleRes);
        try
        {
            mClickableChildId = a.getResourceId(R.styleable.NotInterceptingFrameLayout_redirectTo, -1);
        }
        finally
        {
            a.recycle();
        }
    }

    @Override
    protected void onFinishInflate()
    {
        super.onFinishInflate();
        mClickableChild = findViewById(mClickableChildId);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        mClickableChild.onTouchEvent(event);

        if (!mGestureDetector.onTouchEvent(event))
            super.onTouchEvent(event);
        else
        {
            event.setAction(MotionEvent.ACTION_CANCEL);
            super.onTouchEvent(event);
        }
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev)
    {
        return true;
    }

    private static class YScrollDetector extends GestureDetector.SimpleOnGestureListener
    {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
        {
            return true;
        }
    }
}
