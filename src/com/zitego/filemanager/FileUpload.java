package com.zitego.filemanager;

import com.zitego.filemanager.explorer.Explorer;
import com.zitego.util.NonFatalException;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipException;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServlet;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;

/**
 * This is a class used to handle uploading files.
 *
 * @author John Glorioso
 * @version $Id: FileUpload.java,v 1.2 2009/11/10 16:34:06 jglorioso Exp $
 */
public class FileUpload
{
    /** The explorer object used to determine where new files go. */
    protected Explorer _explorer;
    /** The FileSystemObjects that were uploaded. */
    protected Vector _uploadedFiles = new Vector();
    /** The other request parameters passed in. */
    protected Hashtable _params = new Hashtable();

    public static void main(String[] args) throws Exception
    {
        FileUpload f = new FileUpload( new Explorer(args[0]) );
        Vector ret = f.getZippedFiles(new ZipFile(args[1]), null);
        for (int i=0; i<ret.size(); i++)
        {
            System.out.println(ret.get(i));
        }
    }

    /**
     * Creates a new FileUpload object with an Explorer.
     *
     * @param exp The user's explorer.
     */
    public FileUpload(Explorer exp)
    {
        _explorer = exp;
    }

    /**
     * Uploads the files in the given request.
     *
     * @param request The request object.
     * @throws IOException if an error occurs uploading the file(s).
     * @throws ZipException if an error occurs extracting files.
     * @throws NonFatalException if there is not enough space to expand the file.
     */
    public void upload(HttpServletRequest request) throws IOException, ZipException, FileUploadException, NonFatalException
    {
        Vector files = new Vector();
        boolean expand = false;
        String rootPath = _explorer.getFileListing().getParentDirectory().getAbsolutePath();
        //Figure out how much total space we have
        FileSize freeSpace = _explorer.getFreeDiskSpace();

        DiskFileUpload upload = new DiskFileUpload();

        //Setup an upload listener
        HttpSession session = request.getSession();
        FileUploadListener listener = (FileUploadListener)session.getAttribute("UPLOAD_LISTENER");
        if (listener != null) upload.setProgressListener(listener);

        //Always write to disk
        if (freeSpace != null) upload.setSizeMax( freeSpace.getBytes() );
        List requestParams = upload.parseRequest(request);

        int size = requestParams.size();
        String name = null;
        for (int i=0; i<size; i++)
        {
            FileItem param = (FileItem)requestParams.get(i);
            name = param.getFieldName();

            if ( param.isFormField() )
            {
                if ( name.equals("expand_files") ) expand = "1".equals( param.getString() );
                else _params.put( name, param.getString() );
            }
            else
            {
                long fileSize = param.getSize();
                //If we are out of space, then error
                if (freeSpace != null && freeSpace.getBytes()-fileSize < 0)
                {
                    throw new NonFatalException("Not Enough Space left to upload "+param.getName());
                }
                else if (fileSize > 0)
                {
                    String fname = param.getName();
                    //Parse out the name if necessary
                    int index = fname.lastIndexOf("/");
                    if (index > -1)
                    {
                        fname = fname.substring(index+1);
                    }
                    else
                    {
                        //Check for a \
                        index = fname.lastIndexOf("\\");
                        if (index > -1) fname = fname.substring(index+1);
                    }
                    java.io.File f = new java.io.File(rootPath + "/" + fname);
                    try
                    {
                        param.write(f);
                    }
                    catch (Exception e)
                    {
                        throw new RuntimeException("Could not upload file "+fname, e);
                    }
                    files.add(f);
                    _params.put(name, fname);
                }
                else
                {
                    param.delete();
                }
            }
        }

        size = files.size();
        //Go through the files and add them
        for (int i=0; i<size; i++)
        {
            java.io.File f = (java.io.File)files.get(i);
            //If we are expanding and this is a zip file, then expand it
            if (expand)
            {
                FileType type = FileTypes.getFileTypeByName( f.getName() );
                String mimeType = type.getMimeType();
                if (mimeType != null && mimeType.indexOf("zip") > -1)
                {
                    addFiles( getZippedFiles(new ZipFile(f), freeSpace) );
                }
            }
            else
            {
                addFile(f);
            }
        }
        session.removeAttribute("UPLOAD_LISTENER");
    }

