package com.juztoss.rhythmo;

import android.os.Environment;

import junit.framework.Assert;

import org.junit.Test;

import java.io.File;

import static com.juztoss.rhythmo.TestHelper.MUSIC_FOLDER;
import static com.juztoss.rhythmo.TestHelper.MUSIC_FOLDER_NESTED_FULL;

/**
 * Created by JuzTosS on 8/21/2016.
 */
public class RemoveMediaFromDevice
{
    private static boolean removeDir(String dirName)
    {
        File dir = new File(Environment.getExternalStorageDirectory() + "/" + dirName);
        String[] children = dir.list();
        if(children != null && children.length > 0)
        {
            for (String child : children)
            {
                new File(dir, child).delete();
            }
        }
        return dir.delete();
    }

    public static boolean doRemove()
    {
        return removeDir(MUSIC_FOLDER_NESTED_FULL) && removeDir(MUSIC_FOLDER);
    }

    @Test
    public void execute() throws Exception
    {
        Assert.assertTrue(doRemove());
    }


}
