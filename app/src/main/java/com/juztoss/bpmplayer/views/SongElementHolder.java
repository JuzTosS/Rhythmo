package com.juztoss.bpmplayer.views;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.juztoss.bpmplayer.R;
import com.juztoss.bpmplayer.models.Composition;
import com.juztoss.bpmplayer.presenters.BPMPlayerApp;
import com.juztoss.bpmplayer.services.PlaybackService;

/**
 * Created by JuzTosS on 6/18/2016.
 */
public class SongElementHolder extends RecyclerView.ViewHolder implements View.OnClickListener
{
    private Composition mComposition;
    private TextView mFirstLine;
    private TextView mSecondLine;
    private TextView mBpmLabel;
    private View mPlayingState;
    private int mPosition;
    private IOnItemClickListener mListener;


    @Override
    public void onClick(View v)
    {
        Intent intent = new Intent(itemView.getContext(), SingleSongActivity.class);
        intent.putExtra(SingleSongActivity.SONG_ID, mComposition.id());
        itemView.getContext().startActivity(intent);
    }

    public SongElementHolder(View view, IOnItemClickListener listener)
    {
        super(view);
        mListener = listener;
        itemView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(mListener != null)
                    mListener.onItemClick(mPosition);
            }
        });
        itemView.findViewById(R.id.bpm_touch_label).setOnClickListener(this);
        mFirstLine = (TextView) itemView.findViewById(R.id.first_line);
        mSecondLine = (TextView) itemView.findViewById(R.id.second_line);
        mBpmLabel = (TextView) itemView.findViewById(R.id.bpm_label);
        mPlayingState = itemView.findViewById(R.id.playing_state);
    }

    @SuppressLint("DefaultLocale")
    public void update(Composition composition, int position)
    {
        mComposition = composition;
        mPosition = position;
        mFirstLine.setText(composition.name());

        if(Math.abs(composition.bpmShifted() - composition.bpm()) >= 0.001)
        {
            mSecondLine.setText(String.format(composition.getFolder() + " (orig. bpm: %.1f)", composition.bpm()));
        }
        else
            mSecondLine.setText(composition.getFolder());

        SpannableString spannableString = new SpannableString(String.format("%.1f", composition.bpmShifted()));
        int firstPartLength = Integer.toString((int)composition.bpmShifted()).length();
        spannableString.setSpan(new AbsoluteSizeSpan(10, true), firstPartLength, spannableString.length(), 0);
        mBpmLabel.setText(spannableString);
        BPMPlayerApp app = ((BPMPlayerApp)itemView.getContext().getApplicationContext());
        if (app.isPlaybackServiceRunning())
        {
            boolean visible = app.getPlaybackService().currentSongId() == composition.id();
            mPlayingState.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    public void setVisible(boolean visible)
    {
        itemView.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }
}
