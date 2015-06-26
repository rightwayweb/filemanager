package com.zitego.filemanager;

import org.apache.commons.fileupload.ProgressListener;

/**
 * Reads in the number of bytes that have been transferred during an upload process.
 *
 * @author John Glorioso
 * @version $Id: FileUploadListener.java,v 1.1.1.1 2008/02/20 15:05:39 jglorioso Exp $
 */
public class FileUploadListener implements ProgressListener
{
    private volatile long bytesRead = 0;
    private volatile long contentLength = 0;
    private volatile long item = 0;

    public FileUploadListener() { }

    public void update(long aBytesRead, long aContentLength, int anItem)
    {
        bytesRead = aBytesRead;
        contentLength = aContentLength;
        item = anItem;
    }

    /**
     * Returns the bytes read.
     *
     * @return long
     */
    public long getBytesRead()
    {
        return bytesRead;
    }

    /**
     * Returns the content length.
     *
     * @return long
     */
    public long getContentLength()
    {
        return contentLength;
    }

    /**
     * Returns the item.
     *
     * @return long
     */
    public long getItem()
    {
        return item;
    }
}
