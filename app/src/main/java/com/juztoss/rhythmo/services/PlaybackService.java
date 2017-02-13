package com.juztoss.rhythmo.services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.juztoss.rhythmo.R;
import com.juztoss.rhythmo.audio.AdvancedMediaPlayer;
import com.juztoss.rhythmo.models.Composition;
import com.juztoss.rhythmo.models.Playlist;
import com.juztoss.rhythmo.models.songsources.AbstractSongsSource;
import com.juztoss.rhythmo.presenters.RhythmoApp;
import com.juztoss.rhythmo.views.activities.PlayerActivity;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by JuzTosS on 5/3/2016.
 * The heart of the player.
 */
public class PlaybackService extends MediaBrowserServiceCompat implements AdvancedMediaPlayer.OnEndListener, AdvancedMediaPlayer.OnErrorListener, AudioManager.OnAudioFocusChangeListener, Playlist.IUpdateListener
{
    public enum RepeatMode
    {
        DISABLED,
        ONE,
        ALL,
        SHUFFLE
    }

    public static final int NOTIFICATION_ID = 42;

    public static final String ACTION_COMMAND = "com.juztoss.rhythmo.action.ACTION_COMMAND";
    public static final String ACTION_NAME = "com.juztoss.rhythmo.action.ACTION_NAME";
    public static final String ACTION_SONG_ID = "com.juztoss.rhythmo.action.ACTION_SONG_ID";
    public static final String ACTION_PLAYLIST_INDEX = "com.juztoss.rhythmo.action.ACTION_PLAYLIST_INDEX";

    public static final String LAUNCH_NOW_PLAYING_ACTION = "com.juztoss.rhythmo.action.LAUNCH_NOW_PLAYING";
    public static final String SWITCH_PLAYBACK_ACTION = "com.juztoss.rhythmo.action.SWITCH_PLAYBACK";
    public static final String PAUSE_PLAYBACK_ACTION = "com.juztoss.rhythmo.action.PAUSE_PLAYBACK";
    public static final String PLAY_NEXT_ACTION = "com.juztoss.rhythmo.action.PLAY_NEXT_ACTION";
    public static final String PLAY_PREVIOUS_ACTION = "com.juztoss.rhythmo.action.PLAY_PREVIOUS_ACTION";
    public static final String PLAY_NEW_ACTION = "com.juztoss.rhythmo.action.PLAY_NEW_ACTION";
    public static final String UPDATE_UI_ACTION = "com.juztoss.rhythmo.action.UPDATE_UI";
    public static final String UPDATE_UI_SCROLL_TO_CURRENT = "com.juztoss.rhythmo.action.UPDATE_UI_SCROLL_TO_CURRENT";
    public static final String UPDATE_UI_LIST = "com.juztoss.rhythmo.action.UPDATE_UI_LIST";
    public static final String UPDATE_UI_CONTROLS = "com.juztoss.rhythmo.action.UPDATE_UI_CONTROLS";

    private static final String DEFAULT_SAMPLE_RATE = "44100";
    private static final String DEFAULT_BUFFER_SIZE = "512";

    private AdvancedMediaPlayer mPlayer;
    private RhythmoApp mApp;
    private int mCurrentPlaylistIndex = 0;
    private int mCurrentSongIndex = 0;
    private long mCurrentSongId = -1;
    private Queue<BaseAction> mQueue = new LinkedList<>();

    private BaseAction mActionInProgress;
    private boolean mIsPlaying = false;
    private float mCurrentlyPlayingBPM;
    private Timer mStopCooldown = new Timer();

    private RepeatMode mRepeatMode = RepeatMode.DISABLED;
    private List<Long> mAlreadyPlayedInShuffleMode = new ArrayList<>();

    private Handler mHandler;
    private Toast mToast;

    private MediaSessionCompat mMediaSession;

