package comms;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import utils.Utils;

public class SocketClient
{
   private Socket socket;
   
   public SocketClient (final String host, final int port) throws IOException
   {
      socket = new Socket (host, port);
   }
   
   public void write (final String data) throws IOException
   {
      OutputStream stream = socket.getOutputStream();
      stream.write (data.getBytes());
      stream.flush();
   }

   public String read() throws IOException
   {
      StringBuilder reply = new StringBuilder();
      
      InputStream inStream = socket.getInputStream();
      BufferedInputStream bis = new BufferedInputStream (inStream);
      byte[] bytes = new byte[1024];

      Utils.sleep (100); // TBD
      
      while (bis.available() > 0)
      {
         int replyLength = bis.read (bytes);
         String chunk = new String (bytes, 0, replyLength);
         reply.append (chunk);
         Utils.sleep (100); // TBD
      }
      
      return reply.toString();
   }
   
   public void close()
   {
      try
      {
         socket.close();
      }
      catch (IOException x)
      {
         x.printStackTrace (System.err);
      }
   }
   
   public static void main (final String[] args)
   {
      SocketClient client = null;
      try
      {
         System.out.print ("Connecting... ");
         client = new SocketClient ("192.168.3.136", 50050);
         System.out.println ("Connected");
         
         String cmd = "<APICOMMANDS><RADIOLIST/><DRIVERLIST/></APICOMMANDS>";
         System.out.print ("Requesting API: " + cmd);
         client.write (cmd);
         System.out.println();
         
         System.out.print ("Reading... ");
         String s = client.read();
         System.out.println (s.length() + " bytes read:");
         System.out.println (s);
      }
      catch (IOException x)
      {
         x.printStackTrace();
      }
      finally
      {
         if (client != null)
            client.close();
         System.out.print ("Connection closed");
      }
   }
}
