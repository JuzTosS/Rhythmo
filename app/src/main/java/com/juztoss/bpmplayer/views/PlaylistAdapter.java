package com.juztoss.bpmplayer.views;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.juztoss.bpmplayer.R;
import com.juztoss.bpmplayer.models.Composition;
import com.juztoss.bpmplayer.models.Playlist;
import com.juztoss.bpmplayer.presenters.BPMPlayerApp;
import com.juztoss.bpmplayer.services.PlaybackService;

/**
 * Created by JuzTosS on 4/20/2016.
 */
public class PlaylistAdapter extends CursorAdapter implements Playlist.IUpdateListener
{
    private BPMPlayerApp mApp;
    private Context mContext;

    private Playlist mPlaylist;

    BroadcastReceiver mUpdateUIReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            updateList();
        }
    };

    private void updateList()
    {
        swapCursor(mPlaylist.getList());
        notifyDataSetChanged();
    }

    public PlaylistAdapter(Context context, Playlist playlist)
    {
        super(context, playlist.getList(), false);
        mPlaylist = playlist;
        mContext = context;
        mApp = (BPMPlayerApp) context.getApplicationContext();
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent)
    {
        LayoutInflater inflater = (LayoutInflater.from(mContext));
        View v = inflater.inflate(R.layout.song_list_element, null);
        bindView(v, context, cursor);
        return v;
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void bindView(View view, Context context, Cursor cursor)
    {
        final long songId = cursor.getLong(0);
        Composition composition = mApp.getComposition(songId);

        TextView firstLine = (TextView) view.findViewById(R.id.first_line);
        firstLine.setText(composition.name());

        TextView secondLine = (TextView) view.findViewById(R.id.second_line);
        secondLine.setText(String.format("%.1f (%.1f)", composition.bpmShifted(), composition.bpm()));


        ImageView infoButton = (ImageView) view.findViewById(R.id.info_button);
        infoButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(mContext, SingleSongActivity.class);
                intent.putExtra(SingleSongActivity.SONG_ID, songId);
                mContext.startActivity(intent);
            }
        });

        View playingState = view.findViewById(R.id.playing_state);
        playingState.setVisibility(View.INVISIBLE);
        if (mApp.isPlaybackServiceRunning())
        {
            PlaybackService service = mApp.getPlaybackService();
            if (service.getCurrentSongIndex() == cursor.getPosition())
            {
                playingState.setVisibility(View.VISIBLE);
                playingState.setSelected(!service.isPlaying());
            }

        }
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
}