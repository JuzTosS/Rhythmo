package com.juztoss.bpmplayer.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;

/**
 * Created by JuzTosS on 5/9/2016.
 */
public class LinkToMenu implements IExplorerElement
{
    @Override
    public String name()
    {
        return null;
    }

    @Override
    public int compareTo(Object another)
    {
        return 0;
    }

    @Override
    public File source()
    {
        return null;
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {

    }

    public static final Parcelable.Creator<LinkToMenu> CREATOR = new Creator<LinkToMenu>() {
        @Override
        public LinkToMenu createFromParcel(Parcel source) {
            return new LinkToMenu();
        }

        @Override
        public LinkToMenu[] newArray(int size) {
            return new LinkToMenu[size];
        }
    };
}
