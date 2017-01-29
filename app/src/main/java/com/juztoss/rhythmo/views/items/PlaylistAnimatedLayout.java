package com.juztoss.rhythmo.views.items;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import com.juztoss.rhythmo.R;

/**
 * Created by JuzTosS on 1/22/2017.
 */

/**
 * This class encapsulates the logic of hiding and showing animations of a playlist view
 */
public class PlaylistAnimatedLayout extends FrameLayout
{
    private static final float MIN_SCALE = 0.75f;

    private View mHint;
    private View mHintLabel;
    private float mHintLabelCenterX;

    public PlaylistAnimatedLayout(Context context)
    {
        super(context);
    }

    public PlaylistAnimatedLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public PlaylistAnimatedLayout(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PlaylistAnimatedLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate()
    {
        super.onFinishInflate();
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
        {
            @Override
            public void onGlobalLayout()
            {
                mHint = findViewById(R.id.hint);
                mHintLabel = findViewById(R.id.hintLabel);
                mHintLabelCenterX = mHintLabel.getX();
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    /**
     * Reposition background items according to the positionOffset
     *
     * @param positionOffset - from (-1 to 1)
     */
    public void animateBackground(float positionOffset)
    {
        if (mHintLabel == null) return;

        if (mHint.getVisibility() == VISIBLE)
        {
            animateHint(positionOffset);
        }
        else
        {
            animateWhole(positionOffset);
        }
    }

    private void animateWhole(float positionOffset)
    {
        float absOffset = Math.abs(positionOffset);
        setAlpha(1f - absOffset);
        float scaleFactor = MIN_SCALE + (1 - MIN_SCALE) * (1 - Math.abs(absOffset));
        setScaleX(scaleFactor);
        setScaleY(scaleFactor);
    }

    private float getAlphaFromOffset(float offset)
    {
        float alpha = 1f - Math.abs(offset);
        alpha = (alpha - 0.5f) * 2f;
        if (alpha <= 0) alpha = 0;
        return alpha;
    }

    private void animateHint(float positionOffset)
    {
        mHintLabel.setX(mHintLabelCenterX + positionOffset * 500.0f);

        mHint.setAlpha(getAlphaFromOffset(positionOffset));
    }
}
