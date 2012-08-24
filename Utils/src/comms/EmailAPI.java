package comms;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import file.FileUtils;
import gui.ComponentTools;

/**
 * EmailAPI provides a convenient interface for sending and receiving
 * email messages.
 * 
 * http://java.sun.com/products/javamail/FAQ.html
 */
public final class EmailAPI
{
   private EmailAPI() { /* utility class */ }
   
   public static boolean sendMessage (final Authenticator auth,
                                      final Mail mail)
   {
      boolean sent = false;
      try
      {
         Properties mailprops = new Properties();
         mailprops.put ("mail.smtp.host", mail.getMailHost());
         mailprops.put ("mail.smtp.port", "25");

         Session session;
         if (auth != null) // Authenticated
         {
            // String user = auth.getPasswordAuthentication().getUserName();
            // mailprops.put ("mail.smtp.user", user);
            // mailprops.put ("mail.smtp.submitter", user);
            mailprops.put ("mail.smtp.auth", "true");         
            session = Session.getDefaultInstance (mailprops, auth);
         }
         else
            session = Session.getDefaultInstance (mailprops, null);
         
         // session.setDebug (true);
         // session.setDebugOut (null);
         
         MimeMessage mm = new MimeMessage (session);

         setContent (mail, mm);
         setToAddresses (mail, mm);
         mm.setFrom (new InternetAddress (mail.getFrom()));
         mm.setSubject (mail.getSubject());
         mm.setSentDate (new Date());

         send (auth, session, mm);
         sent = true;
      }
      catch (IOException x)
      {
         x.printStackTrace (System.err);
      }
      catch (MessagingException x)
      {
         x.printStackTrace (System.err);
      }
      
      return sent;
   }

   private static void setContent (final Mail mail, final MimeMessage mm)
   throws IOException, MessagingException
   {
      Collection<File> attachments = mail.getAttachments();
      if (attachments != null)
      {
         Multipart multipart = new MimeMultipart();

         // add the message body
         MimeBodyPart messagePart = new MimeBodyPart();
         messagePart.setText (mail.getBody());
         multipart.addBodyPart (messagePart);
         
         // add the attachments
         for (File file : attachments)
         {
            MimeBodyPart attachmentPart = new MimeBodyPart();
            FileDataSource fileDataSource = new FileDataSource (file)
            {
               @Override
               public String getContentType()
               {
                  return "application/octet-stream";
               }
            };
            attachmentPart.setDataHandler (new DataHandler (fileDataSource));
            attachmentPart.setFileName (file.getName()); // TBD
            multipart.addBodyPart (attachmentPart);
         }
         
         mm.setContent (multipart);
      }
      else if (mail.getContentType() != null) // handle text/html
      {
         ByteArrayDataSource bytes = 
            new ByteArrayDataSource (mail.getBody(), mail.getContentType());
         mm.setDataHandler (new DataHandler (bytes));
      }
      else
         mm.setText (mail.getBody());
   }

   private static void setToAddresses (final Mail mail, final MimeMessage mm)
   throws MessagingException
   {
      // must have at least 1 "to" addressee
      InternetAddress[] addresses = getAddresses (mail.getTo()); 
      if (addresses.length > 0)
         mm.setRecipients (Message.RecipientType.TO, addresses);
      
      addresses = getAddresses (mail.getCC());
      if (addresses.length > 0)
         mm.setRecipients (Message.RecipientType.CC, addresses);
      
      addresses = getAddresses (mail.getBCC()); 
      if (addresses.length > 0)
         mm.setRecipients (Message.RecipientType.BCC, addresses);
   }

   private static void send (final Authenticator auth, final Session session, 
                             final MimeMessage mm)
   throws MessagingException
   {
      if (auth != null) // Authenticated
      {
         mm.saveChanges(); // not needed for static Transport.send 
         Transport tr = session.getTransport ("smtp");
         if (auth instanceof SMTPAuthenticator)
         {
            PasswordAuthentication pa = 
               ((SMTPAuthenticator) auth).getPasswordAuthentication();
            tr.connect (pa.getUserName(), pa.getPassword());
         }
         tr.sendMessage (mm, mm.getAllRecipients());
         tr.close();
      }
      else
         Transport.send (mm);
   }
   
   public static boolean sendMessage (final Mail mail)
   {
      return sendMessage (null, mail);
   }

   /**
    * Converts a collection of email addresses (strings) into an
    * InternetAddress[].  Any erroneous email addresses will be excluded.
    */
   public static InternetAddress[] getAddresses (final Collection<String> addresses)
   {
      List<InternetAddress> list = new ArrayList<InternetAddress>();
      if (addresses != null)
      {
         for (String address : addresses)
         {
            try
            {
               if (address != null && address.length() > 0)
                  list.add (new InternetAddress (address));
            }
            catch (AddressException x)
            {
               System.err.println ("Invalid e-mail address: " + address);
            }
         }
      }

      return list.toArray (new InternetAddress [list.size()]);
   }
   
