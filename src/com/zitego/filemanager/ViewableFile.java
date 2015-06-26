package com.zitego.filemanager;

import java.io.InputStream;
import java.io.IOException;

/**
 * This interface is to be implemented to define the file name.
 *
 * @author John Glorioso
 * @version $Id: ViewableFile.java,v 1.1.1.1 2008/02/20 15:05:39 jglorioso Exp $
 */
public interface ViewableFile
{
    /**
     * Returns the name of the file.
     *
     * @return String
     */
    public String getFileName();

    /**
     * Returns the file type.
     *
     * @return FileType
     */
    public FileType getFileType();

    /**
     * Returns the input stream of the ViewableFile.
     *
     * @return InputStream
     * @throws IOException if an erroc occurs getting the stream.
     */
    public InputStream getInputStream() throws IOException;
}