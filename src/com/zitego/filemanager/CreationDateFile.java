package com.zitego.filemanager;

import java.util.Date;
import java.io.*;
import java.util.Vector;

/**
 * This class handles the file that is used to store creation dates for FileSystemObjects.
 * The file is always located at the user's root directory and is called .creation. If
 * the file does not exist and getCreationDate is called, null is returned. If it does
 * not exist and setCreationDate is called, it is automatically created. The root directory
 * is determined by the difference between the absolute path and the root path of the
 * given object.<br><br>
 *
 * The entry in the file will be in the format of rootPath=unixtime. The unix time is
 * the number of seconds that have passed since 01/01/1970.
 *
 * If the System property use_creation_date is set to the value of "0" then any method
 * called will do nothing.
 *
 * @author John Glorioso
 * @version $Id: CreationDateFile.java,v 1.1.1.1 2008/02/20 15:05:39 jglorioso Exp $
 */
public class CreationDateFile
{
    /** The name of the creation date file. */
    public static final String NAME = ".creation";

    /**
     * Retrieves the creation date of the specified FileSystemObject. If the
     * creation file does not exist or the file is not in it, then null is
     * returned.
     *
     * @param FileSystemObject The object to get the creation date for.
     * @return Date
     * @throws IOException if a problem occurs reading the file.
     */
    public static Date getCreationDate(FileSystemObject obj) throws IOException
    {
        String cdate = System.getProperty("use_creation_date");
        if ( "0".equals(cdate) ) return null;
        String rootPath = obj.getRootPath();
        java.io.File f = getCreationDateFile(obj, false);
        if (f == null) return null;

        BufferedReader in = new BufferedReader( new FileReader(f) );
        String line = null;
        while ( (line=in.readLine()) != null )
        {
            if ( line.substring(0, line.lastIndexOf("=")).equals(rootPath) )
            {
                return new Date( Long.parseLong(line.substring(line.indexOf("=")+1))*1000L );
            }
        }
        in.close();
        return null;
    }

    /**
     * Sets the creation date of the specified FileSystemObject. If the
     * creation file does not exist then it is created. If the object's root
     * path and creation date are already in the file, then it is changed. Otherwise,
     * it is appended to the end of the file. This method synchronizes on the root
     * path so that two methods do not write the current time out at the same time.
     *
     * @param FileSystemObject The object to get the creation date for.
     * @throws IOException if a problem occurs re-writing the file.
     */
    public static void setCreationDate(FileSystemObject obj) throws IOException
    {
        String cdate = System.getProperty("use_creation_date");
        if ( "0".equals(cdate) ) return;
        String absolutePath = obj.getAbsolutePath();
        synchronized (absolutePath)
        {
            java.io.File f = getCreationDateFile(obj, true);
            String rootPath = obj.getRootPath();
            Vector contents = new Vector();
            if ( f.exists() )
            {
                BufferedReader in = new BufferedReader( new FileReader(f) );
                String line = null;
                while ( (line=in.readLine()) != null )
                {
                    if ( !line.substring(0, line.lastIndexOf("=")).equals(rootPath) ) contents.add(line);
                }
                in.close();
            }
            //Make sure the creation date is not after the last modified date
            long seconds = System.currentTimeMillis();
            long modified = obj.getLastModifiedDate().getTime();
            if (seconds > modified) seconds = modified;
            seconds = seconds / 1000;
            contents.add(rootPath + "=" + seconds);

            writeCreationDateFile(f, contents);

            //Set the object's creation date
            obj.setCreationDate(seconds);
        }
    }

    /**
     * Removes the creation date of the specified FileSystemObject. If the
     * creation file does not exist then we do nothing. If the object's root
     * path and creation date are not in the file, then we do nothing. Otherwise,
     * it is removed from the file. This method synchronizes on the root
     * path so that two methods do not write the current time out at the same time.
     *
     * @param FileSystemObject The object to get the creation date for.
     * @throws IOException if a problem occurs re-writing the file.
     */
    public static void removeCreationDate(FileSystemObject obj) throws IOException
    {
        String cdate = System.getProperty("use_creation_date");
        if ( "0".equals(cdate) ) return;
        String absolutePath = obj.getAbsolutePath();
        synchronized (absolutePath)
        {
            java.io.File f = getCreationDateFile(obj, false);
            if (f == null) return;

            String rootPath = obj.getRootPath();
            Vector contents = new Vector();

            BufferedReader in = new BufferedReader( new FileReader(f) );
            String line = null;
            boolean inThere = false;
            while ( (line=in.readLine()) != null )
            {
                //indexOf cause we are removing all subfiles too for delete this is great
                //for rename, the sub file creation dates are lost. no way to know right here
                //TO DO - address this with a way to change the subfolder entries somehow
                if (line.indexOf(rootPath) == 0) inThere = true;
                else contents.add(line);
            }
            in.close();
            if (!inThere) return;

            writeCreationDateFile(f, contents);
        }
    }

    /**
     * Writes out the contents of the creation date file. This method is synchronized on
     * the absolute path of the given file.
     *
     * @param java.io.File The creation date file.
     * @param Vector A vector containing the lines to write out.
     * @throws IOException if an error occurred writing the file.
     */
    public static void writeCreationDateFile(java.io.File f, Vector contents) throws IOException
    {
        String cdate = System.getProperty("use_creation_date");
        if ( "0".equals(cdate) ) return;
        String absolutePath = f.getAbsolutePath();
        synchronized (absolutePath)
        {
            int size = contents.size();
            PrintWriter out = new PrintWriter( new BufferedWriter(new FileWriter(f)) );
            for (int i=0; i<size; i++)
            {
                out.println( (String)contents.get(i) );
            }
            out.flush();
            out.close();
        }
    }

    /**
     * Returns the creation file given the SystemFileObject. If create flag is set to true,
     * then it creates the file if it doesn't exist. If the flag is false then it returns
     * null.
     *
     * @param FileSystemObject The file system object to create it from.
     * @param boolean Whether to create if it doesn't exist.
     * @return java.io.File
     */
    private static java.io.File getCreationDateFile(FileSystemObject obj, boolean create)
    {
        String absolutePath = obj.getAbsolutePath();
        String rootPath = obj.getRootPath();
        String root = absolutePath.substring( 0, absolutePath.indexOf(rootPath)+1 );
        java.io.File f = new java.io.File(root+NAME);
        if (!f.exists() && !create) return null;
        else return f;
    }
}