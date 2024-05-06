package com.juztoss.rhythmo.views.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.juztoss.rhythmo.R;
import com.juztoss.rhythmo.models.Composition;
import com.juztoss.rhythmo.models.Playlist;
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
public class PlaylistFragment extends Fragment implements IOnItemClickListener, IPlaylistFragment
{
    public static String PLAYLIST_INDEX = "PlaylistID";
    private int mPlaylistIndex;

    private PlaylistAdapter mPlaylistAdapter;

    private RhythmoApp mApp;
    private LinearLayoutManager mLayoutManager;
    private Composition mScrollOnCreate;
    protected View mHeader;
    protected View mHintNoSongs;
    protected View mHintNoSongsIfFiltered;
    protected ProgressBar mProgrssIndicator;
    protected volatile TextView mHeaderText;
    protected RecyclerView mList;

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
        mList.removeOnScrollListener(mOnListScrollListener);;

        super.onDestroyView();
    }

    private PlaylistAdapter.IOnDataSetChanged mOnDataSetChanged = this::updatePlaylistHeaderAndHintVisibility;

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

        mHeader = view.findViewById(R.id.static_footer_header);
        mHintNoSongs = view.findViewById(R.id.hint);
        mHintNoSongsIfFiltered = view.findViewById(R.id.hintFilterEnabled);
        mProgrssIndicator = view.findViewById(R.id.progressIndicator);
        mHeaderText = view.findViewById(R.id.static_folder_header_text);
        mList = view.findViewById(R.id.listView);
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

        if(mScrollOnCreate != null)
        {
            scrollTo(mScrollOnCreate);
            mScrollOnCreate = null;
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

    @Override
    public boolean isItemVisible(int position)
    {
        int firstVisiblePos = mLayoutManager.findFirstCompletelyVisibleItemPosition();
        int lastVisiblePos = mLayoutManager.findLastCompletelyVisibleItemPosition();

        if(firstVisiblePos == RecyclerView.NO_POSITION || lastVisiblePos == RecyclerView.NO_POSITION)
            return false;

        return position >= firstVisiblePos && position <= lastVisiblePos;
    }

    @Override
    public void scrollTo(Composition composition)
    {
        if(mLayoutManager != null) {
            int offset = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, mApp.getResources().getDisplayMetrics());
            int currentSongPos = Playlist.findPositionById(mPlaylistAdapter.getCursor(),
                    composition,
                    mApp.getPlaylists().get(mPlaylistIndex).getSource().getSortType());

            mLayoutManager.scrollToPositionWithOffset(currentSongPos, offset);
        }
        else
            mScrollOnCreate = composition;
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

    @Override
    public void onScreen()
    {
    }

    @Override
    public void offScreen()
    {
    }

}