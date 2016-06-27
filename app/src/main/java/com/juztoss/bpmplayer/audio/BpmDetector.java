package com.juztoss.bpmplayer.audio;

import android.util.Log;

import com.juztoss.bpmplayer.presenters.BPMPlayerApp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by JuzTosS on 5/25/2016.
 */
public class BpmDetector
{
    public static native double detect(String path);

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
        if(bpm < BPMPlayerApp.MIN_BPM || bpm > BPMPlayerApp.MAX_BPM)
            bpm = 0;

        Log.e(BpmDetector.class.toString(), "Detected bpm: " + bpm + "; From name: " + name + ";");
        return bpm;
    }
}
