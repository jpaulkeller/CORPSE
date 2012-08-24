package comms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Observable;
import java.util.Observer;

public class SocketServer extends Observable implements Runnable
{
   private String name;
   private int port;
   
   private Thread runner;
   private ServerSocket server;
   private Socket socket;
   private boolean shouldStop;
   
   public SocketServer (final String name, final int port)
   {
      this.name = name;
      this.port = port;
   }
   
   public synchronized void start() throws IOException
   {
      if (runner == null)
      {
         server = new ServerSocket (port);
         runner = new Thread (this, name);
         runner.start();
      }
   }

   public synchronized void stop()
   {
      if (server != null)
      {
         shouldStop = true;
         runner.interrupt();
         runner = null;
         try
         {
            server.close();
            server = null;
         }
         catch (IOException x)
         {
            x.printStackTrace (System.err);
         }
      }
   }

   public synchronized void run()
   {
      while (!shouldStop)
      {
         try
         {
            socket = server.accept(); // block
            String line = read();
            if (line != null)
               dispatch (line);
         }
         catch (Exception x)
         {
            System.err.println ("SocketServer run: " + x);
            x.printStackTrace (System.err);
         }
         finally
         {
            if (socket != null)
               try { socket.close(); } catch (IOException x) { System.err.println (x); }
         }
      }
   }

   private String read() throws IOException
   {
      String request = null;
      if (socket != null)
      {
         InputStream stream = socket.getInputStream();
         InputStreamReader isr = new InputStreamReader (stream);
         BufferedReader br = new BufferedReader (isr);
         request = br.readLine();
      }
      return request; 
   }
   
   private void dispatch (final String request)
   {
      setChanged();
      if (countObservers() > 0)
         notifyObservers (request);
      else
         System.err.println (request);
      clearChanged();
   }

   public static void main (final String[] args)
   {
      int port = 8992;
      if (args.length > 0 && args[0] != null)
         port = Integer.parseInt (args[0]);

      try
      {
         SocketServer server = new SocketServer ("SocketServer", port);
         server.addObserver (new Observer() {
            public void update (final Observable o, final Object request)
            {
               System.out.println ("Server received: " + request);
            }
         });
         server.start();
         System.out.println ("SocketServer listening on port: " + port);

         SocketClient client = new SocketClient ("localhost", port);
         String request = "hello";
         System.out.println ("Client sending: " + request);
         client.write (request + "\n");
         client.close();
      }
      catch (IOException x)
      {
         x.printStackTrace (System.err);
      }
   }
}
