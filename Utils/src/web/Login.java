package web;

import gui.form.FormItem;
import gui.form.NumericItem;

import java.awt.Component;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;

import utils.Utils;

public class Login
{
   static LoginInfo info = new LoginInfo();
   static boolean loggedIn; // true once user has successfully logged in

   static FormItem hostItem;
   static NumericItem portItem;
   static FormItem userItem;
   static FormItem passItem;
   static FormItem[] items;
   
   public static boolean login (Component owner, String url)
   {
      return NetUtils.isLocalURL (url) || login (owner);
   }

   public static boolean login (Component owner)
   {
      if (!loggedIn)
         login (info);
         
      return loggedIn;
   }
   
   private static boolean login (LoginInfo loginInfo)
   {
      return login (loginInfo.getHost(), loginInfo.getPort() + "",
                    loginInfo.getUser(), loginInfo.getPassword());
   }

   private static boolean login (String proxyHost, String proxyPort,
                                 String userName, String password)
   {
      if (proxyHost != null && userName != null && password != null)
      {
         Authenticator.setDefault (new MyAuthenticator (userName, password));
         System.setProperty ("proxySet", "true" );
         System.setProperty ("http.proxyHost", proxyHost );
         System.setProperty ("http.proxyPort", proxyPort );
         System.setProperty ("http.proxyUser", userName);
         System.setProperty ("http.proxyPassword", password);

         // hack to see if we can get through the firewall
         try
         {
            URL url = new URL ("http://www.google.com");
            loggedIn = url.getContent() != null;
         }
         catch (Exception x)
         {
            System.err.println ("Login: " + x);
         }
      }

      return loggedIn;
   }
   
   private final static class MyAuthenticator extends Authenticator
   {
      private final PasswordAuthentication auth;

      public MyAuthenticator (String user, String password)
      {
         auth = new PasswordAuthentication (user, password.toCharArray());
      }

      @Override
      protected PasswordAuthentication getPasswordAuthentication()
      {
         return auth;
      }
   }

   public static void main (String[] args)
   {
      boolean ok = Login.login ((Component) null);
      System.out.println ("Login successful: " + ok);
      System.out.println (Login.info);
      ok = Login.login ((Component) null);
      System.out.println ("Second Login successful: " + ok);
      Utils.sleep (1000); // wait for MS-Access
      System.exit (0);
   }
}
