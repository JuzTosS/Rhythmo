package com.juztoss.bpmplayer.utils;

import android.os.Build;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Pattern;

/**
 * Created by JuzTosS on 5/30/2016.
 */
public class SystemHelper
{
    /**
     * @Return number of processors on the device
     */
    public static int getNumberOfCores()
    {
        if (Build.VERSION.SDK_INT >= 17)
        {
            return Runtime.getRuntime().availableProcessors();
        }
        else
        {
            return getNumCoresOldPhones();
        }
    }

    /**
     * Gets the number of cores available in this device, across all processors.
     * Requires: Ability to peruse the filesystem at "/sys/devices/system/cpu"
     *
     * @return The number of cores, or 1 if failed to get result
     */
    private static int getNumCoresOldPhones()
    {
        //Private Class to display only CPU devices in the directory listing
        class CpuFilter implements FileFilter
        {
            @Override
            public boolean accept(File pathname)
            {
                //Check if filename is "cpu", followed by a single digit number
                if (Pattern.matches("cpu[0-9]+", pathname.getName()))
                {
                    return true;
                }
                return false;
            }
        }

        try
        {
            //Get directory containing CPU info
            File dir = new File("/sys/devices/system/cpu/");
            //Filter to only list the devices we care about
            File[] files = dir.listFiles(new CpuFilter());
            //Return the number of cores (virtual CPU devices)
            return files.length;
        } 
        catch (Exception e)
        {
            //Default to return 1 core
            return 1;
        }
    }

    public static final String SEPARATOR = "/";

    /**
     * Any path with segments separated by "/"
     * @Return last segement of the input path
     */
    public static String getLastSegmentOfPath(String path)
    {
        int lastIndexOfSeparator = path.lastIndexOf(SEPARATOR);
        if(lastIndexOfSeparator == path.length() - 1)
        {
            path = path.substring(0, path.length() - 1);
            lastIndexOfSeparator = path.lastIndexOf(SEPARATOR);
        }
        if(lastIndexOfSeparator >= 0)
            return path.substring(lastIndexOfSeparator + 1);
        else
            return path;
    }
}
