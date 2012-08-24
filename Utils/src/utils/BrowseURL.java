package utils;

import java.io.IOException;

/**
 * A simple, static class to display a URL in the system browser.
 *
 * Under Unix, the system browser is hard-coded to be 'netscape'.
 * Netscape must be in your PATH for this to work. This has been
 * tested with the following platforms: AIX, HP-UX and Solaris.
 *
 * Under Windows, this will bring up the default browser under windows,
 * usually either Netscape or Microsoft IE. The default browser is
 * determined by the OS. This has been tested under Windows 95/98/NT.
 *
 * Examples:
 *
 * BrowseURL.displayURL ("http://www.javaworld.com")
 * BrowseURL.displayURL ("file://c:\\docs\\index.html#")
 * BrowseURL.displayURL ("file:///user/joe/index.html#");
 *
 * Note - you must include the url type -- either "http://" or "file://".
 */
public final class BrowseURL
{
   // The default system browser under windows.
   private static final String WIN_PATH = "rundll32";

   // The flag to display a url.
   private static final String WIN_FLAG = "url.dll,FileProtocolHandler";

   // The default browser under unix.
   private static final String UNIX_PATH = "netscape";

   // The flag to display a url.
   private static final String UNIX_FLAG = "-remote openURL";

   private BrowseURL() { }

   /**
    * Display a file in the system browser. If you want to display a
    * file, you must include the absolute path name.
    *
    * @param urlString the file's url (the url must start with either
    * "http://" or "file://").
    */
   public static void displayURL (final String url)
   {
      if (Utils.isWindowsPlatform())
         execWindowsCommand (url);
      else
         execUnixCommand (url);
   }

   public static void execWindowsCommand (final String url)
   {
      String cmd = null;
      if (url != null && url.length() > 0)
      {
         try
         {
            String fixedURL = validateWindowsRundll (url);
            cmd = WIN_PATH + " " + WIN_FLAG + " " + fixedURL;
            Runtime.getRuntime().exec (cmd);
         }
         catch (IOException x)
         {
            System.err.println ("Error starting windows command: (" + cmd + ")");
            System.err.println ("BrowseURL.execWindowsCommand: " + x);
         }
      }
   }

   // rundll32 has problems with http:// URLs that end with .html and .htm.
   // Local paths such as file://C:\index.html and C:\index.htm are ok.
   // The fix is to replace a character with a hex token that the browser
   // can safely interpret.
   
   public static String validateWindowsRundll (final String url)
   {
      String newUrl = url;
      
      String low = url.toLowerCase();
      if ((low.startsWith("http://") || low.startsWith("https://")) &&
          (low.endsWith (".html") || low.endsWith (".htm")))
      {
         int length = url.length();
         newUrl = url.substring(0, length - 2);
         for (int i = length - 2; i < length; i++)
         {
            char c = url.charAt(i);
            switch (c)
            {
            case 'm':
               newUrl += "%6D";
               break;
            case 'M':
               newUrl += "%4D";
               break;
            default:
               newUrl += c;
            }
         }
      }
      
      return newUrl;
   }

   public static void execUnixCommand (final String url)
   {
      String cmd = UNIX_PATH + " " + UNIX_FLAG + "(" + url + ")";
      try
      {
         // Under Unix, Netscape has to be running for the "-remote"
         // command to work. So, we try sending the command and
         // check for an exit value. If the exit command is 0,
         // it worked, otherwise we need to start the browser.
         // cmd = 'netscape -remote openURL (http://www.javaworld.com)'
         Process p = Runtime.getRuntime().exec (cmd);

         try
         {
            // wait for exit code -- if it's 0, command worked,
            int exitCode = p.waitFor();
            if (exitCode != 0) // command failed
            {
               // cmd = 'netscape http://www.javaworld.com'
               cmd = UNIX_PATH + " " + url;
               Runtime.getRuntime().exec (cmd);
            }
         }
         catch (InterruptedException x)
         {
            System.err.println ("Error starting unix browser: (" + cmd + ")");
            System.err.println ("BrowseURL.execUnixCommand: " + x);
         }
      }
      catch (IOException x)
      {
         // couldn't exec browser
         System.err.println ("Error opening in existing unix browser: (" +
                             cmd + ")");
         System.err.println ("BrowseURL.execUnixCommand: " + x);
      }
   }

   /**
    * From the Command Line, BrowseURL will by default, launch
    * the localhosts index.html in the appropriate browser.  If a url
    * argument is passed to BrowseURL, the appropriate browser
    * will be launched displaying the results of the url.
    */
   public static void main (final String[] args)
   {
      if (args.length == 0)
         displayURL ("http://localhost:8990/htdocs/comet/pub/index.html");
      else
         displayURL (args[0]);
   }
}
