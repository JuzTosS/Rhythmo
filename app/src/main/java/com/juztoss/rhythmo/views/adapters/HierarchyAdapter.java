package com.juztoss.rhythmo.views.adapters;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.juztoss.rhythmo.R;
import com.juztoss.rhythmo.models.BaseExplorerElement;
import com.juztoss.rhythmo.models.Composition;
import com.juztoss.rhythmo.models.MediaFolder;
import com.juztoss.rhythmo.models.ParentLink;
import com.juztoss.rhythmo.models.Playlist;
import com.juztoss.rhythmo.presenters.RhythmoApp;
import com.juztoss.rhythmo.services.PlaybackService;
import com.juztoss.rhythmo.views.activities.PlayerActivity;

import java.util.ArrayList;
import java.util.List;

import static com.juztoss.rhythmo.models.BaseExplorerElement.BACK_LINK;
import static com.juztoss.rhythmo.models.BaseExplorerElement.FOLDER_LINK;
import static com.juztoss.rhythmo.models.BaseExplorerElement.SINGLE_LINK;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by JuzTosS on 4/20/2016.
 */
public class HierarchyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Playlist.IUpdateListener
{
    private final RhythmoApp mApp;
    private List<BaseExplorerElement> mFoldersList = new ArrayList<>();
    private IOnItemClickListener mListener;
    private final PlayerActivity mActivity;
    private final Playlist mPlaylist;
    private Cursor mCursor;

    private BroadcastReceiver mUpdateUIReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getExtras() != null && intent.getBooleanExtra(PlaybackService.UPDATE_UI_LIST, false))
                onPlaylistUpdated();
        }
    };

    public HierarchyAdapter(IOnItemClickListener listener, PlayerActivity activity, Playlist playlist, RhythmoApp app) {
        this.mListener = listener;
        this.mActivity = activity;
        this.mPlaylist = playlist;
        this.mApp = app;
    }

    @Override
    public int getItemViewType(int position)
    {
        if (position < mFoldersList.size())
            return mFoldersList.get(position).type();
        else
            return SINGLE_LINK;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = (LayoutInflater.from(parent.getContext()));
        if (viewType == BACK_LINK)
        {
            View v = inflater.inflate(R.layout.hierarchy_back_element, parent, false);
            return new HierarchyBackElementHolder(v, mListener);
        }
        else if (viewType == FOLDER_LINK)
        {
            View v = inflater.inflate(R.layout.hierarchy_folder_element, parent, false);
            return new HierarchyFolderElementHolder(v, mListener);
        } else
        {
            View row = inflater.inflate(R.layout.song_list_element, null);
            View header = inflater.inflate(R.layout.song_list_folder_header, null);
            return new SongElementHolder(row, header, mListener, false);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
    {
        int viewType = getItemViewType(position);
        if (viewType == BACK_LINK)
        {
            ((HierarchyBackElementHolder) holder).update((ParentLink) mFoldersList.get(position), mActivity.playbackService());
        }
        else if (viewType == FOLDER_LINK)
        {
            ((HierarchyFolderElementHolder) holder).update((MediaFolder) mFoldersList.get(position), mActivity.playbackService());
        }
        else// if (viewType == SINGLE_LINK)
        {
            getCursor().moveToPosition(position - mFoldersList.size());
            ((SongElementHolder) holder).update(Composition.fromCursor(getCursor()), mActivity.playbackService(), false);
        }
    }

    public Cursor getCursor() {
        if (mCursor == null)
            mCursor = mPlaylist.getCursor();

        return mCursor;
    }

    private void closeCursor() {
        if (mCursor != null)
            mCursor.close();

        mCursor = null;
    }

    @Override
    public int getItemCount()
    {
        int listSize = mFoldersList != null ? mFoldersList.size() : 0;
        return listSize + getCursor().getCount() + 1;
    }

    public void update(List<BaseExplorerElement> list)
    {
        mFoldersList = list;
        onPlaylistUpdated();
    }

    public List<BaseExplorerElement> getList()
    {
        return mFoldersList;
    }

    public void bind() {
        mPlaylist.addUpdateListener(this);
        LocalBroadcastManager.getInstance(mApp).registerReceiver(mUpdateUIReceiver, new IntentFilter(PlaybackService.UPDATE_UI_ACTION));
    }

    public void unbind() {
        closeCursor();
        mPlaylist.removeUpdateListener(this);
        LocalBroadcastManager.getInstance(mApp).unregisterReceiver(mUpdateUIReceiver);
    }

    @Override
    public void onPlaylistUpdated() {
        closeCursor();
        notifyDataSetChanged();
    }
}