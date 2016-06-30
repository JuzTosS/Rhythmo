package com.juztoss.bpmplayer.views.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.juztoss.bpmplayer.R;
import com.juztoss.bpmplayer.presenters.BPMPlayerApp;
import com.juztoss.bpmplayer.views.fragments.BrowserFragment;

/**
 * Created by JuzTosS on 6/13/2016.
 */
public class SelectSongsActivity extends AppCompatActivity
{
    public static final String FOLDERS_PATHS = "FoldersPaths";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_song);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle(R.string.select_folder);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new BrowserFragment()).commit();
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
            BPMPlayerApp app = (BPMPlayerApp) getApplicationContext();
            resultIntent.putExtra(FOLDERS_PATHS, app.getBrowserPresenter().getPaths());
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.song_menu, menu);
        return true;
    }
}
