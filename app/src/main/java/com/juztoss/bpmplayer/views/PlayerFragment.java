package com.juztoss.bpmplayer.views;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.juztoss.bpmplayer.R;
import com.juztoss.bpmplayer.presenters.BPMPlayerApp;
import com.juztoss.bpmplayer.services.PlaybackService;

/**
 * Created by JuzTosS on 4/20/2016.
 */
public class PlayerFragment extends Fragment implements DrawerLayout.DrawerListener, AdapterView.OnItemClickListener
{
    public static String PLAYLIST_INDEX = "PlaylistID";

    private PlaylistAdapter mPlaylistAdapter;
    private View mPlayButton;
    private TextView mTimePassed;
    private TextView mTimeLeft;
    private SeekBar mSeekbar;
    private RangeSeekBar<Integer> mRangeSeekbar;
    private BPMPlayerApp mApp;

    public static PlayerFragment newInstance(int playlistIndex) {
        PlayerFragment fragment = new PlayerFragment();
        Bundle args = new Bundle();
        args.putInt(PLAYLIST_INDEX, playlistIndex);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mApp = (BPMPlayerApp) getActivity().getApplicationContext();

        Bundle arguments = getArguments();
        if(arguments == null) return;
        int playlistIndex = arguments.getInt(PLAYLIST_INDEX, -1);
        if(playlistIndex < 0) return;

        if(!mApp.isPlaybackServiceRunning()) return;

        mPlaylistAdapter = new PlaylistAdapter(getActivity(), mApp.getPlaylists().get(playlistIndex));
        ListView list = (ListView) getView().findViewById(R.id.listView);
        list.setOnItemClickListener(this);
        list.setAdapter(mPlaylistAdapter);

        DrawerLayout drawer = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
        drawer.addDrawerListener(this);

        mTimePassed = (TextView) getView().findViewById(R.id.time_passed);
        mTimeLeft = (TextView) getView().findViewById(R.id.time_left);
        mSeekbar = (SeekBar) getView().findViewById(R.id.seekbar);
        mSeekbar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);

        mRangeSeekbar = (RangeSeekBar<Integer>) getView().findViewById(R.id.bpm_ranger);
        mRangeSeekbar.setOnRangeSeekBarChangeListener(mOnBpmRangeChanged);
        mRangeSeekbar.setRangeValues(50, 150);

        mPlayButton = getView().findViewById(R.id.play_button);
        mPlayButton.setOnClickListener(mPlayButtonListenter);

        View nextButton = getView().findViewById(R.id.next_button);
        nextButton.setOnClickListener(mNextButtonListener);
        View previousButton = getView().findViewById(R.id.previous_button);
        previousButton.setOnClickListener(mPreviousButtonListener);

        LocalBroadcastManager.getInstance(mApp).registerReceiver(mUpdateUIReceiver, new IntentFilter(PlaybackService.UPDATE_UI_ACTION));
    }

    private RangeSeekBar.OnRangeSeekBarChangeListener<Integer> mOnBpmRangeChanged = new RangeSeekBar.OnRangeSeekBarChangeListener<Integer>()
    {
        @Override
        public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer minValue, Integer maxValue)
        {
            if (mApp.isPlaybackServiceRunning())
            {
                mPlaylistAdapter.setRange(minValue, maxValue);
            }
        }
    };

    private SeekBar.OnSeekBarChangeListener mOnSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener()
    {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
        {
            int timePassed = progress;
            int timeLeft = seekBar.getMax() - timePassed;
            mTimePassed.setText(DateUtils.formatElapsedTime(timePassed / 1000));
            mTimeLeft.setText(DateUtils.formatElapsedTime(timeLeft / 1000));

            if (mApp.isPlaybackServiceRunning())
            {
                if (fromUser)
                    mApp.getPlaybackService().seekTo(progress);
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

    private View.OnClickListener mNextButtonListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            if (mApp.isPlaybackServiceRunning())
                mApp.getPlaybackService().gotoNext();
        }
    };

    private View.OnClickListener mPreviousButtonListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            if (mApp.isPlaybackServiceRunning())
                mApp.getPlaybackService().gotoPrevious();
        }
    };

    @Override
    public void onStart()
    {
        super.onStart();
        updateAll();
    }

    protected void updateAll()
    {
        if (!mApp.isPlaybackServiceRunning()) return;

        PlaybackService service = mApp.getPlaybackService();

        mPlayButton.setSelected(!service.isPlaying());
        mPlaylistAdapter.notifyDataSetChanged();

        mSeekbar.setMax(service.getDuration());
        mHandler.post(mSeekbarUpdateRunnable);
    }

    BroadcastReceiver mUpdateUIReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            updateAll();
        }
    };


    private Handler mHandler = new Handler();
    private Runnable mSeekbarUpdateRunnable = new Runnable()
    {
        public void run()
        {
            try
            {
                if (!mApp.isPlaybackServiceRunning())
                    return;

                mSeekbar.setProgress(mApp.getPlaybackService().getCurrentPosition());

                if (mApp.getPlaybackService().isPlaying())
                    mHandler.postDelayed(mSeekbarUpdateRunnable, 100);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    };

    private View.OnClickListener mPlayButtonListenter = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            if (mApp.isPlaybackServiceRunning())
                mApp.getPlaybackService().togglePlaybackState();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.songs_list, container, false);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        if (mApp.isPlaybackServiceRunning())
        {
            mApp.getPlaybackService().setSource(position);
            mApp.getPlaybackService().startPlayback();
        }
    }

    @Override
    public void onDrawerSlide(View drawerView, float slideOffset)
    {
    }

    @Override
    public void onDrawerOpened(View drawerView)
    {
    }

    @Override
    public void onDrawerClosed(View drawerView)
    {
        updateAll();
    }


    @Override
    public void onDrawerStateChanged(int newState)
    {

    }
}