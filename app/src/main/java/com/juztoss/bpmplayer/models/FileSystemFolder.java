package com.juztoss.bpmplayer.models;

import com.juztoss.bpmplayer.presenters.ISongsDataSource;

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
    private ISongsDataSource mSource;
    private String mCustomName;
    private BaseExplorerElement mParent;

    public FileSystemFolder(File source, BaseExplorerElement parent)
    {
        mFile = source;
        mParent = parent;
        mSource = new FolderDataSource(source.getAbsolutePath());
    }

    public FileSystemFolder(File source, String customName, BaseExplorerElement parent)
    {
        this(source, parent);
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
                dirs.add(new FileSystemFolder(file, this));
            }
            else
            {
                if (SongFile.isSong(file))
                    files.add(new SongFile(file));
            }
        }

        Collections.sort(dirs);
        Collections.sort(files);

        dirs.addAll(files);

        return dirs;
    }

    @Override
    public ISongsDataSource getSource()
    {
        return mSource;
    }
}
