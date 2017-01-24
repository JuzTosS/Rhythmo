package com.juztoss.rhythmo.views.items;

import android.content.Context;
import android.content.Intent;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

import com.juztoss.rhythmo.R;
import com.juztoss.rhythmo.services.BuildMusicLibraryService;
import com.juztoss.rhythmo.services.LibraryServiceBuilder;

/**
 * Created by JuzTosS on 9/25/2016.
 */
public class ClearLibraryPreference extends Preference implements View.OnLongClickListener
{
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
        new LibraryServiceBuilder(getContext())
                .clearBpm()
                .stopCurrentlyExecuting()
                .start();
        Toast.makeText(getContext().getApplicationContext(), R.string.clear_library_finished, Toast.LENGTH_SHORT).show();
        return true;
    }
}
