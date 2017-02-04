package com.juztoss.rhythmo.views.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

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
    @BindView(R.id.static_footer_header) protected View mHeader;
    @BindView(R.id.hint) protected View mHintNoSongs;
    @BindView(R.id.hintFilterEnabled) protected View mHintNoSongsIfFiltered;
    @BindView(R.id.progressIndicator) protected ProgressBar mProgrssIndicator;
    @BindView(R.id.static_folder_header_text) protected volatile TextView mHeaderText;
    @BindView(R.id.listView) protected RecyclerView mList;
    private Unbinder mUnbinder;

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

    private void updatePlaylistHeaderAndHintVisibility()
    {
        if (mPlaylistAdapter.getSortType() != SortType.DIRECTORY || mPlaylistAdapter.getItemCount() <= 1)//1 - for the fake item
            mHeader.setVisibility(View.GONE);
        else
        {
            mHeader.setVisibility(View.VISIBLE);
            if(getView() != null)
                updatePlaylistHeader(mList);
        }

        if(mPlaylistAdapter.getItemCount() <= 1)
        {
            if(mApp.getPlaylists().get(mPlaylistIndex).getSource().isModifyAvailable())
            {
                mHintNoSongs.setVisibility(!mApp.isBpmFilterEnabled() ? View.VISIBLE : View.GONE);
                mHintNoSongsIfFiltered.setVisibility(mApp.isBpmFilterEnabled()? View.VISIBLE : View.GONE);
                mProgrssIndicator.setVisibility(View.GONE);
            }
            else
            {
                mHintNoSongs.setVisibility(View.GONE);
                mHintNoSongsIfFiltered.setVisibility(View.GONE);
                mProgrssIndicator.setVisibility(mApp.isBuildingLibrary() ? View.VISIBLE : View.GONE);
            }
        }
        else
        {
            mHintNoSongs.setVisibility(View.GONE);
            mHintNoSongsIfFiltered.setVisibility(View.GONE);
            mProgrssIndicator.setVisibility(View.GONE);
        }

    }

    @Override
    public void onDestroyView()
    {
        mList.removeOnScrollListener(mOnListScrollListener);
        if(mUnbinder != null)
            mUnbinder.unbind();

        super.onDestroyView();
    }

    private PlaylistAdapter.IOnDataSetChanged mOnDataSetChanged = () -> updatePlaylistHeaderAndHintVisibility();

    public static PlaylistFragment newInstance(int playlistIndex)
    {
        PlaylistFragment fragment = new PlaylistFragment();
        Bundle args = new Bundle();
        args.putInt(PLAYLIST_INDEX, playlistIndex);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        mUnbinder = ButterKnife.bind(this, view);
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

        mList.addOnScrollListener(mOnListScrollListener);

        mPlaylistAdapter.setOnItemClickListener(this);
        mList.setAdapter(mPlaylistAdapter);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mList.setLayoutManager(mLayoutManager);

        updatePlaylistHeaderAndHintVisibility();
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
            i.putExtra(PlaybackService.ACTION_SONG_ID, composition.id());
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
    public void onResume()
    {
        super.onResume();
        mPlaylistAdapter.bind();
        mPlaylistAdapter.onPlaylistUpdated();
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