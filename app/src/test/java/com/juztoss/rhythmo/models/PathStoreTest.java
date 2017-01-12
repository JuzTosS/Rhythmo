package com.juztoss.rhythmo.models;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.juztoss.rhythmo.models.BaseExplorerElement.AddState.NOT_ADDED;
import static com.juztoss.rhythmo.models.BaseExplorerElement.AddState.PARTLY_ADDED;
import static com.juztoss.rhythmo.models.BaseExplorerElement.AddState.ADDED;
import static com.juztoss.rhythmo.utils.SystemHelper.SEPARATOR;
import static org.junit.Assert.assertEquals;

/**
 * Created by JuzTosS on 12/17/2016.
 */
public class PathStoreTest
{
    @Test
    public void getPaths() throws Exception
    {
        PathStore store = new PathStore();
        assertEquals("Expected one element length if nothing added", 1, store.getPaths().length);
        assertEquals("Expected empty", true, store.isEmpty());

        store.add("1");
        assertEquals(1, store.getPaths().length);
        assertEquals("Expected not empty", false, store.isEmpty());

        store.add("2");
        assertEquals(2, store.getPaths().length);
        assertEquals("Expected not empty", false, store.isEmpty());

        store.add("1" + SEPARATOR + "2");
        assertEquals(2, store.getPaths().length);
        assertEquals("Expected not empty", false, store.isEmpty());
    }

    @Test
    public void clearAdded() throws Exception
    {
        PathStore store = new PathStore();
        assertEquals("Expected one element length if nothing added", 1, store.getPaths().length);
        assertEquals("Expected empty", true, store.isEmpty());
        store.add("1");
        assertEquals("Expected not empty", false, store.isEmpty());
        store.clearAdded();
        assertEquals("Expected one element length if nothing added", 1, store.getPaths().length);
        assertEquals("Expected empty", true, store.isEmpty());
    }

