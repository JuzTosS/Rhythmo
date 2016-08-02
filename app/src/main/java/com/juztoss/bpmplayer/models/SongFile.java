package com.juztoss.bpmplayer.models;

import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.format.DateUtils;

import com.juztoss.bpmplayer.R;
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
    public int getIconResource()
    {
        return R.drawable.ic_music_note_black_24dp;
    }


    @Override
    public boolean isAddable()
    {
        return true;
    }

    @Override
    public AddState getAddState()
    {
        return mApp.getBrowserPresenter().getAddState(mFile.getAbsolutePath());
    }

    @Override
    public void setAddState(AddState state)
    {
        if(state == AddState.ADDED)
            mApp.getBrowserPresenter().add(mFile.getAbsolutePath());
        else if(state == AddState.NOT_ADDED)
            mApp.getBrowserPresenter().remove(mFile.getAbsolutePath());
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

    @Override
    public boolean hasChildren()
    {
        return false;
    }

    @Override
    public String getFileSystemPath()
    {
        return mFile.getAbsolutePath();
    }

    @Override
    public String description()
    {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(mFile.getAbsolutePath());
        try
        {
            int duration = Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
            return DateUtils.formatElapsedTime(duration / 1000);
        }
        catch (NumberFormatException e)
        {
            return "Unknown length";
        }

    }
}
