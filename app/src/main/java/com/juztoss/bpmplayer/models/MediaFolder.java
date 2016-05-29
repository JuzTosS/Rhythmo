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
    private String mName;
    private long mId;
    private BaseExplorerElement mParent;
    private boolean mHasSongs;

    public MediaFolder(long mediaFolderId, String folderName, boolean hasSongs, @Nullable BaseExplorerElement parent)
    {
        mId = mediaFolderId;
        mName = folderName;
        mParent = parent;
        mHasSongs = hasSongs;
    }

    public String name()
    {
        return mName;
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
                new String[]{Long.toString(mId)},
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

        if (mHasSongs)
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
        if(mHasSongs)
        {
            List<Composition> songList = new ArrayList<>();
            Cursor songsCursor = DatabaseHelper.db().query(DatabaseHelper.TABLE_MUSIC_LIBRARY,
                    new String[]{DatabaseHelper._ID, DatabaseHelper.MUSIC_LIBRARY_PATH, DatabaseHelper.MUSIC_LIBRARY_NAME},
                    DatabaseHelper.MUSIC_LIBRARY_PATH + "= ?",
                    new String[]{resolvePath()},
                    null, null, null);

            int idIndex = songsCursor.getColumnIndex(DatabaseHelper._ID);
            int pathIndex = songsCursor.getColumnIndex(DatabaseHelper.MUSIC_LIBRARY_PATH);
            int songNameIndex = songsCursor.getColumnIndex(DatabaseHelper.MUSIC_LIBRARY_NAME);
            try
            {
                while (songsCursor.moveToNext())
                {
                    String songId = songsCursor.getString(idIndex);
                    String folderName = songsCursor.getString(pathIndex);
                    String songName = songsCursor.getString(songNameIndex);

                    songList.add(new Composition(songId, folderName, songName));
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
