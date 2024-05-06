package com.juztoss.rhythmo.models;

import androidx.annotation.Nullable;

import com.juztoss.rhythmo.utils.SystemHelper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by JuzTosS on 12/17/2016.
 */

public class PathStore
{
    private Node mRoot = new Node(null);

    public String[] getPaths()
    {
        Set<Node> endPoints = mRoot.getEndPoints();

        String[] result = new String[endPoints.size()];
        int index = 0;
        for (Node node : endPoints)
        {
            result[index] = node.getPath();
            index++;
        }

        return result;
    }

    public void clearAdded()
    {
        mRoot = new Node(null);
    }

    /**
     * Returns state for the path, has it been added or not
     *
     * @param path          a path to check
     * @param siblingsCount count of his siblings in the parent folder (including himself)
     * @return NOT_ADDED, ADDED, or PARTLY_ADDED
     */
    public BaseExplorerElement.AddState getAddState(String path, int siblingsCount)
    {
        String[] folders = path.split(SystemHelper.SEPARATOR);

        Node current = mRoot;
        for (String segment : folders)
        {
            if (segment.length() <= 0) continue;//Sometimes path.split may return empty string
            Node next = current.get(segment);
            if (next == null)
            {
                return (current.hasChildren() || current == mRoot) ? BaseExplorerElement.AddState.NOT_ADDED : BaseExplorerElement.AddState.ADDED;
            }
            current = next;
        }

        if (current.hasChildren() && current.mChildren.size() < siblingsCount)
            return BaseExplorerElement.AddState.PARTLY_ADDED;
        else
            return BaseExplorerElement.AddState.ADDED;
    }

    public void add(String path)
    {
        String[] folders = path.split(SystemHelper.SEPARATOR);

        Node current = mRoot;
        for (String segment : folders)
        {
            if (segment.length() <= 0) continue;//Sometimes path.split may return empty string

            Node next = current.get(segment);
            if (next != null)
                current = next;
            else
                current = current.add(new Node(segment));
        }
    }

    public void remove(String path, List<BaseExplorerElement> siblings)
    {
        String[] folders = path.split(SystemHelper.SEPARATOR);

        Node current = mRoot;
        for (String segment : folders)
        {
            if (segment.length() <= 0) continue;//Sometimes path.split may return empty string
            Node next = current.get(segment);
            if (next == null)
            {
                current.removeChildren();
                for (BaseExplorerElement element : siblings)
                {
                    String siblingPath = element.getFileSystemPath();
                    if (!siblingPath.equals(path))
                        add(siblingPath);
                }
                return;
            }
            current = next;
        }

        while (current.mParent != null && current.mParent.mChildren.size() == 1)
        {
            current = current.mParent;
        }

        if (current.mParent != null)
            current.mParent.remove(current.mName);
        else
            current.removeChildren();//current is the root
    }


    public boolean isEmpty()
    {
        return !mRoot.hasChildren();
    }

    class Node
    {
        String mName;
        Node mParent;
        private Map<String, Node> mChildren = new HashMap<>();

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
                if (current.mName == null) break;

                path = SystemHelper.SEPARATOR + current.mName + path;
                current = current.mParent;
            }

            return path;
        }

        public void remove(String name)
        {
            mChildren.get(name).mParent = null;
            mChildren.remove(name);
        }
    }
}
