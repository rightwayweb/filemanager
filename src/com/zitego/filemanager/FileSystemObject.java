package com.zitego.filemanager;

import com.zitego.util.Sortable;
import java.io.*;
import java.util.Date;

/**
 * This is an abstract class that represents a FileSystemObject in the
 * zitego file manager. The system object can be a File, a Directory,
 * or some future used object such as a symbolic link possibly. Creation date
 * is not supported by most operating systems therefore is not a method in
 * the java.io.File api. The only way the creation date can be retrieved is
 * if a CreationDateFile exists which includes this object's root path
 * to creation date mapping.<br><br>
 *
 * FileSystemObjects can be compared on various different file system object
 * properties including name, size, file extension, and last modified date.
 * Use setCompare(CompareProperty) to specify what it should be compared by. This
 * is set to name by default.
 *
 * @author John Glorioso
 * @version $Id: FileSystemObject.java,v 1.2 2013/04/04 02:42:03 jglorioso Exp $
 * @see FileType
 * @see FileSize
 * @see CreationDateFile
 */
public abstract class FileSystemObject implements Comparable
{
    /* To keep track of sort direction. */
    private int _sortDirection = Sortable.ASCENDING;
    /* The internal File object that this represents. */
    private java.io.File _file;
    /** The root path of the file system object. */
    private String _rootPath;
    /** The file type. */
    private FileType _fileType;
    /** The file size. */
    private FileSize _size;
    /** The last modified date. */
    private Date _lastModifiedDate;
    /** The creation date. If this cannot be determined, it is the same as last modified. */
    private Date _creationDate;
    /** The property to compare on. */
    private CompareProperty _compareProperty = CompareProperty.NAME;

    /**
     * Creates a new FileSystemObject given the absolute and the
     * viewable root path. The file system object is stored
     * somewhere on the actual server, but the user of the class should
     * view it as relative to their space. For example, if the file
     * resides at /home/jglorioso/temp and the user views the root as
     * /home/jglorioso then the root path would simply be /temp.
     * The creation date is not retrieved until it is asked for since
     * the overhead for the operation can be rather large.
     *
     * @param String The absolute path.
     * @param String The root path.
     */
    protected FileSystemObject(String absolutePath, String rootPath)
    {
        setInternalFile( new java.io.File(absolutePath) );
        _rootPath = rootPath;
    }

    /**
     * Returns the absolute path of the file system object.
     *
     * @return String
     */
    public String getAbsolutePath()
    {
        return _file.getAbsolutePath();
    }

    /**
     * Returns the absolute directory of this file system object. That is the
     * absolute path minus the name.
     *
     * @return String
     */
    public String getAbsoluteDirectory()
    {
        String path = getAbsolutePath();
        int index = path.lastIndexOf("/");
        return ( index == 0 ? "/" : path.substring(0, index) );
    }

    /**
     * Returns the actual absolute directory of the root path for this file system
     * object. That is the absolute path minus the root path.
     *
     * @return String
     */
    public String getRootDirectory()
    {
        String path = getAbsolutePath();
        if ( _rootPath.equals("/") ) return path;
        else return path.substring( 0, path.indexOf(_rootPath) );
    }

    /**
     * Returns the root path of the file system object. This will be prefixed
     * with a forward slash as it will appear to be the root to the user.
     *
     * @return String
     */
    public String getRootPath()
    {
        return _rootPath;
    }

    /**
     * Returns the root directory of the file system object. That is
     * the root path minus the name.
     *
     * @return String
     */
    public String getRootPathDirectory()
    {
        String dir = _rootPath.substring( 0, _rootPath.lastIndexOf("/") );
        if (dir.length() == 0) dir = "/";
        return dir;
    }

    /**
     * Returns the file type.
     *
     * @return FileType
     */
    public FileType getFileType()
    {
        return _fileType;
    }

    /**
     * Returns the location of the file system object. This is the same
     * as getRootPath().
     *
     * @return String
     */
    public String getLocation()
    {
        return getRootPath();
    }

    /**
     * Returns the size of the file system object.
     *
     * @return FileSize
     */
    public FileSize getSize()
    {
        return _size;
    }

    /**
     * Returns the last modified date of the file system object.
     *
     * @return Date
     */
    public Date getLastModifiedDate()
    {
        return _lastModifiedDate;
    }

    /**
     * Returns the creation date of the file system object.  The creation
     * date is retrieved through the CreationDateFile class based on the
     * root path of the file. If the creation date is null then it
     * will be set to the same as the last modified date.
     *
     * @return Date
     */
    public Date getCreationDate()
    {
        if (_creationDate == null)
        {
            Date creationDate = null;
            try
            {
                creationDate = CreationDateFile.getCreationDate(this);
            }
            catch (IOException ignore) { }
            if (creationDate == null) _creationDate = _lastModifiedDate;
            else _creationDate = creationDate;
        }
        return _creationDate;
    }

