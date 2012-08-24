package comms;

import java.io.IOException;
import java.net.Socket;

public final class Ping
{
   private Ping() { } // prevent instantiation
   
   public static void ping (final String host, final int port)
   {
      Socket socket = null;
      
      long stop, start = System.currentTimeMillis();
      try
      {
         socket = new Socket (host, port);
         stop = System.currentTimeMillis();

         // echo test for port 7
         // InputStreamReader isr = new InputStreamReader (socket.getInputStream());
         // BufferedReader br = new BufferedReader (isr);
         // PrintStream ps = new PrintStream (socket.getOutputStream());
         // ps.println ("echo");
         // String echo = br.readLine();
         // if (echo != null && echo.equals ("echo"))
         
         long delay = stop - start;
         System.out.println ("Connect to " + host + ":" + port + " - " +
                             delay + " milliseconds");
      }
      catch (IOException x)
      {
         x.printStackTrace();
         System.out.println ("Connect to " + host + ":" + port + " failed");
      }
      finally
      {
         if (socket != null)
            try { socket.close(); } catch (IOException x) { }
      }
   }
   
   public static void main (final String[] args)
   {
      String host = args.length > 0 ? args[0] : "192.168.3.136";
      int port = args.length > 0 ? Integer.parseInt (args[1]) : 50050;
      ping (host, port);
   }
}
