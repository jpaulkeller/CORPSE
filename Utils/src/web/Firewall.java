package web;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

public class Firewall
{
   public static void defineProxy()
   {
      // System.setProperty ("http.agent", "");
      System.setProperty ("http.proxyHost", "centralproxy.northgrum.com");
      System.setProperty ("http.proxyPort", "80");
      // System.setProperty ("http.nonProxyHosts", "localhost"); 

      Authenticator.setDefault (new Authenticator() {
         protected PasswordAuthentication getPasswordAuthentication()
         {
            return (new PasswordAuthentication ("A32357", "Zse45rdx".toCharArray()));
         }
      });
   }
}
