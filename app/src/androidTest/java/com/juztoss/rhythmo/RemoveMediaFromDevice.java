package com.juztoss.rhythmo;

import android.os.Environment;

import junit.framework.Assert;

import org.junit.Test;

import java.io.File;

import static com.juztoss.rhythmo.TestHelper.MUSIC_FOLDER;

/**
 * Created by JuzTosS on 8/21/2016.
 */
public class RemoveMediaFromDevice
{
    public static boolean doRemove()
    {
        File dir = new File(Environment.getExternalStorageDirectory() + "/" + MUSIC_FOLDER);
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

    @Test
    public void execute() throws Exception
    {
        Assert.assertTrue(doRemove());
    }


}
