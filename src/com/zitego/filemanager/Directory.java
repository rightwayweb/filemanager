package com.zitego.filemanager;

import com.zitego.filemanager.util.*;
import com.zitego.util.*;
import java.io.IOException;

/**
 * This class represents a directory in the filemanager system. The Directory contains
 * a DirectoryTree and a FileListing that are created when asked for. Cached copies of
 * these structures are kept within the object to reduce disk i/o. Use the refresh()
 * method to clear the cache so that they will be retrieved fresh on the next call.
 *
 * @author John Glorioso
 * @version $Id: Directory.java,v 1.1.1.1 2008/02/20 15:05:39 jglorioso Exp $
 * @see FileSystemObjectFactory
 */
public class Directory extends FileSystemObject implements Sortable
{
    /** A cached copy of the DirectoryTree. */
    protected DirectoryTree _directoryTree;
    /** A cached copy of the FileListing. */
    protected FileListing _fileListing;

    public static void main(String[] args) throws Exception
    {
        Directory dir = (Directory)FileSystemObjectFactory.createObject(args[0], args[1]);
        FileListing listing = dir.getFileListing();
        CompareProperty prop = CompareProperty.evaluate(Integer.parseInt(args[2]));
        int direction = Integer.parseInt(args[3]);
        System.out.println( "\nFile Listing of "+dir.getAbsolutePath()+" by "+prop.getDescription()+" "+(direction<0?"de":"as")+"cending");
        listing.sort();
        int size = listing.size();
        for (int i=0; i<size; i++)
        {
            FileSystemObject obj = (FileSystemObject)listing.get(i);
            System.out.println(obj.getName()+(obj instanceof Directory ?" (directory)":"")+", "+FileSizeFormat.getKb(obj.getSize().getBytes())+"kb, "+obj.getLastModifiedDate());
        }
        DirectoryTree tree = dir.getDirectoryTree();
        System.out.println( "\nDirectories:");
        size = tree.size();
        for (int i=0; i<size; i++)
        {
            Directory d = (Directory)tree.get(i);
            System.out.println( d.getName() );
        }
    }

    /**
     * Creates a new Directory given the absolute and viewable root path.
     *
     * @param String The absolute path.
     * @param String The root path.
     */
    Directory(String absolutePath, String rootPath)
    {
        super(absolutePath, rootPath);
    }

    /**
     * Creates a root Directory given the absolute path. The root path would be "/".
     *
     * @param String The path.
     */
    protected Directory(String path)
    {
        this(path, "/");
    }

    /**
     * Creates a new Directory with the given name. The name cannot contain
     * a forward slash.
     *
     * @param String The directory.
     * @return Directory
     * @throws IllegalArgumentException if the directory name is invalid.
     * @throws IOException if the directory cannot be created.
     */
    public Directory createDirectory(String name) throws IOException
    {
        if (name == null || name.indexOf("/") > -1)
        {
            throw new IllegalArgumentException("Directory name cannot be null or contain a forward slash.");
        }
        java.io.File f = new java.io.File(getAbsolutePath()+"/"+name);
        String rootPath = getRootPath();
        if (rootPath.length() > 1) rootPath += "/";
        rootPath += name;
        if ( !f.mkdir() ) throw new IOException("Directory: "+f.getAbsolutePath()+" ("+rootPath+") could not be created");

        Directory dir = new Directory(f.getAbsolutePath(), rootPath);
        CreationDateFile.setCreationDate(dir);
        return dir;
    }

    /**
     * Returns a FileListing for all Files and Directories in this Directory. If the directory
     * is empty, then an empty FileListing object is returned.
     *
     * @return FileListing
     * @throws IOException if a problem occurs retrieving the file listing.
     */
    public FileListing getFileListing() throws IOException
    {
        return getFileListing("*");
    }

