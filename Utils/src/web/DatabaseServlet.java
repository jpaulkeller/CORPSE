package web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import db.ConnectAPI;
import db.SQL;

// servlet-api.jar

/**
 * Provides remote access to a database on the server.  Allows the
 * execution of SQL statements.  Values can be return as a simple
 * String, a List of Strings, or a Map of String/String.  Client
 * access should generally be through the DatabaseAPI class.
 */
public class DatabaseServlet extends Servlet
{
   private static final long serialVersionUID = 3;
   
   public static final String CONNECTION_FIELD = "Connection";
   public static final String FIELD_MAP_FORMAT = "FieldMap";
   public static final String FORMAT_FIELD     = "Format";
   public static final String LIST_FORMAT      = "List";
   public static final String MAP_FORMAT       = "Map";
   public static final String STRING_FORMAT    = "String";   

   private transient SQL sql;
   
   public DatabaseServlet (final String dbms)
   {
      sql = ConnectAPI.connect (dbms);
   }
   
   @Override
   protected void handleRequest (final HttpServletRequest request,
                                 final HttpServletResponse response)
      throws ServletException, IOException
   {
      DatabaseRequest dr = new DatabaseRequest (request);
      executeSQL (response, dr);
   }

   protected void executeSQL (final HttpServletResponse response, 
                              final DatabaseRequest dr)
   {
      try
      {
         if (dr.format == null)
         {
            int count = sql.execute (dr.sql);
            String reply = "SQL executed; " + count + " record(s) affected";
            respond (response, reply);
         }
         else if (STRING_FORMAT.equals (dr.format))
         {
            String value = sql.getString (dr.sql);
            respond (response, value);
         }
         else if (LIST_FORMAT.equals (dr.format))
         {
            List<String> values = sql.getList (dr.sql);
            respond (response, values);
         }
         else if (MAP_FORMAT.equals (dr.format))
         {
            Map<String, String> map = sql.getMapping (dr.sql);
            respond (response, map);
         }
         else if (FIELD_MAP_FORMAT.equals (dr.format))
         {
            Map<String, String> map = sql.getFieldMap (dr.sql);
            respond (response, map);
         }
         else
         {
            System.err.println ("Invalid Format: " + dr.format);
            respond (response, "Invalid Format: " + dr.format);
         }
      }
      catch (SQLException x)
      {
         System.err.println ("SQL: " + dr.sql);
         System.err.println (x);
         x.printStackTrace (System.err);                  
         respond (response, x.getMessage() + "\nSQL: " + dr.sql);
      }
   }
   
   static final class DatabaseRequest
   {
      // String connName;
      private String sql;
      private String format;
      
      private DatabaseRequest (final HttpServletRequest request) throws IOException
      {
         // connName = request.getParameter (CONNECTION_FIELD);
         format = request.getParameter (FORMAT_FIELD);

         /*
         if (connName == null)
            System.err.println ("Unable to handle request; missing Connection: " +
                     request.getRequestURL());
         */

         InputStream is = request.getInputStream();
         InputStreamReader isr = new InputStreamReader (is);
         BufferedReader reader = new BufferedReader (isr);
         sql = reader.readLine();
         reader.close();
      }
   }
}
