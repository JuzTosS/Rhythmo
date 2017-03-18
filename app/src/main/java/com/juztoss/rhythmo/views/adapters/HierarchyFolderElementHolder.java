package com.juztoss.rhythmo.views.adapters;

import android.view.View;
import android.widget.TextView;

import com.juztoss.rhythmo.R;
import com.juztoss.rhythmo.models.MediaFolder;
import com.juztoss.rhythmo.services.PlaybackService;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by JuzTosS on 4/20/2016.
 */
public class HierarchyFolderElementHolder extends BaseHierarchyElementHolder<MediaFolder> {

    @BindView(R.id.name_text_view) TextView mName;
    @BindView(R.id.desc_text_view) TextView mDesc;

    public HierarchyFolderElementHolder(View itemView, IOnItemClickListener listener) {
        super(itemView);
        ButterKnife.bind(this, itemView);

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