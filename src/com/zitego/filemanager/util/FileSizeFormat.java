package com.zitego.filemanager.util;

import com.zitego.filemanager.FileSize;
import java.math.BigDecimal;
import java.util.regex.*;
import java.text.*;

/**
 * This class formats a file size from number of bytes to a string
 * representation of the number of bytes in either kilobytes or megabytes
 * depending on the actual size. If the size is less than 1,000,000 bytes,
 * then it will be formatted into kilobytes. Otherwise, it will be megabytes.
 *
 * @author John Glorioso
 * @version $Id: FileSizeFormat.java,v 1.1.1.1 2008/02/20 15:05:39 jglorioso Exp $
 */
public class FileSizeFormat extends Format
{
    /** The formatter itself. */
    public static final FileSizeFormat FORMATTER = new FileSizeFormat();
    /** The divisor (1024) to determine kilobytes and megabytes. */
    public static final long DIVISOR = 1024;
    /** To parse Strings. */
    private static Pattern _pattern = Pattern.compile("(\\d*\\.?\\d+)([kmg]b)?", Pattern.CASE_INSENSITIVE);

    public static void main(String[] args) throws Exception
    {
        long bytes = ( (FileSize)FORMATTER.parseObject(args[0]) ).getBytes();
        System.out.println(bytes);
        System.out.println( "converted back="+FORMATTER.format(bytes) );
    }

    /**
     * Creates a new FileSizeFormat object.
     */
    private FileSizeFormat()
    {
        super();
    }

    /**
     * Returns a String representation of the number of bytes passed in as
     * either kilobytes or megabytes.
     *
     * @param long The number of bytes.
     */
    public String format(long bytes)
    {
        StringBuffer ret = new StringBuffer();
        if (bytes < 0) ret.append("0mb");
        else if (bytes < 1000000) ret.append( getKb(bytes) ).append("kb");
        else if (bytes < 1000000000) ret.append( getMb(bytes) ).append("mb");
        else if (bytes < 1000000000000L) ret.append( getGb(bytes) ).append("gb");
        else ret.append( getMb(bytes) ).append("gb");
        return ret.toString();
    }

    /**
     * Formats the given FileSize or Long object into kilobytes or megabytes.
     *
     * @param Object A FileSize or a Long.
     * @param StringBuffer The buffer to append text to.
     * @param FieldPosition Ignored.
     * @return String
     * @throws IllegalArgumentException if the Object is not FileSize or Long.
     */
    public StringBuffer format(Object obj, StringBuffer ret, FieldPosition pos) throws IllegalArgumentException
    {
        if (ret == null) ret = new StringBuffer();
        if (obj instanceof FileSize) return ret.append( format(((FileSize)obj).getBytes()) );
        else if (obj instanceof Long) return ret.append( format(((Long)obj).longValue()) );
        else if (obj instanceof Integer) return ret.append( format(((Integer)obj).longValue()) );
        else throw new IllegalArgumentException("Cannot format Object: "+obj+" into a file size string.");
    }

    /**
     * This returns a FileSizeObject given the String source. If the string is empty, null, or
     * no numbers are in it, then it returns a 0 sized FileSize, otherwise it takes the first
     * number and creates a file size out of that and assumes it is the number of bytes. If there
     * is a kb or mb on the end, then it does the appropriate division to turn it into bytes.
     *
     * @param String The string representation of the file size.
     * @return FileSize
     */
    public Object parseObject(String in)
    {
        return parseObject( in, new ParsePosition(0) );
    }

    /**
     * This returns a FileSizeObject given the String source. If the string is empty, null, or
     * no numbers are in it, then it returns a 0 sized FileSize, otherwise it takes the first
     * number and creates a file size out of that and assumes it is the number of bytes. If there
     * is a kb or mb on the end, then it does the appropriate division to turn it into bytes.
     *
     * @param String The string representation of the file size.
     * @param ParsePosition Ignored.
     * @return FileSize
     */
    public Object parseObject(String in, ParsePosition pos)
    {
        long ret = 0L;
        if (in != null)
        {
            Matcher m = _pattern.matcher(in);
            if ( m.matches() )
            {
                double dsize = 0D;
                String size = m.group(1);
                String ext = m.group(2);
                if (size != null) dsize = Double.parseDouble(size);
                if (ext != null)
                {
                    if ( "kb".equalsIgnoreCase(ext) ) dsize = dsize * DIVISOR;
                    else if ( "mb".equalsIgnoreCase(ext) ) dsize = dsize * DIVISOR * DIVISOR;
                    else if ( "gb".equalsIgnoreCase(ext) ) dsize = dsize * DIVISOR * DIVISOR * DIVISOR;
                }
                ret = (long)dsize;
            }
        }
        return new FileSize(ret);
    }

    /**
     * Returns the given number of bytes into kilobytes with one decimal place precision.
     *
     * @param long The number of bytes.
     * @return double
     */
    public static double getKb(long bytes)
    {
        double ret = (double)bytes / (double)DIVISOR;
        return new BigDecimal(ret).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * Returns the given number of bytes into megabytes with one two place precision.
     *
     * @param long The number of bytes.
     * @return double
     */
    public static double getMb(long bytes)
    {
        double ret = (double)bytes / (double)DIVISOR / (double)DIVISOR;
        return new BigDecimal(ret).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * Returns the given number of bytes into gigabytes with one two place precision.
     *
     * @param long The number of bytes.
     * @return double
     */
    public static double getGb(long bytes)
    {
        double ret = (double)bytes / (double)DIVISOR / (double)DIVISOR / (double)DIVISOR;
        return new BigDecimal(ret).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }
}