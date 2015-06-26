package com.zitego.filemanager.explorer;

import com.zitego.filemanager.util.FileSizeFormat;
import com.zitego.filemanager.Directory;
import com.zitego.filemanager.FileListing;
import com.zitego.filemanager.FileSize;
import com.zitego.filemanager.FileSystemObject;
import com.zitego.filemanager.search.Search;
import com.zitego.filemanager.search.SearchResults;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

/**
 * This class represents an abstract "explorer" type object as used in most graphical
 * operating systems. They typically consist of an optional directory tree pane on the left,
 * a file listing pane on the right, a navigational/view control toolbar, and a location
 * bar to directly access specific directories.<br><br>
 *
 * The FileListing is kept separate from the DirectoryTree so that it is cached. Calls to
 * Directory.getFileListing will not need to be made over and over. If a file is added to
 * the displayed directory, refresh() will need to be called to rebuild it.
 *
 * @author John Glorioso
 * @version $Id: Explorer.java,v 1.1.1.1 2008/02/20 15:05:39 jglorioso Exp $
 */
public class Explorer extends Directory implements ExplorerHolder
{
    /** The default explorer holder session attribute name. */
    public static final String SESSION_NAME = "explorer_holder";
    /** To keep track of the directories that are expanded. */
    protected Hashtable _expandedDirectories = new Hashtable();
    /** Whether we are showing the directory tree or not. Default is true. */
    protected boolean _showDirectoryTree = true;
    /** To cache the FileListing to return. */
    protected FileListing _fileListing;
    /** The search object. */
    protected Search _search;
    /** The search results. */
    protected SearchResults _searchResults;
    /** Holds the explorer directory history. */
    protected ExplorerHistory _history = new ExplorerHistory();
    /** The amount of total disk space. */
    protected FileSize _totalDiskSpace;
    /** The amount of free disk space. */
    protected FileSize _freeDiskSpace;
    /** The view type. Default is ViewType.LIST. */
    protected ViewType _viewType = ViewType.LIST;
    /** The frame target. _top is default. */
    protected String _frameTarget = "_top";
    /** The file system objects that are protected. These are String paths. */
    protected Vector _hiddenObjects = new Vector();

    public static void main(String[] args) throws Exception
    {
        Explorer e = new Explorer(args[0], Long.parseLong(args[1]));
        System.out.println("Name: "+e.getName());
        System.out.println("Absolute Directory: "+e.getAbsoluteDirectory());
        System.out.println("Absolute Path: "+e.getAbsolutePath());
        System.out.println("Root Path Directory: "+e.getRootPathDirectory());
        System.out.println("Root Path: "+e.getRootPath());
        System.out.println("Root Directory: "+e.getRootDirectory());
        FileListing listing = e.getFileListing();
        int size = listing.size();
        for (int i=0; i<size; i++)
        {
            FileSystemObject obj = (FileSystemObject)listing.get(i);
            System.out.println(obj.getName()+(obj instanceof Directory ?" (directory)":"")+", "+FileSizeFormat.getKb(obj.getSize().getBytes())+"kb, "+obj.getLastModifiedDate());
        }
    }

    /**
     * Creates a new explorer object with a user's home directory. This is created with show
     * directory tree to true and as such, will automatically create one upon construction.
     *
     * @param homeDir The absolute path of the home directory.
     * @param totalSpace The total disk space allowed in bytes.
     * @throws IOException if an error occurs creating the explorer.
     */
    public Explorer(String homeDir, long totalSpace) throws IOException
    {
        super(homeDir);
        selectDirectory("/", true);
        _totalDiskSpace = new FileSize(totalSpace);
        getFreeDiskSpace();
    }

    /**
     * Creates a new explorer object with a user's home directory. There will
     * be no total disk space set.
     *
     * @param homeDir The absolute path of the home directory.
     * @throws IOException if an error occurs creating the explorer.
     */
    public Explorer(String homeDir) throws IOException
    {
        super(homeDir);
        selectDirectory("/", false);
    }

    /**
     * Returns the total amount of disk space.
     *
     * @return FileSize
     */
    public FileSize getTotalDiskSpace()
    {
        return _totalDiskSpace;
    }

    /**
     * Returns the free disk space.
     *
     * @return FileSize
     */
    public FileSize getFreeDiskSpace()
    {
        if (_freeDiskSpace == null && _totalDiskSpace != null)
        {
            long spaceUsed = getSpaceUsed( getInternalFile() );
            //Have to loop through all files and get the total space

            _freeDiskSpace = new FileSize(_totalDiskSpace.getBytes()-spaceUsed);
        }
        return _freeDiskSpace;
    }

    /**
     * Recalculates the total disk space.
     */
    public void recalculateFreeDiskSpace()
    {
        _freeDiskSpace = null;
    }

    /**
     * Returns the total disk space (in bytes) used in the given java.io.File.
     * If the file is null, it returns 0. If the file is a directory,
     * it recurses through it till it reaches an end.
     *
     * @param file The file to get used space.
     * @return long
     */
    private long getSpaceUsed(java.io.File file)
    {
        if (file == null)
        {
            return 0L;
        }
        long ret = file.length();
        if ( file.isDirectory() )
        {

            java.io.File[] files = file.listFiles();
            if (files == null) files = new java.io.File[0];
            for (int i=0; i<files.length; i++)
            {
                ret += getSpaceUsed(files[i]);
            }
        }
        return ret;
    }

    /**
     * Sets the view type for the file listing pane. If the type is
     * null, then is it forced to ViewType.LIST;
     *
     * @param type The view type.
     */
    public void setViewType(ViewType type)
    {
        if (type == null) type = ViewType.LIST;
        _viewType = type;
    }

