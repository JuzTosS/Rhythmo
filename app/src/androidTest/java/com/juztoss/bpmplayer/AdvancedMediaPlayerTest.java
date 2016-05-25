package com.juztoss.bpmplayer;

import android.test.InstrumentationTestCase;
import android.util.Log;

import com.juztoss.bpmplayer.audio.AdvancedMediaPlayer;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class AdvancedMediaPlayerTest extends InstrumentationTestCase
{
    private static AdvancedMediaPlayer mPlayer;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();

        System.loadLibrary(AdvancedMediaPlayer.LIBRARY_NAME);
        mPlayer = new AdvancedMediaPlayer(44100, 400);
        assertNotNull("Setup failed", mPlayer);
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        mPlayer.release();
    }

    private void setSourceOnErrorTest(String source) throws Exception
    {
        final String finalSource = source;
        final CountDownLatch signal = new CountDownLatch(1);
        final AtomicBoolean onErrorIsCalled = new AtomicBoolean(false);

        mPlayer.setOnErrorListener(new AdvancedMediaPlayer.OnErrorListener()
        {
            @Override
            public void onError(String message)
            {
                Log.d("DEBUG", "Error message is: " + message);
                onErrorIsCalled.set(true);
                signal.countDown();
            }
        });
        mPlayer.setOnPreparedListener(new AdvancedMediaPlayer.OnPreparedListener()
        {
            @Override
            public void onPrepared()
            {
                assertTrue("OnPrepared must haven't been called for source: " + finalSource, false);
                signal.countDown();
            }
        });

        mPlayer.setSource(source);
        signal.await(2, TimeUnit.SECONDS);
        assertTrue("OnError must have been called for source: " + source, onErrorIsCalled.get());
        mPlayer.setOnPreparedListener(null);
        mPlayer.setOnErrorListener(null);
    }

    public void testSetSourceOnError() throws Exception
    {
        setSourceOnErrorTest(null);
        setSourceOnErrorTest("");
        setSourceOnErrorTest("Path/doesnt/exist");
        setSourceOnPreparedTest();
    }

    private void setSourceOnPreparedTest() throws Exception
    {
        final CountDownLatch signal = new CountDownLatch(1);
        final AtomicBoolean onErrorIsCalled = new AtomicBoolean(false);

        mPlayer.setOnErrorListener(new AdvancedMediaPlayer.OnErrorListener()
        {
            @Override
            public void onError(String message)
            {
                Log.d("DEBUG", "Error message is: " + message);
                onErrorIsCalled.set(true);
                signal.countDown();
            }
        });
        mPlayer.setOnPreparedListener(new AdvancedMediaPlayer.OnPreparedListener()
        {
            @Override
            public void onPrepared()
            {
                signal.countDown();
            }
        });


        mPlayer.setSource(getInstrumentation().getContext().getPackageResourcePath());
        signal.await(2, TimeUnit.SECONDS);
        assertFalse("OnError mustn't have been called for correct source", onErrorIsCalled.get());
        mPlayer.setOnPreparedListener(null);
        mPlayer.setOnErrorListener(null);
    }

    public void testPlayback() throws Exception
    {
        final CountDownLatch signal = new CountDownLatch(1);

        mPlayer.setOnErrorListener(new AdvancedMediaPlayer.OnErrorListener()
        {
            @Override
            public void onError(String message)
            {
                Log.d("DEBUG", "Error message is: " + message);
                assertTrue("OnError must haven't been called for source: ", false);
                signal.countDown();
            }
        });
        mPlayer.setOnPreparedListener(new AdvancedMediaPlayer.OnPreparedListener()
        {
            @Override
            public void onPrepared()
            {
                assertTrue("Not positive duration!", mPlayer.getDuration() > 0);
                assertTrue("Non zero position!", mPlayer.getPosition() == 0);
                mPlayer.play();
                mPlayer.pause();
                mPlayer.play();
                int position = mPlayer.getPosition() / 2;
                mPlayer.setPosition(position);
                assertTrue("Incorrect position after setPosition!", mPlayer.getPosition() == position);
                signal.countDown();
            }
        });
        mPlayer.setOnEndListener(new AdvancedMediaPlayer.OnEndListener()
        {
            @Override
            public void onEnd()
            {
                assertTrue("OnPrepared must haven't been called", false);
                signal.countDown();
            }
        });

        assertTrue("Non-zero duration before setSource!", mPlayer.getDuration() == 0);
        mPlayer.setSource(getInstrumentation().getContext().getPackageResourcePath());
        signal.await(2, TimeUnit.SECONDS);
    }

    public void testBPM() throws Exception
    {
        final CountDownLatch signal = new CountDownLatch(1);

        mPlayer.setOnErrorListener(new AdvancedMediaPlayer.OnErrorListener()
        {
            @Override
            public void onError(String message)
            {
                Log.d("DEBUG", "Error message is: " + message);
                assertTrue("OnError must haven't been called for source: ", false);
                signal.countDown();
            }
        });
        mPlayer.setOnPreparedListener(new AdvancedMediaPlayer.OnPreparedListener()
        {
            @Override
            public void onPrepared()
            {
                mPlayer.play();
                mPlayer.setBPM(-1);
                mPlayer.setNewBPM(120);
                mPlayer.setBPM(0);
                mPlayer.setNewBPM(120);
                mPlayer.setBPM(5);
                mPlayer.setNewBPM(120);
                mPlayer.setBPM(100);
                mPlayer.setNewBPM(120);
                mPlayer.setNewBPM(30);
                mPlayer.setNewBPM(0);
            }
        });
        mPlayer.setOnEndListener(new AdvancedMediaPlayer.OnEndListener()
        {
            @Override
            public void onEnd()
            {
                assertTrue("OnPrepared must haven't been called", false);
                signal.countDown();
            }
        });

        mPlayer.setBPM(100);//There is no crash if setting a bpm before source
        mPlayer.setSource(getInstrumentation().getContext().getPackageResourcePath());
        signal.await(2, TimeUnit.SECONDS);
    }
}