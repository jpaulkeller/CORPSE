package web;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public final class Zoho
{
   private Zoho() { }

   /**
    * CALLER BEWARE: This method relies on the HTTP server throwing an
    * I/O exception rather than returning an error HTML page.
    */
   public static boolean urlExists (final Component owner, final String address)
   {
      try
      {
         // if (Login.login (owner, address))
         URL url = new URL (address);
         if (url.getContent() != null)
            return true;
      }
      catch (Exception x)
      {
         // If the HTTP server has a dump handler and returns Error
         // 404 page, this exception will not be caught (can be
         // unreliable).
      }
      return false;
   }

   public static void read (final Component owner, final String address, 
                            final String outName)
   {
      try
      {
         // if (!Login.login (owner, address))
         //    return;
         
         URL url = new URL (address);

         URLConnection conn = url.openConnection();
         InputStream in = conn.getInputStream();

         System.out.println ("URL Connection = " + conn);
         System.out.println ("Content Type   = " + conn.getContentType());
         System.out.println ("Content Length = " + conn.getContentLength());
         System.out.println ("Input Stream   = " + in);

         PrintStream out = System.out;
         FileOutputStream fos = null;
         if (outName != null && outName.length() > 0)
         {
            fos = new FileOutputStream (outName);
            out = new PrintStream (fos);
         }

         byte[] bytes = new byte[4096];
         int len;

         while ((len = in.read (bytes)) >= 0)
            if (len > 0)
               out.write (bytes, 0, len);

         in.close();
         out.flush();
         out.close();
         if (fos != null)
            fos.close();
      }
      catch (Exception x)
      {
         x.printStackTrace();
      }
   }

   public static StringBuilder capture (final String address)
   {
      StringBuilder buf = new StringBuilder();

      try
      {
         // if (!Login.login (owner, address))
         //    return null;
         
         URL url = new URL (address);
         URLConnection conn = url.openConnection();
         InputStream in = conn.getInputStream();
         InputStreamReader isr = new InputStreamReader (in);
         BufferedReader reader = new BufferedReader (isr);

         // System.out.println ("URL Connection = " + conn);
         // System.out.println ("Content Type   = " + conn.getContentType());
         // System.out.println ("Content Length = " + conn.getContentLength());
         // System.out.println ("Input Stream   = " + in);

         String line;
         while ((line = reader.readLine()) != null)
            buf.append (line + "\n");

         reader.close();
         isr.close();
         in.close();
      }
      catch (Exception x)
      {
         System.err.println ("ReadURL.capture: " + address);
         x.printStackTrace (System.err);
         return null;
      }

      return buf;
   }

   public static StringBuilder capture (final String address, final String input)
   {
      StringBuilder s = new StringBuilder();
      
      try
      {
         BufferedReader reader = sendInput (address, input);
         if (reader != null)
         {
            String value;
            while ((value = reader.readLine()) != null)
               s.append (value + "\n");
            reader.close();
         }
      }
      catch (IOException x)
      {
         System.err.println (x);
      }

      return s;
   }

   private static BufferedReader sendInput (final String address, final String input)
   {
      try
      {
         URL url = new URL (address);

         URLConnection conn = url.openConnection();
         conn.setDoInput (true);
         conn.setDoOutput (true);
         conn.setUseCaches (false);
         conn.setRequestProperty ("Content-type", "text/plain");

         // push the input data to the server
         PrintStream out = new PrintStream (conn.getOutputStream());
         out.println (input);
         out.flush();
         out.close();

         InputStreamReader isr = new InputStreamReader (conn.getInputStream());
         return new BufferedReader (isr);
      }
      catch (MalformedURLException x)
      {
         System.err.println ("Error creating URL: " + address + "\n" + x);
      }
      catch (IOException x)
      {
         System.err.println ("Error opening URL: " + address + "\n" + x);
      }
      catch (Exception x)
      {
         System.err.println ("Error processing URL: " + address + "\n" + x);
      }

      return null;
   }
   
   // https://accounts.zoho.com/login?servicename=ZohoCreator&FROM_AGENT=true&LOGIN_ID=palantiri&PASSWORD=necron99
   // Zoho API Key: 2c544ce97cbed25a26ccbc5faaf4c62e
   // Secretkey: 6e3564540db3861a4c64b90b4f045f75
   // TicketAPI = d9251dea1fb5564b3eeccef1fe7b5b21
   
   // REST / List
   // http://creator.zoho.com/api/xml/applications/apikey=<API Key>&ticket=<Ticket>&limit=<specify limit>
   
   // GET / View
   // http://creator.zoho.com/api/xml/<applicationName>/view/<view_name>/apikey=<API Key>&ticket=<Ticket>
   
   private static final String URL = "http://creator.zoho.com/api/xml/";
   private static final String API_KEY = "2c544ce97cbed25a26ccbc5faaf4c62e";
   private static final String TICKET = "d9251dea1fb5564b3eeccef1fe7b5b21";
   
   public static String getApplications()
   {
      StringBuffer address = new StringBuffer (URL);
      address.append ("applications"); // get applications
      address.append ("/apikey=" + API_KEY);
      address.append ("&ticket=" + TICKET);
      address.append ("&limit=100");
      StringBuilder output = ReadURL.capture (address.toString());
      return output.toString ();
   }
   
   public static String getFormsAndViews (final String application)
   {
      StringBuffer address = new StringBuffer (URL);
      address.append (application);
      address.append ("/formsandviews"); // get forms and views
      address.append ("/apikey=" + API_KEY);
      address.append ("&ticket=" + TICKET);
      StringBuilder output = ReadURL.capture (address.toString());
      return output.toString ();
   }
   
   public static String getRecords (final String application, final String view)
   {
      StringBuffer address = new StringBuffer (URL);
      address.append (application);
      address.append ("/view/" + view);
      address.append ("/apikey=" + API_KEY);
      address.append ("&ticket=" + TICKET);
      StringBuilder output = ReadURL.capture (address.toString());
      return output.toString ();
   }
   
   // http://api.creator.zoho.com/
   
   public static void main (final String[] args)
   {
      System.out.println ("\nusage: java " + ReadURL.class.getName() +
                          " URL {output-file}\n");
      
      Firewall.defineProxy();
      if (args.length > 0)
      {
         String url = args[0];
         if (args.length > 1)
            Zoho.read (null, url, args[1]);
         else
            Zoho.urlExists (null, url);
      }
      else
      {
         // String xml = getApplications();
         // String xml = getFormsAndViews ("deed-list");
         String xml = getRecords ("deed-list", "Deed_List_Form_View");
         System.out.println (xml);
      }
   }
}
