package com.zitego.filemanager;

import com.zitego.util.SortColumn;
import java.util.*;

/**
 * This class represents a directory tree from a parent Directory.
 *
 * @author John Glorioso
 * @version $Id: DirectoryTree.java,v 1.1.1.1 2008/02/20 15:05:39 jglorioso Exp $
 * @see Directory
 */
public class DirectoryTree extends FileListing
{
    /**
     * Creates a new empty DirectoryTree object with a parent directory.
     *
     * @param Directory The parent directory.
     * @throws IllegalArgumentException if the parent directory is null.
     */
    DirectoryTree(Directory parent) throws IllegalArgumentException
    {
        super(parent);
        super.setSortColumn( new SortColumn(CompareProperty.ROOT_PATH) );
        super.setSortAscending();
    }

    /**
     * Creates a new DirectoryTree with an initial size and a parent.
     *
     * @param Directory The parent directory.
     * @param int The number of directories in this listing.
     * @throws IllegalArgumentException if the parent directory is null.
     */
    DirectoryTree(Directory parent, int size) throws IllegalArgumentException
    {
        super(parent, size);
        super.setSortColumn( new SortColumn(CompareProperty.ROOT_PATH) );
        super.setSortAscending();
    }

    /**
     * Adds a directory.
     *
     * @param Directory
     * @throws IllegalArgumentException if the directory is null.
     */
    public void addDirectory(Directory dir)
    {
        if (dir == null) throw new IllegalArgumentException("Directory cannot be null.");
        super.add(dir);
    }

    /**
     * Overrides addObject to make sure the argument is a directory.
     *
     * @param FileSystemObject
     * @throws IllegalArgumentException if the directory is null or not a Directory.
     */
    public void addObject(FileSystemObject obj)
    {
        if (obj == null) throw new IllegalArgumentException("Directory cannot be null.");
        if ( !(obj instanceof Directory) )
        {
            throw new IllegalArgumentException("Object must be of type com.zitego.filemanager.Directory");
        }
        super.add(obj);
    }

    /**
     * Overrides add to make sure that the Object being added is a Directory.
     *
     * @param Object Hopefully a Directory.
     * @return boolean (true) as per the general contract of Collection.add
     * @throws IllegalArgumentException if the Object is null or not a Directory.
     */
    public boolean add(Object obj) throws IllegalArgumentException
    {
        add(size(), obj);
        return true;
    }

    /**
     * Overrides add to make sure that the Object being added is a Directory.
     *
     * @param int The index at which to add the Directory.
     * @param Object Hopefully a Directory.
     * @throws IllegalArgumentException if the Object is null or not a Directory.
     */
    public void add(int index, Object obj) throws IllegalArgumentException
    {
        if (obj == null) throw new IllegalArgumentException("Directory cannot be null.");
        if ( !(obj instanceof Directory) )
        {
            throw new IllegalArgumentException("Object must be of type com.zitego.filemanager.Directory");
        }
        super.add(index, obj);
    }

    /**
     * Overrides addAll to make sure that this is a Collection of Directories. If
     * the collection is null then nothing is added. Returns true if the listing changed.
     *
     * @param Collection Hopefully a Collection of Directories.
     * @return boolean
     * @throws IllegalArgumentException if any of the Objects are null or not Directories.
     */
    public boolean addAll(Collection collection) throws IllegalArgumentException
    {
        return addAll(size(), collection);
    }

    /**
     * Overrides addAll to make sure that this is a Collection of Directories. If
     * the collection is null then nothing is added. Returns true if the listing changed.
     *
     * @param int The index at which to add the collection.
     * @param Collection Hopefully a Collection of Directories.
     * @return boolean
     * @throws IllegalArgumentException if any of the Objects are null or not Directories.
     */
    public boolean addAll(int index, Collection collection) throws IllegalArgumentException
    {
        if (collection == null) return false;
        for (Iterator i=collection.iterator(); i.hasNext();)
        {
            if ( !(i.next() instanceof Directory) )
            {
                throw new IllegalArgumentException("Objects in Collection must all be of type com.zitego.filemanager.Directory");
            }
        }
        return super.addAll(index, collection);
    }

    public void setSortDescending() { }

    public void setSortColumn(SortColumn col) { }
}