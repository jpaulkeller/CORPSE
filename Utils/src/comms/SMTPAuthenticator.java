package comms;

import javax.mail.PasswordAuthentication;

/**
 * The class Authenticator represents an object that knows how to obtain
 * authentication for a network connection. Applications use this class by
 * creating a subclass, and registering an instance of that subclass with the
 * system with setDefault(). When authentication is required, the system will
 * invoke a method on the subclass (like getPasswordAuthentication)
 * 
 * @author Paloma Trigueros Cabezon
 * @version 1.0
 */

public class SMTPAuthenticator extends javax.mail.Authenticator 
{
   private String pass = "";
   private String login = "";

   public SMTPAuthenticator()
   {
   }
   
   public SMTPAuthenticator (final String login, final String pass)
   {
      this.login = login;
      this.pass = pass;
   }
   
   @Override
   public PasswordAuthentication getPasswordAuthentication()
   {
      return pass.equals ("") ? null : new PasswordAuthentication (login, pass);
   }
}
