package com.juztoss.rhythmo.views.adapters;

import android.content.Context;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.juztoss.rhythmo.R;
import com.juztoss.rhythmo.models.BaseExplorerElement;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JuzTosS on 4/20/2016.
 */
public class BrowserAdapter extends RecyclerView.Adapter<BrowserElementHolder> implements BrowserElementHolder.IBrowserElementClickListener, FastScrollRecyclerView.SectionedAdapter
{
    private Context mContext;
    private BrowserElementHolder.IBrowserElementClickListener mOnItemClickListener;
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
            mOnItemClickListener.onItemClick(position);
    }

    @Override
    public void onActionClick(int position)
    {
        if (mOnItemClickListener != null)
            mOnItemClickListener.onActionClick(position);
    }

    @Override
    public BrowserElementHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = (LayoutInflater.from(mContext));
        View v = inflater.inflate(R.layout.browser_row, null);
        return new BrowserElementHolder(v, this);
    }

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

    public void setOnItemClickListener(BrowserElementHolder.IBrowserElementClickListener onItemClickListener)
    {
        mOnItemClickListener = onItemClickListener;
    }

    public void update(List<BaseExplorerElement> data)
    {
        mData = data;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public String getSectionName(int position)
    {
        return mData.get(position).name().substring(0,1).toUpperCase();
    }

    public BaseExplorerElement getItem(int position)
    {
        return mData.get(position);
    }
}