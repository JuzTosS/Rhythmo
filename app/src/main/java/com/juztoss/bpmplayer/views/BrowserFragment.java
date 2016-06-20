package com.juztoss.bpmplayer.views;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.juztoss.bpmplayer.R;
import com.juztoss.bpmplayer.models.BaseExplorerElement;
import com.juztoss.bpmplayer.presenters.BPMPlayerApp;
import com.juztoss.bpmplayer.presenters.BrowserPresenter;


/**
 * Created by JuzTosS on 4/20/2016.
 */
public class BrowserFragment extends Fragment implements IOnItemClickListener, BrowserPresenter.OnDataChangedListener
{
    private BrowserAdapter mBrowserAdapter;
    private BPMPlayerApp mApp;

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        mApp = (BPMPlayerApp) getActivity().getApplicationContext();

        mBrowserAdapter = new BrowserAdapter(getActivity());
        RecyclerView list = (RecyclerView) getView().findViewById(R.id.listView);
        mBrowserAdapter.setOnItemClickListener(this);
        list.setAdapter(mBrowserAdapter);
        list.setLayoutManager(new LinearLayoutManager(getActivity()));

        getLoaderManager().initLoader(0, null, mApp.getBrowserPresenter());

        mApp.getBrowserPresenter().setOnDataChangedListener(this);
    }

    @Override
    public void onDataChanged()
    {
        mBrowserAdapter.update(mApp.getBrowserPresenter().getList());
    }

    @Override
    public void onItemClick(int position)
    {
        BaseExplorerElement element = mBrowserAdapter.getItem(position);
        if(element.hasChildren())
        {
            mApp.getBrowserPresenter().listItemClicked(element);
            getLoaderManager().restartLoader(0, null, mApp.getBrowserPresenter());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.browser_fragment, container, false);
    }

}