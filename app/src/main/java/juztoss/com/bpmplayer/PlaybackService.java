package juztoss.com.bpmplayer;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import juztoss.com.bpmplayer.models.Song;
import juztoss.com.bpmplayer.presenters.BPMPlayerApp;


/**
 * Created by JuzTosS on 5/3/2016.
 */
public class PlaybackService extends Service {
    private MediaPlayer mPlayer;
    private BPMPlayerApp mApp;
    private PlaybackService mSelf = this;

    private Queue<BaseAction> mQueue = new LinkedList<>();

    private BaseAction mActionInProgress;
    private boolean mIsPlaying = false;

    private void putAction(BaseAction action) {
        mQueue.add(action);
        synchronized (this) {
            if (mActionInProgress == null)
                action.doNext();
        }
    }

    private void clearQueue() {
        mQueue.clear();
        if (mActionInProgress != null)
            mActionInProgress.interrupt();

        mPlayer.reset();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mApp = (BPMPlayerApp) getApplicationContext();
        mApp.setPlaybackService(this);
        mPlayer = new MediaPlayer();
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        mApp.setPlaybackService(null);
        super.onDestroy();
    }

    public void setSource(Song song) {
        clearQueue();
        putAction(new ActionPrepare(song));
    }

    private boolean isPlaying() {
        return mIsPlaying;
    }

    public void togglePlaybackState() {
        if (isPlaying())
            pausePlayback();
        else
            startPlayback();
    }

    public void startPlayback() {
        putAction(new ActionPlay());
    }

    public void pausePlayback() {
        putAction(new ActionPause());
    }

    public void stopPlayback() {
        putAction(new ActionStop());
    }

    private class BaseAction {
        public void doAction() {
            doNext();
        }

        public final void doNext() {
            if (mQueue.size() > 0) {
                mActionInProgress = mQueue.remove();
                mActionInProgress.doAction();
            } else {
                mActionInProgress = null;
            }
        }

        public void interrupt() {
        }
    }

    class ActionPrepare extends BaseAction {

        private MediaPlayer.OnPreparedListener mOnPrepared = new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                doNext();
            }
        };

        private Song mSong;

        public ActionPrepare(Song song) {
            mSong = song;
        }

        @Override
        public void doAction() {
            mIsPlaying = true;
            try {
                mPlayer.setOnPreparedListener(mOnPrepared);
                mPlayer.setDataSource(mSelf, android.net.Uri.fromFile(mSong.source()));
                mPlayer.prepareAsync();
            } catch (IOException e) {
                Log.e(this.getClass().toString(), e.toString());
            }
        }
    }

    class ActionPlay extends BaseAction {
        @Override
        public void doAction() {
            mIsPlaying = true;
            try {
                mPlayer.start();
            } catch (IllegalStateException e) {
                mIsPlaying = false;
            } finally {
                doNext();
            }
        }
    }

    class ActionPause extends BaseAction {
        @Override
        public void doAction() {
            mIsPlaying = false;
            mPlayer.pause();
            doNext();
        }
    }

    class ActionStop extends ActionPause {
        @Override
        public void doAction() {
            mIsPlaying = false;
            mPlayer.seekTo(0);
            super.doAction();
        }
    }
}
