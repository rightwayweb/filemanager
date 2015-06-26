package com.zitego.filemanager;

import java.util.Vector;
import com.zitego.util.Constant;

/**
 * This constant class defines what properties a file system object can be
 * compared on.
 *
 * @author John Glorioso
 * @version $Id: CompareProperty.java,v 1.1.1.1 2008/02/20 15:05:39 jglorioso Exp $
 */
public final class CompareProperty extends Constant
{
    public static final CompareProperty NAME = new CompareProperty("Name");
    public static final CompareProperty SIZE = new CompareProperty("Size");
    public static final CompareProperty TYPE = new CompareProperty("Type");
    public static final CompareProperty MODIFIED = new CompareProperty("Modified");
    public static final CompareProperty ROOT_PATH = new CompareProperty("Root Path");
    /** Gets incremented as compare properties are initialized. */
    private static int _nextId = 0;
    /** To keep track of each property. */
    private static Vector _properties;

    /**
     * Creates a new CompareProperty given the description.
     *
     * @param String The description.
     */
    private CompareProperty(String desc)
    {
        super(_nextId++, desc);
        if (_properties == null) _properties = new Vector();
        _properties.add(this);
    }

    /**
     * Returns an CompareProperty based on the id passed in. If the id does not match the id of
     * a constant, then we return null. If there are two constants with the same id, then
     * the first one is returned.
     *
     * @param int The constant id.
     * @return CompareProperty
     */
    public static CompareProperty evaluate(int id)
    {
        return (CompareProperty)Constant.evaluate(id, _properties);
    }

    /**
     * Returns an Constant based on the description passed in. If the description does not
     * match the description of a constant, then we return null. If there are two constants
     * with the same description, then the first one is returned.
     *
     * @param String The description.
     * @return CompareProperty
     */
    protected static CompareProperty evaluate(String name)
    {
        return (CompareProperty)Constant.evaluate(name, _properties);
    }

    public Vector getTypes()
    {
        return _properties;
    }
}