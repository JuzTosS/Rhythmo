package com.juztoss.rhythmo.views.items;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.util.AttributeSet;
import android.widget.ImageButton;

/**
 * Created by JuzTosS on 1/22/2017.
 */

public class FlippableButton extends ImageButton
{
    private AnimatorSet mAnimatorSet;

    public FlippableButton(Context context)
    {
        super(context);
    }

    public FlippableButton(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public FlippableButton(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public FlippableButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void flipTo(@DrawableRes final int res)
    {
        if(mAnimatorSet != null)
            mAnimatorSet.cancel();

        mAnimatorSet = new AnimatorSet();

        Animator hideAnimator = ObjectAnimator
                .ofFloat(this, "scaleX", 1, 0)
                .setDuration(100);

        hideAnimator.addListener(new AnimatorListenerAdapter()
        {
            @Override
            public void onAnimationEnd(Animator animation)
            {
                flipInstantlyTo(res);
            }
        });

        Animator showAnimator = ObjectAnimator
                .ofFloat(this, "scaleX", 0, 1)
                .setDuration(100);

        mAnimatorSet.play(hideAnimator).before(showAnimator);
        mAnimatorSet.start();

    }

    @Override
    protected void onDetachedFromWindow()
    {
        super.onDetachedFromWindow();
        if(mAnimatorSet != null)
            mAnimatorSet.cancel();

        mAnimatorSet = null;
    }

    public void flipInstantlyTo(@DrawableRes int res)
    {
        setImageResource(res);
    }
}
