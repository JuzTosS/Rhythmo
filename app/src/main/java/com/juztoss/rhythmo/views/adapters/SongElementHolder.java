package com.juztoss.rhythmo.views.adapters;

import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
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
    private TextView mFirstLine;
    private TextView mSecondLine;
    private TextView mBpmLabel;
    private View mPlayingState;
    private int mPosition;
    private View mHeader;
    private TextView mHeaderLabel;
    private LinearLayout mRoot;
    private IOnItemClickListener mListener;
    private PopupMenu mPopupMenu;
    private boolean mIsFolderHeader;
    private String mFolderName;

    public SongElementHolder(View row, View header, IOnItemClickListener listener, boolean isModifyAvailable)
    {
        super(row);
        row.setTag(this);
        mHeader = header;
        mApp = ((RhythmoApp) itemView.getContext().getApplicationContext());
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
        mRoot = (LinearLayout) itemView.findViewById(R.id.song_list_root);
        mHeaderLabel = (TextView) mHeader.findViewById(R.id.folder_header_text);
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
        boolean visible = service != null && service.currentSongId() == composition.id() && service.isPlaying();
        mPlayingState.setVisibility(visible ? View.VISIBLE : View.GONE);

        mFolderName = composition.getFolder();
        mHeaderLabel.setText(mFolderName);

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
