package com.juztoss.bpmplayer.models;

import android.database.Cursor;
import android.support.annotation.Nullable;

import com.juztoss.bpmplayer.DatabaseHelper;
import com.juztoss.bpmplayer.presenters.BPMPlayerApp;

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
    private BPMPlayerApp mApp;

    public MediaFolder(long mediaFolderId, String folderName, boolean hasSongs, @Nullable BaseExplorerElement parent, BPMPlayerApp app)
    {
        this(mediaFolderId, folderName, hasSongs, parent, true, app);
    }

    public MediaFolder(long mediaFolderId, String folderName, boolean hasSongs, @Nullable BaseExplorerElement parent, boolean isCompacting, BPMPlayerApp app)
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
            if (mFullName.length() > 0) mFullName += "/";

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
                    songPathBuilder.append("/");
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
        while (current.getParent() != null && current.getParent() instanceof MediaFolder)
        {
            path = "/" + current.name() + path;
            current = (MediaFolder) current.getParent();
        }

        return path;
    }

    @Nullable
    @Override
    public Cursor getSongIds()
    {
        return mApp.getMusicLibraryHelper().getSongIdsCursor(resolvePath(), false);
    }
}
