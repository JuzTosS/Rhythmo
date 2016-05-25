package com.juztoss.bpmplayer.models;

import android.os.Parcelable;

import java.util.List;

/**
 * Created by JuzTosS on 4/25/2016.
 */
public interface IExplorerElement extends Comparable<IExplorerElement>, Parcelable, ISongSource {
    String name();
}
