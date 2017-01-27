package com.juztoss.rhythmo.services;

import android.content.ContentValues;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.juztoss.rhythmo.models.DatabaseHelper;
import com.juztoss.rhythmo.presenters.RhythmoApp;
import com.juztoss.rhythmo.utils.StorageUtils;
import com.juztoss.rhythmo.utils.SystemHelper;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by JuzTosS on 1/27/2017.
 */

public class LibraryHelper
{
    private final boolean DEBUG = true;

    private static final Set<String> SUPPORTED_FORMATS = new HashSet<>(Arrays.asList("wav", "mp3", "m4a", "aiff"));

    private boolean mHasBegun = false;
    private RhythmoApp mApp;
    private Node mFolderIds;

    public LibraryHelper(RhythmoApp app)
    {
        mApp = app;
    }

    /**
     * Must be called before any addSong(...) invocations
     */
    public void begin()
    {
        if(DEBUG) Log.d(LibraryHelper.class.toString(), "Library build started");

        mHasBegun = true;
        mFolderIds = new Node(-1);

        //Set all songs as deleted, we'll update each song that exists later
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.MUSIC_LIBRARY_DELETED, 1);
        mApp.getDatabaseHelper().getWritableDatabase().update(DatabaseHelper.TABLE_MUSIC_LIBRARY, cv, null, null);