    /**
     * Returns a FileListing for all Files and Directories in this Directory that match the given
     * wildcard pattern String. If the directory is empty or nothing matches the wildcard pattern,
     * then an empty FileListing object is returned. The wildcard pattern can match any given
     * file system wildcard pattern such as *.*, *.txt, etc.
     *
     * @param String The regular expression.
     * @return FileListing
     * @throws IOException if a problem occurs retrieving the file listing.
     */
    public FileListing getFileListing(String filter) throws IOException
    {
        return getFileListing( new WildcardFilter(filter, false, true, true) );
    }

    /**
     * Returns a FileListing for all Files and Directories in this Directory that match the given
     * WildcardFilter. If the directory is empty or nothing matches the filter, then an
     * empty FileListing object is returned. The wildcard pattern can match any given file system
     * wildcard pattern such as *.*, *.txt, etc.
     *
     * @param WildcardFilter The filter.
     * @return FileListing
     * @throws IOException if a problem occurs retrieving the file listing.
     */
    public FileListing getFileListing(WildcardFilter filter) throws IOException
    {
        String pattern = filter.getPattern();
        boolean fullListing = "*".equals(pattern);
        if (fullListing && _fileListing != null)
        {
            return _fileListing;
        }
        else
        {
            String[] results = getInternalFile().list(filter);
            if (results == null) results = new String[0];
            FileListing listing = new FileListing(this, results.length);
            String absolutePath = getAbsolutePath();
            String rootDir = getRootDirectory();
            for (int i=0; i<results.length; i++)
            {
                listing.addObject( FileSystemObjectFactory.createObject(absolutePath + "/" + results[i], rootDir) );
            }
            if (fullListing)
            {
                _fileListing = listing;
                _fileListing.sort();
            }
            return listing;
        }
    }

    /**
     * Returns a DirectoryTree for all directories in this directory.
     *
     * @return DirectoryTree
     * @throws DirectoryTree if a problem occurs retrieving the file listing.
     */
    public DirectoryTree getDirectoryTree() throws IOException
    {
        if (_directoryTree != null)
        {
            return _directoryTree;
        }
        else
        {
            String[] results = getInternalFile().list( new DirectoryFilter() );
            if (results == null) results = new String[0];
            DirectoryTree tree = new DirectoryTree(this, results.length);
            String absolutePath = getAbsolutePath();
            String rootDir = getRootDirectory();
            for (int i=0; i<results.length; i++)
            {
                tree.addObject( FileSystemObjectFactory.createObject(absolutePath + "/" + results[i], rootDir) );
            }
            _directoryTree = tree;
            _directoryTree.sort();
            return tree;
        }
    }

    /**
     * Resets the directory tree and file listing object.
     */
    public void refresh()
    {
        _directoryTree = null;
        _fileListing = null;
    }

    /**
     * Returns the specified file or directory given the root path. This will check to
     * see if there is a file before it checks for a directory.
     *
     * @param String The root path.
     * @return FileSystemObject
     * @throws IOException if an error occurs searching for the object.
     */
    public FileSystemObject getFileSystemObject(String rootPath) throws IOException
    {
        FileSystemObject ret = getFile(rootPath);
        if (ret == null) ret = getDirectory(rootPath);
        return ret;
    }

    /**
     * Returns a directory given a root path. If it is not found, then this returns null.
     *
     * @param String The given directory.
     * @return Directory
     * @throws IOException if an error occurs searching for the directory.
     */
    public Directory getDirectory(String rootPath) throws IOException
    {
        //First see if this is the one we want
        if ( getRootPath().equals(rootPath) ) return this;

        //Not this directory, so go through each child directory looking
        //for it in there
        DirectoryTree tree = getDirectoryTree();
        int size = tree.size();
        for (int i=0; i<size; i++)
        {
            Directory dir = (Directory)tree.get(i);
            if ( rootPath.startsWith(dir.getRootPath()) )
            {
                Directory dir2 = dir.getDirectory(rootPath);
                if (dir2 != null) return dir2;
            }
        }
        //Couldn't find it
        return null;
    }

