package com.juztoss.rhythmo.services;

import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.juztoss.rhythmo.audio.AdvancedMediaPlayer;
import com.juztoss.rhythmo.models.Composition;
import com.juztoss.rhythmo.models.Playlist;
import com.juztoss.rhythmo.presenters.RhythmoApp;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by JuzTosS on 5/3/2016.
 * The heart of the player.
 */
public class PlaybackService extends Service implements AdvancedMediaPlayer.OnEndListener, AdvancedMediaPlayer.OnErrorListener, AudioManager.OnAudioFocusChangeListener, Playlist.IUpdateListener
{
    public enum RepeatMode
    {
        DISABLED,
        ONE,
        ALL
    }

    public static final int NOTIFICATION_ID = 42;

    public static final String ACTION_COMMAND = "com.juztoss.rhythmo.action.ACTION_COMMAND";
    public static final String ACTION_NAME = "com.juztoss.rhythmo.action.ACTION_NAME";
    public static final String ACTION_PLAYLIST_INDEX = "com.juztoss.rhythmo.action.ACTION_PLAYLIST_INDEX";
    public static final String ACTION_PLAYLIST_POSITION = "com.juztoss.rhythmo.action.ACTION_PLAYLIST_POSITION";

    public static final String LAUNCH_NOW_PLAYING_ACTION = "com.juztoss.rhythmo.action.LAUNCH_NOW_PLAYING";
    public static final String SWITCH_PLAYBACK_ACTION = "com.juztoss.rhythmo.action.SWITCH_PLAYBACK";
    public static final String PAUSE_PLAYBACK_ACTION = "com.juztoss.rhythmo.action.PAUSE_PLAYBACK";
    public static final String PLAY_NEXT_ACTION = "com.juztoss.rhythmo.action.PLAY_NEXT_ACTION";
    public static final String PLAY_PREVIOUS_ACTION = "com.juztoss.rhythmo.action.PLAY_PREVIOUS_ACTION";
    public static final String PLAY_NEW_ACTION = "com.juztoss.rhythmo.action.PLAY_NEW_ACTION";
    public static final String UPDATE_UI_ACTION = "com.juztoss.rhythmo.action.UPDATE_UI";

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
    private boolean mIsShuffleEnabled = false;
    private Set<Integer> mAlreadyPlayedInShuffleMode = new HashSet<>();

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

    public void setShuffleMode(boolean enabled)
    {
        if (enabled && !mIsShuffleEnabled)//Reset played songs
        {
            mAlreadyPlayedInShuffleMode.clear();
        }
        mIsShuffleEnabled = enabled;
        if (mIsShuffleEnabled)
        {
            mRepeatMode = RepeatMode.DISABLED;
        }
    }

    public boolean isShuffleEnabled()
    {
        return mIsShuffleEnabled;
    }

    public void setRepeatMode(RepeatMode mode)
    {
        mRepeatMode = mode;
        if (mRepeatMode != RepeatMode.DISABLED)
        {
            mIsShuffleEnabled = false;
        }
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
        gotoNext(false);
    }

    @Override
    public void onError()
    {
        Log.e(getClass().toString(), "Internal player error");
        clearQueue();
        gotoNext(false);
    }

    @Nullable
    private Playlist getCurrentPlaylist()
    {
        if (mCurrentPlaylistIndex < 0 || mCurrentPlaylistIndex >= mApp.getPlaylists().size())
            return null;
        else
            return mApp.getPlaylists().get(mCurrentPlaylistIndex);
    }

    @Nullable
    private Cursor getSongsList()
    {
        Playlist playlist = getCurrentPlaylist();
        if (playlist != null)
            return getCurrentPlaylist().getList();
        else
            return null;
    }

