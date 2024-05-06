package com.juztoss.rhythmo;

import android.os.Environment;
import androidx.test.runner.AndroidJUnit4;
import android.util.Log;

import com.juztoss.rhythmo.audio.AdvancedMediaPlayer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.juztoss.rhythmo.TestHelper.MUSIC_FOLDER;
import static com.juztoss.rhythmo.TestHelper.getSongName;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class AdvancedMediaPlayerTest {
    public AdvancedMediaPlayerTest() {
        super();
    }

    private static String mPath;
    private static AdvancedMediaPlayer mPlayer;
    private static AdvancedMediaPlayer mPlayer2;

    @Before
    public void setUp()
    {
        System.loadLibrary(AdvancedMediaPlayer.LIBRARY_NAME);
        mPlayer = new AdvancedMediaPlayer(44100, 400);
        mPlayer2 = new AdvancedMediaPlayer(48000, 500);
        assertNotNull("Setup first player failed", mPlayer);
        assertNotNull("Setup second player failed", mPlayer2);
        mPath = Environment.getExternalStorageDirectory() + "/" + MUSIC_FOLDER + "/" + getSongName(1);
    }

    @After
    public void tearDown()
    {
        mPlayer.release();
        mPlayer2.release();
    }

    private void setSourceOnErrorTest(String source) throws Exception
    {
        final String finalSource = source;
        final CountDownLatch signal = new CountDownLatch(1);
        final AtomicBoolean onErrorIsCalled = new AtomicBoolean(false);

        mPlayer.setOnErrorListener(() -> {
            Log.d("DEBUG", "Error occurred");
            onErrorIsCalled.set(true);
            signal.countDown();
        });
        mPlayer.setOnPreparedListener(() -> {
            assertTrue("OnPrepared must haven't been called for source: " + finalSource, false);
            signal.countDown();
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

        mPlayer.setOnErrorListener(() -> {
            Log.d("DEBUG", "Error occurred");
            onErrorIsCalled.set(true);
            signal.countDown();
        });
        mPlayer.setOnPreparedListener(() -> signal.countDown());


        mPlayer.setSource(mPath);
        signal.await(2, TimeUnit.SECONDS);
        assertFalse("OnError mustn't have been called for correct source", onErrorIsCalled.get());
        assertTrue("Non zero signal count!", signal.getCount() == 0);
        mPlayer.setOnPreparedListener(null);
        mPlayer.setOnErrorListener(null);
    }

    private void doPlayback(AdvancedMediaPlayer player, CountDownLatch playbackFinished) throws Exception
    {
        final CountDownLatch signal = new CountDownLatch(1);
        player.setOnErrorListener(() -> {
            Log.d("DEBUG", "Error occurred");
            assertTrue("OnError must haven't been called for source: ", false);
            signal.countDown();
        });
        player.setOnPreparedListener(() -> {
            assertTrue("Not positive duration!", mPlayer.getDuration() > 0);
            assertTrue("Non zero position!", mPlayer.getPosition() == 0);
            mPlayer.play();
            mPlayer.pause();
            mPlayer.play();
            int position = mPlayer.getPosition() / 2;
            mPlayer.setPosition(position);
            assertTrue("Incorrect position after setPosition!", mPlayer.getPosition() == position);
            signal.countDown();
        });
        player.setOnEndListener(() -> {
            assertTrue("OnPrepared must haven't been called", false);
            signal.countDown();
        });

        assertTrue("Non-zero duration before setSource!", player.getDuration() == 0);
        player.setSource(mPath);
        signal.await(2, TimeUnit.SECONDS);
        assertTrue("Non zero signal count!", signal.getCount() == 0);
        playbackFinished.countDown();
    }

    @Test
    public void testPlayback() throws Exception
    {
        final CountDownLatch signal = new CountDownLatch(2);
        Runnable run1 = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    doPlayback(mPlayer, signal);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    assertTrue("Exception during playback", false);
                }
            }
        };
        Runnable run2 = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    doPlayback(mPlayer2, signal);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    assertTrue("Exception during playback", false);
                }
            }
        };

        new Thread(run1).start();
        new Thread(run2).start();
        signal.await(5, TimeUnit.SECONDS);
        assertTrue("Non zero signal count!", signal.getCount() == 0);
    }

    @Test
    public void testPlaybackPauseThenTheSamePlayback() throws Exception
    {
        for(int i = 0; i < 3; i++)
        {
            playbackPauseThenTheSamePlaybackStep(i);
        }
    }

    private void playbackPauseThenTheSamePlaybackStep(int iteration) throws Exception
    {
        final CountDownLatch signal = new CountDownLatch(1);
        Runnable run = () -> {
            try
            {
                final CountDownLatch signalPlayer1 = new CountDownLatch(1);
                final CountDownLatch signalPlayer2 = new CountDownLatch(2);
                mPlayer.setOnErrorListener(() -> {
                    Log.d("DEBUG", "Error occurred");
                    assertTrue("OnError must haven't been called for source: ", false);
                    signal.countDown();
                });
                mPlayer.setOnPreparedListener(() -> {
                    mPlayer.setBPM(100);
                    mPlayer.setNewBPM(100);
                    signalPlayer1.countDown();
                    signalPlayer2.countDown();
                });
                mPlayer.setOnEndListener(() -> {
                    assertTrue("OnPrepared must haven't been called", false);
                    signal.countDown();
                });
                mPlayer.setSource(mPath);
                signalPlayer1.await(5, TimeUnit.SECONDS);
                mPlayer.play();
                Thread.sleep(1000);
                mPlayer.pause();
                Thread.sleep(1000);
                mPlayer.setPosition(0);
                mPlayer.setPosition(10);
                mPlayer.setPosition(10);
                mPlayer.setSource(mPath);
                signalPlayer2.await(5, TimeUnit.SECONDS);

                assertTrue("Non zero signal1 count!", signalPlayer1.getCount() == 0);
                assertTrue("Non zero signal2 count!", signalPlayer2.getCount() == 0);
                signal.countDown();
            }
            catch (Exception e)
            {
                e.printStackTrace();
                assertTrue("Exception during playback", false);
            }
        };
        new Thread(run).start();

        signal.await(5, TimeUnit.SECONDS);
        assertTrue("Non zero signal count! for iteration: " + iteration, signal.getCount() == 0);
    }

    @Test
    public void testBPM() throws Exception
    {
        final CountDownLatch signal = new CountDownLatch(1);

        mPlayer.setOnErrorListener(() -> {
            Log.d("DEBUG", "Error occurred");
            assertTrue("OnError must haven't been called for source: ", false);
            signal.countDown();
        });
        mPlayer.setOnPreparedListener(() -> {
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
        });
        mPlayer.setOnEndListener(() -> {
            assertTrue("OnPrepared must haven't been called", false);
            signal.countDown();
        });

        mPlayer.setBPM(100);//There is no crash if setting a bpm before source
        mPlayer.setSource(mPath);
        signal.await(2, TimeUnit.SECONDS);
    }
}