    /**
     * Returns the view type of the file listing pane.
     *
     * @return ViewType
     */
    public ViewType getViewType()
    {
        return _viewType;
    }

    /**
     * Sets the frame target. Null will make it _top.
     *
     * @param target The frame target name.
     */
    public void setFrameTarget(String target)
    {
        if (target == null) target = "_top";
        _frameTarget = target;
    }

    /**
     * Returns the frame target name.
     *
     * @return String
     */
    public String getFrameTarget()
    {
        return _frameTarget;
    }

    /**
     * Sets whether or not we are showing the directory tree.
     *
     * @param flag The flag.
     */
    public void setShowDirectoryTree(boolean flag)
    {
        _showDirectoryTree = flag;
    }

    /**
     * Returns whether or not we are showing the directory tree.
     *
     * @return boolean
     */
    public boolean showDirectoryTree()
    {
        return _showDirectoryTree;
    }

    /**
     * Returns the file current listing. Overrides the parent to return the selected
     * directory's listing.
     *
     * @return FileListing
     */
    public FileListing getFileListing()
    {
        return _fileListing;
    }

    /**
     * Selects a directory given the root path and creates a file
     * listing for it.
     *
     * @param rootPath The root path.
     * @param store Whether to store the page in history.
     * @throws IOException if an error occurs searching for the directory.
     */
    public void selectDirectory(String rootPath, boolean store) throws IOException
    {
        Directory dir = getDirectory(rootPath);
        if ( dir != null && !isHidden(rootPath) )
        {
            //Gotta check to see if we are the directory so that we can call the super class
            //to generate the file listing. Otherwise, it will never get initialized.
            _fileListing = ( dir == this ? super.getFileListing() : dir.getFileListing() );
            //Go through and expand all directories in this one
            expandDirectory(rootPath);
            int index = 0;
            while ( (index=rootPath.lastIndexOf("/")) > 0 )
            {
                rootPath = rootPath.substring(0, index);
                expandDirectory(rootPath);
            }
            if (store) _history.add( dir.getRootPath() );
        }
    }

    /**
     * Expands a directory given the root path.
     *
     * @param rootPath The root path.
     */
    public void expandDirectory(String rootPath) throws IOException
    {
        _expandedDirectories.put(rootPath, "1");
    }

    /**
     * Collapses a directory given the root path.
     *
     * @param rootPath The root path.
     */
    public void collapseDirectory(String rootPath) throws IOException
    {
        _expandedDirectories.remove(rootPath);
    }

    /**
     * Returns whether the current directory is expanded. If the index is not
     * on a valid directory, then false is returned.
     *
     * @param rootPath The root path of the directory to check.
     * @return boolean
     */
    public boolean isDirectoryExpanded(String rootPath)
    {
        return (_expandedDirectories.get(rootPath) != null);
    }

    /**
     * Returns the explorer history.
     *
     * @return ExplorerHistory
     */
    public ExplorerHistory getHistory()
    {
        return _history;
    }

    /**
     * Creates a new search object using the current file listing directory as the starting
     * directory.
     */
    public void createNewSearch()
    {
        _search = new Search( _fileListing.getParentDirectory() );
        _searchResults = null;
    }

    /**
     * Clears the search object (sets to null).
     */
    public void clearSearch()
    {
        _search = null;
        _searchResults = null;
    }

    /**
     * Returns the search object.
     *
     * @return Search
     */
    public Search getSearch()
    {
        return _search;
    }

    /**
     * Sets the search results. This is stored separately from the Search object because
     * the search object does not store its results.
     *
     * @param results The results
     */
    public void setSearchResults(SearchResults results)
    {
        _searchResults = results;
    }

    /**
     * Returns the search results.
     *
     * @return SearchResults
     */
    public SearchResults getSearchResults()
    {
        return _searchResults;
    }

    /**
     * Returns whether we are searching or not. This is just a call to getSearch() to see
     * if the Search object is null.
     *
     * @return boolean
     */
    public boolean isSearchExpanded()
    {
        return (_search != null);
    }

    /**
     * Refreshes the given directory. If the directory does not exist, then nothing happens
     *
     * @param rootPath The root path of the directory to refresh.
     * @throws IOException if an error occurs retrieving the directory.
     */
    public void refresh(String rootPath) throws IOException
    {
        Directory dir = getDirectory(rootPath);
        if (dir != null)
        {
            dir.refresh();
            selectDirectory(dir.getRootPath(), false);
        }
    }

    public Explorer getExplorer() throws IOException
    {
        return this;
    }

    /**
     * Adds the path of an object to hide (not be visible in the explorer gui).
     * The path should be an absolute path that is relative to the user's base path.
     *
     * @param path The path of the object to hide.
     * @throws IllegalArgumentException if the path does not appear absolute.
     */
    public void addHiddenObject(String path) throws IllegalArgumentException
    {
        if (path == null || path.length() == 0) return;
        if (path.indexOf("/") != 0) throw new IllegalArgumentException("hidden object path must begin with a /");
        _hiddenObjects.add(path);
    }

    /**
     * Removes the given path from the hidden objects.
     *
     * @param path The path of the object to hide.
     * @throws IllegalArgumentException if the path does not appear absolute.
     */
    public void removeHiddenObject(String path) throws IllegalArgumentException
    {
        if (path == null) return;
        _hiddenObjects.remove(path);
    }

    /**
     * Returns the hidden objects.
     *
     * @return Vector
     */
    public Vector getHiddenObjects()
    {
        return _hiddenObjects;
    }

    /**
     * Returns whether or not the given path is hidden.
     *
     * @param path The path to check.
     * @return boolean
     */
    public boolean isHidden(String path)
    {
        return ( path != null && _hiddenObjects.contains(path) );
    }
}