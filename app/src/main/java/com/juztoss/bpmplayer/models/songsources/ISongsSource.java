package com.juztoss.bpmplayer.models.songsources;

import android.database.Cursor;
import android.support.annotation.Nullable;

/**
 * Created by JuzTosS on 6/22/2016.
 */
public interface ISongsSource
{
    @Nullable
    Cursor getIds(float minBPM, float maxBPM);

    String getName();

    void delete();

    void rename(String newName);

    boolean isRenameAvailable();

    boolean isDeleteAvailable();

    boolean isModifyAvailable();

    void add(Cursor songIdsCursor);
}
