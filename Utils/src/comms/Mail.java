package comms;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Mail
{
   private String mailHost;
   private String from;
   private String subject;
   private Collection<String> to;
   private Collection<String> cc;
   private Collection<String> bcc;
   private String contentType; // e.g. "text/html"
   private CharSequence body;
   private List<File> attachments;
   
   public String getMailHost()
   {
      return mailHost;
   }
   public void setMailHost (final String mailHost)
   {
      this.mailHost = mailHost;
   }
   
   public String getFrom()
   {
      return from;
   }
   public void setFrom (final String from)
   {
      this.from = from;
   }
   
   public String getSubject()
   {
      return subject;
   }
   public void setSubject (final String subject)
   {
      this.subject = subject;
   }
   
   public Collection<String> getTo()
   {
      return to;
   }
   public void addTo (final String addr)
   {
      if (to == null)
         to = new ArrayList<String>();
      if (addr != null)
         to.add (addr);
   }
   
   public Collection<String> getCC()
   {
      return cc;
   }
   public void addCC (final String addr)
   {
      if (cc == null)
         cc = new ArrayList<String>();
      if (addr != null)
         cc.add (addr);
   }
   
   public Collection<String> getBCC()
   {
      return bcc;
   }
   public void addBCC (final String addr)
   {
      if (bcc == null)
         bcc = new ArrayList<String>();
      if (addr != null)
         bcc.add (addr);
   }
   
   public String getContentType()
   {
      return contentType;
   }
   public void setContentType (final String contentType)
   {
      this.contentType = contentType;
   }
   
   public String getBody()
   {
      return body != null ? body.toString() : null;
   }
   public void setBody (final CharSequence body)
   {
      this.body = body;
   }
   
   public void addAttachment (final File attachment)
   {
      if (attachments == null)
         attachments = new ArrayList<File>();
      if (attachment != null)
         attachments.add (attachment);
   }
   public List<File> getAttachments()
   {
      return attachments;
   }
}
