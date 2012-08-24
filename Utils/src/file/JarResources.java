/**
 * Copyright 2010 Northrop Grumman Corporation
 * All Rights Reserved
 *
 * This material may be reproduced by or for the U.S. Government pursuant to the
 * copyright license under the clause at Defense Federal Acquisition Regulation
 * Supplement (DFARS) 252.227-7014 (June 1995).
 */

package file;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * JarResources maps all resources included in a Zip or Jar file. 
 * Additionally, it provides a method to extract one as a blob.  Source: 
 * <a href=http://www.javaworld.com/javaworld/javatips/jw-javatip49.html>JavaWorld</a>
 */
public final class JarResources
{
   // jar resource mapping tables
   private Map<String, Integer> htSizes = new Hashtable<String, Integer>();
   private Map<String, byte[]> htJarContents = new Hashtable<String, byte[]>();

   private String jarFileName;

   /**
    * Creates a JarResources. It extracts all resources from a Jar
    * into an internal hashtable, keyed by resource names.
    * @param jarFileName a jar or zip file
    */
   public JarResources(final String jarFileName)
   {
      this.jarFileName = jarFileName;
      init();
   }

   /**
    * Extracts a jar resource as a blob.
    * @param name a resource name.
    */
   public byte[] getResource(final String name)
   {
      return (byte[]) htJarContents.get(name);
   }

   /**
    * Initializes internal hash tables with Jar file resources.
    */
   private void init()
   {
      try
      {
         // extracts just sizes only.
         ZipFile zf = new ZipFile(jarFileName);
         Enumeration e = zf.entries();
         while (e.hasMoreElements())
         {
            ZipEntry ze = (ZipEntry) e.nextElement();
            htSizes.put(ze.getName(), new Integer((int) ze.getSize()));
         }
         zf.close();

         // extract resources and put them into the hashtable.
         FileInputStream fis = new FileInputStream(jarFileName);
         BufferedInputStream bis = new BufferedInputStream(fis);
         ZipInputStream zis = new ZipInputStream(bis);
         ZipEntry ze = null;
         while ((ze = zis.getNextEntry()) != null)
         {
            if (ze.isDirectory())
               continue;

            int size = (int) ze.getSize();
            // -1 means unknown size.
            if (size == -1)
               size = ((Integer) htSizes.get(ze.getName())).intValue();

            byte[] b = new byte[(int) size];
            int rb = 0;
            int chunk = 0;
            while (((int) size - rb) > 0)
            {
               chunk = zis.read(b, rb, (int) size - rb);
               if (chunk == -1)
                  break;
               rb += chunk;
            }
            // add to internal resource hashtable
            htJarContents.put(ze.getName(), b);
         }
      }
      catch (NullPointerException e)
      {
         System.out.println("done.");
      }
      catch (FileNotFoundException e)
      {
         e.printStackTrace();
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }

   /**
    * Test driver. Given a jar file and a resource name, it tries to
    * extract the resource and then tells us whether it could or not.
    *
    * <strong>Example</strong>
    * Let's say you have a JAR file which jarred up a bunch of gif image
    * files. Now, by using JarResources, you could extract, create, and display
    * those images on-the-fly.
    * <pre>
    *     ...
    *     JarResources JR=new JarResources("GifBundle.jar");
    *     Image image=Toolkit.createImage(JR.getResource("logo.gif");
    *     Image logo=Toolkit.getDefaultToolkit().createImage(
    *                   JR.getResources("logo.gif")
    *                   );
    *     ...
    * </pre>
    */
   public static void main(String[] args) throws IOException
   {
      if (args.length != 2)
      {
         System.err.println("usage: java JarResources <jar file name> <resource name>");
         System.exit(1);
      }
      JarResources jr = new JarResources(args[0]);
      byte[] buff = jr.getResource(args[1]);
      if (buff == null)
         System.out.println("Could not find " + args[1] + ".");
      else
         System.out.println("Found " + args[1] + " (length=" + buff.length + ").");
   }
}
