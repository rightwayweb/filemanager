package com.zitego.filemanager.explorer;

import com.zitego.util.History;

/**
 * This class simply keeps track of the directories viewed in the explorer.
 *
 * @author John Glorioso
 * @version $Id: ExplorerHistory.java,v 1.1.1.1 2008/02/20 15:05:39 jglorioso Exp $
 */
public class ExplorerHistory extends History
{
    /**
     * Creates a new history object.
     */
    public ExplorerHistory()
    {
        super();
    }

    /**
     * Adds a new explorer entry unless it is already in there.
     *
     * @param String The root path to add.
     */
    public void add(String rootPath)
    {
        if ( !contains(rootPath) ) super.add(rootPath);
    }

    /**
     * Returns the current path.
     *
     * @return String
     */
    public String getCurrentPath()
    {
        return getCurrentEntry();
    }

    /**
     * Returns the last path in history or null if it does not exist.
     *
     * @return String
     */
    public String getLastPath()
    {
        return getLastEntry();
    }

    /**
     * Returns the next path in history or null if it does not exist.
     *
     * @return String
     */
    public String getNextPath()
    {
        return getNextEntry();
    }
}