package com.zitego.filemanager;

import com.zitego.filemanager.util.FileSizeFormat;
import java.math.BigDecimal;

/**
 * This represents the file size of a file. It stores the number of
 * bytes, kilobytes, and megabytes. kilobytes and megabytes are stored
 * to the 10th place as doubles.
 *
 * @author John Glorioso
 * @version $Id: FileSize.java,v 1.1.1.1 2008/02/20 15:05:39 jglorioso Exp $
 */
public class FileSize implements Comparable
{
    /** The number of bytes. */
    protected long _bytes;

    /**
     * Creates a new FileSize given the number of bytes.
     *
     * @param long The number of bytes.
     */
    public FileSize(long bytes)
    {
        _bytes = bytes;
    }

    /**
     * Returns the number of bytes.
     *
     * @return long
     */
    public long getBytes()
    {
        return _bytes;
    }

    /**
     * Compares this FileSize to another.
     *
     * @param Object The FileSize.
     * @throws IllegalArgumentException if the object is not a FileSize.
     */
    public int compareTo(Object obj) throws IllegalArgumentException
    {
        if ( !(obj instanceof FileSize) )
        {
            throw new IllegalArgumentException( "Object must be of type " + getClass() );
        }

        if (obj == null) throw new IllegalArgumentException("Object cannot be null");

        FileSize size = (FileSize)obj;
        long diff = _bytes - size.getBytes();
        if (diff > 0L) return 1;
        else if (diff < 0L) return -1;
        else return 0;
    }

    public String toString()
    {
        return _bytes + " bytes";
    }
}