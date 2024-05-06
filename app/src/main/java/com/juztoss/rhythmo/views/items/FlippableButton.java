package com.juztoss.rhythmo.views.items;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;

import androidx.annotation.DrawableRes;
import androidx.appcompat.widget.AppCompatImageButton;

import com.juztoss.rhythmo.R;
import com.juztoss.rhythmo.utils.SystemHelper;

/**
 * Created by JuzTosS on 1/22/2017.
 */

public class FlippableButton extends AppCompatImageButton
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
        if(res == R.drawable.ic_remove_circle_black_36dp || res == R.drawable.ic_remove_circle_outline_black_36dp)
            setColorFilter(SystemHelper.getColor(getContext(),R.attr.rForeground));
        else
            setColorFilter(SystemHelper.getColor(getContext(),R.attr.rForegroundGrayedOut));
    }
}
