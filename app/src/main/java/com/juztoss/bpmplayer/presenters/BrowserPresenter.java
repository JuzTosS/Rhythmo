package com.juztoss.bpmplayer.presenters;

import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.juztoss.bpmplayer.models.FileTree;
import com.juztoss.bpmplayer.models.IExplorerElement;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JuzTosS on 4/20/2016.
 */
public class BrowserPresenter extends BasePresenter implements LoaderManager.LoaderCallbacks<List<IExplorerElement>>
{
    public static final String UPDATE_FILE_TREE = "com.juztoss.bpmplayer.action.UPDATE_FILE_TREE";
    private FileTree fileTree;
    private List<IExplorerElement> mData;
    private AsyncTaskLoader<List<IExplorerElement>> mFileLoader;

    public BrowserPresenter(BPMPlayerApp app)
    {
        super(app);
        fileTree = new FileTree();
        mData = new ArrayList<>();
    }

    public void listItemClicked(IExplorerElement element)
    {
        if (element.source().isDirectory())
        {
            getApp().setSongsFolder(element);
            fileTree.gotoNext(element.source());
        }
    }

    @Override
    public Loader<List<IExplorerElement>> onCreateLoader(int id, Bundle args)
    {
        mFileLoader = new AsyncTaskLoader<List<IExplorerElement>>(getApp())
        {
            @Override
            public List<IExplorerElement> loadInBackground()
            {
                return fileTree.getAllFiles(fileTree.getCurrentDir());
            }
        };
        mFileLoader.forceLoad();
        return mFileLoader;
    }

    @Override
    public void onLoadFinished(Loader<List<IExplorerElement>> loader, List<IExplorerElement> data)
    {
        this.mData = data;
        Intent intent = new Intent(UPDATE_FILE_TREE);
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(getApp());
        broadcastManager.sendBroadcast(intent);
    }

    @Override
    public void onLoaderReset(Loader<List<IExplorerElement>> loader)
    {

    }

    public List<IExplorerElement> getFileList()
    {
        return mData;
    }
}