package juztoss.com.bpmplayer.presenters;

import android.app.Application;
import android.content.res.Configuration;

import juztoss.com.bpmplayer.models.ISongSource;

/**
 * Created by JuzTosS on 4/16/2016.
 */
public class BPMPlayerApp extends Application {

    private ISongSource mSongsFolder;
    private BrowserPresenter mBrowserPresenter;
    private PlayerPresenter mPlayerPresenter;

    @Override
    public void onCreate() {
        super.onCreate();
        mBrowserPresenter = new BrowserPresenter(this);
        mPlayerPresenter = new PlayerPresenter(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public void setSongsFolder(ISongSource songsFolder) {
        mSongsFolder = songsFolder;
    }

    public BrowserPresenter getFileTreePresenter() {
        return mBrowserPresenter;
    }

    public PlayerPresenter getPlayerPresenter() {
        return mPlayerPresenter;
    }

    public ISongSource getSongsFolder() {
        return mSongsFolder;
    }
}