    /**
     * Adds the Vector of files to the uploaded files.
     *
     * @param files The files.
     * @throws IOException if a problem occurs creating the files.
     */
    private void addFiles(Vector files) throws IOException
    {
        int size = files.size();
        for (int i=0; i<size; i++)
        {
            addFile( (java.io.File)files.get(i) );
        }
    }

    /**
     * Adds the specified file to the uploaded files.
     *
     * @param f The file to add.
     * @throws IOException if a problem occurs creating the file.
     */
    private void addFile(java.io.File f) throws IOException
    {
        _uploadedFiles.add( FileSystemObjectFactory.createObject(f.getAbsolutePath(), _explorer.getRootDirectory()) );
    }

    /**
     * Returns a Vector of java.io.File objects from the given ZipFile.
     *
     * @param zipFile The ZipFile.
     * @param freeSpace The amount of freespace.
     * @throws IOException if an error occurs unzipping the file.
     * @throws NonFatalException if there is not enough space to expand the file.
     */
    public Vector getZippedFiles(ZipFile zipFile, FileSize freeSpace) throws IOException, NonFatalException
    {
        Vector ret = new Vector();
        long freeSpaceBytes = (freeSpace != null ? freeSpace.getBytes() : 0L);
        try
        {
            String rootPath = _explorer.getFileListing().getParentDirectory().getAbsolutePath();
            //Go through each entry
            for (Enumeration e=zipFile.entries(); e.hasMoreElements();)
            {
                ZipEntry entry = (ZipEntry)e.nextElement();
                String name = entry.getName();
                //Strip off the leading slash if there is one
                if (name.charAt(0) == '/') name = name.substring(1);

                //Create the file object
                java.io.File f = new java.io.File(rootPath + "/" + name);

                //Check to see if this is a directory and it already exists
                if ( entry.isDirectory() && f.exists() ) continue;

                //See if the directories in this path exist. If not, then create them
                int index = -1;
                int lastIndex = 0;
                while ( (index=name.indexOf("/", lastIndex)) != -1 )
                {
                    java.io.File dir = new java.io.File( rootPath + "/" + name.substring(0, index) );
                    if ( !dir.exists() )
                    {
                        dir.mkdir();
                        ret.add(dir);
                    }
                    lastIndex = index+1;
                }
                ret.add(f);

                //Write the new files
                if ( !entry.isDirectory() )
                {
                    if (freeSpace != null)
                    {
                        //Check for available space
                        freeSpaceBytes -= entry.getSize();
                        if (freeSpaceBytes < 0) throw new NonFatalException("Not enough space left to expand "+zipFile.getName());
                    }

                    //Extract the file
                    InputStream in = zipFile.getInputStream(entry);
                    FileOutputStream out = new FileOutputStream(f);
                    byte[] buffer = new byte[4096];
                    int bytes_read;
                    while ( (bytes_read=in.read(buffer)) != -1 )
                    {
                        out.write(buffer, 0, bytes_read);
                    }
                    in.close();
                    out.close();
                }
            }
        }
        finally
        {
            //Close and delete the zip file
            zipFile.close();
            new java.io.File( zipFile.getName() ).delete();
        }

        return ret;
    }

    /**
     * Returns the files uploaded as an array of FileSystemObjects.
     *
     * @return FileSystemObject[]
     */
    public FileSystemObject[] getUploadedFiles()
    {
        FileSystemObject[] ret = new FileSystemObject[_uploadedFiles.size()];
        _uploadedFiles.copyInto(ret);
        return ret;
    }

    /**
     * Returns the requested param or null if it does not exist.
     *
     * @param name The param name.
     * @return String
     */
    public String getParameter(String name)
    {
        return (String)_params.get(name);
    }
}
