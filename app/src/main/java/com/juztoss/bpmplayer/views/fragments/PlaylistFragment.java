package com.juztoss.bpmplayer.views.fragments;

import android.content.Intent;
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
import com.juztoss.bpmplayer.services.PlaybackService;
import com.juztoss.bpmplayer.views.activities.PlayerActivity;
import com.juztoss.bpmplayer.views.activities.SingleSongActivity;
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
    private PlayerActivity mActivity;
    private LinearLayoutManager mLayoutManager;

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

        mActivity = ((PlayerActivity) getActivity());
        mPlaylistIndex = playlistIndex;
        mPlaylistAdapter = new PlaylistAdapter((PlayerActivity) getActivity(), mApp.getPlaylists().get(playlistIndex));
        RecyclerView list = (RecyclerView) getView().findViewById(R.id.listView);
        mPlaylistAdapter.setOnItemClickListener(this);
        list.setAdapter(mPlaylistAdapter);
        mLayoutManager = new LinearLayoutManager(getActivity());
        list.setLayoutManager(mLayoutManager);
    }

    private void showSongActivity(Composition composition)
    {
        Intent intent = new Intent(getContext(), SingleSongActivity.class);
        intent.putExtra(SingleSongActivity.SONG_ID, composition.id());
        getContext().startActivity(intent);
    }

    @Override
    public void onPlaylistItemClick(int position, int action, Composition composition)
    {
        if (action == SongElementHolder.ACTION_PLAY)
        {
            Intent i = new Intent(getContext(), PlaybackService.class);
            i.setAction(PlaybackService.ACTION_COMMAND);
            i.putExtra(PlaybackService.ACTION_NAME, PlaybackService.PLAY_NEW_ACTION);
            i.putExtra(PlaybackService.ACTION_PLAYLIST_INDEX, mPlaylistIndex);
            i.putExtra(PlaybackService.ACTION_PLAYLIST_POSITION, position);
            getContext().startService(i);
        }
        else if (action == SongElementHolder.ACTION_SHOW_DETAIL)
        {
            showSongActivity(composition);
        }
        else if (action == SongElementHolder.ACTION_REMOVE)
        {
            mApp.getPlaylists().get(mPlaylistIndex).getSource().remove(composition.id());
            mPlaylistAdapter.updateList();
        }
    }

    @Override
    public void onStart()
    {
        super.onStart();
        mPlaylistAdapter.updateList();
        onResumeFragment();
    }

    public void scrollTo(int position)
    {
        mLayoutManager.scrollToPositionWithOffset(position, 0);
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
        if (mPlaylistAdapter == null) return;
            mPlaylistAdapter.bind();
    }

    public void onPauseFragment()
    {
        if (mPlaylistAdapter != null)
            mPlaylistAdapter.unbind();
    }

}