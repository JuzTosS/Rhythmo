package com.juztoss.bpmplayer.views.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.juztoss.bpmplayer.R;
import com.juztoss.bpmplayer.models.Composition;
import com.juztoss.bpmplayer.presenters.BPMPlayerApp;
import com.juztoss.bpmplayer.views.activities.PlayerActivity;
import com.juztoss.bpmplayer.views.adapters.IOnItemClickListener;
import com.juztoss.bpmplayer.views.adapters.PlaylistAdapter;
import com.juztoss.bpmplayer.views.adapters.SongElementHolder;

/**
 * Created by JuzTosS on 4/20/2016.
 */
public class PlaylistFragment extends Fragment implements IOnItemClickListener
{
    public static String PLAYLIST_INDEX = "PlaylistID";
    private int mPlaylistIndex;

    private PlaylistAdapter mPlaylistAdapter;

    private BPMPlayerApp mApp;

    public static PlaylistFragment newInstance(int playlistIndex)
    {
        PlaylistFragment fragment = new PlaylistFragment();
        Bundle args = new Bundle();
        args.putInt(PLAYLIST_INDEX, playlistIndex);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        mApp = (BPMPlayerApp) getActivity().getApplicationContext();

        Bundle arguments = getArguments();
        if (arguments == null) return;
        int playlistIndex = arguments.getInt(PLAYLIST_INDEX, -1);
        if (playlistIndex < 0) return;

        if (!mApp.isPlaybackServiceRunning()) return;

        mPlaylistIndex = playlistIndex;
        mPlaylistAdapter = new PlaylistAdapter((PlayerActivity) getActivity(), mApp.getPlaylists().get(playlistIndex));
        RecyclerView list = (RecyclerView) getView().findViewById(R.id.listView);
        mPlaylistAdapter.setOnItemClickListener(this);
        list.setAdapter(mPlaylistAdapter);
        list.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    @Override
    public void onPlaylistItemClick(int position, int action, Composition composition)
    {
        if(action == SongElementHolder.ACTION_PLAY)
        {
            if (mApp.isPlaybackServiceRunning())
            {
                mApp.getPlaybackService().setSource(mPlaylistIndex, position);
                mApp.getPlaybackService().startPlayback();
            }
        }
        else if(action == SongElementHolder.ACTION_REMOVE)
        {
            mApp.getPlaylists().get(mPlaylistIndex).getSource().remove(composition.id());
            mPlaylistAdapter.updateList();
        }
    }

    @Override
    public void onStart()
    {
        super.onStart();
        onResumeFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.songs_fragment, container, false);
    }

    @Override
    public void onDestroy()
    {
        onPauseFragment();
        super.onDestroy();
    }

    public void onResumeFragment()
    {
        if(mPlaylistAdapter == null) return;
        mPlaylistAdapter.updateList();
        mPlaylistAdapter.bind();
    }

    public void onPauseFragment()
    {
        if(mPlaylistAdapter != null)
            mPlaylistAdapter.unbind();
    }

}