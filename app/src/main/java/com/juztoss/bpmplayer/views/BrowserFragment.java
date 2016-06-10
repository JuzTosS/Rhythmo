package com.juztoss.bpmplayer.views;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;

import com.juztoss.bpmplayer.R;
import com.juztoss.bpmplayer.models.BaseExplorerElement;
import com.juztoss.bpmplayer.presenters.BPMPlayerApp;
import com.juztoss.bpmplayer.presenters.BrowserPresenter;


/**
 * Created by JuzTosS on 4/20/2016.
 */
public class BrowserFragment extends ListFragment implements BrowserPresenter.OnDataChangedListener
{
    private BrowserAdapter mBrowserAdapter;
    private BPMPlayerApp mApp;
    private Switch mTypeSwitcher;

    private CompoundButton.OnCheckedChangeListener mSwitcherListener = new CompoundButton.OnCheckedChangeListener()
    {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
        {
            mApp.getBrowserPresenter().switchMode();
        }
    };

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mBrowserAdapter = new BrowserAdapter(getActivity(), R.layout.list_row);

        mApp = (BPMPlayerApp) getActivity().getApplicationContext();
        setListAdapter(mBrowserAdapter);
        getLoaderManager().initLoader(0, null, mApp.getBrowserPresenter());

        mTypeSwitcher = (Switch) getView().findViewById(R.id.browser_type_switcher);
        mTypeSwitcher.setOnCheckedChangeListener(mSwitcherListener);
        mApp.getBrowserPresenter().setOnDataChangedListener(this);

    }

    @Override
    public void onDataChanged()
    {
        mBrowserAdapter.clear();
        mBrowserAdapter.addAll(mApp.getBrowserPresenter().getList());
        mBrowserAdapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.listfragment_main, container, false);
    }

    @Override
    public void onListItemClick(ListView listView, android.view.View view, int position, long id)
    {
        super.onListItemClick(listView, view, position, id);
        BaseExplorerElement element = mBrowserAdapter.getItem(position);
        mApp.getBrowserPresenter().listItemClicked(element);
        getLoaderManager().restartLoader(0, null, mApp.getBrowserPresenter());
    }

}