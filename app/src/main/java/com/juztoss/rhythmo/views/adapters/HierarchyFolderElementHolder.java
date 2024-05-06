package com.juztoss.rhythmo.views.adapters;

import android.view.View;
import android.widget.TextView;

import com.juztoss.rhythmo.R;
import com.juztoss.rhythmo.models.MediaFolder;
import com.juztoss.rhythmo.services.PlaybackService;

/**
 * Created by JuzTosS on 4/20/2016.
 */
public class HierarchyFolderElementHolder extends BaseHierarchyElementHolder<MediaFolder> {

    TextView mName;
    TextView mDesc;

    public HierarchyFolderElementHolder(View itemView, IOnItemClickListener listener) {
        super(itemView);
        mName = itemView.findViewById(R.id.name_text_view);
        mDesc = itemView.findViewById(R.id.desc_text_view);

        itemView.setOnClickListener(v -> {
            listener.onPlaylistItemClick(getAdapterPosition(), ACTION_OPEN, null);
        });
    }

    @Override
    public void update(MediaFolder element, PlaybackService service) {
        mName.setText(element.name());
        mDesc.setText(element.description());
    }
}