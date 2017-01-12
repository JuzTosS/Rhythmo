package com.juztoss.rhythmo.views.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.juztoss.rhythmo.R;
import com.juztoss.rhythmo.models.BaseExplorerElement;
import com.juztoss.rhythmo.utils.SystemHelper;

/**
 * Created by JuzTosS on 6/18/2016.
 */
public class BrowserElementHolder extends RecyclerView.ViewHolder
{
    private final TextView mName;
    private final TextView mDesc;
    private final ImageView mIcon;
    private final ImageButton mAddButton;

    private int mPosition;
    private int mImageRes;
    private int mIconImageRes;
    private IBrowserElementClickListener mListener;

    public BrowserElementHolder(View view, IBrowserElementClickListener listener)
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

        mName = ((TextView) itemView.findViewById(R.id.name_text_view));
        mDesc = ((TextView) itemView.findViewById(R.id.desc_text_view));
        mIcon = (ImageView) itemView.findViewById(R.id.element_icon);
        mAddButton = (ImageButton) itemView.findViewById(R.id.add_icon);
        mAddButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mListener.onActionClick(mPosition);
            }
        });
    }

    private void setImageResource(int res)
    {
        if(mImageRes == res) return;

        mImageRes = res;
        mAddButton.setImageResource(res);
    }

    public void update(BaseExplorerElement element, int position)
    {
        mPosition = position;
        mName.setText(element.name());
        mDesc.setText(element.description());

        if (element.getIconResource() > 0)
        {
            int iconRes = element.getIconResource();
            if(mIconImageRes != iconRes)
            {
                mIconImageRes = iconRes;
                mIcon.setImageResource(iconRes);
            }

            mIcon.setVisibility(View.VISIBLE);
        }
        else
            mIcon.setVisibility(View.GONE);

        Context context = itemView.getContext();
        if(element.isAddable())
        {
            mAddButton.setVisibility(View.VISIBLE);
            if (element.getAddState() == BaseExplorerElement.AddState.NOT_ADDED)
            {
                itemView.setBackgroundColor(SystemHelper.getColor(context, R.attr.rBackground));
                setImageResource(R.drawable.ic_add_circle_black_36dp);
            }
            else if (element.getAddState() == BaseExplorerElement.AddState.ADDED)
            {
                itemView.setBackgroundColor(SystemHelper.getColor(context, R.attr.rAccentSecondary));
                setImageResource(R.drawable.ic_remove_circle_black_36dp);
            }
            else if (element.getAddState() == BaseExplorerElement.AddState.PARTLY_ADDED)
            {
                itemView.setBackgroundColor(SystemHelper.getColor(context, R.attr.rAccentSecondaryAlpha));
                setImageResource(R.drawable.ic_remove_circle_outline_black_36dp);
            }
        }else
        {
            mAddButton.setVisibility(View.GONE);
            itemView.setBackgroundColor(SystemHelper.getColor(context, R.attr.rBackground));
        }
    }

    public interface IBrowserElementClickListener
    {
        void onItemClick(int position);
        void onActionClick(int position);
    }
}
