package com.juztoss.rhythmo.views.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.text.style.AbsoluteSizeSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.juztoss.rhythmo.R;
import com.juztoss.rhythmo.models.Composition;
import com.juztoss.rhythmo.models.Playlist;
import com.juztoss.rhythmo.models.songsources.AbstractSongsSource;
import com.juztoss.rhythmo.models.songsources.SortType;
import com.juztoss.rhythmo.presenters.RhythmoApp;
import com.juztoss.rhythmo.services.BuildMusicLibraryService;
import com.juztoss.rhythmo.services.PlaybackService;
import com.juztoss.rhythmo.views.adapters.TabsAdapter;
import com.juztoss.rhythmo.views.items.RangeSeekBar;

import java.util.List;
import java.util.Locale;

public class PlayerActivity extends BasePlayerActivity implements View.OnClickListener, ViewPager.OnPageChangeListener
{
    public static final String DISABLE_RESCAN_ON_LAUNCHING = "DisableRescanOnLaunching";

    private RhythmoApp mApp;
    private View mPlayButton;
    private ImageView mRepeatButton;
    private ImageView mShuffleButton;
    private TextView mTimePassed;
    private TextView mTimeLeft;
    private SeekBar mSeekbar;
    private RangeSeekBar<Integer> mRangeSeekbar;
    ViewPager mPlaylistsPager;
    private FloatingActionButton mFab;
    ActionBar mActionBar;
    private TextView mMinBPMField;
    private TextView mMaxBPMField;

