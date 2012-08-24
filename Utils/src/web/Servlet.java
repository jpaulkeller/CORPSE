package web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Abstract base class for servlets.  Provides a few convenience
 * methods such as getUserName.  Provides respond methods that will
 * write various data types to the servlet's output stream (String,
 * List&lt;String&gt;, Map&lt;String, String&gt;, and File). */

public abstract class Servlet extends HttpServlet
{
   private static final long serialVersionUID = 2;

   public static final String USERNAME = "UserName";
   
   @Override
   protected void doGet (final HttpServletRequest request,
                         final HttpServletResponse response)
      throws ServletException, IOException
   {
      handleRequest (request, response);
   }

   @Override
   protected void doPost (final HttpServletRequest request,
                          final HttpServletResponse response)
      throws ServletException, IOException
   {
      handleRequest (request, response);
   }

   protected abstract void handleRequest (HttpServletRequest request,
                                          HttpServletResponse response)
      throws ServletException, IOException;

   protected static String getUserName (final HttpServletRequest request)
   {
      String userName = request.getParameter (USERNAME);
      if (userName == null || userName.trim().length() == 0)
      {
         HttpSession session = request.getSession();
         userName = (String) session.getAttribute (USERNAME);
      }
      if (userName == null || userName.trim().length() == 0)
      {
         userName = "Default";
         System.err.println (USERNAME + " unknown, using '" + userName + "'");
      }
      return userName;
   }

   protected static void respond (final HttpServletResponse response, 
                                  final String text)
   {
      try
      {
         response.setContentType ("text/plain");
         ServletOutputStream output = response.getOutputStream();
         if (text != null)
            output.println (text);
         output.flush();
         output.close();
      }
      catch (Exception x)
      {
         System.err.println (x); 
      }
   }
   
   protected static void respond (final HttpServletResponse response,
                                  final Collection<String> list)
   {
      try
      {
         response.setContentType ("text/plain");
         ServletOutputStream output = response.getOutputStream();
         if (list != null)
            for (String element : list)
               output.println (element);
         output.flush();
         output.close();
      }
      catch (Exception x)
      {
         System.err.println (x); 
      }
   }
   
   /* This method should not be used for maps that contain multi-line
    * values.  Use an EncodableMap instead. */

   protected static void respond (final HttpServletResponse response,
                                  final Map<String, String> map)
   {
      try
      {
         response.setContentType ("text/plain");
         ServletOutputStream output = response.getOutputStream();
         if (map != null)
            for (Map.Entry<String, String> entry : map.entrySet())
               output.println (entry.getKey() + "==" + entry.getValue());
         output.flush();
         output.close();
      }
      catch (Exception x)
      {
         System.err.println (x); 
      }
   }
   
   protected static void respond (final HttpServletResponse response, 
                                  final File file)
   {
      FileInputStream in = null;
      ServletOutputStream out = null;
      
      try
      {
         // send back the exported file's contents
         response.setContentType ("application/octet-stream");
         response.setHeader
            ("Content-disposition", "attachment; filename=" + file.getName());
         out = response.getOutputStream();

         in = new FileInputStream (file);
         byte[] b = new byte[4096];
         int read;
         while ((read = in.read (b)) != -1)
            out.write (b, 0, read);
         out.flush();
         out.close();
      }
      catch (Exception x)
      {
         x.printStackTrace();
         System.err.println (x); 
         response.setHeader ("", "");
         respond (response, "Unable to export: " + file.getPath() + "\n" + x);
      }
      finally
      {
         if (in  != null) try { in.close();  } catch (IOException x) { }
         if (out != null) try { out.close(); } catch (IOException x) { }
      }
   }

   protected static void respondObject (final HttpServletResponse response,
                                        final Serializable obj)
   {
      try
      {
         ServletOutputStream output = response.getOutputStream();
         if (obj != null)
         {
            OutputStream os = response.getOutputStream();
            ObjectOutputStream out = new ObjectOutputStream (os);
            out.writeObject (obj);
         }
         output.flush();
         output.close();
      }
      catch (Exception x)
      {
         System.err.println (x); 
      }
   }
}
