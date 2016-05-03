package juztoss.com.bpmplayer.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;

/**
 * Created by JuzTosS on 4/25/2016.
 */
public class Folder implements IExplorerElement, Parcelable{
    private File mSource;
    public Folder(File source)
    {
        mSource = source;
    }

    public File source()
    {
        return mSource;
    }

    public String name()
    {
        return mSource.getName();
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

    @Override
    public int compareTo(Object another) {
        return mSource.compareTo(((IExplorerElement)another).source());
    }

    public static final Parcelable.Creator<Folder> CREATOR = new Creator<Folder>() {
        @Override
        public Folder createFromParcel(Parcel source) {
            return null;
        }

        @Override
        public Folder[] newArray(int size) {
            return new Folder[size];
        }
    };
}
