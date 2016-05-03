package juztoss.com.bpmplayer.presenters;

import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Loader;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.List;

import juztoss.com.bpmplayer.models.FileTree;
import juztoss.com.bpmplayer.models.ISongSource;
import juztoss.com.bpmplayer.models.Song;
import juztoss.com.bpmplayer.views.IBaseRenderer;

/**
 * Created by JuzTosS on 4/25/2016.
 */
public class PlayerPresenter extends BasePresenter implements LoaderManager.LoaderCallbacks<List<Song>> {
    public static final String BUNDLE_PATH = "path";
    private IBaseRenderer mRenderer;
    private AsyncTaskLoader<List<Song>> mSongsLoader;
    private FileTree fileTree = new FileTree();
    private List<Song> mCurrentSongsList;


    public PlayerPresenter(BPMPlayerApp app) {
        super(app);
    }

    public void init(IBaseRenderer renderer) {
        mRenderer = renderer;
    }

    @Override
    public Loader<List<Song>> onCreateLoader(int id, Bundle args) {
        ISongSource path = null;
        if(args != null)
            path = args.getParcelable(BUNDLE_PATH);

        final ISongSource pathClause = path;
        mSongsLoader = new AsyncTaskLoader<List<Song>>(getApp()) {
            @Override
            public List<Song> loadInBackground() {
                if(pathClause != null)
                    return fileTree.getSongsFiles(pathClause.source());
                else
                    return  new ArrayList<>();
            }

        };
        return mSongsLoader;
    }

    @Override
    public void onLoadFinished(Loader<List<Song>> loader, List<Song> data) {
        mCurrentSongsList = data;
        mRenderer.update();
    }

    @Override
    public void onLoaderReset(Loader<List<Song>> loader) {

    }

    public List<Song> getCurrentSongsList() {
        return mCurrentSongsList;
    }

    public ISongSource getSongsFolder() {
        return getApp().getSongsFolder();
    }
}
