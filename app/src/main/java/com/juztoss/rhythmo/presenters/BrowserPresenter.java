package com.juztoss.rhythmo.presenters;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;

import com.juztoss.rhythmo.models.BaseExplorerElement;
import com.juztoss.rhythmo.models.CustomExplorerElement;
import com.juztoss.rhythmo.models.ExplorerPriority;
import com.juztoss.rhythmo.models.FileSystemFolder;
import com.juztoss.rhythmo.models.MediaFolder;
import com.juztoss.rhythmo.utils.SystemHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by JuzTosS on 4/20/2016.
 */
public class BrowserPresenter extends BasePresenter implements LoaderManager.LoaderCallbacks<List<BaseExplorerElement>>
{
    BaseExplorerElement mCurrent;
    private OnDataChangedListener mListener;

    private List<BaseExplorerElement> mData;
    private BaseExplorerElement mRoot;

    public BrowserPresenter(RhythmoApp app)
    {
        super(app);
        CustomExplorerElement root = new CustomExplorerElement("", new ArrayList<BaseExplorerElement>(), ExplorerPriority.HIGHEST);
        root.add(new FileSystemFolder(new File(SystemHelper.SEPARATOR), "File system", root, app));
        root.add(new MediaFolder(-1, "Media", false, root, false, app));

        mRoot = root;
        mCurrent = root;

        mData = new ArrayList<>();
    }

    public BaseExplorerElement getRoot()
    {
        return mRoot;
    }

    public void setCurrent(BaseExplorerElement element)
    {
        mCurrent = element;
    }

    @Override
    public Loader<List<BaseExplorerElement>> onCreateLoader(int id, Bundle args)
    {
        AsyncTaskLoader<List<BaseExplorerElement>> fileLoader = new AsyncTaskLoader<List<BaseExplorerElement>>(getApp())
        {
            @Override
            public List<BaseExplorerElement> loadInBackground()
            {
                return mCurrent.getChildren();
            }
        };
        fileLoader.forceLoad();
        return fileLoader;
    }

    @Override
    public void onLoadFinished(Loader<List<BaseExplorerElement>> loader, List<BaseExplorerElement> data)
    {
        mData = data;
        if (mListener != null)
            mListener.onDataChanged();
    }

    @Override
    public void onLoaderReset(Loader<List<BaseExplorerElement>> loader)
    {

    }

    public Cursor getSongIds()
    {
        return mCurrent.getSongIds();
    }

    public void setOnDataChangedListener(OnDataChangedListener listener)
    {
        mListener = listener;
    }

    public List<BaseExplorerElement> getList()
    {
        return mData;
    }

    public BaseExplorerElement getCurrent()
    {
        return mCurrent;
    }

    private Node root = new Node(null);

    public String[] getPaths()
    {
        if(root.hasChildren())//Files were selected
        {
            Set<Node> endPoints = root.getEndPoints();

            String[] result = new String[endPoints.size()];
            int index = 0;
            for (Node node : endPoints)
            {
                result[index] = node.getPath();
                index++;
            }

            return result;
        }
        else
        {
            return new String[]{mCurrent.getFileSystemPath()};
        }
    }

    public void clearAdded()
    {
        root = new Node(null);
    }

    public BaseExplorerElement.AddState getAddState(String path)
    {
        String[] folders = path.split(SystemHelper.SEPARATOR);
        folders = Arrays.copyOfRange(folders, 1, folders.length);

        Node current = root;
        for (String segment : folders)
        {
            current = current.get(segment);
            if (current == null)
                return BaseExplorerElement.AddState.NOT_ADDED;
        }

        if (current.hasChildren())
            return BaseExplorerElement.AddState.PARTLY_ADDED;
        else
            return BaseExplorerElement.AddState.ADDED;
    }

    public void add(String path)
    {
        String[] folders = path.split(SystemHelper.SEPARATOR);
        folders = Arrays.copyOfRange(folders, 1, folders.length);

        Node current = root;
        for (String segment : folders)
        {
            Node next = current.get(segment);
            if (next != null)
                current = next;
            else
                current = current.add(new Node(segment));
        }
    }

    public void remove(String path)
    {
        String[] folders = path.split(SystemHelper.SEPARATOR);
        folders = Arrays.copyOfRange(folders, 1, folders.length);

        Node current = root;
        for (String segment : folders)
        {
            if (current.childrenCount() <= 1)
            {
                current.removeChildren();
                return;
            }
            current = current.get(segment);
            if (current == null)
                return;//Path doesn't exist
        }
    }

    public interface OnDataChangedListener
    {
        void onDataChanged();
    }

    class Node
    {
        String mName;

        public Node(@Nullable String name)
        {
            mName = name;
        }

        public Node add(Node node)
        {
            mChildren.put(node.mName, node);
            node.mParent = this;
            return node;
        }

        Node mParent;
        private Map<String, Node> mChildren = new HashMap<>();

        public Node get(String folder)
        {
            return mChildren.get(folder);
        }

        public boolean hasChildren()
        {
            return !mChildren.isEmpty();
        }

        public int childrenCount()
        {
            return mChildren.size();
        }

        public void removeChildren()
        {
            mChildren.clear();
        }

        public Set<Node> getEndPoints()
        {
            Set<Node> endPoints = new HashSet<>();
            if (hasChildren())
            {
                Iterator iterator = mChildren.entrySet().iterator();
                while (iterator.hasNext())
                {
                    Map.Entry<String, Node> pair = (Map.Entry<String, Node>) iterator.next();
                    endPoints.addAll(pair.getValue().getEndPoints());
                }
            }
            else
            {
                endPoints.add(this);
            }
            return endPoints;
        }

        public String getPath()
        {
            String path = "";
            Node current = this;
            while (current != null)
            {
                if(current.mName == null) break;

                path = SystemHelper.SEPARATOR + current.mName + path;
                current = current.mParent;
            }

            return path;
        }
    }
}