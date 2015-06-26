package com.zitego.filemanager.util;

import com.zitego.filemanager.FileSize;
import com.zitego.markup.xml.XmlTag;
import com.zitego.format.FormatType;
import com.zitego.format.UnsupportedFormatException;
import com.zitego.util.FileUtils;
import java.io.IOException;
import java.util.Vector;

/**
 * This class is basically an xml document that specifies users that are
 * allowed to login to the file manager application and what their base
 * path is. The xml file is passed in the constructor and it is immediately
 * parsed. The file should look as follows:<br>
 * <xmp>
 * <?xml version="1.0" ?>
 * <users>
 *  <!-- user1 has a 100mb space limit -->
 *  <user>
 *   <username>user1</username>
 *   <password>pass1</password>
 *   <basepath>/home/path1</basepath>
 *   <disk-space>100mb</disk-space>
 *  </user>
 *  <!-- user2 has no disk space limit -->
 *  <user>
 *   <username>user2</username>
 *   <password>pass2</password>
 *   <basepath>/home/path2</basepath>
 *  </user>
 *  <!-- list of files and/or directories to hide (optional) -->
 *  <hidden-objects>
 *   <object>/example.txt</object>
 *   <object>/example-dir</object>
 *   ...
 *  </hidden-objects>
 *  ...
 * </users>
 * </xmp>
 *
 * @author John Glorioso
 * @version $Id: XmlUsers.java,v 1.1.1.1 2008/02/20 15:05:39 jglorioso Exp $
 */
public class XmlUsers extends XmlTag
{
    private String _path;

    /**
     * Creates a new XmlUsers file with a path to the file.
     *
     * @param String The file path.
     * @throws IllegalArgumentException if the path is null.
     * @throws IOException if an error occurs loading the file.
     */
    public XmlUsers(String path) throws IllegalArgumentException, IOException
    {
        super("users");
        if (path == null) throw new IllegalArgumentException("path cannot be null");
        _path = path;
        loadFile();
    }

    /**
     * Loads the file.
     *
     * @throws IOException
     */
    public void loadFile() throws IOException
    {
        String txt = FileUtils.getFileContents(_path);
        if (txt == null) throw new IOException("File contents are null");
        try
        {
            parse(txt, FormatType.XML);
        }
        catch (UnsupportedFormatException ufe) { }
    }

    /**
     * Returns whether the given username/password is authenticated by returning
     * an ExplorerConfig with the base path and disk space (if any set). If the
     * authentication fails, then null is returned.
     *
     * @param String The username.
     * @param String The password.
     * @return ExplorerConfig
     */
    public ExplorerConfig authenticate(String username, String password)
    {
        if (username == null || password == null) return null;

        Vector children = getChildrenWithName("user");
        int size = children.size();
        ExplorerConfig ret = null;
        for (int i=0; i<size; i++)
        {
            XmlTag child = (XmlTag)children.get(i);
            if ( username.equalsIgnoreCase(child.getChildValue("username")) )
            {
                if ( password.equals(child.getChildValue("password")) )
                {
                    ret = new ExplorerConfig();
                    ret.basePath = child.getChildValue("basepath");
                    String space = child.getChildValue("disk-space");
                    if (space != null) ret.diskSpace = (FileSize)FileSizeFormat.FORMATTER.parseObject(space);
                    XmlTag hidden = child.getFirstOccurrenceOf("hidden-objects");
                    if (hidden != null)
                    {
                        Vector children2 = hidden.getChildrenWithName("object");
                        int size2 = children2.size();
                        for (int j=0; j<size2; j++)
                        {
                            XmlTag child2 = (XmlTag)children2.get(j);
                            ret.hiddenObjects.add( child2.getValue() );
                        }
                    }
                }
                break;
            }
        }
        return ret;
    }

    public class ExplorerConfig
    {
        public String basePath;
        public FileSize diskSpace;
        public Vector hiddenObjects = new Vector();
    }
}