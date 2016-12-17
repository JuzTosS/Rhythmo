package com.juztoss.rhythmo.models;

import android.database.Cursor;
import android.database.DatabaseUtils;
import android.support.annotation.Nullable;

import com.juztoss.rhythmo.R;
import com.juztoss.rhythmo.presenters.RhythmoApp;
import com.juztoss.rhythmo.utils.SystemHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by JuzTosS on 4/25/2016.
 */
public class MediaFolder extends BaseExplorerElement
{
    private String mFirstName;
    private String mFullName;
    private long mFirstId;
    private long mLastId;
    private BaseExplorerElement mParent;
    private boolean mFirstHasSongs;
    private boolean mLastHasSongs;
    private RhythmoApp mApp;

    public MediaFolder(long mediaFolderId, String folderName, boolean hasSongs, @Nullable BaseExplorerElement parent, RhythmoApp app)
    {
        this(mediaFolderId, folderName, hasSongs, parent, true, app);
    }

    public MediaFolder(long mediaFolderId, String folderName, boolean hasSongs, @Nullable BaseExplorerElement parent, boolean isCompacting, RhythmoApp app)
    {
        mApp = app;
        mFirstId = mLastId = mediaFolderId;
        mFirstName = mFullName = folderName;
        mParent = parent;
        mFirstHasSongs = mLastHasSongs = hasSongs;

        if (isCompacting)
            checkCompacting();
    }

    private void checkCompacting()
    {
        long currentId = mFirstId;
        String currentName = mFirstName;
        boolean currentHasSongs = mFirstHasSongs;

        mFullName = "";
        mLastId = mFirstId;
        while (true)
        {
            if (mFullName.length() > 0) mFullName += SystemHelper.SEPARATOR;

            mFullName += currentName;
            mLastId = currentId;
            mLastHasSongs = currentHasSongs;

            if (!currentHasSongs)
            {
                Cursor foldersCursor = mApp.getDatabaseHelper().getReadableDatabase().query(DatabaseHelper.TABLE_FOLDERS,
                        new String[]{DatabaseHelper._ID, DatabaseHelper.FOLDERS_NAME, DatabaseHelper.FOLDERS_PARENT_ID, DatabaseHelper.FOLDERS_HAS_SONGS},
                        DatabaseHelper.FOLDERS_PARENT_ID + "= ?",
                        new String[]{Long.toString(currentId)},
                        null, null, null);

                try
                {
                    if (foldersCursor.getCount() == 1)
                    {
                        foldersCursor.moveToFirst();
                        int idIndex = foldersCursor.getColumnIndex(DatabaseHelper._ID);
                        int nameIndex = foldersCursor.getColumnIndex(DatabaseHelper.FOLDERS_NAME);
                        int hasSongsIndex = foldersCursor.getColumnIndex(DatabaseHelper.FOLDERS_HAS_SONGS);
                        currentId = foldersCursor.getInt(idIndex);
                        currentName = foldersCursor.getString(nameIndex);
                        currentHasSongs = foldersCursor.getInt(hasSongsIndex) > 0;
                    }
                    else
                        break;
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    break;
                }
                finally
                {
                    foldersCursor.close();
                }
            }
            else
                break;
        }
    }

    public String name()
    {
        return mFullName;
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
        return true;
    }

    @Override
    public AddState getAddState()
    {
        return mApp.getBrowserPresenter().getAddState(resolvePath());
    }

    @Override
    public void setAddState(AddState state)
    {
        if(state == AddState.ADDED)
            mApp.getBrowserPresenter().add(resolvePath());
        else if(state == AddState.NOT_ADDED)
            mApp.getBrowserPresenter().remove(resolvePath());
    }

