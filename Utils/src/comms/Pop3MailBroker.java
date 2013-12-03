package comms;

// pop3.jar
import com.ibm.network.mail.base.Header;
import com.ibm.network.mail.base.MimeMessage;
import com.ibm.network.mail.pop3.event.MessageEvent;
import com.ibm.network.mail.pop3.event.MessageListener;
import com.ibm.network.mail.pop3.event.StatusEvent;
import com.ibm.network.mail.pop3.event.StatusListener;
import com.ibm.network.mail.pop3.protocol.CoreProtocolBean;

/**
 * This class controls the retrieval of mail messages from a registered user
 * account on a POP3 mail server. It establishes a connection with the server
 * and brokers MessageEvents and StatusEvents to registered objects.
 */
public class Pop3MailBroker
{
   private CoreProtocolBean cpb;
   private String host;
   private int port;
   private String user;
   private String pswd;
   private volatile boolean fetch;

   /**
    * @param hostName
    *           POP3 server host name or IP address
    * @param port
    *           POP3 server port. Pass in -1 to get the default value of 110 as
    *           the port number.
    * @param loginName
    *           login name of the user account on the POP3 mail server.
    * @param password
    *           login password
    * 
    * @throws IllegalArgumentException
    *            if the server host or user login name or password are blank.
    */
   public Pop3MailBroker (final String hostName, final int port, 
                          final String loginName, final String password)
   {
      host = hostName;
      this.port = port;
      user = loginName;
      pswd = password;
      setupAuthentication();
   }

   /**
    * Creates the object that is used to communicate with the POP3 server. This
    * information is used to authenticate a user connection and defines what the
    * server should do with downloaded messages (e.g. delete them from the
    * server or load them into a local file).
    * 
    * @throws IllegalArgumentException
    *            if the server host or user login name or password are blank.
    */
   private void setupAuthentication()
   {
      cpb = new CoreProtocolBean();
      if (port != -1)
         cpb.setPOP3ServerPort (port);
      cpb.setRememberPassword (false);
      cpb.setSaveIncomingMessages (false);
      cpb.setSocksified (false);
      cpb.setPOP3ServerHost (host);
      cpb.setUserOptions (user, pswd, true, false);
   }

   public void stopFetch()
   {
      fetch = false;
   }

   /**
    * Retrieves messages from the POP3 server. Objects that have registered
    * themselves as MessageListeners with this object will be notified as
    * messages are retrieved.
    * 
    * @param fetchDelay
    *           If zero, this method will attempt to retrieve mail from the
    *           POP3 server one time.  Otherwise, this method goes into a 
    *           continuous mail fetching loop sleeping for the given interval
    *           (in milliseconds). You may want to perform mail fetching on a
    *           separate thread.
    */
   public void fetchMail (final int interval)
   {
      fetch = interval > 0;

      do
      {
         try
         {
            if (cpb.isReady()) // catches fetch in progress
               cpb.receiveMessage();
            if (fetch)
               Thread.sleep (interval);
         }
         catch (InterruptedException x)
         {
            x.printStackTrace();
         }
      }
      while (fetch);
   }
   
   public void spawnFetch (final int interval)
   {
      Thread thread = new Thread (new Runnable()
      {
         @Override
         public void run()
         {
            fetchMail (interval);
         }
      });
      thread.start();      
   }

   /**
    * Controls whether messages will be deleted from the mail server after they
    * are received.
    * 
    * @param flag
    *           false if they are to be deleted after they are received.
    */
   public void setLeaveMessagesOnServer (final boolean flag)
   {
      cpb.setLeaveMessagesOnServer (flag);
   }

   /**
    * Returns true if the messages will remain on the server after they are
    * received. False if they are to be deleted.
    */
   public boolean isLeaveMessagesOnServer()
   {
      return cpb.isLeaveMessagesOnServer();
   }

   /**
    * Registers the indicated listener object as one that will be notified when
    * a piece of mail is retrieved from the server.
    * 
    * @see com.ibm.network.mail.pop3.event.MessageListener
    */
   public void addMsgListener (final MessageListener ml)
   {
      cpb.addMessageListener (ml);
   }

   /**
    * Removes the listener object so it will no longer be notified when mail is
    * retrieved.
    */
   public void removeMsgListener (final MessageListener ml)
   {
      cpb.removeMessageListener (ml);
   }

   /**
    * Registers the indicated listener object as one that will be notified when
    * a change in operation status occurs.
    * 
    * @see com.ibm.network.mail.pop3.event.StatusListener
    */
   public void addStatusListener (final StatusListener sl)
   {
      cpb.addStatusListener (sl);
   }

   /**
    * Removes the listener object so it will no longer be notified when a change
    * in operation status occurs.
    */
   public void removeStatusListener (final StatusListener sl)
   {
      cpb.removeStatusListener (sl);
   }
   
   public static void main (final String[] args)
   {
      Pop3MailBroker pop3 = new Pop3MailBroker ("localhost", 110, "JK", "Password");
      
      pop3.addStatusListener (new StatusListener() 
      {
         @Override
         public void processStatus (final StatusEvent e)
         {
            System.out.println ("> Status: " + e.getStatusString());
         }
      });
      
      pop3.addMsgListener (new MessageListener()
      {
         @Override
         public void messageReceived (final MessageEvent e)
         {
            MimeMessage message = e.getMessage();
            Header header = message.getHeader();
            String from = header.getField (Header.FROM);
            String subject = header.getField (Header.SUBJECT);
            System.out.println ("Message: " + subject + " (from " + from + ")");
         }
      });
      
      pop3.setLeaveMessagesOnServer (true);
      pop3.fetchMail (5000);
   }
}
