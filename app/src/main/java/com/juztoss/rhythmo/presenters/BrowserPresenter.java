package com.juztoss.rhythmo.presenters;

import android.os.Bundle;

import com.juztoss.rhythmo.models.BaseExplorerElement;
import com.juztoss.rhythmo.models.MediaFolder;
import com.juztoss.rhythmo.models.PathStore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.juztoss.rhythmo.presenters.RhythmoApp.BROWSER_MODE_PATH;

import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;

/**
 * Created by JuzTosS on 4/20/2016.
 */
public class BrowserPresenter extends BasePresenter implements LoaderManager.LoaderCallbacks<List<BaseExplorerElement>>
{
    public static final String ONLY_FOLDERS = "OnlyFolders";

    private BaseExplorerElement mCurrent;
    private Set<OnDataChangedListener> mListeners = new HashSet<>();

    private List<BaseExplorerElement> mData;
    private List<BaseExplorerElement> mFolders;
    private BaseExplorerElement mRoot;
    private PathStore mPathStore = new PathStore();

    public BrowserPresenter(RhythmoApp app)
    {
        super(app);
        mRoot = new MediaFolder(-1, "", false, null, true, app);
        mCurrent = mRoot;
        mData = new ArrayList<>();
    }


    public BaseExplorerElement getRoot()
    {
        return mRoot;
    }

    public void setCurrent(BaseExplorerElement element)
    {
        mCurrent = element;
    }

    @Override
    public Loader<List<BaseExplorerElement>> onCreateLoader(int id, Bundle args)
    {
        AsyncTaskLoader<List<BaseExplorerElement>> fileLoader = new AsyncTaskLoader<List<BaseExplorerElement>>(getApp())
        {
            @Override
            public List<BaseExplorerElement> loadInBackground()
            {
                boolean onlyFolders = false;
                if(args != null) {
                    onlyFolders = args.getBoolean(ONLY_FOLDERS, false);
                }
                return mCurrent.getChildren(onlyFolders);
            }
        };
        fileLoader.forceLoad();
        return fileLoader;
    }

    @Override
    public void onLoadFinished(Loader<List<BaseExplorerElement>> loader, List<BaseExplorerElement> data)
    {
        mData = data;
        notifyListeners();
    }

    private void notifyListeners()
    {
        for(OnDataChangedListener listener : mListeners)
        {
            listener.onDataChanged();
        }
    }

    @Override
    public void onLoaderReset(Loader<List<BaseExplorerElement>> loader)
    {

    }

    public void addOnDataChangedListener(OnDataChangedListener listener)
    {
        mListeners.add(listener);
    }

    public void removeOnDataChangedListener(OnDataChangedListener listener)
    {
        mListeners.remove(listener);
    }

    public List<BaseExplorerElement> getList()
    {
        return mData;
    }

    public BaseExplorerElement getCurrent()
    {
        return mCurrent;
    }

    public BaseExplorerElement restoreSavedElement() {

        String path = getApp().getSharedPreferences().getString(BROWSER_MODE_PATH, "");
        if (path.isEmpty())
            return getRoot();


        BaseExplorerElement result = getRoot().getChildFromPath(path, true);
        return result == null ? getRoot() : result;
    }

    public void storeCurrentElement() {
        getApp().getSharedPreferences().edit().putString(
                BROWSER_MODE_PATH, mCurrent.getFileSystemPath())
                .apply();

    }

    public interface OnDataChangedListener
    {
        void onDataChanged();
    }

    public void clear()
    {
        if(mRoot != null)
            mRoot.dispose();

        mRoot = new MediaFolder(-1, "", false, null, true, getApp());
        mPathStore.clearAdded();
    }

    public void remove(String path, List<BaseExplorerElement> siblings)
    {
        mPathStore.remove(path, siblings);
    }

    public void add(String path)
    {
        mPathStore.add(path);
    }

    public BaseExplorerElement.AddState getAddState(String path, List<BaseExplorerElement> siblings)
    {
        return mPathStore.getAddState(path, siblings.size() - 1);// -1 because we have the back button as a child
    }

    public String[] getPaths()
    {
        if (!mPathStore.isEmpty())
            return mPathStore.getPaths();
        else
            return new String[]{};
    }
}