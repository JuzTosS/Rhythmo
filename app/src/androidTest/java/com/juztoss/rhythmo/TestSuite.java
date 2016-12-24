package com.juztoss.rhythmo;

import com.juztoss.rhythmo.audio.AdvancedMediaPlayer;
import com.juztoss.rhythmo.views.activities.FileWasDeleted;
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
        FileWasDeleted.class,
        PlayerActivityTest.class,
        RemoveMediaFromDevice.class,
})

public class TestSuite
{

}
