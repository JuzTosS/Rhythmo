package com.juztoss.rhythmo.views.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.juztoss.rhythmo.R;
import com.juztoss.rhythmo.models.Composition;
import com.juztoss.rhythmo.presenters.RhythmoApp;
import com.juztoss.rhythmo.services.PlaybackService;
import com.juztoss.rhythmo.views.activities.PlayerActivity;
import com.juztoss.rhythmo.views.activities.SingleSongActivity;
import com.juztoss.rhythmo.views.adapters.IOnItemClickListener;
import com.juztoss.rhythmo.views.adapters.PlaylistAdapter;
import com.juztoss.rhythmo.views.adapters.SongElementHolder;

/**
 * Created by JuzTosS on 4/20/2016.
 */
public class PlaylistFragment extends Fragment implements IOnItemClickListener
{
    public static String PLAYLIST_INDEX = "PlaylistID";
    private int mPlaylistIndex;

    private PlaylistAdapter mPlaylistAdapter;

    private RhythmoApp mApp;
    private LinearLayoutManager mLayoutManager;
    private int mScrollOnCreateToPosition = -1;

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
        mApp = (RhythmoApp) getActivity().getApplicationContext();

        Bundle arguments = getArguments();
        if (arguments == null) return;
        int playlistIndex = arguments.getInt(PLAYLIST_INDEX, -1);
        if (playlistIndex < 0) return;

        mPlaylistIndex = playlistIndex;
        mPlaylistAdapter = new PlaylistAdapter((PlayerActivity) getActivity(), mApp.getPlaylists().get(playlistIndex));
        RecyclerView list = (RecyclerView) getView().findViewById(R.id.listView);
        mPlaylistAdapter.setOnItemClickListener(this);
        list.setAdapter(mPlaylistAdapter);
        mLayoutManager = new LinearLayoutManager(getActivity());
        list.setLayoutManager(mLayoutManager);
        if(mScrollOnCreateToPosition >= 0)
        {
            scrollTo(mScrollOnCreateToPosition);
            mScrollOnCreateToPosition = -1;
        }
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

    public void scrollTo(int position)
    {
        if(mLayoutManager != null)
            mLayoutManager.scrollToPositionWithOffset(position, 0);
        else
            mScrollOnCreateToPosition = position;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.songs_fragment, container, false);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        mPlaylistAdapter.unbind();
    }

    public void onResumeFragment()
    {
        if (mPlaylistAdapter == null) return;

        mPlaylistAdapter.bind();
    }

    public void onPauseFragment()
    {

    }

}