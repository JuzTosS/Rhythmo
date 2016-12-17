package com.juztoss.rhythmo;

import com.juztoss.rhythmo.audio.AdvancedMediaPlayer;
import com.juztoss.rhythmo.views.activities.PlayerActivityTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Created by JuzTosS on 8/21/2016.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        CopyMediaToDevice.class,
        TouchToWakeUp.class,
        AdvancedMediaPlayerTest.class,
        SettingsTest.class,
        OperationsWithPlaylists.class,
        PlayerActivityTest.class,
        RemoveMediaFromDevice.class,
})

public class TestSuite
{
    public static final String MUSIC_FOLDER = "RhythmoTestTemp";
    public static final String SONG1 = "audio220.mp3";
    public static final String SONG2 = "audio440.mp3";
    public static final String SONG3 = "audio880.mp3";
}
