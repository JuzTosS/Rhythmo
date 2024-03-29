package com.juztoss.rhythmo.audio;

import com.juztoss.rhythmo.presenters.RhythmoApp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by JuzTosS on 5/25/2016.
 * A helper for detect songs BPM
 */
public class BpmDetector
{
    /**
     * Detects song BPM from audio data (May takes a long time)
     */
    private static native double detect(String path);

    public static double detectFromData(String path)
    {
        double value = detect(path);
        return value < RhythmoApp.MIN_BPM ? 0 : value;
    }

    /**
     * Detects song BPM from file name
     */
    public static double detectFromName(String name)
    {
        Pattern p = Pattern.compile("^[0-9]+\\.?[0-9]*");
        Matcher m = p.matcher(name);
        String bpmString = "0";
        if (m.find())
        {
            bpmString = m.group();
        }
        else
        {
            p = Pattern.compile("([0-9]+\\.?[0-9]*).{0,3}bpm");
            m = p.matcher(name);
            if (m.find())
            {
                bpmString = m.group();
            }
        }

        double bpm = Double.valueOf(bpmString);
        if(bpm < RhythmoApp.MIN_BPM || bpm > RhythmoApp.MAX_BPM)
            bpm = 0;

        return bpm;
    }
}
