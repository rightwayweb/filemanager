package com.zitego.filemanager;

import com.zitego.util.*;
import com.zitego.filemanager.util.FileSizeFormat;
import java.util.*;

/**
 * This class represents a file system object listing from a Directory. This
 * listing can be sorted on various FileType attributes including name, size,
 * extension (type), and last modified.
 *
 * @author John Glorioso
 * @version $Id: FileListing.java,v 1.1.1.1 2008/02/20 15:05:39 jglorioso Exp $
 * @see Directory
 */
public class FileListing extends ArrayList implements Sortable
{
    /** The directory that created this tree. */
    protected Directory _parentDirectory;
    /* To keep track of sort direction. */
    private int _sortDirection = Sortable.ASCENDING;
    /* To keep track of the sort column. */
    private SortColumn _sortColumn;

    /**
     * Creates a new empty FileListing object.
     */
    public FileListing()
    {
        super();
    }

    /**
     * Creates a new FileListing with an initial size.
     *
     * @param int The number of objects in this listing.
     */
    public FileListing(int size)
    {
        super(size);
    }

    /**
     * Creates a new empty FileListing object with a parent directory.
     *
     * @param Directory The parent directory.
     * @throws IllegalArgumentException if the parent directory is null.
     */
    public FileListing(Directory parent) throws IllegalArgumentException
    {
        this();
        setParentDirectory(parent);
    }

    /**
     * Creates a new FileListing with an initial size.
     *
     * @param Directory The parent directory.
     * @param int The number of objects in this listing.
     * @throws IllegalArgumentException if the parent directory is null.
     */
    public FileListing(Directory parent, int size) throws IllegalArgumentException
    {
        this(size);
        setParentDirectory(parent);
    }

    /**
     * Sets the parent directory.
     *
     * @param Directory The parent.
     * @throws IllegalArgumentException if the parent directory is null.
     */
    private void setParentDirectory(Directory parent) throws IllegalArgumentException
    {
        if (parent == null) throw new IllegalArgumentException("Parent directory cannot be null");
        _parentDirectory = parent;
    }

    /**
     * Returns the parent directory.
     *
     * @return Directory
     */
    public Directory getParentDirectory()
    {
        return _parentDirectory;
    }

    /**
     * Checks to see if the listing contains the file system object specified.
     *
     * @param FileSystemObject
     */
    public boolean contains(FileSystemObject obj)
    {
        int size = size();
        for (int i=0; i<size; i++)
        {
            if ( get(i).equals(obj) ) return true;
        }
        return false;
    }

    /**
     * Adds a file system object.
     *
     * @param FileSystemObject
     * @throws IllegalArgumentException if the object is null.
     */
    public void addObject(FileSystemObject obj)
    {
        if (obj == null) throw new IllegalArgumentException("FileSystemObject cannot be null.");
        super.add(obj);
    }

    /**
     * Overrides add to make sure that the Object being added is a FileSystemObject.
     *
     * @param Object Hopefully a FileSystemObject.
     * @return boolean (true) as per the general contract of Collection.add
     * @throws IllegalArgumentException if the Object is null or not a FileSystemObject.
     */
    public boolean add(Object obj) throws IllegalArgumentException
    {
        add(size(), obj);
        return true;
    }

    /**
     * Overrides add to make sure that the Object being added is a FileSystemObject.
     *
     * @param int The index at which to add the Object.
     * @param Object Hopefully a FileSystemObject.
     * @throws IllegalArgumentException if the Object is null or not a FileSystemObject.
     */
    public void add(int index, Object obj) throws IllegalArgumentException
    {
        if (obj == null) throw new IllegalArgumentException("FileSystemObject cannot be null.");
        if ( !(obj instanceof FileSystemObject) )
        {
            throw new IllegalArgumentException("Object must be of type com.zitego.filemanager.FileSystemObject");
        }
        super.add(index, obj);
    }

    /**
     * Overrides addAll to make sure that this is a Collection of FileSystemObjects. If
     * the collection is null then nothing is added. Returns true if the listing changed.
     *
     * @param Collection Hopefully a Collection of FileSystemObjects.
     * @return boolean
     * @throws IllegalArgumentException if any of the Objects are null or not FileSystemObjects.
     */
    public boolean addAll(Collection collection) throws IllegalArgumentException
    {
        return addAll(size(), collection);
    }

    /**
     * Overrides addAll to make sure that this is a Collection of FileSystemObjects. If
     * the collection is null then nothing is added. Returns true if the listing changed.
     *
     * @param int The index at which to add the collection.
     * @param Collection Hopefully a Collection of FileSystemObjects.
     * @return boolean
     * @throws IllegalArgumentException if any of the Objects are null or not FileSystemObjects.
     */
    public boolean addAll(int index, Collection collection) throws IllegalArgumentException
    {
        if (collection == null) return false;
        for (Iterator i=collection.iterator(); i.hasNext();)
        {
            if ( !(i.next() instanceof FileSystemObject) )
            {
                throw new IllegalArgumentException("Objects in Collection must all be of type com.zitego.filemanager.FileSystemObject");
            }
        }
        return super.addAll(index, collection);
    }

    public void setSortAscending()
    {
        _sortDirection = Sortable.ASCENDING;
    }

    public void setSortDescending()
    {
        _sortDirection = Sortable.DESCENDING;
    }

    public int getSortDirection()
    {
        return _sortDirection;
    }

    public void setSortColumn(SortColumn col)
    {
        Constant c = col.getConstant();
        if ( !(c instanceof CompareProperty) ) throw new IllegalArgumentException("Sort column must be on a com.zitego.filemanager.CompareProperty");
        _sortColumn = col;
    }

    public SortColumn getSortColumn()
    {
        return _sortColumn;
    }

    public void sort()
    {
        FileSystemObject[] objs = new FileSystemObject[size()];
        CompareProperty prop = (_sortColumn != null ? (CompareProperty)_sortColumn.getConstant(): null);
        for (int i=0; i<objs.length; i++)
        {
            objs[i] = (FileSystemObject)get(i);
            if (prop != null) objs[i].setCompareProperty(prop);
            objs[i].setSortDirection(_sortDirection);
        }
        Arrays.sort(objs);
        clear();
        addAll( Arrays.asList(objs) );
    }

    /**
     * Returns the total size of the sum of all the files as a formatted string.
     *
     * @return String
     */
    public String getTotalSize()
    {
        long bytes = 0L;
        int size = size();
        for (int i=0; i<size; i++)
        {
            bytes += ( (FileSystemObject)get(i) ).getSize().getBytes();
        }
        return FileSizeFormat.FORMATTER.format(bytes);
    }
}