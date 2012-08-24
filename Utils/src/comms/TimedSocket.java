package comms;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.Charset;

import utils.Utils;

// Original version by David Reilly, for JavaWorld

/**
 * This class offers a timeout feature on socket connections. A maximum length
 * of time allowed for a connection can be specified, along with a host and
 * port.
 */

public class TimedSocket
{
   // Polling delay for socket checks (in milliseconds)
   private static final int POLL_DELAY = 100;

   private String host;
   private int port;
   private InetAddress addr;
   private int timeout;
   private Socket socket;
   private Charset charset; // optional charset for string-to-byte encoding
   
   public static Socket getSocket (final String host, final int port,
                                   final int timeoutMilliseconds)
   throws IOException
   {
      TimedSocket ts = new TimedSocket (host, port, timeoutMilliseconds);
      ts.connect();
      return ts.socket;
   }
   
   public TimedSocket (final String host, final int port,
                       final int timeoutMilliseconds)
   {
      this.host = host;
      this.port = port;
      this.timeout = timeoutMilliseconds;
   }
   
   public Socket connect() throws IOException
   {
      this.addr = InetAddress.getByName (host);

      // Create a new socket thread, and start it running
      SocketThread st = new SocketThread();
      st.start();

      int timer = 0;

      for (;;)
      {
         // Check to see if a connection is established
         if (st.isConnected())
         {
            socket = st.getSocket();
            break;
         }

         // Check to see if an error occurred
         if (st.isError())
            throw (st.getException()); // No connection could be established

         Utils.sleep (POLL_DELAY);
         timer += POLL_DELAY;
         
         if (timer > timeout) // Check to see if time limit exceeded
            throw new InterruptedIOException
            ("Socket timeout connecting to " + host + ":" + port + 
             " (exceeded " + timeout + " milliseconds)");
      }

      return socket;
   }
   
   public void setCharset (final String name)
   {
      this.charset = name != null ? Charset.forName (name) : null;
   }

   public void write (final String data) throws IOException
   {
      OutputStream stream = socket.getOutputStream();
      byte[] bytes = charset != null ? data.getBytes (charset) : data.getBytes();
      stream.write (bytes);
      stream.flush();
   }

   public String read() throws IOException
   {
      StringBuilder reply = new StringBuilder();
      
      InputStream inStream = socket.getInputStream();
      BufferedInputStream bis = new BufferedInputStream (inStream);
      byte[] bytes = new byte[1024];

      // TBD: thread this off, like connect?
      Utils.sleep (POLL_DELAY); // TBD
      
      while (bis.available() > 0)
      {
         int replyLength = bis.read (bytes);
         String chunk = new String (bytes, 0, replyLength);
         reply.append (chunk);
         Utils.sleep (POLL_DELAY); // TBD
      }
      
      return reply.toString();
   }
   
   public void close()
   {
      if (socket != null)
      {
         try
         {
            socket.close();
         }
         catch (IOException x) { }
      }
   }
   
   // Inner class for establishing a socket thread within another thread, 
   // to prevent blocking.
   
   class SocketThread extends Thread
   {
      private volatile Socket connection = null; // remote host
      private IOException exception = null; // for connection errors

      @Override
      public void run()
      {
         Socket sock = null;

         try
         {
            // Connect to a remote host - BLOCKING I/O
            sock = new Socket (addr, port);
         }
         catch (IOException x)
         {
            exception = x;
            return;
         }

         // If socket constructor returned without error,
         // then connection finished
         connection = sock;
      }

      public boolean isConnected()
      {
         return connection != null;
      }

      public Socket getSocket()
      {
         return connection;
      }

      public boolean isError()
      {
         return exception != null;
      }

      public IOException getException()
      {
         return exception;
      }
   }
   
   public static void main (final String[] args) throws Exception
   {
      String host = "192.168.3.136"; // good
      int port = 50050; // for MoVer
      // int port = 80;
      int timeout = 2000; // milliseconds

      TimedSocket ts = null;
      try
      {
         System.out.print ("Connecting to " + host + ":" + port + " ... ");
         ts = new TimedSocket (host, port, timeout);
         ts.connect();
         System.out.println ("Connected");

         StringBuilder cmd = new StringBuilder();
         cmd.append ("<APICOMMANDS>");
         cmd.append ("<DRIVERLIST><DRIVER/></DRIVERLIST>");
         cmd.append ("<RADIOLIST><RADIO/></RADIOLIST>");
         cmd.append ("<VER>Request</VER>");
         cmd.append ("</APICOMMANDS>");
         System.out.print ("Requesting API");
         ts.write (cmd.toString());
         System.out.println();
         
         System.out.print ("Reading... ");
         String s = ts.read();
         System.out.println (s.length() + " bytes read:");
         System.out.println (s);
      }
      catch (IOException x)
      {
         System.out.println (x);
      }
      finally
      {
         if (ts != null)
            ts.close();
         System.out.print ("Connection closed");
      }
   }
}
