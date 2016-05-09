package com.juztoss.bpmplayer.models;

import android.os.Parcelable;

import java.util.List;

/**
 * Created by JuzTosS on 4/25/2016.
 */
public interface IExplorerElement extends Comparable, Parcelable, ISongSource {
    String name();
//    List<IExplorerElement> getLinked();
}
