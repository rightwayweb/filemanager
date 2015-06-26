package com.zitego.filemanager;

import java.io.IOException;

/**
 * This class handles creating Files or Directories based on what they are
 * in the actual file system.
 *
 * @author John Glorioso
 * @version $Id: FileSystemObjectFactory.java,v 1.1.1.1 2008/02/20 15:05:39 jglorioso Exp $
 */
public class FileSystemObjectFactory
{
    public static void main(String[] args) throws Exception
    {
        FileSystemObject obj = null;
        if (args.length == 2) obj = FileSystemObjectFactory.createObject(args[0], args[1]);
        else if (args.length == 1) obj = FileSystemObjectFactory.createObject(args[0]);
        else
        {
            System.out.println("Usage: java FileSystemObjectFactor <absolute path> [<viewable path>]");
            System.exit(1);
        }
        System.out.println("Name: "+obj.getName());
        System.out.println("Absolute Directory: "+obj.getAbsoluteDirectory());
        System.out.println("Absolute Path: "+obj.getAbsolutePath());
        System.out.println("Root Path Directory: "+obj.getRootPathDirectory());
        System.out.println("Root Path: "+obj.getRootPath());
        System.out.println("Root Directory: "+obj.getRootDirectory());
        FileType type = obj.getFileType();
        System.out.println("Extension: "+type.getExtension());
        System.out.println("Description: "+type.getDescription());
        System.out.println("Icon Name: "+type.getIconName());
        System.out.println("Mime Type: "+type.getMimeType());
        System.out.println("Creation Date: "+obj.getCreationDate());
        System.out.println("Last Modified: "+obj.getLastModifiedDate());
        System.out.println("Bytes: "+obj.getSize().getBytes());
        System.out.println("Kilobytes: "+com.zitego.filemanager.util.FileSizeFormat.getKb(obj.getSize().getBytes()));
        System.out.println("Megabytes: "+com.zitego.filemanager.util.FileSizeFormat.getMb(obj.getSize().getBytes()));
    }

    /**
     * Creates a FileSystemObject given the absolute path of the object. The viewable path is assumed to be the same
     * as the absolute path.
     *
     * @param String The absolute path of the object.
     * @return FileSystemObject
     * @throws IOException if the absolute path is invalid or the object does not exist.
     * @throws IllegalArgumentException if the root directory is invalid or either argument is null.
     */
    public static FileSystemObject createObject(String absolutePath) throws IOException, IllegalArgumentException
    {
        return createObject(absolutePath, "");
    }

    /**
     * Creates a FileSystemObject given the absolute path of the object and the
     * viewable root directory to be used to create a "root path" that masks
     * the full path to the user. The root path must be a prefix of the absolute
     * path. For example, if the absolute path is /home/jglorioso/tmp/somefile.txt
     * then the root could be /home/jglorioso or /home/jglorioso/tmp but could not
     * be tmp. You may specify the trailing slash in the root path (/home/jglorioso/)
     * but it will be ignored.
     *
     * @param String The absolute path of the object.
     * @param String The root directory.
     * @return FileSystemObject
     * @throws IOException if the absolute path is invalid or the object does not exist.
     * @throws IllegalArgumentException if the root directory is invalid or either argument is null.
     */
    public static FileSystemObject createObject(String absolutePath, String rootDir)
    throws IOException, IllegalArgumentException
    {
        checkPaths(absolutePath, rootDir);
        //Trim off the trailing slash if there for both paths. It is a guarantee at this point that the
        //root dir is equal or shorter then the absolute path
        rootDir = cleanTrailingSlash(rootDir);
        absolutePath = cleanTrailingSlash(absolutePath);
        String rootPath = getRootPath(absolutePath, rootDir);

        java.io.File f = new java.io.File(absolutePath);
        if ( !f.exists() ) throw new IOException(absolutePath+" does not exist");

        FileSystemObject ret = null;
        if ( f.isDirectory() ) ret = new Directory(absolutePath, rootPath);
        else ret = new File(absolutePath, rootPath);

        return ret;
    }

    /**
     * Returns a root path suitable for passing into the constructor of a FileSystemObject
     * given an absolute path and the viewable root directory.
     *
     * @param String The absolute path.
     * @param String The root viewable path.
     * @throws IllegalArgumentException if the root directory is invalid or either argument is null.
     */
    public static String getRootPath(String absolutePath, String rootDir)
    {
        checkPaths(absolutePath, rootDir);
        //Trim off the trailing slash if there for both paths. It is a guarantee at this point that the
        //root dir is equal or shorter then the absolute path
        rootDir = cleanTrailingSlash(rootDir);
        absolutePath = cleanTrailingSlash(absolutePath);
        String rootPath = absolutePath.substring( rootDir.length() );
        int len = rootPath.length();
        if (len == 0) rootPath = "/";
        else if (rootPath.charAt(len-1) == '/') rootPath = rootPath.substring(0, len-1);
        return rootPath;
    }

    /**
     * Makes sure that the paths are valid.
     *
     * @param String The absolute path.
     * @param String The root directory.
     * @throws IllegalArgumentException if the root directory is invalid or either argument is null.
     */
    private static void checkPaths(String absolutePath, String rootDir)
    {
        if (absolutePath == null || rootDir == null || absolutePath.indexOf(rootDir) != 0)
        {
            throw new IllegalArgumentException("Invalid Path: absolute="+absolutePath+", root directory="+rootDir);
        }
    }

    /**
     * Trims off the trailing slash if there.
     *
     * @param String The path.
     */
    private static String cleanTrailingSlash(String path)
    {
        int len = path.length();
        if (len > 1 && path.charAt(len-1) == '/') return path.substring(0, len-1);
        else return path;
    }
}