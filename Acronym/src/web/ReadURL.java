package web;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;

public final class ReadURL
{
   private ReadURL() { }

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

   public static void main (final String[] args)
   {
      if (args.length > 0)
      {
         String url = args[0];
         if (args.length > 1)
            ReadURL.read (null, url, args[1]);
         else
            ReadURL.urlExists (null, url);
      }
      else
         System.out.println ("\nusage: java " + ReadURL.class.getName() +
            " URL {output-file}\n");
   }
}
