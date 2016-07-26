package com.juztoss.bpmplayer.views.activities;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.juztoss.bpmplayer.R;
import com.juztoss.bpmplayer.models.Composition;
import com.juztoss.bpmplayer.presenters.BPMPlayerApp;

import java.util.Locale;

/**
 * Created by JuzTosS on 6/13/2016.
 */
public class SingleSongActivity extends BasePlayerActivity
{
    private long mLastTapTime = 0;
    private long mTapsCount = 0;

    SeekBar mSeekBar;
    Composition mComposition;
    Button mButtonHalf;
    Button mButtonDouble;
    TextView mBpmField;
    private static final int DETECT_WINDOW_SIZE = 10;
    public static final String SONG_ID = "SongId";

    private void setCompositionBPM(float bpm)
    {
        float shift = mComposition.bpmShifted() - mComposition.bpm();
        mComposition.setBPM(bpm);
        mComposition.setShiftedBPM(mComposition.bpm() + shift);
    }

    private void updateBpmField(float bpm)
    {
        mBpmField.setText(String.format(Locale.US, "%.1f", bpm));
    }

    private View.OnClickListener mOnHalfClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            updateBpmField(mComposition.bpm() / 2);
        }
    };

    private View.OnClickListener mOnDoubleClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            updateBpmField(mComposition.bpm() * 2);
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

            if (interval > MAX_INTERVAL)
            {
                mTapsCount = 0;
                updateBpmField(0);
            }
            else
            {
                float currentBpm = 60 * 1000 / interval;
                float lastBpm = mComposition.bpm();
                updateBpmField((lastBpm * mTapsCount + currentBpm) / (mTapsCount + 1));
                if (mTapsCount < DETECT_WINDOW_SIZE)
                    mTapsCount++;
            }
        }
    };
    private SeekBar.OnSeekBarChangeListener mOnSeekBarChanged = new SeekBar.OnSeekBarChangeListener()
    {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
        {
            if (fromUser)
            {
                mComposition.setShiftedBPM(mComposition.bpm() + progress - BPMPlayerApp.MAX_BPM_SHIFT);
                updateSeekBar();
                if (playbackService() != null)
                {
                    if (playbackService().isPlaying() && playbackService().currentSongId() == mComposition.id())
                        playbackService().setNewPlayingBPM(mComposition.bpm(), mComposition.bpmShifted());
                }
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar)
        {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar)
        {

        }
    };

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

        mButtonHalf = (Button) findViewById(R.id.button_half_bpm);
        mButtonHalf.setOnClickListener(mOnHalfClick);
        mButtonDouble = (Button) findViewById(R.id.button_double_bpm);
        mButtonDouble.setOnClickListener(mOnDoubleClick);
        Button buttonTap = (Button) findViewById(R.id.button_tab_bpm);
        buttonTap.setOnClickListener(mOnTapClick);


        mBpmField = (TextView) findViewById(R.id.bpm_text);
        mBpmField.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                if (s.toString().isEmpty())
                    setCompositionBPM(0);
                else
                    setCompositionBPM(Float.valueOf(s.toString()));

                updateEnv();
            }

            @Override
            public void afterTextChanged(Editable s)
            {

            }
        });
        mSeekBar = (SeekBar) findViewById(R.id.seekBar);
        mSeekBar.setMax((int) BPMPlayerApp.MAX_BPM_SHIFT * 2);
        mSeekBar.setOnSeekBarChangeListener(mOnSeekBarChanged);
        updateBpmField(mComposition.bpm());
    }

    @SuppressWarnings("ConstantConditions")
    private void updateSeekBar()
    {
        int bpmShift = (int) (mComposition.bpmShifted() - mComposition.bpm());
        mSeekBar.setProgress((int) BPMPlayerApp.MAX_BPM_SHIFT + bpmShift);

        TextView bpmDesc = (TextView) findViewById(R.id.shiftedBpmValue);
        bpmDesc.setText(String.format(Locale.US, "%.1f (%d)", mComposition.bpmShifted(), bpmShift));
    }

    private void updateEnv()
    {
        mButtonDouble.setEnabled(mComposition.bpm() * 2 <= BPMPlayerApp.MAX_BPM);
        mButtonHalf.setEnabled(mComposition.bpm() / 2 >= BPMPlayerApp.MIN_BPM);

        updateSeekBar();
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
            saveChanges();
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveChanges()
    {
        if (mComposition == null) return;
        BPMPlayerApp app = (BPMPlayerApp) getApplication();
        app.updateBpm(mComposition);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.song_activity_menu, menu);
        MenuItem item = menu.findItem(R.id.apply);
        Drawable newIcon = item.getIcon();
        newIcon.mutate().setColorFilter(getResources().getColor(R.color.foregroundInverted), PorterDuff.Mode.SRC_IN);
        item.setIcon(newIcon);
        return true;
    }
}
