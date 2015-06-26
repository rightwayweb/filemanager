package com.zitego.filemanager.search;

import com.zitego.filemanager.*;
import com.zitego.filemanager.util.WildcardFilter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.*;

/**
 * This class represents a file search from a specific directory.
 * Files can be searched on by name (including wildcards) and text contained
 * within (if they are of type text). In addition, search options can be set
 * to search sub directories (default behavior) and whether the search
 * term is case sensitive.
 *
 * @author John Glorioso
 * @version $Id: Search.java,v 1.1.1.1 2008/02/20 15:05:39 jglorioso Exp $
 */
public class Search
{
    /** The file name text pattern. */
    protected String _fileNamePattern = "*";
    /** The matcher object used to search text within files. */
    protected Matcher _containingTextPattern;
    /** The text to search on within files. */
    protected String _containingTextPatternString;
    /** The directory we are starting in. */
    protected Directory _startDirectory;
    /** Whether the search is case sensitive. Default is false. */
    protected boolean _caseSensitive = false;
    /** Whether this is a text file search (forces text pattern searching on all file types). */
    protected boolean _textSearch = false;
    /** Whether to search subdirectories. Default is true. */
    protected boolean _searchSubDirectories = true;

    public static void main(String[] args) throws Exception
    {
        Search s = new Search( (Directory)FileSystemObjectFactory.createObject(args[0], args[0]), args[1] );
        s.setContainingTextPattern(args[2]);
        System.out.println("Search Results:");
        s.setCaseSensitive(true);
        SearchResults results = s.getResults();
        int size = results.size();
        for (int i=0; i<size; i++)
        {
            File f = (File)results.get(i);
            System.out.println( f.getRootPath() );
        }
    }

    /**
     * Creates a new search with the starting Directory.
     *
     * @param Directory The start directory.
     * @throws IllegalArgumentException if the start directory is null.
     */
    public Search(Directory start) throws IllegalArgumentException
    {
        setStartDirectory(start);
    }

    /**
     * Creates a new filename search with the starting Directory and the filename
     * search pattern.
     *
     * @param Directory The start directory.
     * @param String The filename search pattern.
     * @throws IllegalArgumentException if the start directory is null.
     */
    public Search(Directory start, String pattern) throws IllegalArgumentException
    {
        this(start);
        setFileNamePattern(pattern);
    }

    /**
     * Sets the file name search pattern. If the pattern is null then a wildcard will
     * be used.
     *
     * @param String The filename pattern.
     */
    public void setFileNamePattern(String pattern)
    {
        _fileNamePattern = (pattern == null ? "*" : pattern);
    }

    /**
     * Returns the file name search pattern.
     *
     * @return String
     */
    public String getFileNamePattern()
    {
        return _fileNamePattern;
    }

    /**
     * Clears the containing text pattern.
     */
    public void clearContainingTextPattern()
    {
        _containingTextPatternString = null;
        _containingTextPattern = null;
    }

    /**
     * Sets the text to search for within files. If the text is null or the pattern is an empty
     * string or just a wildcard then an IllegalArgumentException will be thrown.
     *
     * @param String The containing text.
     * @throws IllegalArgumentException if the pattern is invalid.
     */
    public void setContainingTextPattern(String text) throws IllegalArgumentException
    {
        if (text == null || text.length() == 0) throw new IllegalArgumentException("Search text cannot be null");
        if ( text.equals("*") || text.equals("?") )
        {
            throw new IllegalArgumentException("Search text cannot contain only a wildcard");
        }
        //First make sure that we are not sticking on extra wildcards at the beginning and end
        //Take out leading or trailing ? wildcards
        _containingTextPatternString = text;
        if (text.charAt(0) == '?') text = text.substring(1);
        if (text.charAt(text.length()-1) == '?') text = text.substring(0, text.length()-1);
        if (text.charAt(0) != '*') text = "*" + text;
        if (text.charAt(text.length()-1) != '*') text += "*";
        _containingTextPattern = WildcardFilter.createMatcher(text, _caseSensitive);
    }

