package com.juztoss.rhythmo.views.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.juztoss.rhythmo.R;
import com.juztoss.rhythmo.models.BaseExplorerElement;
import com.juztoss.rhythmo.presenters.RhythmoApp;
import com.juztoss.rhythmo.presenters.BrowserPresenter;
import com.juztoss.rhythmo.views.adapters.BrowserAdapter;
import com.juztoss.rhythmo.views.adapters.BrowserElementHolder;


/**
 * Created by JuzTosS on 4/20/2016.
 */
public class BrowserFragment extends Fragment implements BrowserElementHolder.IBrowserElementClickListener, BrowserPresenter.OnDataChangedListener
{
    private BrowserAdapter mBrowserAdapter;
    private RhythmoApp mApp;

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        mApp = (RhythmoApp) getActivity().getApplicationContext();

        mBrowserAdapter = new BrowserAdapter(getActivity());
        RecyclerView list = (RecyclerView) getView().findViewById(R.id.listView);
        mBrowserAdapter.setOnItemClickListener(this);
        list.setAdapter(mBrowserAdapter);
        list.setLayoutManager(new LinearLayoutManager(getActivity()));

        mApp.getBrowserPresenter().setOnDataChangedListener(this);
    }

    @Override
    public void onStart()
    {
        super.onStart();
        mApp.getBrowserPresenter().clearAdded();
        mApp.getBrowserPresenter().setCurrent(mApp.getBrowserPresenter().getRoot());
        getLoaderManager().initLoader(0, null, mApp.getBrowserPresenter());
    }

    @Override
    public void onDataChanged()
    {
        mBrowserAdapter.update(mApp.getBrowserPresenter().getList());
    }

    @Override
    public void onActionClick(int position)
    {
        BaseExplorerElement element = mBrowserAdapter.getItem(position);
        if(element.getAddState() == BaseExplorerElement.AddState.NOT_ADDED)
            element.setAddState(BaseExplorerElement.AddState.ADDED);
        else if(element.getAddState() == BaseExplorerElement.AddState.ADDED)
            element.setAddState(BaseExplorerElement.AddState.NOT_ADDED);
        else if(element.getAddState() == BaseExplorerElement.AddState.PARTLY_ADDED)
            element.setAddState(BaseExplorerElement.AddState.NOT_ADDED);

//        onDataChanged();
    }

    @Override
    public void onItemClick(int position)
    {
        BaseExplorerElement element = mBrowserAdapter.getItem(position);
        if(element.hasChildren())
        {
            mApp.getBrowserPresenter().setCurrent(element);
            getLoaderManager().restartLoader(0, null, mApp.getBrowserPresenter());
        }
        else
        {
            onActionClick(position);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.browser_fragment, container, false);
    }

}