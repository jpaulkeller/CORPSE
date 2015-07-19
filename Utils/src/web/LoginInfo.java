package web;

public class LoginInfo
{
   private String host;
   private int port;
   private String user;
   private String password;

   public LoginInfo()
   {
   }
   
   public LoginInfo (String host, int port, String user, String password)
   {
      this.host = host;
      this.port = port;
      this.user = user;
      this.password = password;
   }
      
   public String getHost()     { return host; }
   public int    getPort()     { return port; }
   public String getUser()     { return user; }
   public String getPassword() { return password; }

   @Override
   public String toString()
   {
      return super.toString() +
         ": host=" + host + ", port=" + port +
         ", user=" + user + ", password=" + password;
   }
}
