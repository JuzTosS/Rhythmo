package com.juztoss.bpmplayer.views;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.juztoss.bpmplayer.R;
import com.juztoss.bpmplayer.models.Composition;
import com.juztoss.bpmplayer.presenters.BPMPlayerApp;

/**
 * Created by JuzTosS on 6/13/2016.
 */
public class SongActivity extends AppCompatActivity
{
    private long mLastTapTime = 0;
    private long mTapsCount = 0;

    Composition mComposition;
    public static final String SONG_ID = "SongId";

    private View.OnClickListener mOnHalfClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            mComposition.setBPM(mComposition.bpm() / 2);
            updateBpm();
        }
    };

    private View.OnClickListener mOnDoubleClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            mComposition.setBPM(mComposition.bpm() * 2);
            updateBpm();
        }
    };

    private View.OnClickListener mOnTapClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            long now = System.currentTimeMillis();
            final long MAX_INTERVAL = 2000;
            final long interval = now - mLastTapTime;
            mLastTapTime = now;
            if(interval > MAX_INTERVAL)
            {
                mTapsCount = 0;
                mComposition.setBPM(0);
                updateBpm();
            }
            else
            {
                float currentBpm = 60 * 1000 / interval;
                float lastBpm = mComposition.bpm();
                mComposition.setBPM((lastBpm * mTapsCount + currentBpm) / (mTapsCount + 1));
                mTapsCount++;
                updateBpm();
            }

        }
    };

    @SuppressLint("DefaultLocale")
    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle(R.string.song_detail_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final long songId = getIntent().getExtras().getLong(SONG_ID);

        BPMPlayerApp app = (BPMPlayerApp) getApplication();
        mComposition = app.getComposition(songId);

        if (mComposition == null) return;

        TextView nameField = (TextView) findViewById(R.id.song_name);
        nameField.setText(mComposition.getAbsolutePath());

        updateBpm();


        Button buttonHalf = (Button) findViewById(R.id.button_half_bpm);
        buttonHalf.setOnClickListener(mOnHalfClick);
        Button buttonDouble = (Button) findViewById(R.id.button_double_bpm);
        buttonDouble.setOnClickListener(mOnDoubleClick);
        Button buttonTap = (Button) findViewById(R.id.button_tab_bpm);
        buttonTap.setOnClickListener(mOnTapClick);
    }

    private void updateBpm()
    {
        TextView bpmField = (TextView) findViewById(R.id.bpm_text);
        bpmField.setText(String.format("%.1f", mComposition.bpm()));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        if (id == android.R.id.home)
        {
            onBackPressed();
        }
        else if (id == R.id.song_apply)
        {
            saveChanges();
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveChanges()
    {
        if(mComposition == null) return;
        BPMPlayerApp app = (BPMPlayerApp) getApplication();
        app.updateBpm(mComposition);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.song_menu, menu);
        return true;
    }
}