    private boolean mNoisyReceiverRegistered = false;
    private final BroadcastReceiver mNoisyReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (action.equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY))
            {
                pausePlayback();
            }
        }
    };

    public boolean isShuffleEnabled()
    {
        return mRepeatMode == RepeatMode.SHUFFLE;
    }

    public void setRepeatMode(RepeatMode mode)
    {
        mRepeatMode = mode;
        mApp.getSharedPreferences().edit().putInt(RhythmoApp.REPEAT_MODE, mRepeatMode.ordinal()).apply();
        if(mRepeatMode == RepeatMode.ALL)
            mToast.setText(R.string.repeat_mode_all);
        else if(mRepeatMode == RepeatMode.ONE)
            mToast.setText(R.string.repeat_mode_one);
        else if(mRepeatMode == RepeatMode.SHUFFLE)
            mToast.setText(R.string.repeat_mode_shuffle);
        else// if(mRepeatMode == RepeatMode.DISABLED)
            mToast.setText(R.string.repeat_mode_disabled);

        mToast.show();
    }

    public RepeatMode getRepeatMode()
    {
        return mRepeatMode;
    }

    /**
     * @Return The id of the currently playing song
     */
    public long currentSongId()
    {
        return mCurrentSongId;
    }

    @Override
    public void onEnd()
    {
        runOnUiThread(() -> gotoNext(false));
    }

    @Override
    public void onError()
    {
        Log.e(getClass().toString(), "Internal player error");
        runOnUiThread(() -> {
            clearQueue();
            gotoNext(false);
        }, 1000);
    }

    @Nullable
    private Playlist getCurrentPlaylist()
    {
        if (mCurrentPlaylistIndex < 0 || mCurrentPlaylistIndex >= mApp.getPlaylists().size())
            return null;
        else
            return mApp.getPlaylists().get(mCurrentPlaylistIndex);
    }

    private Cursor mCursor;

    @Nullable
    private Cursor getSongsList()
    {
        if (mCursor == null)
        {
            Playlist playlist = getCurrentPlaylist();
            if (playlist != null)
                return mCursor = getCurrentPlaylist().getCursor();
        }
        return mCursor;
    }

    /**
     * Go to next song in playlist and start playing
     */
    private void gotoNext(boolean fromUser)
    {
        clearQueue();
        if (getSongsList() == null || getSongsList().getCount() <= 0)
        {
            putAction(new ActionStop());
            return;
        }

        if (!isShuffleEnabled())
        {
            if (mRepeatMode != RepeatMode.ONE)
                mCurrentSongIndex++;

            if (mRepeatMode == RepeatMode.ALL)
            {
                if (mCurrentSongIndex >= getSongsList().getCount())
                    mCurrentSongIndex = 0;
            }
            else
            {
                if (mCurrentSongIndex >= getSongsList().getCount())
                {
                    mCurrentSongIndex = 0;
                }
            }
        }
        else//isShuffleEnabled() == true
        {
            Cursor playlist = getSongsList();
            int songsCount = playlist.getCount();
            if (mAlreadyPlayedInShuffleMode.size() == songsCount)
                mAlreadyPlayedInShuffleMode.clear();

            if (!mAlreadyPlayedInShuffleMode.contains(mCurrentSongId))
                mAlreadyPlayedInShuffleMode.add(mCurrentSongId);

            int newSongIndex = (int) (Math.random() * songsCount);

            playlist.moveToPosition(newSongIndex);

            long newSongId = playlist.getLong(AbstractSongsSource.I_ID);
            int iterations = 0;
            while (mAlreadyPlayedInShuffleMode.contains(newSongId) && ++iterations < songsCount)
            {
                newSongIndex++;
                if (newSongIndex >= songsCount)
                    newSongIndex = 0;
            }

            mCurrentSongIndex = newSongIndex;
        }

        if (mCurrentSongIndex >= 0 && getSongsList() != null && getSongsList().getCount() > 0)
        {
            getSongsList().moveToPosition(mCurrentSongIndex);
            mCurrentSongId = getSongsList().getLong(AbstractSongsSource.I_ID);
            setSource(mCurrentPlaylistIndex, mCurrentSongId, fromUser);
        }
    }

    /**
     * Go to previous song in playlist and start playing
     */
    private void gotoPrevious(boolean fromUser)
    {
        clearQueue();
        mCurrentSongIndex--;
        if (mCurrentSongIndex < 0)
            mCurrentSongIndex = 0;

        if(getSongsList() == null || getSongsList().getCount() <= 0) return;

        if (!isShuffleEnabled() || mAlreadyPlayedInShuffleMode.size() <= 0)
        {
            getSongsList().moveToPosition(mCurrentSongIndex);
            mCurrentSongId = getSongsList().getLong(AbstractSongsSource.I_ID);
            setSource(mCurrentPlaylistIndex, mCurrentSongId, fromUser);
        }
        else//isShuffleEnabled() == true
        {
            mCurrentSongId = mAlreadyPlayedInShuffleMode.remove(mAlreadyPlayedInShuffleMode.size() - 1);
            setSource(mCurrentPlaylistIndex, mCurrentSongId, fromUser);
        }

    }

    private void updateUI(boolean updateList, boolean scrollToCurrent)
    {
        Log.v(getClass().toString(), "updateUI()");
        Intent intent = new Intent(UPDATE_UI_ACTION);
        intent.putExtra(UPDATE_UI_SCROLL_TO_CURRENT, scrollToCurrent);
        intent.putExtra(UPDATE_UI_LIST, updateList);
        intent.putExtra(UPDATE_UI_CONTROLS, true);

        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(mApp);
        broadcastManager.sendBroadcast(intent);

        NotificationManager notificationManager = (NotificationManager) mApp.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = PlaybackNotification.create(this);
        if(notification != null)
            notificationManager.notify(NOTIFICATION_ID, notification);
    }

    private synchronized void putAction(BaseAction action)
    {
        Log.v(getClass().toString(), "putAction() " + action.getClass().toString());
        mQueue.add(action);
        if (mActionInProgress == null)
            action.doNext();
    }

    private void clearQueue()
    {
        mQueue.clear();
        if (mActionInProgress != null)
            mActionInProgress.interrupt();

        mActionInProgress = null;

    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return new PlaybackServiceBinder();
    }

    private boolean hasBeenPlayedOnce()
    {
        return mCurrentSongId >= 0;
    }

    private MediaSessionCompat.Callback mMediaSessionCallback = new MediaSessionCompat.Callback()
    {
        @Override
        public void onPlay()
        {
            if(hasBeenPlayedOnce())
                startPlayback();
        }

        @Override
        public void onPause()
        {
            if(hasBeenPlayedOnce())
                pausePlayback();
        }

        @Override
        public void onSkipToNext()
        {
            if(hasBeenPlayedOnce())
                gotoNext(false);
        }

        @Override
        public void onSkipToPrevious()
        {
            if(hasBeenPlayedOnce())
                gotoPrevious(false);
        }
    };

    public MediaSessionCompat getMediaSession()
    {
        return mMediaSession;
    }

    private void setMediaPlaybackState(int state)
    {
        PlaybackStateCompat.Builder playbackStateBuilder = new PlaybackStateCompat.Builder();
        long baseActions = PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS | PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_PLAY_PAUSE;
        if (state == PlaybackStateCompat.STATE_PLAYING)
            playbackStateBuilder.setActions(baseActions | PlaybackStateCompat.ACTION_PAUSE);
        else
            playbackStateBuilder.setActions(baseActions | PlaybackStateCompat.ACTION_PLAY);

        playbackStateBuilder.setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0);
        mMediaSession.setPlaybackState(playbackStateBuilder.build());
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints)
    {
        if(TextUtils.equals(clientPackageName, getPackageName())) {
            return new BrowserRoot(getString(R.string.app_name), null);
        }

        return null;
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result)
    {
        result.sendResult(null);
    }

    private void initMediaSession()
    {
        ComponentName mediaButtonReceiver = new ComponentName(getApplicationContext(), MediaButtonReceiver.class);
        mMediaSession = new MediaSessionCompat(getApplicationContext(), "Tag", mediaButtonReceiver, null);

        mMediaSession.setCallback(mMediaSessionCallback);
        mMediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        mediaButtonIntent.setClass(this, MediaButtonReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, 0);
        mMediaSession.setMediaButtonReceiver(pendingIntent);
        mMediaSession.setMetadata(new MediaMetadataCompat.Builder().build());
        setSessionToken(mMediaSession.getSessionToken());
        setMediaPlaybackState(PlaybackStateCompat.STATE_STOPPED);
        mMediaSession.setActive(true);
    }

    @SuppressLint("ShowToast")
    @Override
    public void onCreate()
    {
        Log.v(getClass().toString(), "onCreate()");
        mHandler = new Handler();
        mApp = (RhythmoApp) getApplicationContext();
        mToast = Toast.makeText(mApp, null, Toast.LENGTH_SHORT);
        try
        {
            mRepeatMode = RepeatMode.values()[mApp.getSharedPreferences().getInt(RhythmoApp.REPEAT_MODE, RepeatMode.DISABLED.ordinal())];
        }catch (Exception e)
        {
            e.printStackTrace();
            mRepeatMode = RepeatMode.DISABLED;
        }

        initPlayer();

        super.onCreate();

        initMediaSession();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.v(getClass().toString(), "onStartCommand(...)");

        MediaButtonReceiver.handleIntent(mMediaSession, intent);

        super.onStartCommand(intent, flags, startId);

        if (intent != null && ACTION_COMMAND.equals(intent.getAction()))
        {
            registerNoisyReceiver();

            mHandler.removeCallbacksAndMessages(null);
            String command = intent.getStringExtra(ACTION_NAME);
            Log.v(getClass().toString(), "onStartCommand command: " + command);
            if (PAUSE_PLAYBACK_ACTION.equals(command))
            {
                pausePlayback();
            }
            else if (PLAY_NEXT_ACTION.equals(command))
            {
                gotoNext(true);
            }
            else if (PLAY_PREVIOUS_ACTION.equals(command))
            {
                gotoPrevious(true);
            }
            else if (SWITCH_PLAYBACK_ACTION.equals(command))
            {
                togglePlaybackState();
            }
            else if (PLAY_NEW_ACTION.equals(command))
            {
                long songId = intent.getLongExtra(ACTION_SONG_ID, -1);
                int playlistIndex = intent.getIntExtra(ACTION_PLAYLIST_INDEX, 0);
                clearQueue();
                setSource(playlistIndex, songId, false);
            }
        }

        return START_NOT_STICKY;
    }

    private void runOnUiThread(Runnable runnable) {
        mHandler.removeCallbacksAndMessages(null);
        mHandler.post(runnable);
    }

    private void runOnUiThread(Runnable runnable, int delayMillis) {
        mHandler.removeCallbacksAndMessages(null);
        mHandler.postDelayed(runnable, delayMillis);
    }

    public class PlaybackServiceBinder extends Binder
    {
        public PlaybackService getService()
        {
            return PlaybackService.this;
        }
    }

    private void registerNoisyReceiver()
    {
        if (!mNoisyReceiverRegistered)
        {
            mNoisyReceiverRegistered = true;
            IntentFilter filter = new IntentFilter();
            filter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
            registerReceiver(mNoisyReceiver, filter);
        }
    }

    private void initPlayer()
    {
        if (mPlayer == null)
        {
            String sampleRateString = null, bufferSizeString = null;
            if (Build.VERSION.SDK_INT >= 17)
            {
                AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                sampleRateString = audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE);
                bufferSizeString = audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER);
            }
            if (sampleRateString == null) sampleRateString = DEFAULT_SAMPLE_RATE;
            if (bufferSizeString == null) bufferSizeString = DEFAULT_BUFFER_SIZE;

            final int sampleRate = Integer.parseInt(sampleRateString);
            final int bufferSize = Integer.parseInt(bufferSizeString);

            mPlayer = new AdvancedMediaPlayer(sampleRate, bufferSize);
            mPlayer.setOnEndListener(this);
            mPlayer.setOnErrorListener(this);
        }
    }

    private void abandonAudioFocus()
    {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.abandonAudioFocus(this);
    }

    private void unregisterListeners()
    {
        abandonAudioFocus();
        cancelHideCooldown();
        unregisterNoisyReceiver();
    }

    private void disableService()
    {
        Log.v(getClass().toString(), "disableService()");
        stopForeground(true);
        unregisterListeners();
        stopSelf();
    }

    @Override
    public void onDestroy()
    {
        Log.v(getClass().toString(), "onDestroy()");
        unregisterListeners();
        mMediaSession.release();
        mPlayer.release();
        mPlayer = null;
        super.onDestroy();
    }

    @Override
    public void onPlaylistUpdated()
    {
        if(mCursor != null)
        {
            mCursor.close();
            mCursor = null;
        }

        if (mCurrentSongId >= 0 && getCurrentPlaylist() != null)
        {
            Composition composition = mApp.getComposition(mCurrentSongId);
            if(composition != null)
            {
                updateMediaSessionData(composition);
                mCurrentSongIndex = Playlist.findPositionById(getSongsList(), composition, getCurrentPlaylist().getSource().getSortType());
            }
        }
        else
            mCurrentSongIndex = -1;
    }

    private void updateMediaSessionData(@NonNull Composition composition)
    {
        Intent launchNowPlayingIntent = new Intent(this, PlayerActivity.class);
        PendingIntent launchNowPlayingPendingIntent = PendingIntent.getActivity(this, 4, launchNowPlayingIntent, 0);
        mMediaSession.setSessionActivity(launchNowPlayingPendingIntent);

        mMediaSession.setMetadata(new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, composition.name())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, composition.getFolder())
                .build());
    }

    /**
     * Set index of a song that will be played
     */
    private void setSource(int playlistIndex, long songId, boolean scrollToCurrent)
    {
        if (mCurrentPlaylistIndex != playlistIndex)
        {
            mAlreadyPlayedInShuffleMode.clear();
            if(mCurrentPlaylistIndex < mApp.getPlaylists().size())
                mApp.getPlaylists().get(mCurrentPlaylistIndex).removeUpdateListener(this);
        }

        mApp.getPlaylists().get(playlistIndex).addUpdateListener(this);

        mCurrentPlaylistIndex = playlistIndex;
        mCurrentSongId = songId;
        onPlaylistUpdated();

        if (mCurrentSongIndex < 0 || getSongsList() == null || mCurrentSongIndex >= getSongsList().getCount())
            return;

        getSongsList().moveToPosition(mCurrentSongIndex);
        putAction(new ActionPrepare(Composition.fromCursor(getSongsList()), scrollToCurrent));
        putAction(new ActionPlay(false, false));
    }

    @Override
    public void onAudioFocusChange(int focusChange)
    {
        if (focusChange == AudioManager.AUDIOFOCUS_LOSS || focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT)
        {
            pausePlayback();
        }
        else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK)
        {
            //TODO: duck music
        }
    }

    private void setIsPlaying(boolean value)
    {
        Log.v(getClass().toString(), "setIsPlaying: " + value);
        setMediaPlaybackState(value ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED);
        if (!mIsPlaying && value)
            requestAudioFocus();

        mIsPlaying = value;
        if (mIsPlaying)
        {
            cancelHideCooldown();
            Notification notification = PlaybackNotification.create(this);
            if(notification != null)
                startForeground(NOTIFICATION_ID, notification);
        }
        else
        {
            stopForeground(false);
            abandonAudioFocus();
            startHideCooldown();
        }
    }

    private void startHideCooldown()
    {
        final int STOP_SERVICE_COOLDOWN_MS = 10000;

        if (mStopCooldown != null)
            cancelHideCooldown();

        mStopCooldown = new Timer();

        mStopCooldown.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                disableService();
            }
        }, STOP_SERVICE_COOLDOWN_MS);
    }

    private void unregisterNoisyReceiver()
    {
        if (mNoisyReceiverRegistered)
        {
            mNoisyReceiverRegistered = false;
            unregisterReceiver(mNoisyReceiver);
        }
    }

    private void cancelHideCooldown()
    {
        if (mStopCooldown != null)
        {
            mStopCooldown.cancel();
            mStopCooldown = null;
        }
    }

    private void requestAudioFocus()
    {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
    }

    /**
     * Returns true if current song is playing or is in preparing to play state
     */
    public boolean isPlaying()
    {
        return mIsPlaying;
    }

    /**
     * Pause/unpause playback
     */
    private void togglePlaybackState()
    {
        if (isPlaying())
            pausePlayback();
        else
        {
            if(hasBeenPlayedOnce())
            {
                startPlayback();
            }
            else if(getSongsList() != null && getSongsList().getCount() > 0)
            {
                clearQueue();
                Cursor cursor = getSongsList();
                cursor.moveToPosition(0);
                setSource(0, cursor.getLong(AbstractSongsSource.I_ID), false);
            }
        }
    }

    /**
     * Starts playback. setSource(...) have to be called before this method.
     */
    private void startPlayback()
    {
        putAction(new ActionPlay(true, false));
    }

    /**
     * Pauses playback.
     */
    private void pausePlayback()
    {
        putAction(new ActionPause());
    }

    /**
     * Stops playback.
     */
    private void stopPlayback()
    {
        putAction(new ActionStop());
    }

    /**
     * Returns the index of the current song index in the current playlist
     */
    public int getCurrentSongIndex()
    {
        return mCurrentSongIndex;
    }

    /**
     * Returns the index of the currently playing playlist
     */
    public int getCurrentPlaylistIndex()
    {
        return mCurrentPlaylistIndex;
    }

    /**
     * Returns the current position in audio file in milliseconds
     */
    public int getCurrentPosition()
    {
        return mPlayer.getPosition();
    }

    /**
     * Returns a duration of the current audio file in milliseconds
     */
    public int getDuration()
    {
        return mPlayer.getDuration();
    }

    /**
     * Go to a position in the current audio file
     *
     * @param position position in milleseconds
     */
    public void seekTo(int position)
    {
        mPlayer.setPosition(position);
    }

    /**
     * Shift the current song duration
     *
     * @param bpm        original beat rate
     * @param shiftedBpm a beat rate to be played with
     */
    public void setNewPlayingBPM(float bpm, float shiftedBpm)
    {
        mCurrentlyPlayingBPM = shiftedBpm;

        if (bpm <= 10 || mCurrentlyPlayingBPM <= 10)
        {
            mPlayer.setBPM(10);
            mPlayer.setNewBPM(10);
        }
        else
        {
            mPlayer.setBPM(bpm);
            mPlayer.setNewBPM(shiftedBpm);
        }
    }

    /**
     * Retrun a BPM of the currently playing song
     */
    public float getCurrentlyPlayingBPM()
    {
        return mCurrentlyPlayingBPM;
    }

    /**
     * Abstract action class for the actions queue (mQueue)
     */
    private class BaseAction
    {
        public void doAction()
        {
            doNext();
        }

        public final void doNext()
        {
            if (mQueue.size() > 0)
            {
                mActionInProgress = mQueue.remove();
                mActionInProgress.doAction();
            }
            else
            {
                mActionInProgress = null;
            }
        }

        public void interrupt()
        {
        }
    }

    /**
     * Prepare a song to be played
     * Must be put once before ActionPlay.
     */
    class ActionPrepare extends BaseAction
    {

        private AdvancedMediaPlayer.OnPreparedListener mOnPrepared = new AdvancedMediaPlayer.OnPreparedListener()
        {
            @Override
            public void onPrepared()
            {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        setNewPlayingBPM(mComposition.bpm(), mApp.getAvailableToPlayBPM(mComposition.bpmShifted()));
                        doNext();
                    }
                });
            }
        };

        private Composition mComposition;
        private boolean mScrollToCurrent;

        public ActionPrepare(Composition composition, boolean scrollToCurrent)
        {
            mComposition = composition;
            mCurrentSongId = composition.id();
            mScrollToCurrent = scrollToCurrent;
        }

        @Override
        public void doAction()
        {
            setIsPlaying(true);
            mPlayer.setOnPreparedListener(mOnPrepared);
            mPlayer.setSource(mComposition.getAbsolutePath());
            updateUI(true, mScrollToCurrent);
        }
    }

    /**
     * Starts a playback
     */
    class ActionPlay extends BaseAction
    {
        private boolean mNeedToUpdateList;
        private boolean mScrollToCurrent;

        public ActionPlay(boolean needToUpdateList, boolean scrollToCurrent)
        {
            mNeedToUpdateList = needToUpdateList;
            mScrollToCurrent = scrollToCurrent;
        }

        @Override
        public void doAction()
        {
            setIsPlaying(true);
            try
            {
                mPlayer.play();
            }
            catch (IllegalStateException e)
            {
                setIsPlaying(false);
            }
            finally
            {
                updateUI(mNeedToUpdateList, mScrollToCurrent);
                doNext();
            }
        }
    }

    /**
     * Pauses the current playback
     */
    class ActionPause extends BaseAction
    {
        @Override
        public void doAction()
        {
            setIsPlaying(false);
            mPlayer.pause();
            updateUI(true, false);
            doNext();
        }
    }

    /**
     * Stops the current playback
     */
    class ActionStop extends BaseAction
    {
        @Override
        public void doAction()
        {
            setIsPlaying(false);
            mPlayer.pause();
            mPlayer.setPosition(0);
            updateUI(true, false);
            doNext();
        }
    }
}
