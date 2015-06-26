package com.zitego.filemanager.explorer;

import java.io.IOException;

/**
 * This is an interface to be implemented by any class that is connected to an explorer
 * object somehow. The implementation is class dependant as far as caching the object
 * or loading it fresh each time. The only required method to implement is getExplorer.
 *
 * @author John Glorioso
 * @version $Id: ExplorerHolder.java,v 1.1.1.1 2008/02/20 15:05:39 jglorioso Exp $
 */
public interface ExplorerHolder
{
    /**
     * Returns an Explorer object.
     *
     * @return Explorer
     */
    public Explorer getExplorer() throws IOException;
}