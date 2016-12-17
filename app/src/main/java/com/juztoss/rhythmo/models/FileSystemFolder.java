package com.juztoss.rhythmo.models;

import android.database.Cursor;
import android.support.annotation.Nullable;

import com.juztoss.rhythmo.R;
import com.juztoss.rhythmo.presenters.RhythmoApp;

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
    private RhythmoApp mApp;

    public FileSystemFolder(File source, BaseExplorerElement parent, RhythmoApp app)
    {
        mFile = source;
        mParent = parent;
        mApp = app;
    }

    public FileSystemFolder(File source, String customName, BaseExplorerElement parent, RhythmoApp app)
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
    public int getIconResource()
    {
        return R.drawable.ic_folder_black_24dp;
    }

    @Override
    public boolean isAddable()
    {
        return false;
    }

    @Override
    public AddState getAddState()
    {
        return AddState.NOT_ADDED;
    }

    @Override
    public void setAddState(AddState state)
    {

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
                    files.add(new SongFile(file, true, mApp, this));
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

    @Override
    public String getFileSystemPath()
    {
        return mFile.getAbsolutePath();
    }

    @Override
    public String description()
    {
        String[] list = mFile.list();
        if(list != null)
            return mApp.getResources().getString(R.string.folder_desc, mFile.list().length);
        else
            return "";
    }
}
