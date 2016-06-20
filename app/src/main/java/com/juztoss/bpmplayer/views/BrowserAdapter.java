package com.juztoss.bpmplayer.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.juztoss.bpmplayer.R;
import com.juztoss.bpmplayer.models.BaseExplorerElement;
import com.juztoss.bpmplayer.models.Composition;
import com.juztoss.bpmplayer.presenters.BPMPlayerApp;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JuzTosS on 4/20/2016.
 */
public class BrowserAdapter extends RecyclerView.Adapter<BrowserElementHolder> implements IOnItemClickListener
{
    private Context mContext;
    private IOnItemClickListener mOnItemClickListener;
    private List<BaseExplorerElement> mData = new ArrayList<>();

    public BrowserAdapter(Context context)
    {
        super();
        mContext = context;
    }

    @Override
    public void onItemClick(int position)
    {
        if (mOnItemClickListener != null)
        {
            mOnItemClickListener.onItemClick(position);
        }
    }

    @Override
    public BrowserElementHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = (LayoutInflater.from(mContext));
        View v = inflater.inflate(R.layout.browser_row, null);
        return new BrowserElementHolder(v, this);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(BrowserElementHolder holder, int position)
    {
        holder.update(mData.get(position), position);
    }

    @Override
    public int getItemCount()
    {
        return mData.size();
    }

    public void setOnItemClickListener(IOnItemClickListener onItemClickListener)
    {
        mOnItemClickListener = onItemClickListener;
    }

    public void update(List<BaseExplorerElement> data)
    {
        mData = data;
        notifyDataSetChanged();
    }

    public BaseExplorerElement getItem(int position)
    {
        return mData.get(position);
    }
}