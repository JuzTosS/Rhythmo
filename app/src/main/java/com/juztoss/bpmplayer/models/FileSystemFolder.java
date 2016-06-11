package com.juztoss.bpmplayer.models;

import android.database.Cursor;
import android.support.annotation.Nullable;

import com.juztoss.bpmplayer.presenters.BPMPlayerApp;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by JuzTosS on 4/25/2016.
 */
public class FileSystemFolder extends BaseExplorerElement
{
    private File mFile;
    private String mCustomName;
    private BaseExplorerElement mParent;
    private BPMPlayerApp mApp;

    public FileSystemFolder(File source, BaseExplorerElement parent, BPMPlayerApp app)
    {
        mFile = source;
        mParent = parent;
        mApp = app;
    }

    public FileSystemFolder(File source, String customName, BaseExplorerElement parent, BPMPlayerApp app)
    {
        this(source, parent, app);
        mCustomName = customName;
    }

    public String name()
    {
        if(mCustomName != null)
            return mCustomName;
        else
            return mFile.getName();
    }

    @Override
    public ExplorerPriority priority()
    {
        return ExplorerPriority.FOLDER;
    }

    @Override
    public List<BaseExplorerElement> getChildren()
    {
        File[] allFiles = mFile.listFiles();

        List<BaseExplorerElement> dirs = new ArrayList<>();
        List<BaseExplorerElement> files = new ArrayList<>();

        if (mParent != null)
            dirs.add(new ParentLink(mParent));

        if (allFiles == null)
            return dirs;

        for (File file : allFiles)
        {
            if (file.isDirectory())
            {
                dirs.add(new FileSystemFolder(file, this, mApp));
            }
            else
            {
                if (SongFile.isSong(file))
                    files.add(new SongFile(file, true, mApp));
            }
        }

        Collections.sort(dirs);
        Collections.sort(files);

        dirs.addAll(files);

        return dirs;
    }

    @Nullable
    @Override
    public Cursor getSongIds()
    {
        return mApp.getMusicLibraryHelper().getSongIdsCursor(mFile.getAbsolutePath(), true);
    }

    @Override
    public boolean hasChildren()
    {
        return true;
    }
}
