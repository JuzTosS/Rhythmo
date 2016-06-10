package com.juztoss.bpmplayer.models;

import android.database.Cursor;
import android.support.annotation.Nullable;

import com.juztoss.bpmplayer.presenters.BPMPlayerApp;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by JuzTosS on 4/25/2016.
 */
public class SongFile extends BaseExplorerElement
{
    private static final Set<String> SUPPORTED_FORMATS = new HashSet<>(Arrays.asList("wav", "mp3", "m4a", "aiff"));

    private File mFile;
    private boolean mDoCheckFileSystem;
    private BPMPlayerApp mApp;

    public SongFile(File source, boolean doCheckFileSystem, BPMPlayerApp app)
    {
        mFile = source;
        mDoCheckFileSystem = doCheckFileSystem;
        mApp = app;
    }

    public String name()
    {
        return mFile.getName();
    }

    @Override
    public ExplorerPriority priority()
    {
        return ExplorerPriority.SONG;
    }

    @Override
    public List<BaseExplorerElement> getChildren()
    {
        return null;
    }

    public static boolean isSong(File file)
    {
        if(file.isDirectory()) return false;

        String name = file.getName();
        int dotPosition= name.lastIndexOf(".");
        String extension = name.substring(dotPosition + 1, name.length());
        return SUPPORTED_FORMATS.contains(extension);
    }

    @Nullable
    @Override
    public Cursor getSongIds()
    {
        return mApp.getMusicLibraryHelper().getSongIdsCursor(mFile.getAbsolutePath(), mDoCheckFileSystem);
    }
}
