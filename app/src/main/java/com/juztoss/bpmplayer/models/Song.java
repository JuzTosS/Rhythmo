package com.juztoss.bpmplayer.models;

import android.media.MediaMetadataRetriever;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by JuzTosS on 4/25/2016.
 */
public class Song implements IExplorerElement, Parcelable
{
    private static final Set<String> SUPPORTED_FORMATS = new HashSet<String>(Arrays.asList("3gp", "mp4", "m4a", "aac", "flag", "mp3", "mid",
            "xmf", "mxmf", "rtttl", "rtx", "ota", "imy", "ogg", "mkv", "wav"));

    private File mSource;

    public Song(File source)
    {
        mSource = source;
    }

    @Override
    public File source()
    {
        return mSource;
    }

    @Override
    public String name()
    {
        return mSource.getName();
    }

    public int length()
    {
        //TODO: Cache value
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(source().getPath());
        int duration = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        retriever.release();
        return duration;
    }

    @Override
    public int compareTo(Object another)
    {
        return 0;
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        if (source() != null)
            dest.writeString(source().getAbsolutePath());
    }

    public static final Parcelable.Creator<Song> CREATOR = new Creator<Song>()
    {
        @Override
        public Song createFromParcel(Parcel source)
        {
            return null;
        }

        @Override
        public Song[] newArray(int size)
        {
            return new Song[size];
        }
    };

    public static boolean isSong(File file)
    {
        if(file.isDirectory()) return false;

        String name = file.getName();
        int dotPosition= name.lastIndexOf(".");
        String extension = name.substring(dotPosition + 1, name.length());
        return SUPPORTED_FORMATS.contains(extension);
    }
}