    /**
     * This should only be set by CreationDateFile that is why the permission for
     * the method is at the package level.
     *
     * @param long The number of seconds old the file is.
     */
    void setCreationDate(long seconds)
    {
        _creationDate = new Date(seconds*1000);
    }

    /**
     * Returns the name of the file system obbject.
     *
     * @return String
     */
    public String getName()
    {
        return _file.getName();
    }

    /**
     * This method renames the file by calling renameTo with a new path.
     *
     * @param String The new name.
     * @throws IOException if the file could not be renamed.
     */
    public void setName(String name) throws IOException
    {
        //Check for null
        if (name == null) throw new IOException("file name cannot be null");

        //No forward slashes
        if (name.indexOf("/") > -1) throw new IOException("file name: "+name+" cannot contain a forward slash");

        renameTo(getAbsoluteDirectory()+"/"+name);
    }

    /**
     * A private method to handle renames, copies, and moves. This creates the new file, resets the
     * creation date, resets the last modified date, changes the root path, and handles changing the
     * file type if the extension changed.
     *
     * @param String The full path of what to rename this to.
     * @throws IOException if an error occurs.
     */
    private void renameTo(String absolutePath) throws IOException
    {
        String name = absolutePath.substring(absolutePath.lastIndexOf("/")+1);
        if ( !_file.renameTo(new java.io.File(absolutePath)) ) throw new IOException("Could not rename file to "+name);

        //Remove the old file's creation date
        CreationDateFile.removeCreationDate(this);

        //Reset the internal file object to the new one
        setInternalFile( new java.io.File(absolutePath) );

        //Set the creation date
        CreationDateFile.setCreationDate(this);
    }

    /**
     * Deletes the file.
     *
     * @throws IOException if an error occurs deleting the file.
     */
    public void delete() throws IOException
    {
        if ( !_file.delete() ) throw new IOException( "Could not delete file: "+getRootPath() );
        //Remove from .creation file
        CreationDateFile.removeCreationDate(this);
    }

    /**
     * This method moves this FileSystemObject to the given directory. All this does is
     * call renameTo with the rootPathDirectory of the given directory with this file's
     * name and removes the file from the file system.
     *
     * @param Directory The directory to move to.
     * @throws IOException if an error occurs moving the file.
     */
    public void moveTo(Directory to) throws IOException
    {
        //See if this already exists. If so, just copy it
        String path = to.getAbsolutePath() + "/" + getName();
        java.io.File toFile = new java.io.File(path);
        if ( toFile.exists() ) copyTo(to);
        else renameTo(path);
    }

    /**
     * This method copies this FileSystemObject to the given directory. This copies the actual
     * file contents to the new directory and sets a creations date.
     *
     * @param Directory The directory to move to.
     * @return FileSystemObject The new copied file.
     * @throws IOException if an error occurs copying the file.
     */
    public FileSystemObject copyTo(Directory to) throws IOException
    {
        writeToOutputStream( this, new FileOutputStream(new java.io.File(to.getAbsolutePath(), getName())) );
        FileSystemObject copiedFile = FileSystemObjectFactory.createObject( to.getAbsolutePath()+"/"+getName(), to.getRootDirectory() );

        //Set the creation date
        CreationDateFile.setCreationDate(copiedFile);
        return copiedFile;
    }

    /**
     * Sets the sort direction. Greater than 0 is ascending, and less than or equal to 0 is descending.
     *
     * @param int
     */
    public void setSortDirection(int dir)
    {
        if (dir > 0) _sortDirection = Sortable.ASCENDING;
        else _sortDirection = Sortable.DESCENDING;
    }

    /**
     * Returns the sort direction.
     *
     * @return int
     */
    public int getSortDirection()
    {
        return _sortDirection;
    }

    /**
     * Sets the property to compare on when CompareTo gets called. If the
     * property is null, then it defaults to name.
     *
     * @param CompareProperty The compare to property.
     */
    public void setCompareProperty(CompareProperty prop)
    {
        if (prop == null) _compareProperty = CompareProperty.NAME;
        else _compareProperty = prop;
    }

    /**
     * Returns the property we are comparing on.
     *
     * @return CompareProperty
     */
    public CompareProperty getCompareProperty()
    {
        return _compareProperty;
    }

    public boolean equals(Object obj)
    {
        if (obj instanceof FileSystemObject) return ( (FileSystemObject)obj ).getRootPath().equals(_rootPath);
        else return false;
    }