    @Override
    public List<BaseExplorerElement> getChildren()
    {
        List<BaseExplorerElement> result = new ArrayList<>();

        if (mParent != null)
            result.add(new ParentLink(mParent));

        Cursor foldersCursor = mApp.getDatabaseHelper().getReadableDatabase().query(DatabaseHelper.TABLE_FOLDERS,
                new String[]{DatabaseHelper._ID, DatabaseHelper.FOLDERS_NAME, DatabaseHelper.FOLDERS_PARENT_ID, DatabaseHelper.FOLDERS_HAS_SONGS},
                DatabaseHelper.FOLDERS_PARENT_ID + "= ?",
                new String[]{Long.toString(mLastId)},
                null, null, null);

        int idIndex = foldersCursor.getColumnIndex(DatabaseHelper._ID);
        int nameIndex = foldersCursor.getColumnIndex(DatabaseHelper.FOLDERS_NAME);
        int hasSongsIndex = foldersCursor.getColumnIndex(DatabaseHelper.FOLDERS_HAS_SONGS);
        try
        {
            while (foldersCursor.moveToNext())
            {
                int folderId = foldersCursor.getInt(idIndex);
                String folderName = foldersCursor.getString(nameIndex);
                Boolean hasSongs = foldersCursor.getInt(hasSongsIndex) > 0;

                result.add(new MediaFolder(folderId, folderName, hasSongs, this, mApp));
            }
        }
        finally
        {
            foldersCursor.close();
        }

        Collections.sort(result);

        if (mLastHasSongs)
        {
            List<BaseExplorerElement> songs = new ArrayList<>();
            Cursor songsCursor = mApp.getDatabaseHelper().getReadableDatabase().query(DatabaseHelper.TABLE_MUSIC_LIBRARY,
                    new String[]{DatabaseHelper._ID, DatabaseHelper.MUSIC_LIBRARY_PATH, DatabaseHelper.MUSIC_LIBRARY_NAME},
                    DatabaseHelper.MUSIC_LIBRARY_PATH + "= ?",
                    new String[]{resolvePath()},
                    null, null, null);

            int pathIndex = songsCursor.getColumnIndex(DatabaseHelper.MUSIC_LIBRARY_PATH);
            int songNameIndex = songsCursor.getColumnIndex(DatabaseHelper.MUSIC_LIBRARY_NAME);
            try
            {
                while (songsCursor.moveToNext())
                {
                    String folderName = songsCursor.getString(pathIndex);
                    String songName = songsCursor.getString(songNameIndex);

                    StringBuilder songPathBuilder = new StringBuilder();
                    songPathBuilder.append(folderName);
                    songPathBuilder.append(SystemHelper.SEPARATOR);
                    songPathBuilder.append(songName);
                    songs.add(new SongFile(new File(songPathBuilder.toString()), false, mApp));
                }
            }
            finally
            {
                songsCursor.close();
            }
            Collections.sort(songs);
            result.addAll(songs);
        }

        return result;
    }

    public BaseExplorerElement getParent()
    {
        return mParent;
    }

    private String resolvePath()
    {
        MediaFolder current = this;
        String path = "";
        do
        {
            path = SystemHelper.SEPARATOR + current.name() + path;
            current = (MediaFolder) current.getParent();
        }
        while (current != null && current instanceof MediaFolder);
        return path;
    }

    @Nullable
    @Override
    public Cursor getSongIds()
    {
        return mApp.getMusicLibraryHelper().getSongIdsCursor(resolvePath(), false);
    }

    @Override
    public boolean hasChildren()
    {
        return true;
    }

    @Override
    public String getFileSystemPath()
    {
        return resolvePath();
    }

    @Override
    public String description()
    {
        int count = (int) DatabaseUtils.queryNumEntries(mApp.getDatabaseHelper().getReadableDatabase(),
                DatabaseHelper.TABLE_FOLDERS,
                DatabaseHelper.FOLDERS_PARENT_ID + "= ?",
                new String[]{Long.toString(mLastId)})
                +
                (int) DatabaseUtils.queryNumEntries(mApp.getDatabaseHelper().getReadableDatabase(),
                        DatabaseHelper.TABLE_MUSIC_LIBRARY,
                        DatabaseHelper.MUSIC_LIBRARY_PATH + "= ?",
                        new String[]{resolvePath()});
        return mApp.getResources().getString(R.string.folder_desc, count);
    }
}
