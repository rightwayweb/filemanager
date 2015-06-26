package com.zitego.filemanager.servlet;

import com.zitego.web.servlet.BaseServlet;
import com.zitego.util.InvalidLoginException;
import com.zitego.util.NonFatalException;
import com.zitego.filemanager.*;
import com.zitego.filemanager.explorer.Explorer;
import com.zitego.filemanager.explorer.ExplorerHolder;
import java.io.*;
import javax.servlet.ServletException;
import javax.servlet.http.*;

/**
 * This servlet allows the viewing of a file attachment by streaming the content
 * directly to the client in bytes. It will attempt to determine the mimetype and
 * set that first so that the client's browser can handle it if possible. In
 * addition, it will set the disposition to inline so that it may be viewed in
 * the browser if possible.
 *
 * @author John Glorioso
 * @version $Id: ViewFileServlet.java,v 1.1.1.1 2008/02/20 15:05:39 jglorioso Exp $
 */
public class ViewFileServlet extends BaseServlet
{
    /**
     * <p>Displays the file in bytes by making a call to getViewableFile passing the request object.
     * The file is then streamed to the client and a Content-Disposition header is set so that
     * the browser can display it accurately. That being the case, the response should not already
     * be committed.</p>
     * <p>The override_text request parameter can be passed in with a value of 1 in order to
     * display any text/ mime type as text/plain in order to override a web browser's setting
     * to display it as the given mimetype. For example, use this to display an html file's actual
     * contents rather then to display it as an html page.</p>
     *
     * @param HttpServletRequest The request.
     * @param HttpServletResponse The response.
     * @throws IOException
     * @throws ServletException
     * @throws IllegalStateException if getViewableFile returns null.
     */
    public void service(HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException, IllegalStateException
    {
        try
        {
            ViewableFile file = getViewableFile(request);
            if (file == null) throw new IllegalStateException("Viewable file cannot be null");

            String mime = file.getFileType().getMimeType();
            if (mime != null)
            {
                if ( mime.startsWith("text/") && "1".equals(request.getParameter("override_text")) ) response.setContentType("text/plain");
                else response.setContentType(mime);
            }
            response.addHeader( "Content-Disposition", "inline;filename="+file.getFileName() );
            FileSystemObject.writeToOutputStream( file, response.getOutputStream() );
        }
        catch (InvalidLoginException ile)
        {
            request.setAttribute("err", ile);
            gotoPage(getLoginPage(), request, response);
        }
        catch (Throwable t)
        {
            handleError(request, response, t);
        }
    }

    /**
     * Returns a ViewableFile by looking in the session for an Explorer object and retrieving
     * the file specified by the request parameter "path".
     *
     * @param HttpServletRequest The request object.
     * @return ViewableFile
     * @throws NonFatalException if an error occurs retrieving the file.
     * @throws InvalidLoginException if the user is not logged in.
     * @throws IOException if an error occurs retrieving the file.
     */
    protected ViewableFile getViewableFile(HttpServletRequest request)
    throws NonFatalException, InvalidLoginException, IOException
    {
        HttpSession session = request.getSession();
        ExplorerHolder holder = (ExplorerHolder)session.getAttribute(Explorer.SESSION_NAME);
        if (holder == null) throw new InvalidLoginException("You must be logged in to view a file");

        Explorer explorer = holder.getExplorer();
        String path = request.getParameter("path");
        //Make sure they are not getting a directory
        if ( path != null && path.endsWith("/") ) throw new NonFatalException("-You cannot view a directory");
        com.zitego.filemanager.File f = (path != null ? explorer.getFile(path) : null);
        if (f == null) throw new NonFatalException("-The file you selected does not exist.");
        return f;
    }

    /**
     * Returns the login page.
     *
     * @return String
     */
    public String getLoginPage()
    {
        return "/login.jsp";
    }

    protected String getErrorPage()
    {
        return "/error_full.jsp";
    }
}