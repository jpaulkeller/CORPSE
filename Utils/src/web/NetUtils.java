package web;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import str.RegExp;

public class NetUtils
{
   private static Pattern hostPattern = Pattern.compile ("//([^:/]+)(?::([0-9]+))?/(.+)");

   /**
    * Determine the IP address for the given host, and return it as a
    * String. The host argument can be an IP address.
    */

   public static String getIP (String hostOrIP) throws UnknownHostException
   {
      // TBD remove when JDK 1.4
      if (isLocalHost (hostOrIP))   // may be 127.0.0.1
         return (InetAddress.getLocalHost().getHostAddress());
      else if (RegExp.matches (RegExp.IP4, hostOrIP))
         return (hostOrIP);
      else
         return (getInetAddress (hostOrIP).getHostAddress());
   }

   /**
    * Determine the InetAddress for the given host or IP.
    */

   public static InetAddress getInetAddress (String hostOrIP)
      throws UnknownHostException
   {
      InetAddress ia;

      if (isLocalHost (hostOrIP))   // may be 127.0.0.1
         ia = InetAddress.getLocalHost();
      else if (RegExp.matches (RegExp.IP4, hostOrIP))
      {
         // TBD JDK 1.4
         // byte[] address = new byte[4];
         // for (int i = 0; i < 4; i++)
         // address[i] = Byte.parseByte (match.toString (i + 1));
         // ia = InetAddress.getByAddr (address);
         ia = null;
      }
      else
         ia = InetAddress.getByName (hostOrIP);

      return (ia);
   }

   /**
    * Returns true if hostName is that of the local machine.
    */
   public static boolean isLocalHost (String hostName)
   {
      if ((hostName == null) ||
          hostName.equals ("localhost") || hostName.equals ("127.0.0.1"))
         return true;

      try
      {
         InetAddress localHost = InetAddress.getLocalHost();
         InetAddress testHost = InetAddress.getByName (hostName);
         return localHost.equals (testHost);
      }
      catch (UnknownHostException e)
      {
         e.printStackTrace();
      }

      return false;
   }

   /**
    * Returns true if the given URL is local.
    */
   public static boolean isLocalURL (String url)
   {
      try
      {
         url = url.toUpperCase();
         return (url.indexOf ("://LOCALHOST") > 0 ||
                 url.indexOf ("://127.0.0.1") > 0 ||
                 url.indexOf ("://" + getIP ("localhost")) > 0);
      }
      catch (UnknownHostException x) { /* won't happen */ }
      return false;
   }

   /**
    * Changes the given host (name or IP) into a "canonical" IP address,
    * and returns a server address.
    */
   public static String getServerAddress (String hostName, String port)
      throws UnknownHostException
   {
      String ip = getIP (hostName);
      return ("//" + ip + ":" + port);
   }

   public static String getServerAddress (String host, String port, String name)
      throws UnknownHostException
   {
      return (getServerAddress (host, port) + "/" + name);
   }

   private static String getServerAliasFrom (String address)
   {
      String alias = null;

      if (address != null)
      {
         int indexA = address.indexOf ("//") + 1;
         int indexB = address.lastIndexOf ("/");
         if (indexB != indexA)
            alias = address.substring (indexB + 1);
      }

      return alias;
   }

   private static String getServerHostFrom (String address)
   {
      String serverHost = "localhost";

      if (address != null)
      {
         Matcher matcher = hostPattern.matcher(address);
         if (matcher.matches())
            serverHost = matcher.group(1);
      }

      return serverHost;
   }

   public static void main (String[] args)
   {
      try
      {
         String url = "http://localhost:8080/test";
         System.out.print ("isLocalURL (\"" + url + "\") => ");
         System.out.println (isLocalURL (url) + "");
         url = "http://" + getIP ("localhost") + "/test";
         System.out.print ("isLocalURL (\"" + url + "\") => ");
         System.out.println (isLocalURL (url) + "");
         url = "http://www.google.com";
         System.out.print ("isLocalURL (\"" + url + "\") => ");
         System.out.println (isLocalURL (url) + "");
         System.out.println();
      }
      catch (Exception e)
      {
         System.err.println (e);
      }
   }
}
