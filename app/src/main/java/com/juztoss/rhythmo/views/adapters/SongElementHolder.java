package com.juztoss.rhythmo.views.adapters;

import android.graphics.drawable.AnimationDrawable;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.juztoss.rhythmo.R;
import com.juztoss.rhythmo.models.Composition;
import com.juztoss.rhythmo.presenters.RhythmoApp;
import com.juztoss.rhythmo.services.PlaybackService;
import com.juztoss.rhythmo.utils.SystemHelper;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by JuzTosS on 6/18/2016.
 */
public class SongElementHolder extends RecyclerView.ViewHolder
{
    public static final int ACTION_PLAY = 0;
    public static final int ACTION_REMOVE = 1;
    public static final int ACTION_SHOW_DETAIL = 2;

    private RhythmoApp mApp;
    private Composition mComposition;
    private final View mHeader;
    private int mPosition;
    private final PopupMenu mPopupMenu;
    private IOnItemClickListener mListener;
    private boolean mIsFolderHeader;
    private String mFolderName;

    private AnimationDrawable mPlaybackAnimation;
    protected TextView mHeaderLabel;
    @BindView(R.id.first_line) protected TextView mFirstLine;
    @BindView(R.id.second_line) protected TextView mSecondLine;
    @BindView(R.id.bpm_label) protected TextView mBpmLabel;
    @BindView(R.id.playing_state) protected View mPlayingState;
    @BindView(R.id.song_list_root) protected LinearLayout mRoot;

    public SongElementHolder(View row, View header, IOnItemClickListener listener, boolean isModifyAvailable)
    {
        super(row);
        ButterKnife.bind(this, itemView);
        row.setTag(this);
        mHeader = header;
        mApp = ((RhythmoApp) itemView.getContext().getApplicationContext());
        mListener = listener;
        itemView.setOnClickListener(v -> {
            if (mListener != null)
                mListener.onPlaylistItemClick(mPosition, ACTION_PLAY, mComposition);
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

    public void update(Composition composition, int position, PlaybackService service, boolean folderMode)
    {
        mComposition = composition;
        mPosition = position;
        mFirstLine.setText(composition.name());

        mSecondLine.setText(composition.getFolder());

        if (Math.abs(composition.bpmShifted() - composition.bpm()) >= 0.001 || !mApp.isBPMInRange(composition.bpmShifted()))
            mBpmLabel.setTextColor(SystemHelper.getColor(itemView.getContext(), R.attr.rAccentPrimary));
        else
            mBpmLabel.setTextColor(SystemHelper.getColor(itemView.getContext(), R.attr.rForeground));

        float bpm = mApp.getAvailableToPlayBPM(composition.bpmShifted());
        SpannableString spannableString = new SpannableString(String.format(Locale.US, "%.1f", bpm));
        int firstPartLength = Integer.toString((int) bpm).length();
        spannableString.setSpan(new AbsoluteSizeSpan(10, true), firstPartLength, spannableString.length(), 0);
        mBpmLabel.setText(spannableString);
        boolean isCurrentlyPlaying = service != null && service.currentSongId() == composition.id() && service.isPlaying();
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
