package com.juztoss.bpmplayer.models;

import android.database.Cursor;

import com.juztoss.bpmplayer.DatabaseHelper;
import com.juztoss.bpmplayer.presenters.BPMPlayerApp;
import com.juztoss.bpmplayer.presenters.ISongsDataSource;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JuzTosS on 6/10/2016.
 */
public class FolderDataSource implements ISongsDataSource
{
    public FolderDataSource(BPMPlayerApp app, String absolutePath, boolean doCheckFileSystem)
    {
        //TODO: Check file system if doCheckFileSystem = true
        Cursor songsCursor = app.getDatabaseHelper().getReadableDatabase().query(DatabaseHelper.TABLE_MUSIC_LIBRARY,
                new String[]{DatabaseHelper._ID},
                DatabaseHelper.MUSIC_LIBRARY_PATH + " LIKE ?%",
                new String[]{absolutePath},
                null, null, null);

        int idIndex = songsCursor.getColumnIndex(DatabaseHelper._ID);
        try
        {
            while (songsCursor.moveToNext())
            {
                long songId = songsCursor.getLong(idIndex);
                String folderName = songsCursor.getString(pathIndex);
                String songName = songsCursor.getString(songNameIndex);
                float bpm = songsCursor.getInt(bpmx10Index) / 10;

                mSongList.add(new Composition(songId, folderName, songName, bpm));
            }
        }
        finally
        {
            songsCursor.close();
        }
    }

    @Override
    public void goToItem(int index)
    {

    }

    @Override
    public int getCount()
    {
        return 0;
    }

    @Override
    public long getId()
    {
        return 0;
    }

    @Override
    public void close()
    {

    }
}
