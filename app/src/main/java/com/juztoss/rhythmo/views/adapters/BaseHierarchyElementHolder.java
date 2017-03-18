package com.juztoss.rhythmo.views.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.juztoss.rhythmo.models.BaseExplorerElement;
import com.juztoss.rhythmo.services.PlaybackService;

public abstract class BaseHierarchyElementHolder<T extends BaseExplorerElement> extends RecyclerView.ViewHolder {

    public static final int ACTION_PLAY = 0;
    public static final int ACTION_REMOVE = 1;
    public static final int ACTION_SHOW_DETAIL = 2;
    public static final int ACTION_OPEN = 3;

    public BaseHierarchyElementHolder(View itemView) {
        super(itemView);
    }

    public abstract void update(T element, PlaybackService service);
}
