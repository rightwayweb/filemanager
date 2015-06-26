package com.zitego.filemanager;

/**
 * This is a class used to hold information about various file types including
 * the extension, description, icon name (if this is a known file type), and
 * mime type (if known). The description, icon name, and mime-type are determined
 * based on the information stored in the static FileTypes class. This class
 * is loaded at jvm initialization based on the the values stored in a file that
 * is located by default in the Meta-Inf/filetypes.props file relative to the
 * application path. See FileTypes for more information about changing the location
 * of this file.
 *
 * @author John Glorioso
 * @version $Id: FileType.java,v 1.1.1.1 2008/02/20 15:05:39 jglorioso Exp $
 * @see FileTypes
 */
public class FileType implements Comparable
{
    /** The extension. */
    private String _extension;
    /** The description. */
    private String _description;
    /** The icon name. */
    private String _iconName;
    /** The mimetype. */
    private String _mimeType;
    /** If this is a text file. */
    private boolean _isTextFile = false;
    /** If this is a binary file. */
    private boolean _isBinaryFile = false;

    /**
     * Creates a new FileType with the extension, description, icon name,
     * and mime type. A null extension will be converted to an empty string.
     *
     * @param String The extension.
     * @param String The description.
     * @param String The icon name.
     * @param String The mime type.
     */
    FileType(String ext, String desc, String icon, String mime)
    {
        _extension = (ext == null ? "" : ext);
        _description = ("".equals(desc) ? null : desc);
        _iconName = ("".equals(icon) ? null : icon);;
        _mimeType = ("".equals(mime) ? null : mime);;
        if (_mimeType != null)
        {
            if (_mimeType.indexOf("text") == 0) _isTextFile = true;
            else _isBinaryFile = true;
        }
    }

    /**
     * Returns the extension.
     *
     * @return String
     */
    public String getExtension()
    {
        return _extension;
    }

    /**
     * Returns the description.
     *
     * @return String
     */
    public String getDescription()
    {
        return _description;
    }

    /**
     * Returns the icon name.
     *
     * @return String
     */
    public String getIconName()
    {
        return _iconName;
    }

    /**
     * Returns the mime type.
     *
     * @return String
     */
    public String getMimeType()
    {
        return _mimeType;
    }

    /**
     * Returns if this file is text.
     *
     * @return boolean
     */
    public boolean isText()
    {
        return _isTextFile;
    }

    /**
     * Returns if this is a binary file.
     *
     * @return boolean
     */
    public boolean isBinary()
    {
        return _isBinaryFile;
    }

    /**
     * Compares this FileType to another based on the extension.
     *
     * @param Object The FileType.
     * @throws IllegalArgumentException if the object is not a FileType.
     */
    public int compareTo(Object obj) throws IllegalArgumentException
    {
        if ( !(obj instanceof FileType) )
        {
            throw new IllegalArgumentException( "Object must be of type " + getClass() );
        }

        if (obj == null) throw new IllegalArgumentException("Object cannot be null");

        FileType type = (FileType)obj;
        return _extension.compareTo( type.getExtension() );
    }
}