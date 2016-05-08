package com.juztoss.bpmplayer.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.juztoss.bpmplayer.R;
import com.juztoss.bpmplayer.models.IExplorerElement;
import com.juztoss.bpmplayer.presenters.BPMPlayerApp;

/**
 * Created by JuzTosS on 4/20/2016.
 */
public class SongsListAdapter extends BaseAdapter
{
    private BPMPlayerApp mApp;
    private Context mContext;

    public SongsListAdapter(Context context)
    {
        mContext = context;
        mApp = (BPMPlayerApp) context.getApplicationContext();
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public Object getItem(int position)
    {
        if (mApp.isPlaybackServiceRunning())
        {
            return mApp.getPlaybackService().getPlaylist().songs().get(position);
        }
        return null;
    }

    @Override
    public int getCount()
    {
        if (mApp.isPlaybackServiceRunning())
        {
            return mApp.getPlaybackService().getPlaylist().songs().size();
        }
        return 0;
    }

    /**
     * Allows me to pull out specific views from the row xml file for the ListView.   I can then
     * make any modifications I want to the ImageView and TextViews inside it.
     *
     * @param position    - The position of an item in the List received from my model.
     * @param convertView - list_row.xml as a View object.
     * @param parent      - The parent ViewGroup that holds the rows.  In this case, the ListView.
     ***/
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View v = convertView;
        if (v == null)
        {
            LayoutInflater inflater = (LayoutInflater.from(mContext));

            v = inflater.inflate(R.layout.song_list_element, null);
        }

        TextView nameView = (TextView) v.findViewById(R.id.name_field);
        IExplorerElement file = (IExplorerElement) getItem(position);

        nameView.setText(file.name());

        return v;
    }
}