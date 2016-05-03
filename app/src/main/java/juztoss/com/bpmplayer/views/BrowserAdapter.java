package juztoss.com.bpmplayer.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import juztoss.com.bpmplayer.R;
import juztoss.com.bpmplayer.models.IExplorerElement;

/**
 * Created by JuzTosS on 4/20/2016.
 */
public class BrowserAdapter extends ArrayAdapter<IExplorerElement> {
    private Context mContext;
    private int mResource;

    public BrowserAdapter(Context c, int res) {
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

        /* We pull out the ImageView and TextViews so we can set their properties.*/
        ImageView iv = (ImageView) v.findViewById(R.id.imageView);

        TextView nameView = (TextView) v.findViewById(R.id.name_text_view);

        IExplorerElement file = getItem(position);

        /* If the file is a dir, set the image view's image to a folder, else, a file. */
        if (file.source().isDirectory()) {
            iv.setImageResource(R.drawable.ic_folder_black_24dp);
        } else {
            iv.setImageResource(0);
        }

        //Finally, set the name of the file or directory.
        nameView.setText(file.name());

        //Send the view back so the ListView can show it as a row, the way we modified it.
        return v;
    }
}