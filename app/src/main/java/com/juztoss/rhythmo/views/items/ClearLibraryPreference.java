package com.juztoss.rhythmo.views.items;

import android.content.Context;
import android.content.Intent;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.juztoss.rhythmo.R;
import com.juztoss.rhythmo.services.BuildMusicLibraryService;

/**
 * Created by JuzTosS on 9/25/2016.
 */
public class ClearLibraryPreference extends Preference implements View.OnLongClickListener
{

    public ClearLibraryPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public ClearLibraryPreference(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }

    public ClearLibraryPreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public ClearLibraryPreference(Context context)
    {
        super(context);
    }

    @Override
    public boolean onLongClick(View v)
    {
        Intent intent = new Intent(getContext(), BuildMusicLibraryService.class);
        intent.putExtra(BuildMusicLibraryService.STOP_AND_CLEAR, true);
        getContext().startService(intent);
        return true;
    }
}
