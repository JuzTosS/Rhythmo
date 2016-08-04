package com.juztoss.bpmplayer;

import com.juztoss.bpmplayer.utils.SystemHelper;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class getLastSegmentOfPathTest
{
    @Test
    public void aTest() throws Exception
    {
        assertEquals("onesegment", SystemHelper.getLastSegmentOfPath("onesegment"));
        assertEquals("last", SystemHelper.getLastSegmentOfPath("first/last"));
        assertEquals("last", SystemHelper.getLastSegmentOfPath("first/second/last"));
        assertEquals("last", SystemHelper.getLastSegmentOfPath("first/second/last/"));
        assertEquals("", SystemHelper.getLastSegmentOfPath("/"));
        assertEquals("last", SystemHelper.getLastSegmentOfPath("last/"));
        assertEquals("last", SystemHelper.getLastSegmentOfPath("/last/"));
        assertEquals("last", SystemHelper.getLastSegmentOfPath("/last"));
    }
}