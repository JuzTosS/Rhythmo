package com.juztoss.bpmplayer.models;

import android.database.Cursor;

/**
 * Created by JuzTosS on 6/22/2016.
 */
public interface ISongsSource
{
    Cursor getIds(float minBPM, float maxBPM);

    String getName();

    void delete();

    void rename(String newName);

    boolean isRenameAvailable();

    boolean isDeleteAvailable();

    boolean isModifyAvailable();
}
