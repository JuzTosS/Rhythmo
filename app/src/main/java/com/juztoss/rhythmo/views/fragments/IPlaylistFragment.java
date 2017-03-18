package com.juztoss.rhythmo.views.fragments;

import android.support.annotation.Nullable;

import com.juztoss.rhythmo.models.Composition;

/**
 * Created by JuzTosS on 3/6/2017.
 */

public interface IPlaylistFragment
{
    boolean isItemVisible(int position);

    void scrollTo(int position, @Nullable Composition composition);

    void onScreen();

    void offScreen();
}
