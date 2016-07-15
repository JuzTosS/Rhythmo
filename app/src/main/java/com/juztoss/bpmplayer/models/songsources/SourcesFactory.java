package com.juztoss.bpmplayer.models.songsources;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.Nullable;

import com.juztoss.bpmplayer.models.DatabaseHelper;
import com.juztoss.bpmplayer.presenters.BPMPlayerApp;

/**
 * Created by JuzTosS on 6/25/2016.
 */
public class SourcesFactory
{
    private static int LOCAL_PLAYLIST_SOURCE = 1;
    private static int FOLDER_SOURCE = 2;

    @Nullable
    public static ISongsSource loadExist(BPMPlayerApp app, long id)
    {
        Cursor sources = app.getDatabaseHelper().getReadableDatabase().query(DatabaseHelper.TABLE_SOURCES,
                new String[]{DatabaseHelper._ID, DatabaseHelper.SOURCE_NAME, DatabaseHelper.SOURCE_TYPE, DatabaseHelper.SOURCE_OPTIONS},
                DatabaseHelper._ID + " = ?", new String[]{Long.toString(id)}, null, null, null);

        ISongsSource result = null;
        try
        {
            int idIndex = sources.getColumnIndex(DatabaseHelper._ID);
            int nameIndex = sources.getColumnIndex(DatabaseHelper.SOURCE_NAME);
            int typeIndex = sources.getColumnIndex(DatabaseHelper.SOURCE_TYPE);
            int optionsIndex = sources.getColumnIndex(DatabaseHelper.SOURCE_OPTIONS);


            if (sources.moveToFirst())
            {
                int type = sources.getInt(typeIndex);
                if (type == LOCAL_PLAYLIST_SOURCE)
                    result = new LocalPlaylistSongsSource(sources.getLong(idIndex), app, sources.getString(nameIndex));
                else if (type == FOLDER_SOURCE)
                    result = new FolderSongsSource(sources.getLong(idIndex), app, sources.getString(optionsIndex));
            }
        }
        finally
        {
            sources.close();
        }

        return result;
    }

    public static FolderSongsSource createFolderSongSource(String path, BPMPlayerApp app)
    {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.SOURCE_TYPE, FOLDER_SOURCE);
        values.put(DatabaseHelper.SOURCE_OPTIONS, path);
        long id = app.getDatabaseHelper().getWritableDatabase().insert(DatabaseHelper.TABLE_SOURCES, null, values);
        return new FolderSongsSource(id, app, path);
    }

    public static LocalPlaylistSongsSource createLocalPlaylistSongSource(BPMPlayerApp app)
    {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.SOURCE_TYPE, LOCAL_PLAYLIST_SOURCE);
        values.put(DatabaseHelper.SOURCE_NAME, "");
        long id = app.getDatabaseHelper().getWritableDatabase().insert(DatabaseHelper.TABLE_SOURCES, null, values);
        return new LocalPlaylistSongsSource(id, app, "");
    }

    public static AllSongsSource createAllSongsSource(BPMPlayerApp app)
    {
        return new AllSongsSource(app);
    }
}
