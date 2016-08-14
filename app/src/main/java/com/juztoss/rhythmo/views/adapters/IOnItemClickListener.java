package com.juztoss.rhythmo.views.adapters;


import com.juztoss.rhythmo.models.Composition;

/**
 * Created by JuzTosS on 6/19/2016.
 */
public interface IOnItemClickListener
{
    void onPlaylistItemClick(int position, int action, Composition composition);
}