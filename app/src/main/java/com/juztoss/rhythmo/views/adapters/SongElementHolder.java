package com.juztoss.rhythmo.views.adapters;

import android.graphics.drawable.AnimationDrawable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.style.AbsoluteSizeSpan;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.juztoss.rhythmo.R;
import com.juztoss.rhythmo.models.Composition;
import com.juztoss.rhythmo.presenters.RhythmoApp;
import com.juztoss.rhythmo.services.PlaybackService;
import com.juztoss.rhythmo.utils.SystemHelper;

import java.util.Locale;


/**
 * Created by JuzTosS on 6/18/2016.
 */
public class SongElementHolder extends RecyclerView.ViewHolder
{
    public static final int ACTION_PLAY = 0;
    public static final int ACTION_REMOVE = 1;
    public static final int ACTION_SHOW_DETAIL = 2;
    public static final int ACTION_OPEN = 3;

    private RhythmoApp mApp;
    private Composition mComposition;
    private final View mHeader;
    private final PopupMenu mPopupMenu;
    private IOnItemClickListener mListener;
    private boolean mIsFolderHeader;
    private String mFolderName;

    private AnimationDrawable mPlaybackAnimation;
    protected TextView mHeaderLabel;
    protected TextView mFirstLine;
    protected TextView mSecondLine;
    protected TextView mBpmLabel;
    protected View mPlayingState;
    protected LinearLayout mRoot;

    public SongElementHolder(View row, View header, IOnItemClickListener listener, boolean isModifyAvailable)
    {
        super(row);

        mFirstLine = itemView.findViewById(R.id.first_line);
        mSecondLine = itemView.findViewById(R.id.second_line);
        mBpmLabel = itemView.findViewById(R.id.bpm_label);
        mPlayingState = itemView.findViewById(R.id.playing_state);
        mRoot = itemView.findViewById(R.id.song_list_root);

        row.setTag(this);
        mHeader = header;
        mApp = ((RhythmoApp) itemView.getContext().getApplicationContext());
        mListener = listener;
        itemView.setOnClickListener(v -> {
            if (mListener != null)
                mListener.onPlaylistItemClick(getAdapterPosition(), ACTION_PLAY, mComposition);
        });

        View menuButton = itemView.findViewById(R.id.menu_button);
        mPopupMenu = new PopupMenu(menuButton.getContext(), menuButton);
        menuButton.setOnClickListener(v -> mPopupMenu.show());

        mPopupMenu.inflate(R.menu.song_menu);
        mPopupMenu.getMenu().findItem(R.id.remove).setEnabled(isModifyAvailable);
        mPopupMenu.setOnMenuItemClickListener(mMenuClickListener);

        mHeaderLabel = (TextView) mHeader.findViewById(R.id.folder_header_text);
        mPlaybackAnimation = (AnimationDrawable) mPlayingState.getBackground();
    }

    private final PopupMenu.OnMenuItemClickListener mMenuClickListener = new PopupMenu.OnMenuItemClickListener()
    {
        @Override
        public boolean onMenuItemClick(MenuItem item)
        {
            switch (item.getItemId())
            {
                case R.id.detail:
                    mListener.onPlaylistItemClick(getAdapterPosition(), ACTION_SHOW_DETAIL, mComposition);
                    break;

                case R.id.remove:
                    if (mListener != null)
                        mListener.onPlaylistItemClick(getAdapterPosition(), ACTION_REMOVE, mComposition);
                    break;
            }
            return true;
        }

    };

    public void update(Composition composition, PlaybackService service, boolean folderMode)
    {
        if(composition == null) {
            itemView.setVisibility(View.INVISIBLE);
            return;
        } else {
            itemView.setVisibility(View.VISIBLE);
        }

        mComposition = composition;
        mFirstLine.setText(composition.name());

        mSecondLine.setText(formatLength(composition.getLength()) + " | " + composition.getFolder());

        if (Math.abs(composition.bpmShifted() - composition.bpm()) >= 0.001 || !mApp.isBPMInRange(composition.bpmShifted()))
            mBpmLabel.setTextColor(SystemHelper.getColor(itemView.getContext(), R.attr.rAccentPrimary));
        else
            mBpmLabel.setTextColor(SystemHelper.getColor(itemView.getContext(), R.attr.rForeground));

        float bpm = mApp.getAvailableToPlayBPM(composition.bpmShifted());
        SpannableString spannableString = new SpannableString(String.format(Locale.US, "%.1f", bpm));
        int firstPartLength = Integer.toString((int) bpm).length();
        spannableString.setSpan(new AbsoluteSizeSpan(10, true), firstPartLength, spannableString.length(), 0);
        mBpmLabel.setText(spannableString);
        boolean isCurrentlyPlaying = service != null && service.getCurrentSongId() == composition.id() && service.isPlaying();
        mPlayingState.setVisibility(isCurrentlyPlaying ? View.VISIBLE : View.GONE);
        if(isCurrentlyPlaying)
            mPlaybackAnimation.start();
        else
            mPlaybackAnimation.stop();

        mFolderName = composition.getFolder();
        mHeaderLabel.setText(mFolderName);

        mFirstLine.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        mFirstLine.setMarqueeRepeatLimit(-1);
        mFirstLine.setSingleLine(true);
        mFirstLine.setHorizontalFadingEdgeEnabled(true);
        mFirstLine.setSelected(isCurrentlyPlaying);

        if (folderMode)
            addFolder();
        else
            removeFolder();
    }

    private String formatLength(int length) {
        return DateUtils.formatElapsedTime(length / 1000);
    }

    private void removeFolder()
    {
        if (mRoot.getChildAt(0) == mHeader)
        {
            mIsFolderHeader = false;
            mRoot.removeViewAt(0);
            mRoot.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, mApp.getResources().getDisplayMetrics());
        }
    }

    private void addFolder()
    {
        if (mRoot.getChildAt(0) != mHeader)
        {
            mIsFolderHeader = true;
            mRoot.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 90, mApp.getResources().getDisplayMetrics());
            mRoot.addView(mHeader, 0);
        }
    }

    public void setVisible(boolean visible)
    {
        itemView.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }

    public boolean isFolderHeader()
    {
        return mIsFolderHeader;
    }

    public String getFolderName()
    {
        return mFolderName;
    }
}
