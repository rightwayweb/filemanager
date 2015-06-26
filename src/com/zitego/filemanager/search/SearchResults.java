package com.zitego.filemanager.search;

import com.zitego.util.SortColumn;
import com.zitego.filemanager.*;
import java.util.*;

/**
 * This class represents a search results list of Files.
 *
 * @author John Glorioso
 * @version $Id: SearchResults.java,v 1.1.1.1 2008/02/20 15:05:39 jglorioso Exp $
 * @see Search
 */
public class SearchResults extends FileListing
{
    /**
     * Creates a new empty SearchResults object.
     */
    protected SearchResults()
    {
        super();
    }

    /**
     * Creates a new SearchResults with an initial size.
     *
     * @param int The number of files in this listing.
     */
    protected SearchResults(int size)
    {
        super(size);
    }

    /**
     * Adds a file.
     *
     * @param File
     * @throws IllegalArgumentException if the file is null.
     */
    public void addFile(File f)
    {
        checkObject(f);
        super.add(f);
    }

    /**
     * Overrides addObject to make sure the argument is a File.
     *
     * @param FileSystemObject
     * @throws IllegalArgumentException if the object is null or not a File.
     */
    public void addObject(FileSystemObject obj)
    {
        checkObject(obj);
        super.add(obj);
    }

    /**
     * Overrides add to make sure that the Object being added is a File.
     *
     * @param Object Hopefully a File.
     * @return boolean (true) as per the general contract of Collection.add
     * @throws IllegalArgumentException if the Object is null or not a File.
     */
    public boolean add(Object obj) throws IllegalArgumentException
    {
        add(size(), obj);
        return true;
    }

    /**
     * Overrides add to make sure that the Object being added is a File.
     *
     * @param int The index at which to add the File.
     * @param Object Hopefully a File.
     * @throws IllegalArgumentException if the Object is null or not a File.
     */
    public void add(int index, Object obj) throws IllegalArgumentException
    {
        checkObject(obj);
        super.add(index, obj);
    }

    /**
     * Overrides addAll to make sure that this is a Collection of Files. If
     * the collection is null then nothing is added. Returns true if the listing changed.
     *
     * @param Collection Hopefully a Collection of Files.
     * @return boolean
     * @throws IllegalArgumentException if any of the Objects are null or not Files.
     */
    public boolean addAll(Collection collection) throws IllegalArgumentException
    {
        return addAll(size(), collection);
    }

    /**
     * Overrides addAll to make sure that this is a Collection of Files. If
     * the collection is null then nothing is added. Returns true if the listing changed.
     *
     * @param int The index at which to add the collection.
     * @param Collection Hopefully a Collection of Files.
     * @return boolean
     * @throws IllegalArgumentException if any of the Objects are null or not Files.
     */
    public boolean addAll(int index, Collection collection) throws IllegalArgumentException
    {
        if (collection == null) return false;
        for (Iterator i=collection.iterator(); i.hasNext();)
        {
            checkObject( i.next() );
        }
        return super.addAll(index, collection);
    }

    /**
     * Needs to take the object and check to see if it can be added. If not, it should throw an
     * IllegalArgumentException explaining why.
     *
     * @param Object The object to be added.
     */
    protected void checkObject(Object obj)
    {
        if (obj == null) throw new IllegalArgumentException("Object cannot be null.");
        if ( !(obj instanceof File) )
        {
            throw new IllegalArgumentException("Object must be of type com.zitego.filemanager.File");
        }
    }
}