    @Test
    public void addAndRemove() throws Exception
    {
        PathStore store = new PathStore();
        store.add("1" + SEPARATOR + "2");
        assertEquals(ADDED, store.getAddState("1", 0));
        assertEquals(ADDED, store.getAddState("/1", 1));
        assertEquals(PARTLY_ADDED, store.getAddState("/1", 2));
        assertEquals(ADDED, store.getAddState("1" + SEPARATOR + "2", 0));
        assertEquals(ADDED, store.getAddState("/1" + SEPARATOR + "2", 0));
        assertEquals(NOT_ADDED, store.getAddState("2", 0));
        assertEquals(NOT_ADDED, store.getAddState("/1" + SEPARATOR + "3", 0));
        assertEquals(NOT_ADDED, store.getAddState("/1" + SEPARATOR + "3", 1));
        assertEquals(NOT_ADDED, store.getAddState("/1" + SEPARATOR + "3", 2));
        assertEquals(NOT_ADDED, store.getAddState("/1" + SEPARATOR + "3", 3));

        store.add("1" + SEPARATOR + "3");
        assertEquals(ADDED, store.getAddState("1", 0));
        assertEquals(ADDED, store.getAddState("/1", 1));
        assertEquals(ADDED, store.getAddState("/1", 2));
        assertEquals(PARTLY_ADDED, store.getAddState("/1", 3));
        assertEquals(ADDED, store.getAddState("1" + SEPARATOR + "2", 0));
        assertEquals(ADDED, store.getAddState("/1" + SEPARATOR + "2", 0));
        assertEquals(NOT_ADDED, store.getAddState("2", 0));
        assertEquals(ADDED, store.getAddState("/1" + SEPARATOR + "3", 0));
        assertEquals(ADDED, store.getAddState("/1" + SEPARATOR + "3", 1));
        assertEquals(ADDED, store.getAddState("/1" + SEPARATOR + "3", 2));
        assertEquals(ADDED, store.getAddState("/1" + SEPARATOR + "3", 3));

        List<BaseExplorerElement> siblings = new ArrayList<>();
        siblings.add(new MockExplorerElement("1" + SEPARATOR + "2"));
        siblings.add(new MockExplorerElement("1" + SEPARATOR + "3"));

        store.remove("1" + SEPARATOR + "3", siblings);
        assertEquals(ADDED, store.getAddState("1", 0));
        assertEquals(ADDED, store.getAddState("/1", 1));
        assertEquals(PARTLY_ADDED, store.getAddState("/1", 2));
        assertEquals(ADDED, store.getAddState("1" + SEPARATOR + "2", 0));
        assertEquals(ADDED, store.getAddState("/1" + SEPARATOR + "2", 0));
        assertEquals(NOT_ADDED, store.getAddState("2", 0));
        assertEquals(NOT_ADDED, store.getAddState("/1" + SEPARATOR + "3", 0));
        assertEquals(NOT_ADDED, store.getAddState("/1" + SEPARATOR + "3", 1));
        assertEquals(NOT_ADDED, store.getAddState("/1" + SEPARATOR + "3", 2));
        assertEquals(NOT_ADDED, store.getAddState("/1" + SEPARATOR + "3", 3));

        store.add("1" + SEPARATOR + "3");
        store.remove("1" + SEPARATOR + "2", siblings);
        assertEquals(ADDED, store.getAddState("1", 0));
        assertEquals(ADDED, store.getAddState("/1", 1));
        assertEquals(PARTLY_ADDED, store.getAddState("/1", 2));
        assertEquals(NOT_ADDED, store.getAddState("1" + SEPARATOR + "2", 0));
        assertEquals(NOT_ADDED, store.getAddState("/1" + SEPARATOR + "2", 0));
        assertEquals(NOT_ADDED, store.getAddState("2", 0));
        assertEquals(ADDED, store.getAddState("/1" + SEPARATOR + "3", 0));
        assertEquals(ADDED, store.getAddState("/1" + SEPARATOR + "3", 1));
        assertEquals(ADDED, store.getAddState("/1" + SEPARATOR + "3", 2));
        assertEquals(ADDED, store.getAddState("/1" + SEPARATOR + "3", 3));
    }

    @Test
    public void partlyAdded() throws Exception
    {
        PathStore store = new PathStore();
        store.add("1" + SEPARATOR + "1");
        store.add("1" + SEPARATOR + "2");
        store.add("1" + SEPARATOR + "3");
        assertEquals(PARTLY_ADDED, store.getAddState("1", 4));
    }

    @Test
    public void getAddStateForIncludedChild() throws Exception
    {
        PathStore store = new PathStore();
        store.add("1" + SEPARATOR + "1");
        store.add("1" + SEPARATOR + "2");
        store.add("1" + SEPARATOR + "3");
        assertEquals(ADDED, store.getAddState("1" + SEPARATOR + "2" + SEPARATOR + "3", 4));
        assertEquals(ADDED, store.getAddState("1" + SEPARATOR + "1", 3));
        assertEquals(ADDED, store.getAddState("1" + SEPARATOR + "2", 3));
        assertEquals(ADDED, store.getAddState("1" + SEPARATOR + "3", 3));
        assertEquals(NOT_ADDED, store.getAddState("1" + SEPARATOR + "4", 3));
    }

    @Test
    public void empty() throws Exception
    {
        PathStore store = new PathStore();
        assertEquals(NOT_ADDED, store.getAddState("1" + SEPARATOR + "2" + SEPARATOR + "3", 4));
        assertEquals(NOT_ADDED, store.getAddState("1" + SEPARATOR + "1", 3));
        assertEquals(NOT_ADDED, store.getAddState("1" + SEPARATOR + "2", 3));
        assertEquals(NOT_ADDED, store.getAddState("1" + SEPARATOR + "3", 3));
        assertEquals(NOT_ADDED, store.getAddState("1" + SEPARATOR + "4", 3));
    }
}