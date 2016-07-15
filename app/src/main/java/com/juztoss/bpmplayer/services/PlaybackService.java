package com.juztoss.bpmplayer.services;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.juztoss.bpmplayer.R;
import com.juztoss.bpmplayer.audio.AdvancedMediaPlayer;
import com.juztoss.bpmplayer.models.Composition;
import com.juztoss.bpmplayer.models.Playlist;
import com.juztoss.bpmplayer.presenters.BPMPlayerApp;

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

    public enum RepeatMode {
        DISABLED,
        ONE,
        ALL
    }

    public static final int NOTIFICATION_ID = 42;
    public static final String LAUNCH_NOW_PLAYING_ACTION = "com.juztoss.bpmplayer.action.LAUNCH_NOW_PLAYING";
    public static final String SWITCH_PLAYBACK_ACTION = "com.juztoss.bpmplayer.action.SWITCH_PLAYBACK";
    public static final String PLAY_NEXT_ACTION = "com.juztoss.bpmplayer.action.PLAY_NEXT_ACTION";
    public static final String UPDATE_UI_ACTION = "com.juztoss.bpmplayer.action.UPDATE_UI";
    private static final String DEFAULT_SAMPLE_RATE = "44100";
    private static final String DEFAULT_BUFFER_SIZE = "512";

    private AdvancedMediaPlayer mPlayer;
    private BPMPlayerApp mApp;
    private int mCurrentPlaylistIndex = 0;
    private int mCurrentSongIndex = 0;
    private Queue<BaseAction> mQueue = new LinkedList<>();

    private BaseAction mActionInProgress;
    private boolean mIsPlaying = false;
    private float mCurrentlyPlayingBPM;
    private Timer mStopCooldown = new Timer();

    private RepeatMode mRepeatMode = RepeatMode.DISABLED;
    private boolean mIsShuffleEnabled = false;
    private Set<Integer> mAlreadyPlayedInShuffleMode = new HashSet<>();

    public void setShuffleMode(boolean enabled)
    {
        if(enabled && !mIsShuffleEnabled)//Reset played songs
        {
            mAlreadyPlayedInShuffleMode.clear();
        }
        mIsShuffleEnabled = enabled;
        if(mIsShuffleEnabled)
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
        if(mRepeatMode != RepeatMode.DISABLED)
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
        if(getSongsList() == null)
        {
            mCurrentPlaylistIndex = 0;
            return -1;
        }
        if (mCurrentSongIndex < 0 || mCurrentSongIndex >= getSongsList().getCount()) return -1;

        getSongsList().moveToPosition(mCurrentSongIndex);
        return getSongsList().getLong(0);
    }

    @Override
    public void onEnd()
    {
        gotoNext(false);
    }

    @Override
    public void onError(String message)
    {
        clearQueue();
        Log.e(getResources().getString(R.string.app_name), "Player error: " + message);
    }

    @Nullable
    private Cursor getSongsList()
    {
        if(mCurrentPlaylistIndex < 0 || mCurrentPlaylistIndex >= mApp.getPlaylists().size())
            return null;
        else
            return mApp.getPlaylists().get(mCurrentPlaylistIndex).getList();
    }

    /**
     * Go to next song in playlist and start playing
     */
    public void gotoNext(boolean byUser)
    {
        clearQueue();
        if(getSongsList() == null)
        {
            putAction(new ActionStop());
            return;
        }

        if(!isShuffleEnabled() || byUser)
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
                    if(byUser)
                        mCurrentSongIndex = 0;
                    else
                        mCurrentSongIndex = -1;
                }
            }
        }
        else//isShuffleEnabled() == true
        {
            int songsCount = getSongsList().getCount();
            if(mAlreadyPlayedInShuffleMode.size() == songsCount)
                mAlreadyPlayedInShuffleMode.clear();

            if(!mAlreadyPlayedInShuffleMode.contains(mCurrentSongIndex))
                mAlreadyPlayedInShuffleMode.add(mCurrentSongIndex);

            int newSongIndex = (int)(Math.random() * songsCount);
            int iterations = 0;
            while (mAlreadyPlayedInShuffleMode.contains(newSongIndex) && ++iterations < songsCount)
            {
                newSongIndex++;
                if (newSongIndex >= songsCount)
                    newSongIndex = 0;
            }

            mCurrentSongIndex = newSongIndex;
        }

        if(mCurrentSongIndex >= 0)
        {
            setSource(mCurrentPlaylistIndex, mCurrentSongIndex);
            putAction(new ActionPlay());
        }
    }

    /**
     * Go to previous song in playlist and start playing
     */
    public void gotoPrevious()
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
        Intent intent = new Intent(UPDATE_UI_ACTION);
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(mApp);
        broadcastManager.sendBroadcast(intent);

        NotificationManager notificationManager = (NotificationManager) mApp.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, PlaybackNotification.create(this));
    }

    private synchronized void putAction(BaseAction action)
    {
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
        return null;
    }

    @Override
    public void onCreate()
    {
        mApp = (BPMPlayerApp) getApplicationContext();
        mApp.setPlaybackService(this);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);

        System.loadLibrary(AdvancedMediaPlayer.LIBRARY_NAME);

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

        return START_STICKY;
    }

    @Override
    public void onDestroy()
    {
        mPlayer.release();
        mApp.setPlaybackService(null);
        super.onDestroy();
    }

    @Override
    public void onPlaylistUpdated()
    {
        mCurrentSongIndex = -1;
    }

    /**
     * Set index of a song that will be played
     */
    public void setSource(int playlistIndex, int index)
    {
        if(mCurrentPlaylistIndex != playlistIndex)
        {
            mAlreadyPlayedInShuffleMode.clear();
            mApp.getPlaylists().get(mCurrentPlaylistIndex).removeUpdateListener(this);
        }

        mApp.getPlaylists().get(playlistIndex).addUpdateListener(this);

        mCurrentPlaylistIndex = playlistIndex;
        mCurrentSongIndex = index;

        if(index < 0 || getSongsList() == null || index >= getSongsList().getCount())
            return;

        getSongsList().moveToPosition(index);
        putAction(new ActionPrepare(mApp.getComposition(getSongsList().getLong(0))));
    }

    @Override
    public void onAudioFocusChange(int focusChange)
    {
        if(focusChange == AudioManager.AUDIOFOCUS_LOSS || focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT)
        {
            pausePlayback();
        }
        else if(focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK)
        {
            //TODO: duck music
        }
    }

    private void setIsPlaying(boolean value)
    {
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
        final int STOP_SERVICE_COOLDOWN_MS = 3000;

        if(mStopCooldown != null)
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

    private void disableService()
    {
        stopForeground(false);
        NotificationManager notificationManager = (NotificationManager) mApp.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.abandonAudioFocus(this);
    }

    private void cancelHideCooldown()
    {
        if(mStopCooldown != null)
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
    public void togglePlaybackState()
    {
        if (isPlaying())
            pausePlayback();
        else
            startPlayback();
    }

    /**
     * Starts playback. setSource(...) have to be called before this method.
     */
    public void startPlayback()
    {
        putAction(new ActionPlay());
    }

    /**
     * Pauses playback.
     */
    public void pausePlayback()
    {
        putAction(new ActionPause());
    }

    /**
     * Stops playback.
     */
    public void stopPlayback()
    {
        putAction(new ActionStop());
    }

    /**
     * Returns the id of the current song index in the current playlist
     */
    public int getCurrentSongIndex()
    {
        return mCurrentSongIndex;
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
     * @param position position in milleseconds
     */
    public void seekTo(int position)
    {
        mPlayer.setPosition(position);
    }

    /**
     * Shift the current song duration
     * @param bpm original beat rate
     * @param shiftedBpm a beat rate to be played with
     */
    public void setNewPlayingBPM(float bpm, float shiftedBpm)
    {
        mCurrentlyPlayingBPM = shiftedBpm;
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
        }

        @Override
        public void doAction()
        {
            setIsPlaying(true);
            mPlayer.setOnPreparedListener(mOnPrepared);
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    mPlayer.setSource(mComposition.getAbsolutePath());
                }
            }).start();
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
