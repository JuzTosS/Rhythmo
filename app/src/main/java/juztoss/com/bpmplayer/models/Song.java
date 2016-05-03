package juztoss.com.bpmplayer.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;

/**
 * Created by JuzTosS on 4/25/2016.
 */
public class Song implements IExplorerElement, Parcelable {

    private File mSource;

    public Song(File source) {
        mSource = source;
    }

    @Override
    public File source() {
        return mSource;
    }

    @Override
    public String name() {
        return mSource.getName();
    }

    @Override
    public int compareTo(Object another) {
        return 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (source() != null)
            dest.writeString(source().getAbsolutePath());
    }

    public static final Parcelable.Creator<Song> CREATOR = new Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel source) {
            return null;
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    public static boolean isSong(File file) {
        return !file.isDirectory();//TODO: Make a proper check that it's a song
    }
}
