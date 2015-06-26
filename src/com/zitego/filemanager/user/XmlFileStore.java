package com.zitego.filemanager.user;

import com.zitego.filemanager.explorer.Explorer;
import com.zitego.filemanager.util.XmlUsers;

/**
 * This class handles logging the user in via an xml file. The xml file should be in the
 * following format:<br>
 * <code><pre>
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
 *   <!-- list of files and/or directories to hide (optional) -->
 *   <hidden-objects>
 *    <object>/example.txt</object>
 *    <object>/example-dir</object>
 *    ...
 *   </hidden-objects>
 *  </user>
 *  ...
 * </users>
 * </pre></code>
 * <p>
 * The init-param user_store must be specified in the web.xml file and should be the classpath
 * of this class. Additionally, the init-param store_config must be specified and it should be
 * the path to the user xml file.
 * </p>
 *
 * @author John Glorioso
 * @version $Id: XmlFileStore.java,v 1.1.1.1 2008/02/20 15:05:39 jglorioso Exp $
 */
public class XmlFileStore extends UserStore
{
    private XmlUsers _file;

    /**
     * Creates an XmlFileStore.
     */
    public XmlFileStore()
    {
        super();
    }

    /**
     * Loads the xml user file.
     *
     * @param path The path to the xml user file.
     * @throws Exception if an error occurs reading the file.
     */
    public void setConfig(String config) throws Exception
    {
        _file = new XmlUsers(config);
    }

    /**
     * Authenticates the user and returns the Explorer object if the login was successful.
     * If the login fails, null is returned.
     *
     * @param username The username.
     * @param password The password.
     * @throws Exception if an error occurs.
     */
    public Explorer authenticate(String username, String password) throws Exception
    {
        XmlUsers.ExplorerConfig cfg = _file.authenticate(username, password);
        Explorer ret = null;
        if (cfg.diskSpace != null) ret = new Explorer( cfg.basePath, cfg.diskSpace.getBytes() );
        else ret = new Explorer(cfg.basePath);
        int size = cfg.hiddenObjects.size();
        for (int i=0; i<size; i++)
        {
            ret.addHiddenObject( (String)cfg.hiddenObjects.get(i) );
        }
        return ret;
    }
}