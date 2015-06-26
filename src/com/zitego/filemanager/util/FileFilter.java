package com.zitego.filemanager.util;

import java.io.File;

/**
 * This class is a base for all other file filters.
 *
 * @author John Glorioso
 * @version $Id: FileFilter.java,v 1.1.1.1 2008/02/20 15:05:39 jglorioso Exp $
 */
public abstract class FileFilter implements java.io.FilenameFilter
{
    /** Whether to include hidden files. Default is false. */
    protected boolean _includeHidden = true;

    /**
     * Creates a new directory filter.
     */
    public FileFilter() { }

    /**
     * Creates a new directory filter and whether or not to include hidden files.
     *
     * @param boolean Whether to include hidden files.
     */
    public FileFilter(boolean includeHidden)
    {
        _includeHidden = includeHidden;
    }

    /**
     * Sets whether or not to include hidden files.
     *
     * @param boolean The include flag.
     */
    public void setIncludeHiddenFiles(boolean include)
    {
        _includeHidden = include;
    }

    /**
     * Returns whether or not to include the hidden files.
     *
     * @return boolean
     */
    public boolean includeHiddenFiles()
    {
        return _includeHidden;
    }
}