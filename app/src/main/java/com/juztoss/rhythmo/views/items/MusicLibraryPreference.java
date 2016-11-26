package com.juztoss.rhythmo.views.items;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.juztoss.rhythmo.R;

/**
 * Created by JuzTosS on 9/25/2016.
 */
public class MusicLibraryPreference extends Preference
{
    private ProgressBar mProgressBar;
    private int mOverallProgress = 0;
    private int mMaxProgress = 0;

    public MusicLibraryPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public MusicLibraryPreference(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }

    public MusicLibraryPreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public MusicLibraryPreference(Context context)
    {
        super(context);
    }

    @Override
    protected void onBindView(View view)
    {
        super.onBindView(view);

        if (view instanceof LinearLayout)
        {
            LinearLayout ll = (LinearLayout) view;
            ll.setOrientation(LinearLayout.VERTICAL);
        }

        View frame = view.findViewById(android.R.id.widget_frame);
        if (frame != null)
        {
            frame.setPadding(0,0,0,0);
            ViewGroup.LayoutParams lp = frame.getLayoutParams();
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        }

        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        updateProgressBar();
    }

    private void updateProgressBar()
    {
        if(mProgressBar == null) return;

        if(mOverallProgress > 0)
        {
            mProgressBar.setVisibility(View.VISIBLE);
            mProgressBar.setMax(mMaxProgress);
            mProgressBar.setProgress(mOverallProgress);
        }
        else
        {
            mProgressBar.setVisibility(View.GONE);
        }
    }

    /**
     * If overallProgress is 0 the progress bar is hidden
     * @param header
     * @param overallProgress
     * @param maxProgress
     */
    public void update(String header, int overallProgress, int maxProgress)
    {
        mOverallProgress = overallProgress;
        mMaxProgress = maxProgress;
        setSummary(header);

        updateProgressBar();
    }
}
