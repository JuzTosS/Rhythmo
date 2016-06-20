package com.juztoss.bpmplayer.views;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.juztoss.bpmplayer.R;
import com.juztoss.bpmplayer.models.Composition;
import com.juztoss.bpmplayer.models.Playlist;
import com.juztoss.bpmplayer.presenters.BPMPlayerApp;
import com.juztoss.bpmplayer.services.PlaybackService;

/**
 * Created by JuzTosS on 4/20/2016.
 */
public class PlaylistAdapter extends RecyclerView.Adapter<SongElementHolder> implements Playlist.IUpdateListener, IOnItemClickListener
{
    private BPMPlayerApp mApp;
    private Context mContext;

    private Playlist mPlaylist;
    private Cursor mCurentCursor;

    BroadcastReceiver mUpdateUIReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            updateList();
        }
    };
    private IOnItemClickListener mOnItemClickListener;

    @Override
    public void onItemClick(Composition composition, int position)
    {
        if(mOnItemClickListener != null)
        {
            mOnItemClickListener.onItemClick(composition, position);
        }
    }

    @Override
    public SongElementHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = (LayoutInflater.from(mContext));
        View v = inflater.inflate(R.layout.song_list_element, null);
        return new SongElementHolder(v, this);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(SongElementHolder holder, int position)
    {
        if (position == getItemCount() - 1)
        {
            holder.setVisible(false);
            return;
        }

        holder.setVisible(true);

        mCurentCursor.moveToPosition(position);
        final long songId = mCurentCursor.getLong(0);
        Composition composition = mApp.getComposition(songId);
        if(composition == null)
        {
            holder.setVisible(false);
            return;
        }

        holder.update(composition, position);
    }

    @Override
    public int getItemCount()
    {
        return mCurentCursor.getCount() + 1;
    }

    public void updateList()
    {
        mCurentCursor = mPlaylist.getList();
        notifyDataSetChanged();
    }

    public PlaylistAdapter(Context context, Playlist playlist)
    {
        super();
        mCurentCursor = playlist.getList();
        mPlaylist = playlist;
        mContext = context;
        mApp = (BPMPlayerApp) context.getApplicationContext();
    }

    public void bind()
    {
        mPlaylist.addUpdateListener(this);
        LocalBroadcastManager.getInstance(mApp).registerReceiver(mUpdateUIReceiver, new IntentFilter(PlaybackService.UPDATE_UI_ACTION));
    }

    public void unbind()
    {
        mPlaylist.removeUpdateListener(this);
        LocalBroadcastManager.getInstance(mApp).unregisterReceiver(mUpdateUIReceiver);
    }

    @Override
    public void onPlaylistUpdated()
    {
        updateList();
    }

    public void setOnItemClickListener(IOnItemClickListener onItemClickListener)
    {
        mOnItemClickListener = onItemClickListener;
    }
}