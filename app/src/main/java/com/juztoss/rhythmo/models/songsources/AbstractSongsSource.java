package com.juztoss.rhythmo.models.songsources;

import android.database.Cursor;
import android.support.annotation.Nullable;

/**
 * Created by JuzTosS on 6/22/2016.
 * Songs source, provides songs to a playlist
 */
public abstract class AbstractSongsSource
{
    /**
     * Indexes of fields of a cursor that return the AbstractSongSource descendants
     */
    public static final int I_ID = 0;
    public static final int I_FOLDER = 1;
    public static final int I_NAME = 2;
    public static final int I_BPM = 3;
    public static final int I_BPM_SHIFT = 4;
    public static final int I_DATE_ADDED = 5;
    public static final int I_LENGTH = 6;

    private AbstractSourceUpdatedListener mListener;

    /**
     * Returns songs in the source filtered by parameters
     * @param minBPM min song bpm
     * @param maxBPM max song bpm
     * @param wordFilter string to filter songs (usually filtered by name)
     * @return a Database cursor
     */
    @Nullable
    public abstract Cursor getIds(float minBPM, float maxBPM, String wordFilter);

    /**
     * A name of the source
     * @return
     */
    public abstract String getName();

    /**
     * Delete the whole song source
     */
    public abstract void delete();

    /**
     * Change the name of the source
     * @param newName
     */
    public abstract void rename(String newName);

    /**
     * Returns true if rename(String) method will affect the name
     * @return
     */
    public abstract boolean isRenameAvailable();

    /**
     * Returns true if delete() method has effect
     * @return
     */
    public abstract boolean isDeleteAvailable();

    /**
     * Returns true if add(...) and remove(...) methods have effects
     * @return
     */
    public abstract boolean isModifyAvailable();

    /**
     * Add songs to the source
     * @param songIdsCursor A database cursor with songs IDs at 0 index
     */
    public abstract void add(Cursor songIdsCursor);

    /**
     * Remove a song from a source
     * @param songId A song ID
     */
    public abstract void remove(long songId);

    /**
     * Set listener to be called on any source parameters update
     * @param listener
     */
    public void setUpdateListener(AbstractSourceUpdatedListener listener)
    {
        mListener = listener;
    }

    /**
     * Call in descendants to notify listeners
     */
    protected void notifyUpdated()
    {
        if(mListener != null)
            mListener.onSourceUpdated();
    }

    /**
     * Sets sorting type of songs in the source
     * @param sortType
     */
    abstract public void setSortType(SortType sortType);

    /**
     * Returns sorting type of songs in the source
     * @return
     */
    abstract public SortType getSortType();

    public interface AbstractSourceUpdatedListener
    {
        void onSourceUpdated();
    }
}
