package lotro.web;

import java.io.File;

public final class Dropbox
{
   public static final String CHAR_PATH = Dropbox.get().getPath ("/palantiri.chr");
   
   private static final long serialVersionUID = 1L;
   
   private static Dropbox singleton;
   
   private String path;
   
   private Dropbox (final String path)
   {
      this.path = path;
   }
   
   public static Dropbox get()
   {
      if (singleton == null)
      {
    	 String defaultPath = System.getProperty ("user.home") + "/My Documents/My Dropbox/Public/Palantiri";
    	 String homePath = "D:/Users/J/Dropbox/Public/Palantiri";
    	 if (new File (defaultPath).exists())
            singleton = new Dropbox (defaultPath);
    	 else if (new File (homePath).exists())
            singleton = new Dropbox (homePath);
         else
            singleton = new Dropbox ("http://dl.dropbox.com/u/86704/Palantiri");
      }
      return singleton;
   }
   
   public boolean isRemote()
   {
      return path.startsWith ("http");
   }
   
   public String getPath()
   {
      return path;
   }
   
   public String getPath (final String sub)
   {
      String subPath = path + sub;
      if (isRemote())
         subPath = subPath.replace (" ", "%20");
      else
         subPath = subPath.replace ("%20", " ");
      return subPath;
   }
   
   public static void main (final String[] args)
   {
	   Dropbox dropbox = Dropbox.get();
	   System.out.println (dropbox.getPath());
   }
}
