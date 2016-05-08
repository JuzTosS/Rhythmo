package com.juztoss.bpmplayer.views;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.os.Bundle;
import android.os.Handler;
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

import com.juztoss.bpmplayer.PlaybackService;
import com.juztoss.bpmplayer.R;
import com.juztoss.bpmplayer.models.Song;
import com.juztoss.bpmplayer.presenters.BPMPlayerApp;
import com.juztoss.bpmplayer.presenters.PlayerPresenter;

import java.util.List;

/**
 * Created by JuzTosS on 4/20/2016.
 */
public class PlayerFragment extends android.app.Fragment implements DrawerLayout.DrawerListener, AdapterView.OnItemClickListener
{
    private SongsListAdapter mSongsListAdapter;
    private View mPlayButton;
    private TextView mTimePassed;
    private TextView mTimeLeft;
    private SeekBar mSeekbar;
    private BPMPlayerApp mApp;

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mApp = (BPMPlayerApp) getActivity().getApplicationContext();
        mSongsListAdapter = new SongsListAdapter(getActivity());
        ListView list = (ListView) getView().findViewById(R.id.listView);
        list.setOnItemClickListener(this);
        list.setAdapter(mSongsListAdapter);

        DrawerLayout drawer = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
        drawer.addDrawerListener(this);

        mTimePassed = (TextView) getView().findViewById(R.id.time_passed);
        mTimeLeft = (TextView) getView().findViewById(R.id.time_left);
        mSeekbar = (SeekBar) getView().findViewById(R.id.seekbar);
        mSeekbar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);

        mPlayButton = getView().findViewById(R.id.play_button);
        mPlayButton.setOnClickListener(mPlayButtonListenter);

        View nextButton = getView().findViewById(R.id.next_button);
        nextButton.setOnClickListener(mNextButtonListener);
        View previousButton = getView().findViewById(R.id.previous_button);
        previousButton.setOnClickListener(mPreviousButtonListener);

        getLoaderManager().initLoader(0, null, mApp.getPlayerPresenter());

        LocalBroadcastManager.getInstance(mApp).registerReceiver(mUpdateUIReceiver, new IntentFilter(PlaybackService.UPDATE_UI_ACTION));
    }

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
        updateList();
    }

    BroadcastReceiver mUpdateUIReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (!mApp.isPlaybackServiceRunning()) return;

            PlaybackService service = mApp.getPlaybackService();

            mPlayButton.setSelected(!service.isPlaying());
            mSongsListAdapter.notifyDataSetChanged();

            mSeekbar.setMax(service.getDuration());
            mHandler.post(mSeekbarUpdateRunnable);
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
        updateList();
    }

    private void updateList()
    {
        Bundle path = new Bundle();
        path.putParcelable(PlayerPresenter.BUNDLE_PATH, mApp.getSongsFolder());
        Loader<List<Song>> loader = getLoaderManager().restartLoader(0, path, mApp.getPlayerPresenter());
        loader.forceLoad();
    }


    @Override
    public void onDrawerStateChanged(int newState)
    {

    }
}