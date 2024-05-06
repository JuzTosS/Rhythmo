package com.juztoss.rhythmo.views.items;

import android.content.Context;
import androidx.annotation.NonNull;
import android.util.AttributeSet;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * Created by JuzTosS on 7/27/2016.
 */
public class FixedFloatingActionButton extends FloatingActionButton
{
    public FixedFloatingActionButton(@NonNull Context context)
    {
        super(context);
    }

    public FixedFloatingActionButton(@NonNull Context context, AttributeSet attributes)
    {
        super(context, attributes);
    }

    public FixedFloatingActionButton(@NonNull Context context, AttributeSet attributes, int defStyleAttribute)
    {
        super(context, attributes, defStyleAttribute);
    }

    private boolean mFabShouldBeShown;
    private FloatingActionButton.OnVisibilityChangedListener mListener = new FloatingActionButton.OnVisibilityChangedListener() {
        @Override
        public void onShown(FloatingActionButton fab) {
            super.onShown(fab);
            if(!mFabShouldBeShown){
                fab.hide();
            }
        }

        @Override
        public void onHidden(FloatingActionButton fab) {
            super.onHidden(fab);
            if(mFabShouldBeShown){
                fab.show();
            }
        }
    };

    @Override
    public void show()
    {
        mFabShouldBeShown = true;
        super.show(mListener);
    }

    @Override
    public void hide()
    {
        mFabShouldBeShown = false;
        super.hide(mListener);
    }
}
