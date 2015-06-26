package com.zitego.filemanager;

import java.io.*;

/**
 * This class represents a directory in the filemanager system.
 *
 * @author John Glorioso
 * @version $Id: File.java,v 1.1.1.1 2008/02/20 15:05:39 jglorioso Exp $
 * @see FileSystemObjectFactory
 */
public class File extends FileSystemObject implements ViewableFile
{
    /**
     * Creates a new File given the absolute and the viewable root path.
     *
     * @param String The absolute path.
     * @param String The root path.
     */
    File(String absolutePath, String rootPath)
    {
        super(absolutePath, rootPath);
    }

    public String getFileName()
    {
        return getName();
    }

    public InputStream getInputStream() throws IOException
    {
        try
        {
            return new FileInputStream( getAbsolutePath() );
        }
        catch (FileNotFoundException fnfe)
        {
            throw new IOException( fnfe.toString() );
        }
    }
}