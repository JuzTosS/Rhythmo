package com.juztoss.rhythmo.models.songsources;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.Nullable;

import com.juztoss.rhythmo.models.DatabaseHelper;
import com.juztoss.rhythmo.presenters.RhythmoApp;

/**
 * Created by JuzTosS on 6/25/2016.
 */
public class SourcesFactory
{
    private static int LOCAL_PLAYLIST_SOURCE = 1;
    private static int FOLDER_SOURCE = 2;

    @Nullable
    public static ISongsSource loadExist(RhythmoApp app, long id)
    {
        Cursor sources = app.getDatabaseHelper().getReadableDatabase().query(DatabaseHelper.TABLE_SOURCES,
                new String[]{DatabaseHelper._ID, DatabaseHelper.SOURCE_NAME, DatabaseHelper.SOURCE_TYPE, DatabaseHelper.SOURCE_OPTIONS, DatabaseHelper.SOURCE_SORT},
                DatabaseHelper._ID + " = ?", new String[]{Long.toString(id)}, null, null, null);

        ISongsSource result = null;
        try
        {
            int idIndex = sources.getColumnIndex(DatabaseHelper._ID);
            int nameIndex = sources.getColumnIndex(DatabaseHelper.SOURCE_NAME);
            int typeIndex = sources.getColumnIndex(DatabaseHelper.SOURCE_TYPE);
            int optionsIndex = sources.getColumnIndex(DatabaseHelper.SOURCE_OPTIONS);
            int sortIndex = sources.getColumnIndex(DatabaseHelper.SOURCE_SORT);


            if (sources.moveToFirst())
            {
                int type = sources.getInt(typeIndex);
                if (type == LOCAL_PLAYLIST_SOURCE)
                    result = new LocalPlaylistSongsSource(sources.getLong(idIndex), app, sources.getString(nameIndex), SortType.values()[sources.getInt(sortIndex)]);
//                else if (type == FOLDER_SOURCE)
//                    result = new FolderSongsSource(sources.getLong(idIndex), app, sources.getString(optionsIndex));
            }
        }
        finally
        {
            sources.close();
        }

        return result;
    }

    public static LocalPlaylistSongsSource createLocalPlaylistSongSource(RhythmoApp app)
    {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.SOURCE_TYPE, LOCAL_PLAYLIST_SOURCE);
        values.put(DatabaseHelper.SOURCE_NAME, "");
        long id = app.getDatabaseHelper().getWritableDatabase().insert(DatabaseHelper.TABLE_SOURCES, null, values);
        return new LocalPlaylistSongsSource(id, app, "", SortType.DIRECTORY);
    }

    public static AllSongsSource createAllSongsSource(RhythmoApp app)
    {
        return new AllSongsSource(app);
    }
}
