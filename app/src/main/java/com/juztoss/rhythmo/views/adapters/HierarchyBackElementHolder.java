package com.juztoss.rhythmo.views.adapters;

import android.view.View;

import com.juztoss.rhythmo.models.ParentLink;
import com.juztoss.rhythmo.services.PlaybackService;

/**
 * Created by JuzTosS on 4/20/2016.
 */
public class HierarchyBackElementHolder extends BaseHierarchyElementHolder<ParentLink>
{
    public HierarchyBackElementHolder(View v, IOnItemClickListener mListener) {
        super(v);
        v.setOnClickListener(view -> {
            mListener.onPlaylistItemClick(getAdapterPosition(), ACTION_OPEN, null);
        });
    }

    @Override
    public void update(ParentLink element, PlaybackService service) {
    }
}