package com.zitego.filemanager.explorer;

import java.util.Vector;
import com.zitego.util.Constant;

/**
 * This represents a view type in the explorer. There are currently only
 * three view types. They are list, details, and thumbnails. List shows
 * only the file name and icon, details shows the file path, icon, type,
 * and last modified date. Thumbnail shows a small image of the file. If
 * the file is an image, then it shows a scaled down version of it. If it
 * is another type of file, then it shows the icon.
 *
 * @author John Glorioso
 * @version $Id: ViewType.java,v 1.1.1.1 2008/02/20 15:05:39 jglorioso Exp $
 */
public class ViewType extends Constant
{
    public static final ViewType LIST = new ViewType("List");
    public static final ViewType DETAILS = new ViewType("Details");
    public static final ViewType THUMBNAILS = new ViewType("Thumbnails");
    /** Gets incremented as format types are initialized. */
    private static int _nextId = 0;
    /** To keep track of each type. */
    private static Vector _types;

    /**
     * Creates a new ViewType given the description.
     *
     * @param String The description.
     */
    private ViewType(String desc)
    {
        super(_nextId++, desc);
        if (_types == null) _types = new Vector();
        _types.add(this);
    }

    /**
     * Returns an ViewType based on the id passed in. If the id does not match the id of
     * a constant, then we return null. If there are two constants with the same id, then
     * the first one is returned.
     *
     * @param int The constant id.
     * @return ViewType
     */
    public static ViewType evaluate(int id)
    {
        return (ViewType)Constant.evaluate(id, _types);
    }

    /**
     * Returns an Constant based on the description passed in. If the description does not
     * match the description of a constant, then we return null. If there are two constants
     * with the same description, then the first one is returned.
     *
     * @param String The description.
     * @return ViewType
     */
    protected static ViewType evaluate(String name)
    {
        return (ViewType)Constant.evaluate(name, _types);
    }

    public Vector getTypes()
    {
        return _types;
    }
}