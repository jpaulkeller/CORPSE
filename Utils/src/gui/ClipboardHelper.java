package gui;

import java.io.IOException;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;

/**
 * This is the ClipboardHelper class, which allows you to easily paste
 * Strings to or copy Strings from the system clipboard.  This is
 * really just a simple wrapper around the setContents and getContents
 * methods of java.awt.datatransfer.Clipboard, although if you try to
 * use that class outside of a java.awt program you can easily run
 * into problems because the AWT Window thread is notoriously hard to
 * kill. For example, compile and run this program:
 *
 *     import java.awt.*;
 *     public class AwtHang {
 *         public static void main (String[] args) {
 *             Toolkit toolkit = Toolkit.getDefaultToolkit();
 *             System.out.println ("All done.");
 *         }
 *     }
 *
 * The program hangs because after you grab an AWT toolkit, you
 * normally have to call System.exit (0) at the end of the program in
 * order to terminate. The system clipboard is obtained via the AWT
 * Toolkit, so...
 *
 * What this class does is it wraps the Toolkit call inside a daemon
 * thread, so that the Toolkit's AWT-Window thread also becomes a
 * daemon thread, and it can be terminated by a normal Java program
 * without a call to System.exit; otherwise the AWT Event Queue is run
 * as a user thread (or something like that) which can only be
 * terminated by System.exit.
 *
 * The copyString and pasteString methods have been made static, so
 * you don't even have to create an instance of this class in order to
 * call the methods. For example, you could use the following code for
 * simple cut and paste operations:
 *
 *     String clipData = "This is my clipboard data";
 *     System.out.println ("Copying \"" + clipData + "\" to the clipboard");
 *     ClipboardHelper.copyString (clipData);
 *     System.out.println ("Pasting: " + ClipboardHelper.pasteString());
 *
 * I set up this class so it's not trying to get a new Toolkit or
 * Clipboard every time you do a copy and paste, so there shouldn't be
 * any penalty for doing multiple copy and/or paste operations. This
 * is true whether you're calling the methods as static methods (as in
 * the example above) or as calls to a single ClipboardHelper object
 * that you've instantiated.
 *
 * I've also included generic copyTransferableObject and pasteObject
 * methods, so you can play around with copying and pasting different
 * kinds of objects to the clipboard (although you'll have to read up
 * on the Transferable and DataFlavor objects to understand how all
 * that works).
 *
 * @author Julian Robichaux ( http://www.nsftools.com )
 * @version 1.0
 */

public final class ClipboardHelper
{
   /**
    * The global Clipboard object that we use for all of our calls --
    * it's static so it only has to get created once, and can then be
    * reused.
    */
   private static Clipboard clipboard = null;

   private ClipboardHelper()
   {
      // utility class; prevent instantiation
   }

   /**
    * The private getClipboard method attempts to populate the local
    * clipboard variable with the system clipboard, if the variable is
    * currently null (if it's already been assigned as the system
    * clipboard, there's no need to assign it again).  The trick here
    * is to get the system clipboard reference in a daemon thread, so
    * the AWT threads created by the call to
    * Toolkit.getDefaultToolkit() can be killed without the need to
    * call System.exit().
    */
   private static void getClipboard()
   {
      // this is our simple thread that grabs the clipboard
      Thread clipThread = new Thread() {
            @Override
            public void run()
            {
               clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            }
         };

      // start the thread as a daemon thread and wait for it to die
      if (clipboard == null)
      {
         try
         {
            clipThread.setDaemon (true);
            clipThread.start();
            clipThread.join();
         }
         catch (Exception x)
         {
            System.err.println (x);
         }
      }
   }

   /** Returns the clipboard name. */
   
   public static String getName()
   {
      getClipboard();
      return clipboard.getName();
   }

   /**
    * Copies a String to the system clipboard.
    *
    * @param data    the String to send to the clipboard
    */
   public static void copyString (final CharSequence data)
   {
      copyTransferableObject (new StringSelection (data.toString()));
   }

   /**
    * Copies a Transferable object to the system clipboard.
    *
    * @param contents    the item to send to the clipboard,
    *                    which must be an object of a class
    *                    that implements java.awt.datatransfer.Transferable
    */
   public static void copyTransferableObject (final Transferable contents)
   {
      getClipboard();
      clipboard.setContents (contents, null);
   }

   /**
    * Gets a String from the system clipboard (if the object on the
    * clipboard does not have a String representation, or the
    * clipboard is empty, this method returns null).
    *
    * @return a String representation of the object on the system
    *         clipboard, or null if the object doesn't have a String
    *         representation or the clipboard is empty.
    */
   public static String pasteString()
   {
      String data = null;
      try
      {
         /* This is the new "better" way to do it (not working yet)
            DataFlavor flavor = new DataFlavor (); // TBD
            Reader reader = flavor.getReaderForText (contents);
            BufferedReader br = new BufferedReader (reader);
            
            StringBuffer buf = new StringBuffer();
            String line;
            while ((line = br.readLine()) != null)
            buf.append (line);
         */

         data = (String) pasteObject (DataFlavor.stringFlavor);
      }
      catch (Exception e)
      {
         System.err.println ("Error getting String from clipboard: " + e);
      }

      return data;
   }

   /**
    * Gets an Object from the system clipboard, using the specified
    * DataFlavor format. This method throws any exceptions that are
    * thrown by the Transferable.getTransferData() method
    * (UnsupportedFlavorException if the DataFlavor is not supported,
    * IOException if the data is not available in the requested
    * flavor).
    *
    * @return    an Object of the class defined by the DataFlavor
    *            that was requested, or null if the clipboard is empty
    * @exception IOException    if the clipboard data is no longer available
    *                           in the requested flavor
    * @exception UnsupportedFlavorException    if the requested data flavor
    *                                          is not supported
    */
   public static Object pasteObject (final DataFlavor flavor)
      throws UnsupportedFlavorException, IOException
   {
      Object obj = null;

      getClipboard();
      Transferable content = clipboard.getContents (null);
      if (content != null && content.isDataFlavorSupported (flavor))
         obj = content.getTransferData (flavor);

      return obj;
   }
}
