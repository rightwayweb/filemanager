package com.zitego.filemanager;

import com.zitego.util.TextUtils;
import java.text.ParseException;
import java.io.IOException;
import java.io.FileInputStream;
import java.util.*;

/**
 * This class holds information about file types by extension. Each file
 * extension has an associated description, icon name, and mime-type. This
 * class is similar to StaticProperties in that it stores the FileType
 * object keyed by the lowercase extension in the form of ".ext".<br><br>
 *
 * It retrieves the information through it's load method by parsing a
 * properties file in the format of:<br>
 * <code>extension=description,icon name,mime type</code><br>
 * If the description has a comma in it, then surround it by quotes.
 * This file, by default, is located at Meta-Inf/filetypes.props. This
 * file path can be changed by using the setPropertiesFilePath(String)
 * method.<br><br>
 *
 * The file types are not loaded until the load() method is called manually
 * or until the getFileType() method is called and this has not yet been
 * intitialized.
 *
 * TO DO - figure out a way to make this cross context in tomcat
 *
 * @author John Glorioso
 * @version $Id: FileTypes.java,v 1.1.1.1 2008/02/20 15:05:39 jglorioso Exp $
 */
public final class FileTypes
{
    /** To store the extension to FileType association. */
    private static HashMap _types;
    /** A file type for directories. */
    public static final FileType DIR = new FileType(null, "File Folder", "folder.gif", null);

    public static void main(String[] args) throws Exception
    {
        FileType type = getFileTypeByName(args[0]);
        System.out.println("ext="+type.getExtension());
        System.out.println("desc="+type.getDescription());
        System.out.println("icon="+type.getIconName());
        System.out.println("mime="+type.getMimeType());
    }

     /**
     * Returns a FileType based on the file name passed in. This automatically
     * determines if the file is a file or a directory.
     *
     * @param String The file name.
     * @return FileType
     * @throws IllegalArgumentException if the path is null.
     */
    public static FileType getFileTypeByName(String name) throws IllegalArgumentException
    {
        if (name == null) throw new IllegalArgumentException("file name cannot be null");
        return getFileTypeByName( name, new java.io.File(name).isDirectory() );
    }

    /**
     * Returns a FileType based on the file name passed in and whether
     * it is a directory or not. We pass in the directory so that we do
     * not need to produce the overhead of creating a File object to
     * determine that. There is a convenience method, however, that will do
     * exactly that if you do not wish to provide the information.
     *
     * @param String The file name.
     * @param boolean Whether or not this is a directory.
     * @return FileType
     * @throws IllegalArgumentException if the name is null.
     */
    public static FileType getFileTypeByName(String name, boolean dir) throws IllegalArgumentException
    {
        if (name == null) throw new IllegalArgumentException("file name cannot be null");

        //See if this is a directory
        if (dir) return DIR;

        FileType type = null;
        int index = name.lastIndexOf(".");
        return getFileType( (index > -1 ? name.substring(index+1) : "") );
    }

    /**
     * Returns the FileType based on the extension passed in. If the extension
     * does not result in a known file type then an "Unknown File Type" will be
     * returned.
     *
     * @param String The extension.
     * @return FileType
     * @throws IllegalArgumentException if the extension is null.
     */
    public static FileType getFileType(String ext) throws IllegalArgumentException
    {
        if (ext == null) throw new IllegalArgumentException("file extension cannot be null");

        //Make sure we are loaded
        if (_types == null)
            throw new RuntimeException("File types not yet loaded");

        ext = ext.toLowerCase();
        FileType type = (FileType)_types.get(ext);
        if (type == null) type = new FileType(ext, "Unknown Type", "unknown.gif", null);
        return type;
    }

    /**
     * Loads the file types properties based on the properties file path.
     *
     * @throws IOException if a problem occurs reading the file.
     * @throws ParseException if a problem occurs parsing the file.
     */
    public synchronized static void load(String path) throws IOException, ParseException
    {
        _types = new HashMap();
        Properties props = new Properties();
        props.load( FileTypes.class.getResourceAsStream(path) );

        for (Enumeration e=props.propertyNames(); e.hasMoreElements();)
        {
            String ext = (String)e.nextElement();
            String line = props.getProperty(ext);
            ext = ext.toLowerCase();
            String[] tokens = TextUtils.split(line, ',', '"');
            if (tokens.length != 3) throw new ParseException("Invalid format: "+ext+"="+line, 0);
            _types.put( ext, new FileType(ext, tokens[0], tokens[1], tokens[2]) );
        }
    }
}