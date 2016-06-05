package com.juztoss.bpmplayer.models;

import android.database.Cursor;
import android.support.design.widget.TabLayout;

import com.juztoss.bpmplayer.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JuzTosS on 5/8/2016.
 */
public class Playlist
{
    private String mName;
    private Cursor mList;

    Playlist(String name)
    {
        this(-1, name);
    }

    public Playlist(int playlist_id, String name)
    {
        mName = name;
    }

    public void add(List<Composition> songs)
    {
        //TODO: Implement
    }

    public Cursor compositions()
    {
        return mList;
    }

    public void clear()
    {
        //TODO: Implement
    }

    public void setRange(int minBPM, int maxBPM)
    {
        //TODO: Implement
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

        }finally
        {
            playlists.close();
        }

        return result;
    }


    public String getName()
    {
        return mName;
    }
}

class StaticAllPlaylist extends Playlist
{
    private Cursor mList;

    public StaticAllPlaylist()
    {
        super("All songs");

        mList = DatabaseHelper.db().query(DatabaseHelper.TABLE_MUSIC_LIBRARY,
                new String[]{DatabaseHelper._ID, DatabaseHelper.MUSIC_LIBRARY_FULL_PATH, DatabaseHelper.MUSIC_LIBRARY_PATH, DatabaseHelper.MUSIC_LIBRARY_NAME, DatabaseHelper.MUSIC_LIBRARY_BPMX10},
                null, null, null, null, DatabaseHelper.MUSIC_LIBRARY_BPMX10 + " ASC");
    }

    @Override
    public Cursor compositions()
    {
        return mList;
    }
}