package com.juztoss.bpmplayer.views.adapters;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.juztoss.bpmplayer.R;
import com.juztoss.bpmplayer.models.Composition;
import com.juztoss.bpmplayer.presenters.BPMPlayerApp;
import com.juztoss.bpmplayer.services.PlaybackService;
import com.juztoss.bpmplayer.views.activities.SingleSongActivity;

import java.util.Locale;

/**
 * Created by JuzTosS on 6/18/2016.
 */
public class SongElementHolder extends RecyclerView.ViewHolder
{
    public static final int ACTION_PLAY = 0;
    public static final int ACTION_REMOVE = 1;
    public static final int ACTION_SHOW_DETAIL = 2;

    private BPMPlayerApp mApp;
    private Composition mComposition;
    private TextView mFirstLine;
    private TextView mSecondLine;
    private TextView mBpmLabel;
    private View mPlayingState;
    private int mPosition;
    private IOnItemClickListener mListener;
    PopupMenu mPopupMenu;

    public SongElementHolder(View view, IOnItemClickListener listener, boolean isModifyAvailable)
    {
        super(view);
        mApp = ((BPMPlayerApp) itemView.getContext().getApplicationContext());
        mListener = listener;
        itemView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (mListener != null)
                    mListener.onPlaylistItemClick(mPosition, ACTION_PLAY, mComposition);
            }
        });

        View menuButton = itemView.findViewById(R.id.menu_button);
        menuButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mPopupMenu.show();
            }
        });

        mPopupMenu = new PopupMenu(menuButton.getContext(), menuButton);

        mPopupMenu.inflate(R.menu.song_menu);
        mPopupMenu.getMenu().findItem(R.id.remove).setEnabled(isModifyAvailable);
        mPopupMenu.setOnMenuItemClickListener(mMenuClickListener);

        mFirstLine = (TextView) itemView.findViewById(R.id.first_line);
        mSecondLine = (TextView) itemView.findViewById(R.id.second_line);
        mBpmLabel = (TextView) itemView.findViewById(R.id.bpm_label);
        mPlayingState = itemView.findViewById(R.id.playing_state);
    }

    private final PopupMenu.OnMenuItemClickListener mMenuClickListener = new PopupMenu.OnMenuItemClickListener()
    {
        @Override
        public boolean onMenuItemClick(MenuItem item)
        {
            switch (item.getItemId())
            {
                case R.id.detail:
                    mListener.onPlaylistItemClick(mPosition, ACTION_SHOW_DETAIL, mComposition);
                    break;

                case R.id.remove:
                    if (mListener != null)
                        mListener.onPlaylistItemClick(mPosition, ACTION_REMOVE, mComposition);
                    break;
            }
            return true;
        }

    };

    public void update(Composition composition, int position, PlaybackService service)
    {
        mComposition = composition;
        mPosition = position;
        mFirstLine.setText(composition.name());

        mSecondLine.setText(composition.getFolder());

        if (Math.abs(composition.bpmShifted() - composition.bpm()) >= 0.001 || !mApp.isBPMInRange(composition.bpmShifted()))
            mBpmLabel.setTextColor(mApp.getResources().getColor(R.color.accentPrimary));
        else
            mBpmLabel.setTextColor(mApp.getResources().getColor(R.color.foreground));

        float bpm = mApp.getAvailableToPlayBPM(composition.bpmShifted());
        SpannableString spannableString = new SpannableString(String.format(Locale.US, "%.1f", bpm));
        int firstPartLength = Integer.toString((int) bpm).length();
        spannableString.setSpan(new AbsoluteSizeSpan(10, true), firstPartLength, spannableString.length(), 0);
        mBpmLabel.setText(spannableString);
        boolean visible = service != null && service.currentSongId() == composition.id();
        mPlayingState.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public void setVisible(boolean visible)
    {
        itemView.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }
}
