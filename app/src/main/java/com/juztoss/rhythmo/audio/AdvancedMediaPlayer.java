package com.juztoss.rhythmo.audio;

import android.util.Log;

/**
 * Created by JuzTosS on 5/22/2016.
 * A media player with ability to stretch audio duration.
 * Uses SuperpoweredSDK and OpenSLES.
 */
public class AdvancedMediaPlayer
{
    private static int totalObjectsCreated = 0;
    public static String LIBRARY_NAME = "AdvancedMediaPlayer";
    private final int mId;

    private OnEndListener mOnEndListener;
    private OnErrorListener mOnErrorListener;
    private OnPreparedListener mOnPreparedListener;

    public AdvancedMediaPlayer(int samplerate, int buffersize)
    {
        mId = ++totalObjectsCreated;
        init(samplerate, buffersize);
    }

    /**
     * Is called when playback of audiofile ends
     */
    public void setOnEndListener(OnEndListener onEndListener)
    {
        mOnEndListener = onEndListener;
    }

    /**
     * Is called audio file is ready to play
     */
    public void setOnPreparedListener(OnPreparedListener onPreparedListener)
    {
        mOnPreparedListener = onPreparedListener;
    }

    /**
     * Is called when any error is occured
     */
    public void setOnErrorListener(OnErrorListener onErrorListener)
    {
        mOnErrorListener = onErrorListener;
    }

    private native void init(int samplerate, int buffersize);

    /**
     * Sets audio source for player, wait for the onPrepared event before call Play method.
     */
    public native void setSource(String path);

    /**
     * Starts playback
     */
    public native void play();

    /**
     * Pauses playback
     */
    public native void pause();

    /**
     * Return duration of file in milliseconds
     */
    public native int getDuration();

    /**
     * Return current position in milliseconds
     */
    public native int getPosition();

    /**
     * Sets current position in milliseconds
     */
    public native void setPosition(int offset);

    /**
     * Sets song original BPM, paired with setNewBPM(double)
     */
    public native void setBPM(double bpm);

    /**
     * Sets song new BPM to play with. Depends on original BPM that set by setBPM(double)
     */
    public native void setNewBPM(double bpm);

    /**
     * Free memory. Have to be called if player will never be used anymore.
     */
    private native void releaseNative();

    /**
     * Free memory and clear callbacks. Have to be called if player will never be used anymore.
     */
    public void release()
    {
        releaseNative();
        mOnEndListener = null;
        mOnErrorListener = null;
        mOnPreparedListener = null;
    }

    /**
     * Return id of player instance
     */
    private int getIdJNI()
    {
        return mId;
    }

    /**
     * JNI callbacks
     */
    private void onPrepared()
    {
        Log.d(AdvancedMediaPlayer.class.toString(), "onPreparedCalled");
        if(mOnPreparedListener != null)
            mOnPreparedListener.onPrepared();
    }

    private void onEnd()
    {
        Log.d(AdvancedMediaPlayer.class.toString(), "onEndCalled");
        if(mOnEndListener != null)
            mOnEndListener.onEnd();
    }

    private void onError()
    {
        Log.d(AdvancedMediaPlayer.class.toString(), "onErrorCalled");
        if(mOnErrorListener != null)
            mOnErrorListener.onError();
    }

    /**
     * Event listeners
     */

    public interface OnEndListener
    {
        void onEnd();
    }

    public interface OnPreparedListener
    {
        void onPrepared();
    }

    public interface OnErrorListener
    {
        void onError();
    }
}
