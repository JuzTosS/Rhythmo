package juztoss.com.bpmplayer.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;

/**
 * Created by JuzTosS on 4/25/2016.
 */
public class ParentLink implements IExplorerElement, Parcelable{
    private File mSource;
    public ParentLink(File source)
    {
        mSource = source;
    }

    public File source()
    {
        return mSource;
    }

    public String name()
    {
        return "..";
    }

    @Override
    public int compareTo(Object another) {
        return mSource.compareTo(((IExplorerElement)another).source());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if(source() != null)
            dest.writeString(source().getAbsolutePath());
    }

    public static final Parcelable.Creator<ParentLink> CREATOR = new Creator<ParentLink>() {
        @Override
        public ParentLink createFromParcel(Parcel source) {
            return null;
        }

        @Override
        public ParentLink[] newArray(int size) {
            return new ParentLink[size];
        }
    };
}
