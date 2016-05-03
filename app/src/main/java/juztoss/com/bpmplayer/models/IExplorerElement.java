package juztoss.com.bpmplayer.models;

import android.os.Parcelable;

/**
 * Created by JuzTosS on 4/25/2016.
 */
public interface IExplorerElement extends Comparable, Parcelable, ISongSource {
    String name();
}
