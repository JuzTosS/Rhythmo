package com.juztoss.rhythmo.views.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.juztoss.rhythmo.R;
import com.juztoss.rhythmo.models.Composition;
import com.juztoss.rhythmo.presenters.RhythmoApp;
import com.juztoss.rhythmo.services.PlaybackService;

/**
 * Created by JuzTosS on 3/1/2017.
 */

public class PlayCustomFileActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);


        Intent launchIntent = getIntent();
        String action = launchIntent.getAction();
        String type = launchIntent.getType();


        if (Intent.ACTION_VIEW.equals(action) && type != null) {
            Uri uri = launchIntent.getData();
            String path = uri.getPath();

            RhythmoApp app = (RhythmoApp) getApplicationContext();
            Composition composition = app.getComposition(path);

            if(composition != null)
            {
                Intent i = new Intent(this, PlaybackService.class);
                i.setAction(PlaybackService.ACTION_COMMAND);
                i.putExtra(PlaybackService.ACTION_NAME, PlaybackService.PLAY_NEW_ACTION);
                i.putExtra(PlaybackService.ACTION_PLAYLIST_INDEX, 0);//0 - is the index of the first playlist with all the songs
                i.putExtra(PlaybackService.ACTION_SONG_ID, composition.id());

                startService(i);
            }
            else
            {
                Toast.makeText(this, R.string.cant_play_this_file, Toast.LENGTH_SHORT).show();
            }
        }

        finish();
    }
}
