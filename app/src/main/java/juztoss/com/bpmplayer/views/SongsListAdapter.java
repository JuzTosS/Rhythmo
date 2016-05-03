package juztoss.com.bpmplayer.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import juztoss.com.bpmplayer.R;
import juztoss.com.bpmplayer.models.IExplorerElement;

/**
 * Created by JuzTosS on 4/20/2016.
 */
public class SongsListAdapter extends ArrayAdapter<IExplorerElement> {
    private Context mContext;
    private int mResource;

    public SongsListAdapter(Context c, int res) {
        super(c, res);
        mContext = c;
        mResource = res;
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
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater inflater = (LayoutInflater.from(mContext));

            v = inflater.inflate(mResource, null);
        }

        TextView nameView = (TextView) v.findViewById(R.id.name_field);
        IExplorerElement file = getItem(position);

        nameView.setText(file.name());

        return v;
    }
}