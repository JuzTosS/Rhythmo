package com.juztoss.bpmplayer;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.juztoss.bpmplayer.models.Playlist;
import com.juztoss.bpmplayer.models.Song;
import com.juztoss.bpmplayer.presenters.BPMPlayerApp;


/**
 * Created by JuzTosS on 5/3/2016.
 */
public class PlaybackService extends Service
{

    public static final int NOTIFICATION_ID = 42;
    public static final String LAUNCH_NOW_PLAYING_ACTION = "juztoss.com.bpmplayer.action.LAUNCH_NOW_PLAYING";
    public static final String SWITCH_PLAYBACK_ACTION = "juztoss.com.bpmplayer.action.SWITCH_PLAYBACK";
    public static final String UPDATE_UI_ACTION = "juztoss.com.bpmplayer.action.UPDATE_UI";

    private MediaPlayer mPlayer;
    private BPMPlayerApp mApp;
    private PlaybackService mSelf = this;
    private Playlist mPlaylist = new Playlist();
    private int mCurrentSongIndex = 0;
    private Queue<BaseAction> mQueue = new LinkedList<>();

    private BaseAction mActionInProgress;
    private boolean mIsPlaying = false;
    private MediaPlayer.OnErrorListener mOnErrorListener = new MediaPlayer.OnErrorListener()
    {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra)
        {
            Log.e(this.getClass().toString(), "MEDIA PLAYER ERROR! " + what + ":" + extra);
            return true;
        }
    };
    private MediaPlayer.OnCompletionListener mOnCompletionListener = new MediaPlayer.OnCompletionListener()
    {
        @Override
        public void onCompletion(MediaPlayer mp)
        {
            gotoNext();
        }
    };

    private void gotoNext()
    {
        clearQueue();
        putAction(new ActionPrepare(mPlaylist.songs().get(++mCurrentSongIndex)));//TODO: Proper next song processing
        putAction(new ActionPlay());
    }

    private void updateUI()
    {
        Intent intent = new Intent(UPDATE_UI_ACTION);
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(mApp);
        broadcastManager.sendBroadcast(intent);

        NotificationManager notifManager = (NotificationManager) mApp.getSystemService(Context.NOTIFICATION_SERVICE);
        notifManager.notify(NOTIFICATION_ID, PlaybackNotification.create(this));
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

        mPlayer.reset();
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
        mPlayer = new MediaPlayer();
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mPlayer.setOnErrorListener(mOnErrorListener);
        mPlayer.setOnCompletionListener(mOnCompletionListener);
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
        clearQueue();
        putAction(new ActionPrepare(mPlaylist.songs().get(index)));
    }

    private void setIsPlaying(boolean value)
    {
        mIsPlaying = value;
        if (mIsPlaying)
        {
            startForeground(NOTIFICATION_ID, PlaybackNotification.create(this));
        }
    }

    public boolean getIsPlaying()
    {
        return mIsPlaying;
    }

    public void togglePlaybackState()
    {
        if (getIsPlaying())
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

        private MediaPlayer.OnPreparedListener mOnPrepared = new MediaPlayer.OnPreparedListener()
        {
            @Override
            public void onPrepared(MediaPlayer mp)
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
            try
            {
                mPlayer.setOnPreparedListener(mOnPrepared);
                mPlayer.setDataSource(mSelf, android.net.Uri.fromFile(mSong.source()));
                mPlayer.prepareAsync();
            }
            catch (IOException e)
            {
                Log.e(this.getClass().toString(), e.toString());
            }
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
                mPlayer.start();
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
            mPlayer.stop();
            mPlayer.reset();
            updateUI();
            doNext();
        }
    }
}
