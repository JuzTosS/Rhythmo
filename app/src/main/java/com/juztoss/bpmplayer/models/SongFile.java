package com.juztoss.bpmplayer.models;

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

    private File mSource;

    public SongFile(File source)
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

    @Override
    public List<Composition> getCompositions()
    {
        return null;//TODO: Return a Composition object of this file
    }
}