    /**
     * Returns the text to search for within files.
     *
     * @return String
     */
    public String getContainingTextPattern()
    {
        return _containingTextPatternString;
    }

    /**
     * Sets the starting search directory.
     *
     * @throws IllegalArgumentException if the directory is null.
     */
    public void setStartDirectory(Directory dir) throws IllegalArgumentException
    {
        if (dir == null) throw new IllegalArgumentException("Start directory cannot be null");
        _startDirectory = dir;
    }

    /**
     * Returns the start directory.
     *
     * @return Directory
     */
    public Directory getStartDirectory()
    {
        return _startDirectory;
    }

    /**
     * Sets whether or not this is a case sensitive search.
     *
     * @param boolean The case sensitivity flag.
     */
    public void setCaseSensitive(boolean flag)
    {
        _caseSensitive = flag;
        //Gotta reset the text pattern if it is not null
        if (_containingTextPattern != null) setContainingTextPattern(_containingTextPatternString);
    }

    /**
     * Returns whether or not this is a case sensitive search.
     *
     * @return boolean
     */
    public boolean isCaseSensitive()
    {
        return _caseSensitive;
    }

    /**
     * Sets whether or not we should search subdirectories.
     *
     * @param boolean Whether we should search subdirectories.
     */
    public void setSearchSubDirectories(boolean flag)
    {
        _searchSubDirectories = flag;
    }

    /**
     * Returns whether or not we are searching subdirectories.
     *
     * @return boolean
     */
    public boolean getSearchSubDirectories()
    {
        return _searchSubDirectories;
    }

    /**
     * Sets whether or not this is a forced text search.
     *
     * @param boolean The text search flag.
     */
    public void setTextSearch(boolean flag)
    {
        _textSearch = flag;
    }

    /**
     * Returns whether or not this is a forced text search.
     *
     * @return boolean
     */
    public boolean isTextSearch()
    {
        return _textSearch;
    }

    /**
     * Performs the search and returns a set of search results.
     *
     * @return SearchResults
     * @throws IOException if a problem occurs performing the search.
     */
    public SearchResults getResults() throws IOException
    {
        FileListing listing = getResults(_startDirectory);
        SearchResults ret = new SearchResults( listing.size() );
        ret.addAll(listing);
        //Sort it by name
        ret.sort();
        return ret;
    }

    /**
     * Searches the given directory and returns a FileListing containing the results of that
     * search.
     *
     * @param Directory The directory to search in.
     * @return FileListing
     */
    private FileListing getResults(Directory dir) throws IOException
    {
        //Get the search listing from the
        FileListing listing = dir.getFileListing
        (
            new WildcardFilter(_fileNamePattern, _caseSensitive, true, true)
        );
        FileListing ret = new FileListing();
        int size = listing.size();
        for (int i=0; i<size; i++)
        {
            FileSystemObject obj = (FileSystemObject)listing.get(i);
            if (obj instanceof File)
            {
                //See if we are searching the file contents
                if (_containingTextPattern != null)
                {
                    if ( _textSearch || obj.getFileType().isText() )
                    {
                        //Gotta read in the file and search
                        BufferedReader in = new BufferedReader( new FileReader(obj.getAbsolutePath()) );
                        String line = null;
                        while ( (line=in.readLine()) != null )
                        {
                            _containingTextPattern.reset(line);
                            if ( _containingTextPattern.matches() )
                            {
                                ret.add(obj);
                                break;
                            }
                        }
                        in.close();
                    }
                }
                else
                {
                    ret.addObject(obj);
                }
            }
            else
            {
                if (_searchSubDirectories)
                {
                    ret.addAll( getResults((Directory)obj) );
                }
            }
        }
        return ret;
    }
}