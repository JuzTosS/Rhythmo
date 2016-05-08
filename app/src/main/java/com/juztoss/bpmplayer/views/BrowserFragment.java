package com.juztoss.bpmplayer.views;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.juztoss.bpmplayer.R;
import com.juztoss.bpmplayer.models.IExplorerElement;
import com.juztoss.bpmplayer.presenters.BPMPlayerApp;
import com.juztoss.bpmplayer.presenters.BrowserPresenter;

/**
 * Created by JuzTosS on 4/20/2016.
 */
public class BrowserFragment extends android.app.ListFragment implements DrawerLayout.DrawerListener
{
    private BrowserAdapter mBrowserAdapter;
    private BPMPlayerApp mApp;

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mBrowserAdapter = new BrowserAdapter(getActivity(), R.layout.list_row);

        mApp = (BPMPlayerApp) getActivity().getApplicationContext();
        setListAdapter(mBrowserAdapter);
        getLoaderManager().initLoader(0, null, mApp.getBrowserPresenter());

        DrawerLayout drawer = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
        LocalBroadcastManager.getInstance(mApp).registerReceiver(mUpdateUIReceiver, new IntentFilter(BrowserPresenter.UPDATE_FILE_TREE));
        drawer.addDrawerListener(this);
    }

    private BroadcastReceiver mUpdateUIReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            updateList();
        }
    };

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

        mApp.getBrowserPresenter().listItemClicked(element);
        getLoaderManager().restartLoader(0, null, mApp.getBrowserPresenter());
    }

    private void updateList() {
        mBrowserAdapter.clear();
        mBrowserAdapter.addAll(mApp.getBrowserPresenter().getFileList());
        mBrowserAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDrawerSlide(View drawerView, float slideOffset)
    {

    }

    @Override
    public void onDrawerOpened(View drawerView)
    {
        getLoaderManager().restartLoader(0, null, mApp.getBrowserPresenter());
    }

    @Override
    public void onDrawerClosed(View drawerView)
    {

    }

    @Override
    public void onDrawerStateChanged(int newState)
    {

    }
}