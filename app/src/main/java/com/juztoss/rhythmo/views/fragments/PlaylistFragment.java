package com.juztoss.rhythmo.views.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.juztoss.rhythmo.R;
import com.juztoss.rhythmo.models.Composition;
import com.juztoss.rhythmo.models.songsources.SortType;
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
    private View mHeader;
    private volatile TextView mHeaderText;

    private RecyclerView.OnScrollListener mOnListScrollListener = new RecyclerView.OnScrollListener()
    {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy)
        {
            updatePlaylistHeader(recyclerView);
        }
    };

    private void updatePlaylistHeader(RecyclerView recyclerView)
    {
        if (mPlaylistAdapter.getSortType() != SortType.DIRECTORY)
            return;

        int firstVisibleIndex = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
        int firstFullyVisibleIndex = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
        float headerY = 0;
        if (firstVisibleIndex >= 0)
        {
            View view = recyclerView.getLayoutManager().findViewByPosition(firstVisibleIndex);
            SongElementHolder holder = (SongElementHolder) view.getTag();
            mHeaderText.setText(holder.getFolderName());
            if (holder.isFolderHeader())
                headerY = view.getY() - mHeader.getHeight();
        }
        if (firstFullyVisibleIndex >= 0)
        {
            View view = recyclerView.getLayoutManager().findViewByPosition(firstFullyVisibleIndex);
            SongElementHolder holder = (SongElementHolder) view.getTag();
            if (holder.isFolderHeader())
                headerY = view.getY() - mHeader.getHeight();
        }

        if (headerY <= -mHeader.getHeight() || headerY >= 0)
            headerY = 0;

        mHeader.setY(headerY);
    }

    private void updatePlaylistHeaderVisibility()
    {
        if (mPlaylistAdapter.getSortType() != SortType.DIRECTORY)
            mHeader.setVisibility(View.GONE);
        else
        {
            mHeader.setVisibility(View.VISIBLE);
            if(getView() != null)
                updatePlaylistHeader((RecyclerView) getView().findViewById(R.id.listView));
        }
    }

    @Override
    public void onDestroyView()
    {
        RecyclerView list = (RecyclerView) getView().findViewById(R.id.listView);
        list.removeOnScrollListener(mOnListScrollListener);
        super.onDestroyView();
    }

    private PlaylistAdapter.IOnDataSetChanged mOnDataSetChanged = new PlaylistAdapter.IOnDataSetChanged()
    {
        @Override
        public void onDataSetChanged()
        {
            updatePlaylistHeaderVisibility();
        }
    };

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
        mPlaylistAdapter.setOnDataSetChanged(mOnDataSetChanged);

        mHeader = getView().findViewById(R.id.static_footer_header);
        mHeaderText = (TextView) mHeader.findViewById(R.id.static_folder_header_text);

        RecyclerView list = (RecyclerView) getView().findViewById(R.id.listView);
        list.addOnScrollListener(mOnListScrollListener);

        mPlaylistAdapter.setOnItemClickListener(this);
        list.setAdapter(mPlaylistAdapter);
        mLayoutManager = new LinearLayoutManager(getActivity());
        list.setLayoutManager(mLayoutManager);

        updatePlaylistHeaderVisibility();
        if(mScrollOnCreateToPosition >= 0)
        {
            scrollTo(mScrollOnCreateToPosition);
            mScrollOnCreateToPosition = -1;
        }
        mPlaylistAdapter.bind();
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
            mPlaylistAdapter.notifyDataSetChanged();
        }
    }

    public boolean isItemVisible(int position)
    {
        int firstVisiblePos = mLayoutManager.findFirstCompletelyVisibleItemPosition();
        int lastVisiblePos = mLayoutManager.findLastCompletelyVisibleItemPosition();

        if(firstVisiblePos == RecyclerView.NO_POSITION || lastVisiblePos == RecyclerView.NO_POSITION)
            return false;

        return position >= firstVisiblePos && position <= lastVisiblePos;
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
    public void onStart()
    {
        super.onStart();
        mPlaylistAdapter.bind();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        mPlaylistAdapter.unbind();
    }

    public void onScreen()
    {
    }

    public void offScreen()
    {
    }

}