    /**
     * Go to next song in playlist and start playing
     */
    private void gotoNext(boolean byUser)
    {
        clearQueue();
        if (getSongsList() == null)
        {
            putAction(new ActionStop());
            return;
        }

        if (!isShuffleEnabled() || byUser)
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
                    if (byUser)
                        mCurrentSongIndex = 0;
                    else
                        mCurrentSongIndex = -1;
                }
            }
        }
        else//isShuffleEnabled() == true
        {
            int songsCount = getSongsList().getCount();
            if (mAlreadyPlayedInShuffleMode.size() == songsCount)
                mAlreadyPlayedInShuffleMode.clear();

            if (!mAlreadyPlayedInShuffleMode.contains(mCurrentSongIndex))
                mAlreadyPlayedInShuffleMode.add(mCurrentSongIndex);

            int newSongIndex = (int) (Math.random() * songsCount);
            int iterations = 0;
            while (mAlreadyPlayedInShuffleMode.contains(newSongIndex) && ++iterations < songsCount)
            {
                newSongIndex++;
                if (newSongIndex >= songsCount)
                    newSongIndex = 0;
            }

            mCurrentSongIndex = newSongIndex;
        }

        if (mCurrentSongIndex >= 0)
        {
            setSource(mCurrentPlaylistIndex, mCurrentSongIndex);
            putAction(new ActionPlay());
        }
    }

    /**
     * Go to previous song in playlist and start playing
     */
    private void gotoPrevious()
    {
        clearQueue();
        mCurrentSongIndex--;
        if (mCurrentSongIndex < 0)
            mCurrentSongIndex = 0;

        setSource(mCurrentPlaylistIndex, mCurrentSongIndex);
        putAction(new ActionPlay());
    }

    private void updateUI()
    {
        Log.d(getClass().toString(), "updateUI()");
        Intent intent = new Intent(UPDATE_UI_ACTION);
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(mApp);
        broadcastManager.sendBroadcast(intent);

        NotificationManager notificationManager = (NotificationManager) mApp.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, PlaybackNotification.create(this));
    }

    private synchronized void putAction(BaseAction action)
    {
        Log.d(getClass().toString(), "putAction() " + action.getClass().toString());
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

    public class PlaybackServiceBinder extends Binder
    {
        public PlaybackService getService()
        {
            return PlaybackService.this;
        }
    }

    @Override
    public void onCreate()
    {
        Log.d(getClass().toString(), "onCreate()");
        mApp = (RhythmoApp) getApplicationContext();
        initPlayer();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.d(getClass().toString(), "onStartCommand(...)");
        super.onStartCommand(intent, flags, startId);

        if (intent != null)
        {
            String action = intent.getAction();
            if (ACTION_COMMAND.equals(action))
            {
                String command = intent.getStringExtra(ACTION_NAME);
                Log.d(getClass().toString(), "onStartCommand command: " + command);
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
                    gotoPrevious();
                }
                else if (SWITCH_PLAYBACK_ACTION.equals(command))
                {
                    togglePlaybackState();
                }
                else if (PLAY_NEW_ACTION.equals(command))
                {
                    int playlistIndex = intent.getIntExtra(ACTION_PLAYLIST_INDEX, 0);
                    int playlistPosition = intent.getIntExtra(ACTION_PLAYLIST_POSITION, 0);
                    clearQueue();
                    setSource(playlistIndex, playlistPosition);
                    startPlayback();
                }
            }
        }

        registerNoisyReceiver();

        return START_NOT_STICKY;
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

    @Override
    public void onDestroy()
    {
        Log.d(getClass().toString(), "onDestroy()");
        unregisterNoisyReceiver();
        cancelHideCooldown();
        mPlayer.release();
        mPlayer = null;
        super.onDestroy();
    }

    @Override
    public void onPlaylistUpdated()
    {
        if (mCurrentSongId >= 0 && getCurrentPlaylist() != null)
            mCurrentSongIndex = getCurrentPlaylist().findPositionById(mCurrentSongId);
        else
            mCurrentSongIndex = -1;
    }

    /**
     * Set index of a song that will be played
     */
    private void setSource(int playlistIndex, int index)
    {
        if (mCurrentPlaylistIndex != playlistIndex)
        {
            mAlreadyPlayedInShuffleMode.clear();
            mApp.getPlaylists().get(mCurrentPlaylistIndex).removeUpdateListener(this);
        }

        mApp.getPlaylists().get(playlistIndex).addUpdateListener(this);

        mCurrentPlaylistIndex = playlistIndex;
        mCurrentSongIndex = index;

        if (index < 0 || getSongsList() == null || index >= getSongsList().getCount())
            return;

        getSongsList().moveToPosition(index);
        putAction(new ActionPrepare(mApp.getComposition(getSongsList().getLong(0))));
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
        Log.d(getClass().toString(), "setIsPlaying: " + value);
        if (!mIsPlaying && value)
            requestAudioFocus();

        mIsPlaying = value;
        if (mIsPlaying)
        {
            cancelHideCooldown();
            startForeground(NOTIFICATION_ID, PlaybackNotification.create(this));
        }
        else
        {
            stopForeground(false);
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

    private void disableService()
    {
        Log.d(getClass().toString(), "disableService()");
        stopForeground(true);
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.abandonAudioFocus(this);
        unregisterNoisyReceiver();
        stopSelf();
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
            startPlayback();
    }

    /**
     * Starts playback. setSource(...) have to be called before this method.
     */
    private void startPlayback()
    {
        putAction(new ActionPlay());
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

        if (bpm <= 10 || shiftedBpm <= 10)
        {
            bpm = 10;
            shiftedBpm = 10;
        }

        mPlayer.setBPM(bpm);
        mPlayer.setNewBPM(shiftedBpm);
        updateUI();
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
                setNewPlayingBPM(mComposition.bpm(), mApp.getAvailableToPlayBPM(mComposition.bpmShifted()));
                doNext();
            }
        };

        private Composition mComposition;

        public ActionPrepare(Composition composition)
        {
            mComposition = composition;
            mCurrentSongId = composition.id();
        }

        @Override
        public void doAction()
        {
            setIsPlaying(true);
            mPlayer.setOnPreparedListener(mOnPrepared);
            mPlayer.setSource(mComposition.getAbsolutePath());
        }
    }

    /**
     * Starts a playback
     */
    class ActionPlay extends BaseAction
    {
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
                updateUI();
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
            updateUI();
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
            updateUI();
            doNext();
        }
    }
}
