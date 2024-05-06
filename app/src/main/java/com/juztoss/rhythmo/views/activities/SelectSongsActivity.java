package com.juztoss.rhythmo.views.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;

import com.juztoss.rhythmo.R;
import com.juztoss.rhythmo.presenters.BrowserPresenter;
import com.juztoss.rhythmo.presenters.RhythmoApp;
import com.juztoss.rhythmo.utils.SystemHelper;
import com.juztoss.rhythmo.views.fragments.BrowserFragment;

/**
 * Created by JuzTosS on 6/13/2016.
 */
public class SelectSongsActivity extends BasePlayerActivity implements BrowserFragment.OnItemsStateChangedListener
{
    private static final String CIRCULAR_REVEAL_X = "CIRCULAR_REVEAL_X";
    private static final String CIRCULAR_REVEAL_Y = "CIRCULAR_REVEAL_Y";
    public static final String FOLDERS_PATHS = "FoldersPaths";

    private MenuItem mMenuItemApply;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_song);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            startCircularReveal();

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle(R.string.select_folder);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new BrowserFragment()).commit();
    }

    @Override
    public void onItemsStateChanged()
    {
        updateMenuButton();
    }

    private void updateMenuButton()
    {
        if (mMenuItemApply == null)
        {
            invalidateOptionsMenu();
            return;
        }

        RhythmoApp app = (RhythmoApp) getApplicationContext();
        if(app.getBrowserPresenter().getPaths().length <= 0)
        {
            Drawable drawable = mMenuItemApply.getIcon();
            mMenuItemApply = null;
            ObjectAnimator animator = ObjectAnimator.ofInt(drawable, "alpha", 255, 0);
            animator.addListener(new AnimatorListenerAdapter()
            {
                @Override
                public void onAnimationEnd(Animator animation)
                {
                    invalidateOptionsMenu();
                }
            });
            animator.setDuration(500);
            animator.start();
        }
    }

    private void startCircularReveal()
    {
        ViewTreeObserver viewTreeObserver = getWindow().getDecorView().getViewTreeObserver();
        if (viewTreeObserver.isAlive())
        {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
            {
                @Override
                public void onGlobalLayout()
                {
                    circularRevealActivity();
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
                    {
                        getWindow().getDecorView().getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                    else
                    {
                        getWindow().getDecorView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
            });
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void circularRevealActivity() {

        if(getIntent().getExtras() == null || (getIntent().getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) > 0)  return;
        int cx = getIntent().getIntExtra(CIRCULAR_REVEAL_X, 0);
        int cy = getIntent().getIntExtra(CIRCULAR_REVEAL_Y, 0);

        float finalRadius = Math.max(getWindow().getDecorView().getWidth(), getWindow().getDecorView().getHeight());

        // create the animator for this view (the start radius is zero)
        Animator circularReveal = ViewAnimationUtils.createCircularReveal(getWindow().getDecorView(), cx, cy, 0, finalRadius);
        circularReveal.setDuration(500);
        circularReveal.start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        if (id == android.R.id.home)
        {
            onBackPressed();
        }
        else if (id == R.id.apply)
        {
            Intent resultIntent = new Intent();
            RhythmoApp app = (RhythmoApp) getApplicationContext();
            resultIntent.putExtra(FOLDERS_PATHS, app.getBrowserPresenter().getPaths());
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        RhythmoApp app = (RhythmoApp) getApplicationContext();
        if(app.getBrowserPresenter().getPaths().length > 0)
        {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.song_activity_menu, menu);
            mMenuItemApply = menu.findItem(R.id.apply);
            Drawable newIcon = mMenuItemApply.getIcon();
            newIcon.mutate().setColorFilter(SystemHelper.getColor(this, R.attr.rForegroundInverted), PorterDuff.Mode.SRC_IN);
            newIcon.setAlpha(0);
            mMenuItemApply.setIcon(newIcon);

            ObjectAnimator animator = ObjectAnimator.ofInt(newIcon, "alpha", 0, 255);
            animator.setDuration(500);
            animator.start();
        }

        return true;
    }

    public static Intent getIntent(Context context, float circularRevealX, float circularRevealY)
    {
        Intent intent = new Intent(context, SelectSongsActivity.class);
        intent.putExtra(CIRCULAR_REVEAL_X, (int)circularRevealX);
        intent.putExtra(CIRCULAR_REVEAL_Y, (int)circularRevealY);
        return intent;
    }
}
