package com.juztoss.bpmplayer.views.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
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
import android.text.InputType;
import android.text.SpannableString;
import android.text.format.DateUtils;
import android.text.style.AbsoluteSizeSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.juztoss.bpmplayer.R;
import com.juztoss.bpmplayer.models.Composition;
import com.juztoss.bpmplayer.models.Playlist;
import com.juztoss.bpmplayer.models.songsources.ISongsSource;
import com.juztoss.bpmplayer.presenters.BPMPlayerApp;
import com.juztoss.bpmplayer.services.BuildMusicLibraryService;
import com.juztoss.bpmplayer.services.PlaybackService;
import com.juztoss.bpmplayer.views.adapters.TabsAdapter;
import com.juztoss.bpmplayer.views.items.RangeSeekBar;

import java.util.List;
import java.util.Locale;

public class PlayerActivity extends BasePlayerActivity implements View.OnClickListener
{
    private BPMPlayerApp mApp;
    private View mPlayButton;
    private ImageView mRepeatButton;
    private ImageView mShuffleButton;
    private TextView mTimePassed;
    private TextView mTimeLeft;
    private SeekBar mSeekbar;
    private RangeSeekBar<Integer> mRangeSeekbar;
    ViewPager mPlaylistsPager;
    private FloatingActionButton fab;
    ActionBar mActionBar;
    private TextView mMinBPMField;
    private TextView mMaxBPMField;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mApp = (BPMPlayerApp) getApplication();
        setContentView(R.layout.activity_main);
        createFabs();
        setupActionBar();
        setupPager();
        setupAllOtherUI();

