package com.juztoss.bpmplayer.views;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.juztoss.bpmplayer.R;
import com.juztoss.bpmplayer.models.IExplorerElement;
import com.juztoss.bpmplayer.presenters.BrowserPresenter;

/**
 * Created by JuzTosS on 4/20/2016.
 */
public class BrowserFragment extends android.app.ListFragment implements IBaseRenderer {
    private BrowserPresenter mPresenter;

    private BrowserAdapter mBrowserAdapter;

    public void init(BrowserPresenter p) {
        mPresenter = p;
        mBrowserAdapter = new BrowserAdapter(getActivity(), R.layout.list_row);

        setListAdapter(mBrowserAdapter);
        getLoaderManager().initLoader(0, null, mPresenter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.listfragment_main, container, false);
    }

    @Override
    public void onListItemClick(ListView listView, android.view.View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);
        IExplorerElement element = mBrowserAdapter.getItem(position);
        if(!element.source().isDirectory())
            return;

        mPresenter.listItemClicked(element);
        getLoaderManager().restartLoader(0, null, mPresenter);
    }

    @Override
    public void update() {
        mBrowserAdapter.clear();
        mBrowserAdapter.addAll(mPresenter.getFileList());
        mBrowserAdapter.notifyDataSetChanged();
    }
}