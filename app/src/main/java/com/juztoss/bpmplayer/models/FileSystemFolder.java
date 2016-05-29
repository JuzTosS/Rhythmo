package com.juztoss.bpmplayer.models;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by JuzTosS on 4/25/2016.
 */
public class FileSystemFolder extends BaseExplorerElement
{
    private File mSource;
    private String mCustomName;
    private BaseExplorerElement mParent;

    public FileSystemFolder(File source, BaseExplorerElement parent)
    {
        mSource = source;
        mParent = parent;
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
            return mSource.getName();
    }

    @Override
    public ExplorerPriority priority()
    {
        return ExplorerPriority.FOLDER;
    }

    @Override
    public List<BaseExplorerElement> getChildren()
    {
        File[] allFiles = mSource.listFiles();
        if (allFiles == null)
            return new ArrayList<>();

        List<BaseExplorerElement> dirs = new ArrayList<>();
        List<BaseExplorerElement> files = new ArrayList<>();

        if (mParent != null)
            dirs.add(new ParentLink(mParent));

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
    public List<Composition> getCompositions()
    {
        return null;//TODO: return compositions list
    }
}
