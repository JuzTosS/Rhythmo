package com.juztoss.rhythmo.views.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.juztoss.rhythmo.R;
import com.juztoss.rhythmo.models.BaseExplorerElement;
import com.juztoss.rhythmo.presenters.BrowserPresenter;
import com.juztoss.rhythmo.presenters.RhythmoApp;
import com.juztoss.rhythmo.views.adapters.BrowserAdapter;
import com.juztoss.rhythmo.views.adapters.BrowserElementHolder;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


/**
 * Created by JuzTosS on 4/20/2016.
 */
public class BrowserFragment extends Fragment implements BrowserElementHolder.IBrowserElementClickListener, BrowserPresenter.OnDataChangedListener
{
    private BrowserAdapter mBrowserAdapter;
    private RhythmoApp mApp;
    @BindView(R.id.folderPathLabel)
    protected TextView mFolderPathLabel;
    private Unbinder mUnbinder;

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

        mApp.getBrowserPresenter().addOnDataChangedListener(this);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        mUnbinder = ButterKnife.bind(this, view);
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();

        if(mUnbinder != null)
            mUnbinder.unbind();
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
        mFolderPathLabel.setText(mApp.getBrowserPresenter().getCurrent().getFileSystemPath());
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

        OnItemsStateChangedListener listener = (OnItemsStateChangedListener) getActivity();
        if(listener != null)
            listener.onItemsStateChanged();
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
    public void onDestroy()
    {
        super.onDestroy();
        mApp.getBrowserPresenter().removeOnDataChangedListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.browser_fragment, container, false);
    }

    public interface OnItemsStateChangedListener
    {
        void onItemsStateChanged();
    }

}