   public static void launchMail (final Component owner,
                                  final CharSequence subject,
                                  final CharSequence to,
                                  final CharSequence cc,
                                  final CharSequence body)
   {
      String cmd = getCommand (subject, to, cc, body);
      
      // Note: the cmd length appears to be limited to 2048 characters for XP
      if (cmd.length() < 2048)
         executeCommand ("cmd.exe", "/c", "start", "\"EmailAPI\"", "\"" + cmd + "\"");
      else
         showWarning (owner, subject, to, cc, body);
   }
   
   public static String getCommand (final CharSequence subject,
                                    final CharSequence to,
                                    final CharSequence cc,
                                    final CharSequence body)
   {
      String cleanBody = body.toString().replaceAll ("\n", "%0A"); // for cmd.exe
      
      StringBuilder cmd = new StringBuilder();
      cmd.append ("mailto:" + to);
      cmd.append ("?subject=" + subject);
      if (cc != null)
         cmd.append ("&cc=" + cc);
      cmd.append ("&body=" + cleanBody);
      
      return cmd.toString();
   }

   public static String executeCommand (final String... args)
   {
      StringBuilder buf = new StringBuilder();

      BufferedReader br = null;
      try
      {
         Runtime rt = Runtime.getRuntime();
         Process proc = rt.exec (args);
         
         InputStream is = proc.getInputStream();
         InputStreamReader isr = new InputStreamReader (is);
         br = new BufferedReader (isr);
         String line;
         while ((line = br.readLine()) != null)
         {
            buf.append (line);
            buf.append ("\n");
         }
      }
      catch (Exception x)
      {
         System.err.println (x); // TBD
      }
      finally
      {
         FileUtils.close (br);
      }

      return buf.toString();
   }
   
   private static void showWarning (final Component owner,
                                    final CharSequence subject,
                                    final CharSequence to,
                                    final CharSequence cc,
                                    final CharSequence body)
   {
      StringBuilder message = new StringBuilder();
      message.append ("Sorry, that message is too long to send.  ");
      message.append ("You may try again with fewer addressees, or create ");
      message.append ("your own message by copy/pasting this information.");
      JTextArea text = new JTextArea (message.toString(), 0, 80);
      text.setLineWrap (true);
      text.setWrapStyleWord (true);
      text.setBackground (null);

      JTextField label = new JTextField (subject.toString());

      JPanel top = new JPanel (new BorderLayout());
      top.add (text, BorderLayout.NORTH);
      top.add (ComponentTools.getTitledPanel (label, "Subject"), BorderLayout.SOUTH);
      
      JTextArea toTxt = new JTextArea (to.toString(), 10, 80);
      toTxt.setLineWrap (true);
      JTextArea ccTxt = new JTextArea (cc.toString(), 10, 80);
      ccTxt.setLineWrap (true);
      JTextArea bodyTxt = new JTextArea (body.toString(), 10, 80);
      bodyTxt.setLineWrap (true);
      bodyTxt.setWrapStyleWord (true);

      JPanel mid = new JPanel (new GridLayout (0, 1)); 
      mid.add (ComponentTools.getTitledPanel (new JScrollPane (toTxt), "To (Employees)"));
      mid.add (ComponentTools.getTitledPanel (new JScrollPane (ccTxt), "CC (Approvers)"));
      mid.add (ComponentTools.getTitledPanel (new JScrollPane (bodyTxt), "Message"));
      
      JPanel panel = new JPanel (new BorderLayout());
      panel.add (top, BorderLayout.NORTH);
      panel.add (mid, BorderLayout.CENTER);
      
      JOptionPane.showMessageDialog (owner, panel, "Message Too Long", 
                                     JOptionPane.WARNING_MESSAGE);
   }
   
   public static void main (final String[] args)
   {
      // Authenticator auth = new SMTPAuthenticator ("jpaulkeller", "TBD");
      Authenticator auth = null;
      
      Mail mail = new Mail();
      // mail.setMailHost ("mail.oberonassociates.com"); // does not support SMTP
      // mail.setMailHost ("smtp.comcast.net"); // requires user/password
      
      // mail.setFrom ("jkeller@oberonassociates.com");
      mail.setFrom ("jpaulkeller@comcast.net");
      mail.setSubject ("EMailAPI Test: " + new Date());
      mail.addTo (mail.getFrom());
      mail.setBody ("This is a test of the <b>EMailAPI</b>");
      mail.setContentType ("text/html");
      mail.addAttachment (new File ("C:/pkgs/workspace/Utils/src/comms/EmailAPI.java"));
      
      EmailAPI.sendMessage (auth, mail);
   }
}
