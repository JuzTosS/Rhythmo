package com.juztoss.bpmplayer.views;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;

import com.juztoss.bpmplayer.PlaybackService;
import com.juztoss.bpmplayer.R;
import com.juztoss.bpmplayer.models.Song;
import com.juztoss.bpmplayer.presenters.BPMPlayerApp;
import com.juztoss.bpmplayer.presenters.PlayerPresenter;

/**
 * Created by JuzTosS on 4/20/2016.
 */
public class PlayerFragment extends android.app.Fragment implements DrawerLayout.DrawerListener, AdapterView.OnItemClickListener
{
    private PlayerPresenter mPresenter;
    private SongsListAdapter mSongsListAdapter;
    private View mPlayButton;
    private BPMPlayerApp mApp;

    public void init(PlayerPresenter p)
    {
        mPresenter = p;
        mApp = (BPMPlayerApp) getActivity().getApplicationContext();
        mSongsListAdapter = new SongsListAdapter(getActivity());
        ListView list = (ListView) getView().findViewById(R.id.listView);
        list.setOnItemClickListener(this);
        list.setAdapter(mSongsListAdapter);

        DrawerLayout drawer = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
        drawer.addDrawerListener(this);

        mPlayButton = getView().findViewById(R.id.play_button);
        mPlayButton.setOnClickListener(mPlayButtonListenter);

        getLoaderManager().initLoader(0, null, mPresenter);

        LocalBroadcastManager.getInstance(mApp).registerReceiver(mUpdateUIReceiver, new IntentFilter(PlaybackService.UPDATE_UI_ACTION));
    }

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
            if(!mApp.isPlaybackServiceRunning()) return;

            mPlayButton.setSelected(!mApp.getPlaybackService().getIsPlaying());

            mSongsListAdapter.notifyDataSetChanged();
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
        Loader<List<Song>> loader = getLoaderManager().restartLoader(0, path, mPresenter);
        loader.forceLoad();
    }


    @Override
    public void onDrawerStateChanged(int newState)
    {

    }
}