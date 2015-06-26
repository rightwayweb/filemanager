package com.zitego.filemanager.util;

import java.io.File;
import java.util.regex.*;
import java.util.StringTokenizer;

/**
 * This class defines how to accept file names from a directory given a wildcard pattern.
 * A regular expression is contructed from the pattern passed into the constructor.
 *
 * @author John Glorioso
 * @version $Id: WildcardFilter.java,v 1.1.1.1 2008/02/20 15:05:39 jglorioso Exp $
 */
public class WildcardFilter extends FileFilter
{
    /** Potential pattern characters that need to be escaped in the regular expression. */
    private static final String ESCAPECHARS = "$^*()-+{}[]|\\.!/";
    /** The original pattern. */
    protected String _pattern;
    /** The regular expression that represents the wildcard pattern. */
    protected Matcher _regexp;
    /** Used to determine whether to include directories regardless of if they match the pattern. Default is false. */
    private boolean _includeDirsRegardless = false;
    /** Whether we are case sensitive or not. */
    private boolean _caseSensitive = false;

    public static void main(String[] args) throws Exception
    {
        File dir = new File(args[1]);
        WildcardFilter filter = new WildcardFilter(args[0], true, true, true);
        System.out.println( "\nPattern: " + filter.getPattern() );
        System.out.println( "Regexp: " + filter.getRegexp() );
        String[] files = dir.list(filter);
        System.out.println("\nMatched Files:");
        for (int i=0; i<files.length; i++)
        {
            System.out.println(files[i]);
        }
    }

    /**
     * Creates the filter with a * pattern, case insensitive, defaulting to force
     * directories to match the pattern, and include all hidden files.
     */
    public WildcardFilter()
    {
        this (null, false, false, true);
    }

    /**
     * Creates the filter with the given pattern defaulting to force directories to match
     * the pattern, whether or not this is case sensitive, and to include hidden
     * files. If the pattern is null then * is assumed.
     *
     * @param String The pattern.
     * @param boolean Whether we are case sensitive or not.
     */
    public WildcardFilter(String pattern, boolean caseSensitive)
    {
        this (pattern, caseSensitive, false, true);
    }

    /**
     * Creates a filter with the pattern, whether or not we are case sensitive, whether
     * to include directories regardless of if they match the pattern or not, and whether
     * to include hidden files. If the pattern is null then * is assumed.
     *
     * @param String The pattern.
     * @param boolean Whether we are case sensitive or not.
     * @param boolean Whether to include directories regardless.
     * @param boolean Whether to include hidden files.
     */
    public WildcardFilter(String pattern, boolean caseSensitive, boolean includeDirs, boolean includeHidden)
    {
        super(includeHidden);

        _pattern = pattern;
        if (_pattern == null) _pattern = "*";
        _caseSensitive = caseSensitive;
        _includeDirsRegardless = includeDirs;

        //Create the matcher now with a bogus string. This is hopefully more efficient
        //then creating a new Matcher each time we call accept.
        _regexp = createMatcher(_pattern, _caseSensitive);
    }

    public boolean accept(File dir, String filename)
    {
        File f = new File(dir.getPath()+"/"+filename);
        boolean ret = false;
        //If this is a directory and we are including directories, return true
        if ( _includeDirsRegardless && f.isDirectory() )
        {
            ret = true;
        }
        else
        {
            _regexp.reset(filename);
            ret = _regexp.matches();
            //Also have to make sure it is not the .creation file
            ret = ( ret && !filename.equalsIgnoreCase(".creation") );
        }
        if (ret) ret = ( includeHiddenFiles() || !f.isHidden() );

        return ret;
    }

    /**
     * Escapes any characters that will screw up the regular expression that are
     * specified in the ESCAPECHARS String.
     *
     * @param String The string to escape.
     * @return String The escaped string.
     */
    private static String escape(String in)
    {
        //Replace all invalid chars
        char[] chars = in.toCharArray();
        StringBuffer out = new StringBuffer();
        for (int i=0; i<chars.length; i++)
        {
            if (ESCAPECHARS.indexOf(chars[i]) > -1) out.append("\\");
            out.append(chars[i]);
        }
        return out.toString();
    }

    /**
     * Returns the String representation of the regular expression created.
     *
     * @return String
     */
    public String getRegexp()
    {
        return _regexp.pattern().pattern();
    }

    /**
     * Returns the wildcard pattern.
     *
     * @return String
     */
    public String getPattern()
    {
        return _pattern;
    }

    /**
     * Returns whether we are including subdirectories regardless of whether they match or not.
     *
     * @return boolean
     */
    public boolean includeDirsRegardless()
    {
        return _includeDirsRegardless;
    }

    /**
     * Returns whether this pattern is case sensitive or not.
     *
     * @return boolean
     */
    public boolean isCaseSensitive()
    {
        return _caseSensitive;
    }

    /**
     * Creates a matcher out of the given pattern and whether the pattern is case sensitive.
     *
     * @param String The pattern.
     * @param boolean Whether the pattern is case sensitive.
     */
    public static Matcher createMatcher(String pattern, boolean caseSensitive)
    {
        //Create a regular expression out of the pattern.
        StringBuffer regexp = new StringBuffer("^");
        //See if it starts with a wild card or not
        if (pattern.charAt(0) == '*') regexp.append(".*");
        StringTokenizer st = new StringTokenizer(pattern, "*");
        while ( st.hasMoreTokens() )
        {
            String token = st.nextToken();
            //Escape special characters
            regexp.append( escape(token) ).append( (st.hasMoreTokens() ? ".*" : "") );
        }
        //See if it ends with a wild card or not and the pattern was not just a single *
        int index = pattern.length()-1;
        if (index > 0 && pattern.charAt(index) == '*') regexp.append(".*");
        regexp.append("$");

        if (caseSensitive) return Pattern.compile( regexp.toString() ).matcher("");
        else return Pattern.compile(regexp.toString(), Pattern.CASE_INSENSITIVE).matcher("");
    }
}