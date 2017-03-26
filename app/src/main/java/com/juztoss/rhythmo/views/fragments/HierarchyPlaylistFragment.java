package com.juztoss.rhythmo.views.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.juztoss.rhythmo.R;
import com.juztoss.rhythmo.models.BaseExplorerElement;
import com.juztoss.rhythmo.models.Composition;
import com.juztoss.rhythmo.models.Playlist;
import com.juztoss.rhythmo.presenters.BrowserPresenter;
import com.juztoss.rhythmo.presenters.RhythmoApp;
import com.juztoss.rhythmo.services.PlaybackService;
import com.juztoss.rhythmo.views.activities.PlayerActivity;
import com.juztoss.rhythmo.views.activities.SingleSongActivity;
import com.juztoss.rhythmo.views.adapters.HierarchyAdapter;
import com.juztoss.rhythmo.views.adapters.IOnItemClickListener;
import com.juztoss.rhythmo.views.adapters.SongElementHolder;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.juztoss.rhythmo.views.fragments.PlaylistFragment.PLAYLIST_INDEX;

/**
 * Created by JuzTosS on 3/5/2017.
 */

public class HierarchyPlaylistFragment extends Fragment implements IPlaylistFragment, BrowserPresenter.OnDataChangedListener, IOnItemClickListener
{
    @BindView(R.id.progressIndicator)
    protected ProgressBar mProgressIndicator;
    @BindView(R.id.listView)
    protected RecyclerView mListView;
    @BindView(R.id.folderPathLabel)
    protected TextView mFolderPathLabel;

    private HierarchyAdapter mAdapter;
    private RhythmoApp mApp;
    private Unbinder mUnbinder;
    private int mPlaylistIndex;
    private LinearLayoutManager mLayoutManager;
    private Composition mScrollOnCreate;

    public static HierarchyPlaylistFragment newInstance(int playlistIndex)
    {
        HierarchyPlaylistFragment fragment = new HierarchyPlaylistFragment();
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

        mAdapter = new HierarchyAdapter(this, (PlayerActivity) getActivity(), mApp.getPlaylists().get(playlistIndex), mApp);
        mListView.setAdapter(mAdapter);
        mLayoutManager = new LinearLayoutManager(getContext());
        mListView.setLayoutManager(mLayoutManager);

        mApp.getBrowserPresenter().addOnDataChangedListener(this);
    }

    private void showSongActivity(Composition composition)
    {
        Intent intent = new Intent(getContext(), SingleSongActivity.class);
        intent.putExtra(SingleSongActivity.SONG_ID, composition.id());
        getContext().startActivity(intent);
    }

    @Override
    public void onPlaylistItemClick(int position, int action, Composition composition) {
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
        else if (action == SongElementHolder.ACTION_OPEN)
        {
            BaseExplorerElement element = mAdapter.getList().get(position);
            mApp.getBrowserPresenter().setCurrent(element);
            Bundle args = new Bundle();
            args.putBoolean(BrowserPresenter.ONLY_FOLDERS, true);
            getLoaderManager().restartLoader(0, args, mApp.getBrowserPresenter());
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        mUnbinder = ButterKnife.bind(this, view);
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();

        if (mUnbinder != null) {
            mUnbinder.unbind();
            mUnbinder = null;
        }
    }

    @Override
    public void onStart()
    {
        super.onStart();

        mProgressIndicator.setVisibility(View.VISIBLE);
        mListView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onDataChanged()
    {
        if(mUnbinder == null) return;//The view is destroyed

        mProgressIndicator.setVisibility(View.GONE);
        mListView.setVisibility(View.VISIBLE);

        mApp.getBrowserPresenter().storeCurrentElement();

        mAdapter.update(mApp.getBrowserPresenter().getList());
        mFolderPathLabel.setText(mApp.getBrowserPresenter().getCurrent().getFileSystemPath());

        if (mScrollOnCreate != null)
        {
            int offset = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, mApp.getResources().getDisplayMetrics());
            int currentSongIndex = Playlist.findPositionById(mAdapter.getCursor(), mScrollOnCreate, mApp.getPlaylists().get(mPlaylistIndex).getSource().getSortType());
            mLayoutManager.scrollToPositionWithOffset(currentSongIndex + mAdapter.getList().size(), offset);
            mScrollOnCreate = null;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.hierarchy_fragment, container, false);
    }

    @Override
    public boolean isItemVisible(int position)
    {
        int positionWithFolders = position + mAdapter.getList().size();
        int firstVisiblePos = mLayoutManager.findFirstCompletelyVisibleItemPosition();
        int lastVisiblePos = mLayoutManager.findLastCompletelyVisibleItemPosition();

        if(firstVisiblePos == RecyclerView.NO_POSITION || lastVisiblePos == RecyclerView.NO_POSITION)
            return false;

        return positionWithFolders >= firstVisiblePos && positionWithFolders <= lastVisiblePos;
    }

    @Override
    public void scrollTo(Composition composition)
    {
        if(composition == null) return;

        mScrollOnCreate = composition;

        if(mApp == null)//Haven't created yet
            return;

        mApp.getBrowserPresenter().clear();
        mApp.getBrowserPresenter().setCurrent(
                mApp.getBrowserPresenter().getRoot().getChildFromPath(composition.getFolderPath(), true)
        );

        Bundle args = new Bundle();
        args.putBoolean(BrowserPresenter.ONLY_FOLDERS, true);
        getLoaderManager().restartLoader(0, args, mApp.getBrowserPresenter());
    }

    @Override
    public void onResume()
    {
        super.onResume();
        mAdapter.bind();
        mAdapter.onPlaylistUpdated();

        mApp.getBrowserPresenter().clear();
        mApp.getBrowserPresenter().setCurrent(mApp.getBrowserPresenter().restoreSavedElement());

        Bundle args = new Bundle();
        args.putBoolean(BrowserPresenter.ONLY_FOLDERS, true);
        getLoaderManager().initLoader(0, args, mApp.getBrowserPresenter());
    }

    @Override
    public void onPause()
    {
        super.onPause();
        mAdapter.unbind();
    }

    @Override
    public void onScreen() {

    }

    @Override
    public void offScreen() {

    }
}
