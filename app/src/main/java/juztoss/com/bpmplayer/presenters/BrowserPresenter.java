package juztoss.com.bpmplayer.presenters;

import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Loader;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import juztoss.com.bpmplayer.models.FileTree;
import juztoss.com.bpmplayer.models.IExplorerElement;
import juztoss.com.bpmplayer.views.IBaseRenderer;

/**
 * Created by JuzTosS on 4/20/2016.
 */
public class BrowserPresenter extends BasePresenter implements LoaderManager.LoaderCallbacks<List<IExplorerElement>> {
    private IBaseRenderer mRenderer;
    private FileTree fileTree;
    private List<IExplorerElement> mData;
    private AsyncTaskLoader<List<IExplorerElement>> mFileLoader;

    public BrowserPresenter(BPMPlayerApp app)   {
        super(app);
    }

    public void init(IBaseRenderer renderer) {
        this.mRenderer = renderer;
        fileTree = new FileTree();
        mData = new ArrayList<>();
    }


    public void listItemClicked(IExplorerElement element) {
        if (element.source().isDirectory()) {
            getApp().setSongsFolder(element);
            fileTree.gotoNext(element.source());
        }
    }

    @Override
    public Loader<List<IExplorerElement>> onCreateLoader(int id, Bundle args) {
        mFileLoader = new AsyncTaskLoader<List<IExplorerElement>>(getApp()) {
            @Override
            public List<IExplorerElement> loadInBackground() {
                return fileTree.getAllFiles(fileTree.getCurrentDir());
            }
        };
        mFileLoader.forceLoad();
        return mFileLoader;
    }

    @Override
    public void onLoadFinished(Loader<List<IExplorerElement>> loader, List<IExplorerElement> data) {
        this.mData = data;
        mRenderer.update();
    }

    @Override
    public void onLoaderReset(Loader<List<IExplorerElement>> loader) {

    }

    public List<IExplorerElement> getFileList() {
        return mData;
    }
}