    /**
     * Compares this FileSystemObject to another. If this file is a directory,
     * then it will always be returned as greater then or less then depending
     * on the sort direction.
     *
     * @param Object The FileSystemObject.
     * @throws IllegalArgumentException if the object is not a FileSystemObject.
     */
    public int compareTo(Object obj) throws IllegalArgumentException
    {
        if ( !(obj instanceof FileSystemObject) )
        {
            throw new IllegalArgumentException( "Object must be of type " + getClass() );
        }

        if (obj == null) throw new IllegalArgumentException("Object cannot be null");

        FileSystemObject fso = (FileSystemObject)obj;

        CompareProperty compareOn = _compareProperty;
        //directories -vs- non-directories get sorted special
        if (this instanceof Directory)
        {
            if (fso instanceof Directory)
            {
                //If this is a directory and the compare to object is a directory, then we should sort
                //by name always unless the sort column is last modified date.
                if (compareOn != CompareProperty.MODIFIED) compareOn = CompareProperty.NAME;
            }
            else
            {
                //if this is a directory and the compare to object is not a directory then this is always
                //going to be greater or smaller depending on the sort direction. If we are sorting ascending,
                //we want the directories at the beginning, so it is smaller. If we are descending, then it
                //is bigger.
                return _sortDirection*-1;
            }
        }
        else
        {
            if (fso instanceof Directory)
            {
                //if this is not a directory and the compare to object is a directory then the compare to
                //object is always going to be greater or smaller depending on the sort direction. If we
                //are sorting ascending, we want the directories at the beginning, so it is smaller. If we
                //are descending, then it is bigger.
                return _sortDirection;
            }
        }

        //Sort by the specified sort column
        if (compareOn == CompareProperty.SIZE)
        {
            return _sortDirection*_size.compareTo( fso.getSize() );
        }
        else if (compareOn == CompareProperty.TYPE)
        {
            return _sortDirection*_fileType.compareTo( fso.getFileType() );
        }
        else if (compareOn == CompareProperty.MODIFIED)
        {
            return _sortDirection*_lastModifiedDate.compareTo( fso.getLastModifiedDate() );
        }
        else if (compareOn == CompareProperty.ROOT_PATH)
        {
            return _sortDirection*_rootPath.toLowerCase().compareTo( fso.getRootPath().toLowerCase() );
        }
        else
        {
            return _sortDirection*getName().toLowerCase().compareTo( fso.getName().toLowerCase() );
        }
    }

    /**
     * Sets the internal file object.
     *
     * @param java.io.File The new internal file object.
     */
    protected void setInternalFile(java.io.File f)
    {
        String name = f.getName();
        int index = name.lastIndexOf(".");
        String ext = null;
        if (index > -1) ext = name.substring(index+1);

        _file = f;

        //Reset the file type if the extension changed
        if ( _fileType == null || !_fileType.getExtension().equals(ext) )
        {
            _fileType = FileTypes.getFileTypeByName( _file.getName(), (this instanceof Directory) );
        }

        _size = new FileSize( _file.length() );
        _lastModifiedDate = new Date( _file.lastModified() );

        if (_rootPath != null)
        {
            _rootPath = getRootPathDirectory();
            _rootPath = _rootPath + (_rootPath.length() > 1 ? "/" : "") + getName();
        }
    }

    /**
     * Returns the internal file.
     *
     * @return java.io.File
     */
    protected java.io.File getInternalFile()
    {
        return _file;
    }

    /**
     * Writes the given FileSystemObject to the given output stream and returns it.
     *
     * @param FileSystemObject The file to write.
     * @param OutputStream The output stream to write to.
     * @return OutputStream
     * @throws IOException if an error occurs.
     */
    public static OutputStream writeToOutputStream(FileSystemObject obj, OutputStream out) throws IOException
    {
        return writeToOutputStream(new FileInputStream( obj.getAbsolutePath() ), out);
    }

    /**
     * Writes the given viewable file to the given output stream and returns it.
     *
     * @param ViewableFile The file to write.
     * @param OutputStream The output stream to write to.
     * @return OutputStream
     * @throws IOException if an error occurs.
     */
    public static OutputStream writeToOutputStream(ViewableFile f, OutputStream out) throws IOException
    {
        return writeToOutputStream(f.getInputStream(), out);
    }

    private static OutputStream writeToOutputStream(InputStream in, OutputStream out) throws IOException
    {
        try
        {
            byte[] buffer = new byte[4096];
            int bytes_read;
            while ( (bytes_read=in.read(buffer)) != -1 )
            {
                out.write(buffer, 0, bytes_read);
            }
        }
        finally
        {
            in.close();
            out.close();
        }
        return out;
    }

    /**
     * Returns the contents of the file as a string. If this is not a text file, then the
     * content will be encoded as a string.
     *
     * @return String
     * @throws IOException if an error occurs retrieving the file.
     */
    public String getContentAsString() throws IOException
    {
        return writeToOutputStream( this, new ByteArrayOutputStream() ).toString();
    }

    /**
     * Saves the given contents for this file to disk.
     *
     * @param String The contents.
     * @throws IOException if an error occurs writing the file.
     */
    public void writeContents(String contents) throws IOException
    {
        if (contents == null) throw new IllegalArgumentException("contents cannot be null");
        writeToOutputStream( new ByteArrayInputStream(contents.getBytes()), new FileOutputStream(getInternalFile()) );
    }
}