    private boolean mIsSearchEnabled = false;
    private ActionBar.LayoutParams mActionBarLayoutParams = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
            ActionBar.LayoutParams.MATCH_PARENT);
    private View mSearchBarLayout;
    private View mActionBarLayout;
    private boolean mNeedToGoToTheCurrentSong = false;

    private int mLastViewedPlaylistIndex = -1;

    @Override
    public void onBackPressed()
    {
        if(mIsSearchEnabled)
            enableDefaultActionBarAndDisableSearch();
        else
            super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mApp = (RhythmoApp) getApplication();
        setContentView(R.layout.activity_main);
        createFab();
        setupPager();
        setupActionBar();
        setupAllOtherUI();

        LocalBroadcastManager.getInstance(mApp).registerReceiver(mUpdateUIReceiver, new IntentFilter(PlaybackService.UPDATE_UI_ACTION));
    }

    @Override
    protected void onServiceConnected()
    {
        super.onServiceConnected();
        updateAll(false);

        if(mNeedToGoToTheCurrentSong)
        {
            mNeedToGoToTheCurrentSong = false;
            gotoTheCurrentlyPlayingSong(false);
        }
    }

    private Playlist getCurrentViewedPlaylist()
    {
        return mApp.getPlaylists().get(mPlaylistsPager.getCurrentItem());
    }

    private void createFab()
    {
        mFab = (FloatingActionButton) findViewById(R.id.btnAddToPlaylist);
        mFab.setOnClickListener(this);
    }

    @Override
    public void onClick(View v)
    {
        //On fab clicked
        Intent intent = new Intent(this, SelectSongsActivity.class);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == RESULT_OK)
        {
            String[] foldersPaths = data.getStringArrayExtra(SelectSongsActivity.FOLDERS_PATHS);
            Playlist playlist = mApp.getPlaylists().get(mPlaylistsPager.getCurrentItem());
//            if(foldersPaths.length == 1)
//            {
//                playlist.setSource(SourcesFactory.createFolderSongSource(foldersPaths[0], mApp));
//                playlist.getSource().delete();
//            }
//            else
//            {
            AbstractSongsSource source = playlist.getSource();
            for (String path : foldersPaths)
            {
                source.add(mApp.getMusicLibraryHelper().getSongIdsCursor(path, true));
            }
//                playlist.setNeedRebuild();
//            }
        }
    }

    private void setupAllOtherUI()
    {
        mTimePassed = (TextView) findViewById(R.id.time_passed);
        mTimeLeft = (TextView) findViewById(R.id.time_left);
        mSeekbar = (SeekBar) findViewById(R.id.seekbar);
        mSeekbar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
        mRangeSeekbar = (RangeSeekBar<Integer>) findViewById(R.id.bpm_ranger);
        mRangeSeekbar.setOnRangeSeekBarChangeListener(mOnBpmRangeChanged);
        mRangeSeekbar.setNotifyWhileDragging(true);

        mMinBPMField = (TextView) findViewById(R.id.bpm_label_min);
        mMaxBPMField = (TextView) findViewById(R.id.bpm_label_max);
        mMinBPMField.setText(R.string.min);
        mMaxBPMField.setText(R.string.max);

        mRepeatButton = (ImageView) findViewById(R.id.repeat_button);
        mRepeatButton.setOnClickListener(mRepeatButtonListener);
        mShuffleButton = (ImageView) findViewById(R.id.shuffle_button);
        mShuffleButton.setOnClickListener(mShuffleButtonListener);

        mPlayButton = findViewById(R.id.play_button);
        mPlayButton.setOnClickListener(mPlayButtonListener);

        View nextButton = findViewById(R.id.next_button);
        nextButton.setOnClickListener(mNextButtonListener);
        View previousButton = findViewById(R.id.previous_button);
        previousButton.setOnClickListener(mPreviousButtonListener);
    }

    private void updateTabs()
    {
        int currentItem = mPlaylistsPager.getCurrentItem();
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.removeAllTabs();
        List<Playlist> playlists = mApp.getPlaylists();
        for (Playlist playlist : playlists)
        {
            tabLayout.addTab(tabLayout.newTab().setText(playlist.getName()));
        }


        TabsAdapter adapter = ((TabsAdapter) mPlaylistsPager.getAdapter());
        adapter.setNumOfLists(playlists.size());
        mPlaylistsPager.setAdapter(null);
        mPlaylistsPager.setAdapter(adapter);
        mPlaylistsPager.setCurrentItem(currentItem);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
    {

    }

    @Override
    public void onPageSelected(int position)
    {
        mLastViewedPlaylistIndex = position;
    }

    @Override
    public void onPageScrollStateChanged(int state)
    {

    }

    private void setupPager()
    {
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setTabGravity(TabLayout.GRAVITY_CENTER);

        mPlaylistsPager = (ViewPager) findViewById(R.id.pager);
        final TabsAdapter adapter = new TabsAdapter(getSupportFragmentManager(), mApp.getPlaylists().size());
        mPlaylistsPager.setAdapter(adapter);
        mPlaylistsPager.addOnPageChangeListener(adapter);
        mPlaylistsPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        mPlaylistsPager.addOnPageChangeListener(this);
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener()
        {
            @Override
            public void onTabSelected(TabLayout.Tab tab)
            {
                mPlaylistsPager.setCurrentItem(tab.getPosition());
                updateFab();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab)
            {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab)
            {

            }
        });

        updateTabs();
    }

    @Override
    protected void onDestroy()
    {
        LocalBroadcastManager.getInstance(mApp).unregisterReceiver(mUpdateUIReceiver);
        super.onDestroy();
    }

    private void updateFab()
    {
        if (getCurrentViewedPlaylist().getSource().isModifyAvailable())
        {
            mFab.show();
        }
        else
            mFab.hide();
    }

    @SuppressLint("InflateParams")
    private void setupActionBar()
    {
        mActionBarLayout = getLayoutInflater().inflate(R.layout.action_bar, null);
        mSearchBarLayout = getLayoutInflater().inflate(R.layout.search_bar, null);

        mActionBarLayout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                gotoTheCurrentlyPlayingSong(true);
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mActionBar = getSupportActionBar();
        mActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        if(getIntent().getExtras() == null || !getIntent().getBooleanExtra(DISABLE_RESCAN_ON_LAUNCHING, false))
        {
            Intent intent = new Intent(this, BuildMusicLibraryService.class);
            intent.putExtra(BuildMusicLibraryService.SILENT_MODE, true);
            intent.putExtra(BuildMusicLibraryService.DONT_INTERRUPT_EXIST_TASKS, true);
            intent.putExtra(BuildMusicLibraryService.DONT_DETECT_BPM, true);
            startService(intent);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    0);
        }
        else
        {
            tryToDoFirstRunService();
        }

        mNeedToGoToTheCurrentSong = !gotoTheCurrentlyPlayingSong(false);
        enableDefaultActionBarAndDisableSearch();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            tryToDoFirstRunService();
        }
    }

    private void tryToDoFirstRunService()
    {
        if (!mApp.getSharedPreferences().getBoolean(RhythmoApp.LIBRARY_BUILD_HAD_STARTED, false))
        {
            mApp.getSharedPreferences().edit().putBoolean(RhythmoApp.LIBRARY_BUILD_HAD_STARTED, true).apply();
            Intent intent = new Intent(getApplicationContext(), BuildMusicLibraryService.class);
            getApplicationContext().startService(intent);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        if (id == android.R.id.home)
        {
            enableDefaultActionBarAndDisableSearch();
        }
        if (id == R.id.settings_menu)
        {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.new_playlist_menu)
        {
            mApp.createNewPlaylist();
            updateTabs();
            mPlaylistsPager.setCurrentItem(mApp.getPlaylists().size() - 1);
        }
        else if (id == R.id.rename_playlist_menu)
        {
            launchRenameDialog();
        }
        else if (id == R.id.remove_playlist_menu)
        {
            mApp.removePlaylist(mPlaylistsPager.getCurrentItem());
            updateTabs();
        }
        else if (id == R.id.search_menu)
        {
            enableSearch();
        }
        else if (id == R.id.sort_menu)
        {
            launchSortDialog();
        }
        else if(id == R.id.detect_playlist_bpm)
        {
            rescanBPMForTheCurrentPlaylist();
        }
        return super.onOptionsItemSelected(item);
    }

    private void rescanBPMForTheCurrentPlaylist()
    {
        Intent intent = new Intent(this, BuildMusicLibraryService.class);
        intent.putExtra(BuildMusicLibraryService.PLAYLIST_INDEX, mPlaylistsPager.getCurrentItem());
        intent.putExtra(BuildMusicLibraryService.RESET_BPM, true);
        startService(intent);
    }

    private TextWatcher mSearchStringChanged = new TextWatcher()
    {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after)
        {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count)
        {
            getCurrentViewedPlaylist().setWordFilter(s.toString());
        }

        @Override
        public void afterTextChanged(Editable s)
        {

        }
    };

    private void enableSearch()
    {
        mIsSearchEnabled = true;
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowHomeEnabled(true);

        EditText editText = (EditText) mSearchBarLayout.findViewById(R.id.search_field);
        editText.addTextChangedListener(mSearchStringChanged);

        mActionBar.setCustomView(mSearchBarLayout, mActionBarLayoutParams);
        Toolbar parent = (Toolbar) mSearchBarLayout.getParent();
        parent.setContentInsetsAbsolute(0, 0);

        invalidateOptionsMenu();
        editText.requestFocus();
        final InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);

        getCurrentViewedPlaylist().setWordFilter(editText.getText().toString());
    }

    private void enableDefaultActionBarAndDisableSearch()
    {
        mIsSearchEnabled = false;
        mActionBar.setDisplayHomeAsUpEnabled(false);
        mActionBar.setDisplayShowHomeEnabled(false);

        EditText editText = (EditText) mSearchBarLayout.findViewById(R.id.search_field);
        editText.removeTextChangedListener(mSearchStringChanged);

        mActionBar.setCustomView(mActionBarLayout, mActionBarLayoutParams);
        Toolbar parent = (Toolbar) mActionBarLayout.getParent();
        parent.setContentInsetsAbsolute(0, 0);

        getCurrentViewedPlaylist().setWordFilter(null);

        invalidateOptionsMenu();
        View view = this.getCurrentFocus();
        if (view != null)
        {
            final InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
        gotoTheCurrentlyPlayingSong(false);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putInt("mLastViewedPlaylistIndex", mLastViewedPlaylistIndex);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        mLastViewedPlaylistIndex = savedInstanceState.getInt("mLastViewedPlaylistIndex");
    }

    private void showCurrentSongIfIsInFocusedPlaylist()
    {
        if (playbackService() == null) return;
        if(mPlaylistsPager.getCurrentItem() == playbackService().getCurrentPlaylistIndex())
        {
            TabsAdapter adapter = (TabsAdapter) mPlaylistsPager.getAdapter();
            if(!adapter.getCurrentFragment().isItemVisible(playbackService().getCurrentSongIndex()))
                gotoTheCurrentlyPlayingSong(true);
        }
    }

    /**
     *
     * @return true if successfully positioned on the currently playing song
     */
    private boolean gotoTheCurrentlyPlayingSong(boolean force)
    {
        if(!force && mLastViewedPlaylistIndex >= 0)
        {
            mPlaylistsPager.setCurrentItem(mLastViewedPlaylistIndex);
            return false;
        }

        TabsAdapter adapter = (TabsAdapter) mPlaylistsPager.getAdapter();

        if (playbackService() == null) return false;

        mPlaylistsPager.setCurrentItem(playbackService().getCurrentPlaylistIndex());
        int songPosition = playbackService().getCurrentSongIndex() - 1;
        adapter.getCurrentFragment().scrollTo(songPosition < 0 ? 0 : songPosition);
        return true;
    }

    private void launchRenameDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.rename_dialog_title));

        final EditText input = new EditText(this);
        final Playlist playlist = mApp.getPlaylists().get(mPlaylistsPager.getCurrentItem());
        input.setText(playlist.getName());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.selectAll();
        builder.setView(input);
        builder.setPositiveButton(getString(R.string.dialog_ok), new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                String newName = input.getText().toString();
                playlist.getSource().rename(newName);
                updateTabNames();
            }
        });
        builder.setNegativeButton(getString(R.string.dialog_cancel), new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.cancel();
            }
        });


        input.requestFocus();
        AlertDialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();
    }

    private void launchSortDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.sort_dialog_title));
        builder.setSingleChoiceItems(new String[]{
                        getString(R.string.sort_alphabetically),
                        getString(R.string.sort_by_bpm),
                        getString(R.string.sort_by_folders),
                        getString(R.string.sort_by_date)
                },
                getCurrentViewedPlaylist().getSource().getSortType().ordinal(),
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        getCurrentViewedPlaylist().getSource().setSortType(SortType.values()[which]);
                        dialog.cancel();
                    }
                });

        builder.setNegativeButton(getString(R.string.dialog_cancel), new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.cancel();
            }
        });

        builder.show();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        Playlist playlist = mApp.getPlaylists().get(mPlaylistsPager.getCurrentItem());
        menu.findItem(R.id.rename_playlist_menu).setEnabled(playlist.getSource().isRenameAvailable());
        menu.findItem(R.id.remove_playlist_menu).setEnabled(playlist.getSource().isDeleteAvailable());
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        if (mIsSearchEnabled)
            return false;

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.player_menu, menu);

        //Coloring icons
        MenuItem item = menu.findItem(R.id.search_menu);
        Drawable icon = item.getIcon();
        icon.setColorFilter(ContextCompat.getColor(this, R.color.foregroundInverted), PorterDuff.Mode.SRC_ATOP);

        item = menu.findItem(R.id.sort_menu);
        icon = item.getIcon();
        icon.setColorFilter(ContextCompat.getColor(this, R.color.foregroundInverted), PorterDuff.Mode.SRC_ATOP);

        return true;
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        updateAll(false);
    }

    BroadcastReceiver mUpdateUIReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if(intent.getExtras() != null && intent.getBooleanExtra(PlaybackService.UPDATE_UI_ACTION_SCROLL_TO_CURRENT, false))
                updateAll(true);
            else
                updateAll(false);
        }
    };

    protected void updateAll(boolean showCurrentSong)
    {
        mRangeSeekbar.setRangeValues(mApp.getSongsMinBpm(), mApp.getSongsMaxBpm());

        if (playbackService() == null) return;

        Composition composition = mApp.getComposition(playbackService().currentSongId());
        View actionBarHeader = mActionBarLayout.findViewById(R.id.action_bar_header);
        View actionBarPlaceholder = mActionBarLayout.findViewById(R.id.action_bar_placeholder);
        if (composition != null)
        {
            actionBarHeader.setVisibility(View.VISIBLE);
            actionBarPlaceholder.setVisibility(View.GONE);
            TextView bpmLabel = ((TextView) mActionBarLayout.findViewById(R.id.bpm_header_label));
            SpannableString spannableString = new SpannableString(String.format(Locale.US, "%.1f", playbackService().getCurrentlyPlayingBPM()));
            int firstPartLength = Integer.toString((int) composition.bpmShifted()).length();
            spannableString.setSpan(new AbsoluteSizeSpan(10, true), firstPartLength, spannableString.length(), 0);
            bpmLabel.setText(spannableString);
            ((TextView) mActionBarLayout.findViewById(R.id.first_header_line)).setText(composition.name());
            ((TextView) mActionBarLayout.findViewById(R.id.second_header_line)).setText(composition.getFolder());
        }
        else
        {
            actionBarHeader.setVisibility(View.GONE);
            actionBarPlaceholder.setVisibility(View.VISIBLE);
        }

        mPlayButton.setSelected(!playbackService().isPlaying());

        mSeekbar.setMax(playbackService().getDuration());
        mHandler.post(mSeekbarUpdateRunnable);
        updateShuffleAndRepeatButtons();

        if(showCurrentSong)
            showCurrentSongIfIsInFocusedPlaylist();
    }

    private Handler mHandler = new Handler();
    private Runnable mSeekbarUpdateRunnable = new Runnable()
    {
        public void run()
        {
            try
            {
                if (playbackService() == null)
                    return;

                mSeekbar.setProgress(playbackService().getCurrentPosition());

                if (playbackService().isPlaying())
                    mHandler.postDelayed(mSeekbarUpdateRunnable, 100);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    };

    private View.OnClickListener mRepeatButtonListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            if (playbackService() == null)
                return;

            if (playbackService().getRepeatMode() == PlaybackService.RepeatMode.DISABLED || playbackService().getRepeatMode() == PlaybackService.RepeatMode.SHUFFLE)
                playbackService().setRepeatMode(PlaybackService.RepeatMode.ALL);
            else if (playbackService().getRepeatMode() == PlaybackService.RepeatMode.ALL)
                playbackService().setRepeatMode(PlaybackService.RepeatMode.ONE);
            else
                playbackService().setRepeatMode(PlaybackService.RepeatMode.DISABLED);

            updateShuffleAndRepeatButtons();
            updateShuffleAndRepeatButtons();
        }
    };

    private View.OnClickListener mShuffleButtonListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            if (playbackService() == null)
                return;

            PlaybackService.RepeatMode newRepeatMode = playbackService().getRepeatMode() == PlaybackService.RepeatMode.SHUFFLE ? PlaybackService.RepeatMode.DISABLED : PlaybackService.RepeatMode.SHUFFLE;
            playbackService().setRepeatMode(newRepeatMode);
            updateShuffleAndRepeatButtons();
            updateShuffleAndRepeatButtons();
        }
    };

    private void updateShuffleAndRepeatButtons()
    {
        if (playbackService() == null || !playbackService().isShuffleEnabled())
            mShuffleButton.setColorFilter(ContextCompat.getColor(this, R.color.foregroundGrayedOut));
        else
            mShuffleButton.setColorFilter(ContextCompat.getColor(this, R.color.accentPrimary));

        if (playbackService() == null)
            return;

        if (playbackService().getRepeatMode() == PlaybackService.RepeatMode.ALL)
        {
            mRepeatButton.setColorFilter(ContextCompat.getColor(this, R.color.accentPrimary));
            mRepeatButton.setImageResource(R.drawable.ic_repeat);
        }
        else if (playbackService().getRepeatMode() == PlaybackService.RepeatMode.ONE)
        {
            mRepeatButton.setColorFilter(ContextCompat.getColor(this, R.color.accentPrimary));
            mRepeatButton.setImageResource(R.drawable.ic_repeat_once);
        }
        else
        {
            mRepeatButton.setColorFilter(ContextCompat.getColor(this, R.color.foregroundGrayedOut));
            mRepeatButton.setImageResource(R.drawable.ic_repeat);
        }
    }


    private View.OnClickListener mPlayButtonListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            Intent i = new Intent(v.getContext(), PlaybackService.class);
            i.setAction(PlaybackService.ACTION_COMMAND);
            i.putExtra(PlaybackService.ACTION_NAME, PlaybackService.SWITCH_PLAYBACK_ACTION);
            v.getContext().startService(i);
        }
    };


    private RangeSeekBar.OnRangeSeekBarChangeListener<Integer> mOnBpmRangeChanged = new RangeSeekBar.OnRangeSeekBarChangeListener<Integer>()
    {
        @Override
        public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer minValue, Integer maxValue)
        {
            onRangeSeekBarValuesMoved(bar, minValue, maxValue);
            if (minValue <= (Integer)bar.getAbsoluteMinValue() && maxValue >= (Integer)bar.getAbsoluteMaxValue())
            {
                mApp.setBPMRange(0, 0);
            }
            else
            {
                mApp.setBPMRange(minValue, maxValue);
            }
        }

        @Override
        public void onRangeSeekBarValuesMoved(RangeSeekBar<?> bar, Integer minValue, Integer maxValue)
        {
            if (minValue <= (Integer)bar.getAbsoluteMinValue() && maxValue >= (Integer)bar.getAbsoluteMaxValue())
            {
                mMinBPMField.setText(R.string.min);
                mMaxBPMField.setText(R.string.max);
            }
            else
            {
                mMinBPMField.setText(String.format(Locale.US, "%d", minValue));
                mMaxBPMField.setText(String.format(Locale.US, "%d", maxValue));
            }
        }
    };

    private SeekBar.OnSeekBarChangeListener mOnSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener()
    {
        @SuppressLint("SetTextI18n")
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
        {
            int timeLeft = seekBar.getMax() - (progress / 1000) * 1000;
            mTimePassed.setText(DateUtils.formatElapsedTime(progress / 1000));
            mTimeLeft.setText("-" + DateUtils.formatElapsedTime(timeLeft / 1000));

            if (playbackService() != null && fromUser)
            {
                playbackService().seekTo(progress);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar)
        {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar)
        {

        }
    };

    private View.OnClickListener mNextButtonListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            Intent i = new Intent(v.getContext(), PlaybackService.class);
            i.setAction(PlaybackService.ACTION_COMMAND);
            i.putExtra(PlaybackService.ACTION_NAME, PlaybackService.PLAY_NEXT_ACTION);
            v.getContext().startService(i);
        }
    };

    private View.OnClickListener mPreviousButtonListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            Intent i = new Intent(v.getContext(), PlaybackService.class);
            i.setAction(PlaybackService.ACTION_COMMAND);
            i.putExtra(PlaybackService.ACTION_NAME, PlaybackService.PLAY_PREVIOUS_ACTION);
            v.getContext().startService(i);
        }
    };

    public void updateTabNames()
    {
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        List<Playlist> playlists = mApp.getPlaylists();
        int tabsCount = tabLayout.getTabCount();
        for (int index = 0; index < playlists.size(); index++)
        {
            if(index >= tabsCount)
            {
                Log.e(this.getClass().toString(), "Trying to update tab that doesn't exist");
                return;
            }
            TabLayout.Tab tab = tabLayout.getTabAt(index);
            tab.setText(playlists.get(index).getName());
        }
    }
}
