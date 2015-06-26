package com.zitego.filemanager.user;

import com.zitego.filemanager.explorer.Explorer;

/**
 * This is an abstract class to handle logging users into the filemanager. The
 * specifics about what is expected for the user_store property and the
 * store_config property in the web.xml file are specifed in extending classes.
 *
 * @author John Glorioso
 * @version $Id: UserStore.java,v 1.1.1.1 2008/02/20 15:05:39 jglorioso Exp $
 */
public abstract class UserStore
{
    /**
     * Creates a UserStore.
     */
    protected UserStore() { }

    /**
     * Configures the user store based on the string passed in.
     *
     * @param config The config string.
     * @throws Exception if an error occurs.
     */
    public abstract void setConfig(String config) throws Exception;

    /**
     * Authenticates the user and returns the Explorer object if the login was successful.
     * If the login fails, null is returned.
     *
     * @param username The username.
     * @param password The password.
     * @throws Exception if an error occurs.
     */
    public abstract Explorer authenticate(String username, String password) throws Exception;
}