        //Set all folders as deleted, we'll update each folder that exists later
        cv = new ContentValues();
        cv.put(DatabaseHelper.FOLDERS_DELETED, 1);
        mApp.getDatabaseHelper().getWritableDatabase().update(DatabaseHelper.TABLE_FOLDERS, cv, null, null);
    }

    /**
     * Call with a path of a song, to add the folders of the path to the tree
     *
     * @param pathToSong
     * @param dateAdded
     */
    private Result addSong(String pathToSong, int dateAdded)
    {
        if (!mHasBegun)
            throw new RuntimeException("begin() must be called before any addSong(...) invocations");

        mApp.getDatabaseHelper().getWritableDatabase().beginTransaction();

        Result result = addSongToLibrary(pathToSong, dateAdded);
        if (result == Result.ADDED || result == Result.UPDATED)
            addSongToTree(pathToSong);

        mApp.getDatabaseHelper().getWritableDatabase().setTransactionSuccessful();
        mApp.getDatabaseHelper().getWritableDatabase().endTransaction();

        return result;

    }

    /**
     * Call with a path of a song, to add the folders of the path to the tree
     *
     * @param file
     */
    public Result addSong(File file)
    {
        if (!file.exists())
            return Result.NOT_ADDED;

        return addSong(file.getAbsolutePath(), (int) (file.lastModified() / 1000f));
    }


    public boolean isSong(File file)
    {
        if(file.isDirectory()) return false;

        String name = file.getName();
        String extension = StorageUtils.getExtension(name).toLowerCase();
        return SUPPORTED_FORMATS.contains(extension);
    }

    /**
     * Call with a path of a song, to add the folders of the path to the tree
     *
     * @param cursor
     */
    public Result addSong(MediaCursor cursor)
    {
        File file = new File(cursor.fullName());
        if (!file.exists())
            return Result.NOT_ADDED;

        return addSong(cursor.fullName(), cursor.dateAdded());
    }

    private Result addSongToLibrary(String songFileFullPath, int dateAdded)
    {
        String[] folders = songFileFullPath.split(SystemHelper.SEPARATOR);
        StringBuilder b = new StringBuilder(songFileFullPath);
        String songFileName = folders[folders.length - 1];

        String songNameWithSlash = SystemHelper.SEPARATOR + songFileName;
        b.replace(songFileFullPath.lastIndexOf(songNameWithSlash), songFileFullPath.lastIndexOf(songNameWithSlash) + songNameWithSlash.length(), "");
        String songFileFolder = b.toString();

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.MUSIC_LIBRARY_PATH, songFileFolder);
        values.put(DatabaseHelper.MUSIC_LIBRARY_NAME, songFileName);
        values.put(DatabaseHelper.MUSIC_LIBRARY_FULL_PATH, songFileFullPath);
        values.put(DatabaseHelper.MUSIC_LIBRARY_DATE_ADDED, dateAdded);
        values.put(DatabaseHelper.MUSIC_LIBRARY_DELETED, false);

        //Add all the entries to the database to build the songs library.
        long songExist = DatabaseUtils.queryNumEntries(mApp.getDatabaseHelper().getWritableDatabase(), DatabaseHelper.TABLE_MUSIC_LIBRARY, DatabaseHelper.MUSIC_LIBRARY_FULL_PATH + " = ?", new String[]{songFileFullPath});
        if (songExist > 0)
        {
            long numUpdated = mApp.getDatabaseHelper().getWritableDatabase().update(DatabaseHelper.TABLE_MUSIC_LIBRARY, values, DatabaseHelper.MUSIC_LIBRARY_FULL_PATH + " = ?", new String[]{songFileFullPath});
            if (numUpdated <= 0)
                return Result.ERROR;
            else
                return Result.UPDATED;
        }
        else
        {
            long rowId = mApp.getDatabaseHelper().getWritableDatabase().insertWithOnConflict(DatabaseHelper.TABLE_MUSIC_LIBRARY, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            if (rowId < 0)
                return Result.ERROR;
            else
                return Result.ADDED;
        }
    }

    private void addSongToTree(String pathToSong)
    {
        String[] folders = pathToSong.split(SystemHelper.SEPARATOR);
        Node parentNode = mFolderIds;
        for (int j = 1; j < (folders.length - 1); j++)
        {
            String folder = folders[j];
            if (parentNode.mChildren.containsKey(folder))
            {
                //Set "HasSongs" to true, for the segment that already has been added to the DB
                if (j == (folders.length - 2))//This is the last segment
                {
                    updateFolder(folder, parentNode.mId, true);
                    break;
                }
                else
                {
                    parentNode = parentNode.get(folder);
                    continue;
                }
            }

            //Set "HasSongs" to true, for the segment that is new
            boolean hasSongs = j == (folders.length - 2);//This is the last segment
            long id = updateFolder(folder, parentNode.mId, hasSongs);
            Node newNode = new Node(id);
            parentNode.add(newNode, folder);
            parentNode = newNode;
        }
    }

    /**
     * Update or insert a folder to the DB
     *
     * @param folder
     * @param parentId
     * @param hasSongs
     * @return The id of inserted row
     */
    private long updateFolder(String folder, long parentId, boolean hasSongs)
    {
        ContentValues folderValues = new ContentValues();
        folderValues.put(DatabaseHelper.FOLDERS_NAME, folder);
        folderValues.put(DatabaseHelper.FOLDERS_PARENT_ID, parentId);
        folderValues.put(DatabaseHelper.FOLDERS_DELETED, false);
        folderValues.put(DatabaseHelper.FOLDERS_HAS_SONGS, hasSongs);

        long folderExist = DatabaseUtils.queryNumEntries(
                mApp.getDatabaseHelper().getWritableDatabase(),
                DatabaseHelper.TABLE_FOLDERS,
                DatabaseHelper.FOLDERS_NAME + " = ? AND " + DatabaseHelper.FOLDERS_PARENT_ID + " = ? ", new String[]{folder, Long.toString(parentId)}
        );
        if (folderExist > 0)
        {
            mApp.getDatabaseHelper().getWritableDatabase().update(
                    DatabaseHelper.TABLE_FOLDERS,
                    folderValues,
                    DatabaseHelper.FOLDERS_NAME + " = ? AND " + DatabaseHelper.FOLDERS_PARENT_ID + " = ? ", new String[]{folder, Long.toString(parentId)}
            );

            return mApp.getDatabaseHelper().getRowId(
                    DatabaseHelper.FOLDERS_NAME + " = ? AND " + DatabaseHelper.FOLDERS_PARENT_ID + " = ? ",
                    new String[]{folder, Long.toString(parentId)}
            );
        }
        else
        {
            return mApp.getDatabaseHelper().getWritableDatabase().insertWithOnConflict(
                    DatabaseHelper.TABLE_FOLDERS,
                    null,
                    folderValues,
                    SQLiteDatabase.CONFLICT_REPLACE
            );
        }
    }

    /**
     * Must be called at the end of operation in any case.
     *
     * @return true if there was changes in the library
     */
    public boolean end()
    {
        mHasBegun = false;
        int songsWasRemoved = mApp.getDatabaseHelper().getWritableDatabase().delete(DatabaseHelper.TABLE_MUSIC_LIBRARY, DatabaseHelper.MUSIC_LIBRARY_DELETED + " = ?", new String[]{"1"});
        int foldersWasRemoved = mApp.getDatabaseHelper().getWritableDatabase().delete(DatabaseHelper.TABLE_FOLDERS, DatabaseHelper.FOLDERS_DELETED + " = ?", new String[]{"1"});

        if(DEBUG) Log.d(LibraryHelper.class.toString(), "Library build complete");

        return songsWasRemoved > 0 || foldersWasRemoved > 0;
    }

    enum Result
    {
        ADDED,
        UPDATED,
        NOT_ADDED,
        ERROR
    }

    private static class Node
    {
        Long mId;
        Node mParent;
        Map<String, Node> mChildren = new HashMap<>();

        Node(long id)
        {
            mId = id;
        }

        public void add(Node node, String name)
        {
            mChildren.put(name, node);
            node.mParent = this;
        }

        public Node get(String folder)
        {
            return mChildren.get(folder);
        }
    }

}
