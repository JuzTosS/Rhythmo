package com.juztoss.bpmplayer.audio;

import android.util.Log;

/**
 * Created by JuzTosS on 5/22/2016.
 */
public class AdvancedMediaPlayer
{
    private OnEndListener mOnEndListener;
    private OnErrorListener mOnErrorListener;
    private OnPreparedListener mOnPreparedListener;

    public AdvancedMediaPlayer(int samplerate, int buffersize)
    {
        init(samplerate, buffersize);
    }

    public void setOnEndListener(OnEndListener onEndListener)
    {
        mOnEndListener = onEndListener;
    }

    public void setOnPreparedListener(OnPreparedListener onPreparedListener)
    {
        mOnPreparedListener = onPreparedListener;
    }

    public void setOnErrorListener(OnErrorListener onErrorListener)
    {
        mOnErrorListener = onErrorListener;
    }
    
    private native void init(int samplerate, int buffersize);

    public native void setSource(String path);

    public native void play();

    public native void pause();

    public native int getDuration();

    public native int getPosition();

    public native void setPosition(int offset);

    public native void setBPM(double bpm);

    public native void setNewBPM(double bpm);

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

    private void onError(String message)
    {
        Log.d(AdvancedMediaPlayer.class.toString(), "onErrorCalled");
        if(mOnErrorListener != null)
            mOnErrorListener.onError(message);
    }

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
        void onError(String message);
    }
}
