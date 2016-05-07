package juztoss.com.bpmplayer.views;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;

import juztoss.com.bpmplayer.PlaybackService;
import juztoss.com.bpmplayer.R;
import juztoss.com.bpmplayer.models.Song;
import juztoss.com.bpmplayer.presenters.BPMPlayerApp;
import juztoss.com.bpmplayer.presenters.PlayerPresenter;

/**
 * Created by JuzTosS on 4/20/2016.
 */
public class PlayerFragment extends android.app.Fragment implements IBaseRenderer, DrawerLayout.DrawerListener, AdapterView.OnItemClickListener
{
    private PlayerPresenter mPresenter;
    private SongsListAdapter mSongsListAdapter;
    private View mPlayButton;
    private BPMPlayerApp mApp;

    public void init(PlayerPresenter p)
    {
        mPresenter = p;
        mApp = (BPMPlayerApp) getActivity().getApplicationContext();
        mSongsListAdapter = new SongsListAdapter(getActivity(), R.layout.song_list_element);
        ListView list = (ListView) getView().findViewById(R.id.listView);
        list.setOnItemClickListener(this);
        list.setAdapter(mSongsListAdapter);

        DrawerLayout drawer = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
        drawer.addDrawerListener(this);

        mPlayButton = getView().findViewById(R.id.play_button);
        mPlayButton.setOnClickListener(mPlayButtonListenter);

        getLoaderManager().initLoader(0, null, mPresenter);

        LocalBroadcastManager.getInstance(mApp).registerReceiver(mUpdateUIReceiver, new IntentFilter(PlaybackService.UPDATE_UI_ACTION));
    }

    BroadcastReceiver mUpdateUIReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if(!mApp.isPlaybackServiceRunning()) return;

            mPlayButton.setSelected(!mApp.getPlaybackService().getIsPlaying());
        }
    };

    private View.OnClickListener mPlayButtonListenter = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            if (mApp.isPlaybackServiceRunning())
                mApp.getPlaybackService().togglePlaybackState();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.songs_list, container, false);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        Song song = (Song) mSongsListAdapter.getItem(position);
        if (mApp.isPlaybackServiceRunning())
        {
            mApp.getPlaybackService().setSource(song);
            mApp.getPlaybackService().startPlayback();
        }

    }

    @Override
    public void update()
    {
        mSongsListAdapter.clear();
        mSongsListAdapter.addAll(mPresenter.getCurrentSongsList());
        mSongsListAdapter.notifyDataSetChanged();
    }


    @Override
    public void onDrawerSlide(View drawerView, float slideOffset)
    {
    }

    @Override
    public void onDrawerOpened(View drawerView)
    {
    }

    @Override
    public void onDrawerClosed(View drawerView)
    {
        updateList();
    }

    private void updateList()
    {
        Bundle path = new Bundle();
        path.putParcelable(PlayerPresenter.BUNDLE_PATH, mPresenter.getSongsFolder());
        Loader<List<Song>> loader = getLoaderManager().restartLoader(0, path, mPresenter);
        loader.forceLoad();
    }


    @Override
    public void onDrawerStateChanged(int newState)
    {

    }
}