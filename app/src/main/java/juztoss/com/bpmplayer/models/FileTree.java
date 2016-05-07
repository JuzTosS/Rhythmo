package juztoss.com.bpmplayer.models;

import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

/**
 * Created by JuzTosS on 4/20/2016.
 */
public class FileTree {
    private File mCurrentDir; //Our current location.
    public static final String TAG = "Current dir"; //for debugging purposes.

    public FileTree() {
        init();
    }

    private void init() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            mCurrentDir = Environment.getExternalStorageDirectory();

            Log.i(TAG, String.valueOf(mCurrentDir));
        } else {
            Log.i(TAG, "External storage unavailable");
        }
    }

    public File getCurrentDir() {
        return mCurrentDir;
    }

    public void gotoNext(File currentDir) {
        mCurrentDir = currentDir;
    }

    public List<IExplorerElement> getAllFiles(File path) {
        File[] allFiles = path.listFiles();
        if(allFiles == null) {
            return new ArrayList<>();

        }
        /* I want all directories to appear before files do, so I have separate lists for both that are merged into one later.*/
        List<IExplorerElement> dirs = new ArrayList<>();
        List<IExplorerElement> files = new ArrayList<>();

        if(mCurrentDir.getParentFile() != null &&  mCurrentDir.getParentFile().list() != null && mCurrentDir.getParentFile().list().length > 0)
            dirs.add(new ParentLink(mCurrentDir.getParentFile()));

        for (File file : allFiles) {
            if (file.isDirectory()) {
                dirs.add(new Folder(file));
            }
            else
            {
                if(Song.isSong(file))
                    files.add(new Song(file));
            }
        }

        Collections.sort(dirs);
        Collections.sort(files);

        dirs.addAll(files);

        return dirs;
    }

    public List<Song> getSongsFiles(File path) {
        File[] allFiles = path.listFiles();
        if(allFiles == null) {
            return new ArrayList<>();
        }

        List<Song> songs = new ArrayList<>();
        for (File file : allFiles) {
            if (Song.isSong(file)) {
                songs.add(new Song(file));
            }
        }

        Collections.sort(songs);

        return songs;
    }
}