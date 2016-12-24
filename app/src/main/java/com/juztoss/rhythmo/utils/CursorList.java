package com.juztoss.rhythmo.utils;

import android.database.Cursor;
import android.support.annotation.NonNull;

import com.juztoss.rhythmo.models.Composition;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.RandomAccess;

/**
 * Created by JuzTosS on 12/24/2016.
 */

public class CursorList implements List<Composition>, RandomAccess
{
    private Cursor mCursor;

    public CursorList(Cursor cursor)
    {
        mCursor = cursor;
    }

    @Override
    public void add(int location, Composition object)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean add(Composition object)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int location, Collection<? extends Composition> collection)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends Composition> collection)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(Object object)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> collection)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Composition get(int location)
    {
        mCursor.moveToPosition(location);
        return Composition.fromCursor(mCursor);
    }

    @Override
    public int indexOf(Object object)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEmpty()
    {
        return mCursor.getCount() <= 0 || mCursor.isClosed();
    }

    @NonNull
    @Override
    public Iterator<Composition> iterator()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public int lastIndexOf(Object object)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public ListIterator<Composition> listIterator()
    {
        return new CursorIterator(mCursor, -1);
    }

    @NonNull
    @Override
    public ListIterator<Composition> listIterator(int location)
    {
        return new CursorIterator(mCursor, location);
    }

    @Override
    public Composition remove(int location)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object object)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> collection)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> collection)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Composition set(int location, Composition object)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size()
    {
        return mCursor.getCount();
    }

    @NonNull
    @Override
    public List<Composition> subList(int start, int end)
    {
        throw new UnsupportedOperationException();
    }

    @NonNull
    @Override
    public Object[] toArray()
    {
        throw new UnsupportedOperationException();
    }

    @NonNull
    @Override
    public <T> T[] toArray(T[] array)
    {
        throw new UnsupportedOperationException();
    }


    private static class CursorIterator implements ListIterator<Composition>
    {
        private final Cursor mCursor;
        private int mPosition;
        public CursorIterator(Cursor cursor, int location)
        {
            mCursor = cursor;
            mPosition = location;
        }

        @Override
        public void add(Composition object)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean hasNext()
        {
            return mPosition < mCursor.getCount() - 1;
        }

        @Override
        public boolean hasPrevious()
        {
            return mPosition > 0;
        }

        @Override
        public Composition next()
        {
            if (!hasNext())
                throw new NoSuchElementException();

            mPosition++;
            mCursor.moveToPosition(mPosition);
            return Composition.fromCursor(mCursor);
        }

        @Override
        public int nextIndex()
        {
            return mPosition + 1;
        }

        @Override
        public Composition previous()
        {
            if (!hasPrevious())
                throw new NoSuchElementException();

            mPosition--;
            mCursor.moveToPosition(mPosition);
            return Composition.fromCursor(mCursor);
        }

        @Override
        public int previousIndex()
        {
            return mPosition - 1;
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(Composition object)
        {
            throw new UnsupportedOperationException();
        }
    }
}
