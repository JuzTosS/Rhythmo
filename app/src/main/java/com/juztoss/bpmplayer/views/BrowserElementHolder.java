package com.juztoss.bpmplayer.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.juztoss.bpmplayer.R;
import com.juztoss.bpmplayer.models.BaseExplorerElement;
import com.juztoss.bpmplayer.models.Composition;
import com.juztoss.bpmplayer.presenters.BPMPlayerApp;

/**
 * Created by JuzTosS on 6/18/2016.
 */
public class BrowserElementHolder extends RecyclerView.ViewHolder
{
    private int mPosition;
    private IOnItemClickListener mListener;

    public BrowserElementHolder(View view, IOnItemClickListener listener)
    {
        super(view);
        mListener = listener;
        view.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mListener.onItemClick(mPosition);
            }
        });
        mListener = listener;
    }

    @SuppressLint("DefaultLocale")
    public void update(BaseExplorerElement element, int position)
    {
        mPosition = position;
        ((TextView)itemView.findViewById(R.id.name_text_view)).setText(element.name());
    }
}
