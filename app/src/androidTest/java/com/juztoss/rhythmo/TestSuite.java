package com.juztoss.rhythmo;

import com.juztoss.rhythmo.audio.AdvancedMediaPlayer;

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
        RemoveMediaFromDevice.class,
})

public class TestSuite
{
    public static final String MUSIC_FOLDER = "RhythmoTestTemp";
}
