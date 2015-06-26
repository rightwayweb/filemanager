package com.zitego.filemanager.util;

import java.io.File;

/**
 * This class defines how to accept only directories from a parent directory.
 *
 * @author John Glorioso
 * @version $Id: DirectoryFilter.java,v 1.1.1.1 2008/02/20 15:05:39 jglorioso Exp $
 */
public class DirectoryFilter extends FileFilter
{
    public static void main(String[] args) throws Exception
    {
        File dir = new File(args[0]);
        String[] files = dir.list( new DirectoryFilter() );
        System.out.println("\nDirectories:");
        for (int i=0; i<files.length; i++)
        {
            System.out.println(files[i]);
        }
    }

    /**
     * Creates a new directory filter.
     */
    public DirectoryFilter()
    {
        super();
    }

    /**
     * Creates a new directory filter and whether or not to include hidden files.
     *
     * @param boolean Whether to include hidden files.
     */
    public DirectoryFilter(boolean includeHidden)
    {
        super(includeHidden);
    }

    public boolean accept(File dir, String filename)
    {
        File f = new File(dir.getPath()+"/"+filename);
        if ( f.isDirectory() && (includeHiddenFiles() || !f.isHidden()) ) return true;
        else return false;
    }
}