package com.juztoss.bpmplayer.models;

import android.database.Cursor;

import com.juztoss.bpmplayer.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JuzTosS on 5/8/2016.
 */
public class Playlist
{
    private String mName;

    Playlist(String name)
    {
        this(-1, name);
    }

    public Playlist(int playlist_id, String name)
    {
        mName = name;
    }

    public Cursor getNewCompositionsCursor()
    {
        return null;
    }

    public static List<Playlist> loadPlaylists()
    {
        List<Playlist> result = new ArrayList<>();

        Cursor playlists = DatabaseHelper.db().query(DatabaseHelper.TABLE_PLAYLISTS,
                new String[]{DatabaseHelper._ID, DatabaseHelper.PLAYLISTS_NAME},
                null, null, null, null, null);

        result.add(new StaticAllPlaylist());
        int idIndex = playlists.getColumnIndex(DatabaseHelper._ID);
        int nameIndex = playlists.getColumnIndex(DatabaseHelper.PLAYLISTS_NAME);

        try
        {
            while (playlists.moveToNext())
            {
                result.add(new Playlist(playlists.getInt(idIndex), playlists.getString(nameIndex)));
            }

        }
        finally
        {
            playlists.close();
        }

        return result;
    }


    public String getName()
    {
        return mName;
    }

    public void setBPMFilter(float minBPM, float maxBPM)
    {

    }
}

class StaticAllPlaylist extends Playlist
{
    private int mMinBPMX10 = 0;
    private int mMaxBPMX10 = Integer.MAX_VALUE;

    public StaticAllPlaylist()
    {
        super("All songs");


    }

    @Override
    public Cursor getNewCompositionsCursor()
    {
        return DatabaseHelper.db().query(DatabaseHelper.TABLE_MUSIC_LIBRARY,
                new String[]{DatabaseHelper._ID, DatabaseHelper.MUSIC_LIBRARY_FULL_PATH, DatabaseHelper.MUSIC_LIBRARY_PATH, DatabaseHelper.MUSIC_LIBRARY_NAME, DatabaseHelper.MUSIC_LIBRARY_BPMX10},
                DatabaseHelper.MUSIC_LIBRARY_BPMX10 + " >= ?" + " AND " + DatabaseHelper.MUSIC_LIBRARY_BPMX10 + " <= ?"
                , new String[]{Integer.toString(mMinBPMX10), Integer.toString(mMaxBPMX10)},
                null, null,
                DatabaseHelper.MUSIC_LIBRARY_BPMX10 + " ASC");
    }

    @Override
    public void setBPMFilter(float minBPM, float maxBPM)
    {
        mMinBPMX10 = (int) (minBPM * 10);
        mMaxBPMX10 = (int) (maxBPM * 10);
    }
}