package com.juztoss.bpmplayer.models;

import android.database.Cursor;
import android.support.annotation.Nullable;

import com.juztoss.bpmplayer.DatabaseHelper;

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

    public MediaFolder(long mediaFolderId, String folderName, boolean hasSongs, @Nullable BaseExplorerElement parent)
    {
        this(mediaFolderId, folderName, hasSongs, parent, true);
    }

    public MediaFolder(long mediaFolderId, String folderName, boolean hasSongs, @Nullable BaseExplorerElement parent, boolean isTunnalable)
    {
        mFirstId = mLastId = mediaFolderId;
        mFirstName = mFullName = folderName;
        mParent = parent;
        mFirstHasSongs = mLastHasSongs = hasSongs;

        if(isTunnalable)
            checkTunneling();
    }

    private void checkTunneling()
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
                Cursor foldersCursor = DatabaseHelper.db().query(DatabaseHelper.TABLE_FOLDERS,
                        new String[]{DatabaseHelper._ID, DatabaseHelper.FOLDERS_NAME, DatabaseHelper.FOLDERS_PARENT_ID, DatabaseHelper.FOLDERS_HAS_SONGS},
                        DatabaseHelper.FOLDERS_PARENT_ID + "= ?",
                        new String[]{Long.toString(currentId)},
                        null, null, null);

                if(foldersCursor.getCount() == 1)
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

        Cursor foldersCursor = DatabaseHelper.db().query(DatabaseHelper.TABLE_FOLDERS,
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

                result.add(new MediaFolder(folderId, folderName, hasSongs, this));
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
            Cursor songsCursor = DatabaseHelper.db().query(DatabaseHelper.TABLE_MUSIC_LIBRARY,
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
                    songs.add(new SongFile(new File(songPathBuilder.toString())));
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

    @Override
    public List<Composition> getCompositions()
    {
        if (mLastHasSongs)
        {
            List<Composition> songList = new ArrayList<>();
            Cursor songsCursor = DatabaseHelper.db().query(DatabaseHelper.TABLE_MUSIC_LIBRARY,
                    new String[]{DatabaseHelper._ID, DatabaseHelper.MUSIC_LIBRARY_PATH, DatabaseHelper.MUSIC_LIBRARY_NAME, DatabaseHelper.MUSIC_LIBRARY_BPMX10},
                    DatabaseHelper.MUSIC_LIBRARY_PATH + "= ?",
                    new String[]{resolvePath()},
                    null, null, null);

            int idIndex = songsCursor.getColumnIndex(DatabaseHelper._ID);
            int pathIndex = songsCursor.getColumnIndex(DatabaseHelper.MUSIC_LIBRARY_PATH);
            int songNameIndex = songsCursor.getColumnIndex(DatabaseHelper.MUSIC_LIBRARY_NAME);
            int bpmx10Index = songsCursor.getColumnIndex(DatabaseHelper.MUSIC_LIBRARY_BPMX10);
            try
            {
                while (songsCursor.moveToNext())
                {
                    String songId = songsCursor.getString(idIndex);
                    String folderName = songsCursor.getString(pathIndex);
                    String songName = songsCursor.getString(songNameIndex);
                    float bpm = songsCursor.getInt(bpmx10Index) / 10;

                    songList.add(new Composition(songId, folderName, songName, bpm));
                }
            }
            finally
            {
                songsCursor.close();
            }

            return songList;
        }

        return null;
    }
}
