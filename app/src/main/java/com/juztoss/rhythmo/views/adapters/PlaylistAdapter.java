package com.juztoss.rhythmo.views.adapters;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.juztoss.rhythmo.R;
import com.juztoss.rhythmo.models.Composition;
import com.juztoss.rhythmo.models.Playlist;
import com.juztoss.rhythmo.models.songsources.SortType;
import com.juztoss.rhythmo.presenters.RhythmoApp;
import com.juztoss.rhythmo.services.PlaybackService;
import com.juztoss.rhythmo.views.activities.PlayerActivity;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

/**
 * Created by JuzTosS on 4/20/2016.
 */
public class PlaylistAdapter extends RecyclerView.Adapter<SongElementHolder> implements Playlist.IUpdateListener, IOnItemClickListener, FastScrollRecyclerView.SectionedAdapter
{
    private RhythmoApp mApp;
    private PlayerActivity mActivity;
    private Playlist mPlaylist;
    private Cursor mCursor;

    BroadcastReceiver mUpdateUIReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            onPlaylistUpdated();
        }
    };
    private IOnItemClickListener mOnItemClickListener;
    private IOnDataSetChanged mDataSetChanged;

    public PlaylistAdapter(PlayerActivity activity, Playlist playlist)
    {
        super();
        mPlaylist = playlist;
        mActivity = activity;
        mApp = (RhythmoApp) activity.getApplicationContext();
    }


    @Override
    public void onPlaylistItemClick(int position, int action, Composition composition)
    {
        if (mOnItemClickListener != null)
        {
            mOnItemClickListener.onPlaylistItemClick(position, action, composition);
        }
    }

    @Override
    public SongElementHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = (LayoutInflater.from(mActivity));
        View row = inflater.inflate(R.layout.song_list_element, null);
        View header = inflater.inflate(R.layout.song_list_folder_header, null);
        return new SongElementHolder(row, header, this, mPlaylist.getSource().isModifyAvailable());
    }

    @Override
    public void onBindViewHolder(SongElementHolder holder, int position)
    {
        if (position == getItemCount() - 1)
        {
            holder.setVisible(false);
            return;
        }

        holder.setVisible(true);

        Cursor cursor = getCursor();
        cursor.moveToPosition(position);
        Composition composition = Composition.fromCursor(cursor);
        if (composition == null)
        {
            holder.setVisible(false);
            return;
        }

        boolean folderMode = false;

        if (mPlaylist.getSource().getSortType() == SortType.DIRECTORY)
        {
            int prevPosition = position - 1;
            if (prevPosition < 0)
            {
                folderMode = true;
            }
            else
            {
                cursor.moveToPosition(prevPosition);
                Composition prevComposition = Composition.fromCursor(cursor);

                if (prevComposition != null)
                    folderMode = !prevComposition.getFolderPath().equals(composition.getFolderPath());
            }
        }
        holder.update(composition, position, mActivity.playbackService(), folderMode);
    }

    private Cursor getCursor()
    {
        if(mCursor == null)
            mCursor = mPlaylist.getCursor();

        return mCursor;
    }

    private void closeCursor()
    {
        if(mCursor != null)
            mCursor.close();

        mCursor = null;
    }

    @Override
    public int getItemCount()
    {
        return getCursor().getCount() + 1;
    }

    public void bind()
    {
        mPlaylist.addUpdateListener(this);
        LocalBroadcastManager.getInstance(mApp).registerReceiver(mUpdateUIReceiver, new IntentFilter(PlaybackService.UPDATE_UI_ACTION));
    }

    public void unbind()
    {
        closeCursor();
        mPlaylist.removeUpdateListener(this);
        LocalBroadcastManager.getInstance(mApp).unregisterReceiver(mUpdateUIReceiver);
    }

    @Override
    public void onPlaylistUpdated()
    {
        closeCursor();
        notifyDataSetChanged();
        mActivity.updateTabNames();

        if(mDataSetChanged != null)
            mDataSetChanged.onDataSetChanged();
    }

    @NonNull
    @Override
    public String getSectionName(int position)
    {
        Cursor cursor = getCursor();
        if(position >= cursor.getCount()-1)
            position--;

        cursor.moveToPosition(position);
        Composition composition = Composition.fromCursor(cursor);

        if(composition == null)
            return "";

        if(getSortType() == SortType.DIRECTORY)
            return "";
        else if(getSortType() == SortType.LAST)
            return "";
        else if(getSortType() == SortType.NAME)
            return composition.name().substring(0,1).toUpperCase();
        else//getSortType() == SortType.BPM
            return Integer.toString((int)composition.bpmShifted());
    }

    public void setOnItemClickListener(IOnItemClickListener onItemClickListener)
    {
        mOnItemClickListener = onItemClickListener;
    }

    public SortType getSortType()
    {
        return mPlaylist.getSource().getSortType();
    }

    public void setOnDataSetChanged(IOnDataSetChanged onDataSetChanged)
    {
        mDataSetChanged = onDataSetChanged;
    }

    public interface IOnDataSetChanged
    {
        void onDataSetChanged();
    }
}