    /**
     * Returns a file given a root path. If it is not found, then this returns null.
     *
     * @param String The given file.
     * @return File
     * @throws IOException if an error occurs searching for the file.
     */
    public File getFile(String rootPath) throws IOException
    {
        if (rootPath == null) return null;

        //Make sure it isn't a directory they are looking for
        if ( rootPath.endsWith("/") ) return null;

        //First check our file list
        FileListing listing = getFileListing();
        int size = listing.size();
        for (int i=0; i<size; i++)
        {
            FileSystemObject obj = (FileSystemObject)listing.get(i);
            if ( obj instanceof File && obj.getRootPath().equals(rootPath) ) return (File)obj;
        }

        //Not in this directory, so go through each child directory looking
        //for it in there
        DirectoryTree tree = getDirectoryTree();
        size = tree.size();
        for (int i=0; i<size; i++)
        {
            Directory dir = (Directory)tree.get(i);
            if ( rootPath.startsWith(dir.getRootPath()) )
            {
                File f = dir.getFile(rootPath);
                if (f != null) return f;
            }
        }
        //Couldn't find it
        return null;
    }

    /**
     * This overrides the parent delete to make sure that we delete all of the contents too.
     *
     * @throws IOException if an error occurs deleting the file.
     */
    public void delete() throws IOException
    {
        //Go through each child and delete them. Reset the directory first to make sure we get everything
        _fileListing = getFileListing();
        int size = _fileListing.size();
        for (int i=0; i<size; i++)
        {
            ( (FileSystemObject)_fileListing.get(i) ).delete();
        }
        super.delete();
    }

    /**
     * This overrides the parent moveTo to make sure that we move all the child files first.
     *
     * @param Directory The directory to move to.
     * @throws IOException if an error occurs moving the directory and contents.
     */
    public void moveTo(Directory to) throws IOException
    {
        //See if we already have a directory by this name here, if so just copy the contents
        String rootPath = to.getRootPath();
        Directory newDir = to.getDirectory( rootPath + (rootPath.length() > 1 ? "/" : "") + getName() );

        if (newDir == null)
        {
            //Create the directory in it's new location
            newDir = to.createDirectory( getName() );
        }

        //Refresh the file listing to make sure that the contents are up to date
        _fileListing = getFileListing();

        //Move each child to the new directory
        int size = _fileListing.size();
        for (int i=0; i<size; i++)
        {
            ( (FileSystemObject)_fileListing.get(i) ).moveTo(newDir);
        }

        //Delete this directory
        super.delete();

        //Create a new internal file object
        setInternalFile( new java.io.File(newDir.getAbsolutePath()) );
    }

    /**
     * This overrides the parent copyTo to make sure that we copy all child files first.
     *
     * @param Directory The directory to move to.
     * @return FileSystemObject
     * @throws IOException if an error occurs moving the directory and contents.
     */
    public FileSystemObject copyTo(Directory to) throws IOException
    {
        //Create the directory in it's new location
        Directory newDir = to.createDirectory( getName() );
        CreationDateFile.setCreationDate(newDir);

        //Refresh the file listing to make sure that the contents are up to date
        _fileListing = getFileListing();

        //Copy each child to the new directory
        int size = _fileListing.size();
        for (int i=0; i<size; i++)
        {
            ( (FileSystemObject)_fileListing.get(i) ).copyTo(newDir);
        }

        return newDir;
    }

    public void setSortAscending()
    {
        if (_fileListing != null) _fileListing.setSortAscending();
    }

    public void setSortDescending()
    {
        if (_fileListing != null) _fileListing.setSortDescending();
    }

    public int getSortDirection()
    {
        if (_fileListing != null) return _fileListing.getSortDirection();
        else return Sortable.ASCENDING;
    }

    public void setSortColumn(SortColumn col)
    {
        if (_fileListing != null) _fileListing.setSortColumn(col);
    }

    public SortColumn getSortColumn()
    {
        if (_fileListing != null) return _fileListing.getSortColumn();
        else return new SortColumn(CompareProperty.NAME);
    }

    public void sort()
    {
        if (_fileListing != null) _fileListing.sort();
    }
}
