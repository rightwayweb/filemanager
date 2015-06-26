package com.zitego.filemanager.user;

import com.zitego.filemanager.explorer.Explorer;
import com.zitego.sql.DBHandle;
import com.zitego.sql.DBConfig;
import com.zitego.sql.DBHandleFactory;
import com.zitego.logging.Logger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Driver;
import java.util.StringTokenizer;

/**
 * <p>
 * This class handles logging the user in the database. A query is performed based on
 * the username and password passed in.
 * </p>
 * <p>
 * The init-param user_store must be specified in the web.xml file and should be the classpath
 * of this class. Additionally, the init-param store_config must be specified and it should
 * appear as follows:
 * </p>
 * <pre><code>
 * url=jdbc:mysql://<db_url>:3306/<db_name>,
 * driver=com.mysql.jdbc.Driver,
 * username=<username>,
 * password=<password>,
 * debug=<1 or 0>
 * </code></pre>
 * <p>
 * A table named filemanager_user is expected to exist with the following column specifications:<br><br>
 * username VARCHAR NOT NULL,<br>
 * password VARCHAR NOT NULL,<br>
 * base_path VARCHAR NOT NULL,<br>
 * disk_space INT,<br>
 * hidden_objects TEXT
 * </p>
 * <p>
 * Other columns may exist, however they will be ignored. A null disk_space parameter is assumed
 * mean unlimited. Otherwise, the disk_space is the number of total bytes allowed.
 * </p>
 * <p>
 * Hidden objects are a | delimited string of paths that may exist in the directory structure, but are
 * to be hidden in the filemanager.
 * </p>
 *
 * @author John Glorioso
 * @version $Id: DatabaseStore.java,v 1.1.1.1 2008/02/20 15:05:39 jglorioso Exp $
 */
public class DatabaseStore extends UserStore
{
    private DBHandle _db;

    /**
     * Creates an DatabaseStore.
     */
    public DatabaseStore()
    {
        super();
    }

    /**
     * Loads the configuration. The configuration is expected to have the url, driver, username, and password
     * at the very least stored in a comma delimited name=value string. See class notes above.
     *
     * @param config The config string.
     * @throws Exception if an error occurs creating the database handle.
     */
    public void setConfig(String config) throws Exception
    {
        StringTokenizer st = new StringTokenizer(config, ",");
        String url = null;
        String driver = null;
        String username = null;
        String password = null;
        String debug = null;
        while ( st.hasMoreTokens() )
        {
            String token = st.nextToken().trim();
            StringTokenizer nv = new StringTokenizer(token, "=");
            String name = nv.nextToken().trim();
            if ( "url".equals(name) ) url = nv.nextToken().trim();
            else if ( "driver".equals(name) ) driver = nv.nextToken().trim();
            else if ( "username".equals(name) ) username = nv.nextToken().trim();
            else if ( "password".equals(name) ) password = nv.nextToken().trim();
            else if ( "debug".equals(name) ) debug = nv.nextToken().trim();
        }
        if (url == null || driver == null || username == null || password == null)
        {
            throw new IllegalArgumentException("url, driver, username, and password must be specified in config string");
        }
        DBConfig cfg = new DBConfig
        (
            url,
            (Driver)Class.forName(driver).newInstance(),
            username, password, DBConfig.MYSQL,
            ("1".equals(debug) ? Logger.getInstance("com.zitego.filemanager") : null)
        );
        cfg.setLogSql( "1".equals(debug) );
        _db = DBHandleFactory.getDBHandle(cfg);
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
        Explorer ret = null;
        _db.connect();
        try
        {
            PreparedStatement pst = _db.prepareStatement("SELECT password, base_path, disk_space, hidden_objects FROM filemanager_user WHERE username = ?");
            pst.setString(1, username);
            ResultSet rs = pst.executeQuery();
            if ( rs.next() )
            {
                if ( rs.getString("password").equals(password) )
                {
                    long diskSpace = rs.getLong("disk_space");
                    if ( !rs.wasNull() ) ret = new Explorer( rs.getString("base_path"), diskSpace );
                    else ret = new Explorer( rs.getString("base_path") );
                    String hidden = rs.getString("hidden_objects");
                    if (hidden != null)
                    {
                        StringTokenizer st = new StringTokenizer(hidden, "|");
                        while ( st.hasMoreTokens() )
                        {
                            ret.addHiddenObject( st.nextToken() );
                        }
                    }
                }
            }
        }
        finally
        {
            _db.disconnect();
        }
        return ret;
    }
}