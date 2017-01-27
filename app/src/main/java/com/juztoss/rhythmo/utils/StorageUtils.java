package com.juztoss.rhythmo.utils;

import android.os.Environment;
import android.support.annotation.NonNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by JuzTosS on 1/25/2017.
 */

public class StorageUtils
{
    public static List<File> getStorageList()
    {

        List<File> list = new ArrayList<>();
        String defPath = Environment.getExternalStorageDirectory().getPath();
        String defPathState = Environment.getExternalStorageState();
        boolean defPathAvailable = defPathState.equals(Environment.MEDIA_MOUNTED)
                || defPathState.equals(Environment.MEDIA_MOUNTED_READ_ONLY);
        BufferedReader bufReader = null;
        try
        {
            HashSet<String> paths = new HashSet<>();
            bufReader = new BufferedReader(new FileReader("/proc/mounts"));
            String line;

            while ((line = bufReader.readLine()) != null)
            {
                if (line.contains("vfat") || line.contains("/mnt"))
                {
                    StringTokenizer tokens = new StringTokenizer(line, " ");
                    tokens.nextToken();//Device
                    String mountPoint = tokens.nextToken(); //Mount point
                    if (paths.contains(mountPoint))
                    {
                        continue;
                    }

                    if (mountPoint.equals(defPath))
                    {
                        paths.add(defPath);
                        list.add(0, new File(defPath));
                    }
                    else if (line.contains("/dev/block/vold"))
                    {
                        if (!line.contains("/mnt/secure")
                                && !line.contains("/mnt/asec")
                                && !line.contains("/mnt/obb")
                                && !line.contains("/dev/mapper")
                                && !line.contains("tmpfs"))
                        {
                            paths.add(mountPoint);
                            list.add(new File(mountPoint));
                        }
                    }
                }
            }

            if (!paths.contains(defPath) && defPathAvailable)
            {
                list.add(0, new File(defPath));
            }

        }
        catch (FileNotFoundException ex)
        {
            ex.printStackTrace();
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
        finally
        {
            if (bufReader != null)
            {
                try
                {
                    bufReader.close();
                }
                catch (IOException ex)
                {
                }
            }
        }
        return list;
    }


    @NonNull
    public static String getExtension(String filename)
    {
        if (filename == null)
            return "";

        int index = filename.lastIndexOf(".");
        if (index == -1)
            return "";
        else
            return filename.substring(index + 1);
    }
}