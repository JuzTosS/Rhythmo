package com.juztoss.bpmplayer;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.juztoss.bpmplayer.models.Playlist;
import com.juztoss.bpmplayer.models.Song;
import com.juztoss.bpmplayer.presenters.BPMPlayerApp;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


/**
 * Created by JuzTosS on 5/3/2016.
 */
public class PlaybackService extends Service implements AdvancedMediaPlayer.OnEndListener, AdvancedMediaPlayer.OnErrorListener
{

    public static final int NOTIFICATION_ID = 42;
    public static final String LAUNCH_NOW_PLAYING_ACTION = "com.juztoss.bpmplayer.action.LAUNCH_NOW_PLAYING";
    public static final String SWITCH_PLAYBACK_ACTION = "com.juztoss.bpmplayer.action.SWITCH_PLAYBACK";
    public static final String UPDATE_UI_ACTION = "com.juztoss.bpmplayer.action.UPDATE_UI";
    private static final String DEFAULT_SAMPLE_RATE = "44100";
    private static final String DEFAULT_BUFFER_SIZE = "512";

    private AdvancedMediaPlayer mPlayer;
    private BPMPlayerApp mApp;
    private PlaybackService mSelf = this;
    private Playlist mPlaylist = new Playlist();
    private int mCurrentSongIndex = 0;
    private Queue<BaseAction> mQueue = new LinkedList<>();

    private BaseAction mActionInProgress;
    private boolean mIsPlaying = false;

    @Override
    public void onEnd()
    {
        gotoNext();
    }

    @Override
    public void onError(String message)
    {
        clearQueue();
        Log.e(getResources().getString(R.string.app_name), "Player error: " + message);
    }

    public void gotoNext()
    {
        clearQueue();
        mCurrentSongIndex++;
        if (mCurrentSongIndex >= mPlaylist.songs().size())
            mCurrentSongIndex = mPlaylist.songs().size() - 1;

        putAction(new ActionPlay());
    }

    public void gotoPrevious()
    {
        clearQueue();
        mCurrentSongIndex--;
        if (mCurrentSongIndex < 0)
            mCurrentSongIndex = 0;

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

    private void putAction(BaseAction action)
    {
        mQueue.add(action);
        synchronized (this)
        {
            if (mActionInProgress == null)
                action.doNext();
        }
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
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        mApp = (BPMPlayerApp) getApplicationContext();
        mApp.setPlaybackService(this);

        System.loadLibrary("AdvancedMediaPlayer");

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

        final PlaybackService self = this;
        Runnable process = new Runnable()
        {
            @Override
            public void run()
            {
                mPlayer = new AdvancedMediaPlayer(sampleRate, bufferSize);
                mPlayer.setOnEndListener(self);
                mPlayer.setOnErrorListener(self);
            }
        };
        new Thread(process).start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy()
    {
        mApp.setPlaybackService(null);
        super.onDestroy();
    }

    public void setSource(int index)
    {
        final Song song = mPlaylist.songs().get(index);
        putAction(new ActionPrepare(song));
    }

    private void setIsPlaying(boolean value)
    {
        mIsPlaying = value;
        if (mIsPlaying)
            startForeground(NOTIFICATION_ID, PlaybackNotification.create(this));
    }

    public boolean isPlaying()
    {
        return mIsPlaying;
    }

    public void togglePlaybackState()
    {
        if (isPlaying())
            pausePlayback();
        else
            startPlayback();
    }

    public void startPlayback()
    {
        putAction(new ActionPlay());
    }

    public void pausePlayback()
    {
        putAction(new ActionPause());
    }

    public void stopPlayback()
    {
        putAction(new ActionStop());
    }

    public void resetPlaylist(List<Song> data)
    {
        mPlaylist.clear();
        mPlaylist.add(data);
        updateUI();
    }

    public Playlist getPlaylist()
    {
        return mPlaylist;
    }

    public int getCurrentSongIndex()
    {
        return mCurrentSongIndex;
    }

    public int getCurrentPosition()
    {
        return mPlayer.getPosition();
    }

    public int getDuration()
    {
        return mPlayer.getDuration();
    }

    public void seekTo(int position)
    {
        mPlayer.setPosition(position);
    }

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

    class ActionPrepare extends BaseAction
    {

        private AdvancedMediaPlayer.OnPreparedListener mOnPrepared = new AdvancedMediaPlayer.OnPreparedListener()
        {
            @Override
            public void onPrepared()
            {
                doNext();
            }
        };

        private Song mSong;

        public ActionPrepare(Song song)
        {
            mSong = song;
        }

        @Override
        public void doAction()
        {
            setIsPlaying(true);
            mPlayer.setOnPreparedListener(mOnPrepared);
            mPlayer.setSource(mSong.source().getAbsolutePath());
        }
    }

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