        LocalBroadcastManager.getInstance(mApp).registerReceiver(mUpdateUIReceiver, new IntentFilter(PlaybackService.UPDATE_UI_ACTION));
    }

    @Override
    protected void onServiceConnected()
    {
        super.onServiceConnected();
        updateAll();
    }

    private Playlist getCurrentViewedPlaylist()
    {
        return mApp.getPlaylists().get(mPlaylistsPager.getCurrentItem());
    }

    private void createFabs()
    {
        fab = (FloatingActionButton) findViewById(R.id.btnAddToPlaylist);
        fab.setOnClickListener(this);
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
            ISongsSource source = playlist.getSource();
            for (String path : foldersPaths)
            {
                source.add(mApp.getMusicLibraryHelper().getSongIdsCursor(path, true));
            }
//                playlist.setNeedRebuild();
//            }
            updateTabs();
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
        mRangeSeekbar.setRangeValues((int) BPMPlayerApp.MIN_BPM, (int) BPMPlayerApp.MAX_BPM);

        mMinBPMField = (TextView) findViewById(R.id.bpm_label_min);
        mMaxBPMField = (TextView) findViewById(R.id.bpm_label_max);
        mMinBPMField.setText("Min");
        mMaxBPMField.setText("Max");

        mRepeatButton = (ImageView) findViewById(R.id.repeat_button);
        mRepeatButton.setOnClickListener(mRepeatButtonListener);
        mShuffleButton = (ImageView) findViewById(R.id.shuffle_button);
        mShuffleButton.setOnClickListener(mShuffleButtonListener);

        mPlayButton = findViewById(R.id.play_button);
        mPlayButton.setOnClickListener(mPlayButtonListenter);

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

    private void setupPager()
    {
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setTabGravity(TabLayout.GRAVITY_CENTER);

        mPlaylistsPager = (ViewPager) findViewById(R.id.pager);
        final TabsAdapter adapter = new TabsAdapter(getSupportFragmentManager(), mApp.getPlaylists().size());
        mPlaylistsPager.setAdapter(adapter);
        mPlaylistsPager.addOnPageChangeListener(adapter);
        mPlaylistsPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
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

    private void updateFab()
    {
        final int MIN_AVAILABLE_SONGS_TO_SHOW_ADD_ICON = 3;
        if (getCurrentViewedPlaylist().getSource().isModifyAvailable() || (getCurrentViewedPlaylist().getList() == null
                || getCurrentViewedPlaylist().getList().getCount() <= MIN_AVAILABLE_SONGS_TO_SHOW_ADD_ICON))
        {
            fab.show();
        }
        else
            fab.hide();
    }

    private void setupActionBar()
    {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mActionBar = getSupportActionBar();
        mActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.MATCH_PARENT);
        View actionBarLayout = getLayoutInflater().inflate(R.layout.action_bar, null);
        mActionBar.setCustomView(actionBarLayout, layoutParams);
        Toolbar parent = (Toolbar) actionBarLayout.getParent();
        parent.setContentInsetsAbsolute(0, 0);
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        if (mApp.getSharedPreferences().getBoolean(BPMPlayerApp.FIRST_RUN, true))
        {
            mApp.getSharedPreferences().edit().putBoolean(BPMPlayerApp.FIRST_RUN, false).commit();
            Intent launchStartActivity = new Intent(this, LauncherActivity.class);
            startActivity(launchStartActivity);
        }
        else
        {
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
        }
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
        if (mApp.getSharedPreferences().getBoolean(BPMPlayerApp.LIBRARY_BUILD_STARTED, true))
        {
            mApp.getSharedPreferences().edit().putBoolean(BPMPlayerApp.LIBRARY_BUILD_STARTED, false).commit();
            Intent intent = new Intent(getApplicationContext(), BuildMusicLibraryService.class);
            getApplicationContext().startService(intent);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
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
        return super.onOptionsItemSelected(item);
    }

    private void launchRenameDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.rename_dialog_title));

        final EditText input = new EditText(this);
        final Playlist playlist = mApp.getPlaylists().get(mPlaylistsPager.getCurrentItem());
        input.setText(playlist.getName());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
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
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.player_menu, menu);
        return true;
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        updateAll();
    }

    BroadcastReceiver mUpdateUIReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            updateAll();
        }
    };

    protected void updateAll()
    {
        if (playbackService() == null) return;

        Composition composition = mApp.getComposition(playbackService().currentSongId());
        if (composition != null)
        {
            TextView bpmLabel = ((TextView) mActionBar.getCustomView().findViewById(R.id.bpm_label));
            SpannableString spannableString = new SpannableString(String.format(Locale.US, "%.1f", playbackService().getCurrentlyPlayingBPM()));
            int firstPartLength = Integer.toString((int) composition.bpmShifted()).length();
            spannableString.setSpan(new AbsoluteSizeSpan(10, true), firstPartLength, spannableString.length(), 0);
            bpmLabel.setText(spannableString);
            ((TextView) mActionBar.getCustomView().findViewById(R.id.first_line)).setText(composition.name());
            ((TextView) mActionBar.getCustomView().findViewById(R.id.second_line)).setText(composition.getFolder());
        }

        mPlayButton.setSelected(!playbackService().isPlaying());

        mSeekbar.setMax(playbackService().getDuration());
        mHandler.post(mSeekbarUpdateRunnable);

        updateRepeatButton();
        updateShuffleButton();
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

            if (playbackService().getRepeatMode() == PlaybackService.RepeatMode.DISABLED)
                playbackService().setRepeatMode(PlaybackService.RepeatMode.ALL);
            else if (playbackService().getRepeatMode() == PlaybackService.RepeatMode.ALL)
                playbackService().setRepeatMode(PlaybackService.RepeatMode.ONE);
            else
                playbackService().setRepeatMode(PlaybackService.RepeatMode.DISABLED);

            updateRepeatButton();
            updateShuffleButton();
        }
    };

    private void updateRepeatButton()
    {
        if (playbackService() == null)
            return;

        if (playbackService().getRepeatMode() == PlaybackService.RepeatMode.ALL)
        {
            mRepeatButton.setColorFilter(getResources().getColor(R.color.accentPrimary));
            mRepeatButton.setImageResource(R.drawable.ic_repeat);
        }
        else if (playbackService().getRepeatMode() == PlaybackService.RepeatMode.ONE)
        {
            mRepeatButton.setColorFilter(getResources().getColor(R.color.accentPrimary));
            mRepeatButton.setImageResource(R.drawable.ic_repeat_once);
        }
        else
        {
            mRepeatButton.setColorFilter(getResources().getColor(R.color.foregroundGrayedOut));
            mRepeatButton.setImageResource(R.drawable.ic_repeat);
        }
    }

    private View.OnClickListener mShuffleButtonListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            if (playbackService() == null)
                return;

            playbackService().setShuffleMode(!playbackService().isShuffleEnabled());
            updateShuffleButton();
            updateRepeatButton();
        }
    };

    private void updateShuffleButton()
    {
        if (playbackService() != null || !playbackService().isShuffleEnabled())
            mShuffleButton.setColorFilter(getResources().getColor(R.color.foregroundGrayedOut));
        else
            mShuffleButton.setColorFilter(getResources().getColor(R.color.accentPrimary));
    }


    private View.OnClickListener mPlayButtonListenter = new View.OnClickListener()
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
            if (minValue <= BPMPlayerApp.MIN_BPM && maxValue >= BPMPlayerApp.MAX_BPM)
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
            if (minValue <= BPMPlayerApp.MIN_BPM && maxValue >= BPMPlayerApp.MAX_BPM)
            {
                mMinBPMField.setText("Min");
                mMaxBPMField.setText("Max");
            }
            else
            {
                mMinBPMField.setText(Integer.toString(minValue));
                mMaxBPMField.setText(Integer.toString(maxValue));
            }
        }
    };

    private SeekBar.OnSeekBarChangeListener mOnSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener()
    {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
        {
            int timePassed = progress;
            int timeLeft = seekBar.getMax() - (timePassed / 1000) * 1000;
            mTimePassed.setText(DateUtils.formatElapsedTime(timePassed / 1000));
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
        for (int index = 0; index < playlists.size(); index++)
        {
            TabLayout.Tab tab = tabLayout.getTabAt(index);
            tab.setText(playlists.get(index).getName());
        }
    }
}
