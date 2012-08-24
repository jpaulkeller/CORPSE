package file;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFileChooser;

import gui.ComponentTools;
import gui.comp.FileChooser;

public final class Rename
{
   private static final Map<String, String> PARTS = new HashMap<String, String>();
   static
   {
      // PARTS.put ("", "");
   }
   
   private static final Map<String, String> NAMES = new HashMap<String, String>();
   static
   {
      // NAMES.put ("", "");
   }
   
   private Rename() { }
   
   private static void renameAll (final File entry)
   {
      if (entry.isFile())
         renameFile (entry);
      else if (entry.isDirectory())
      {
         System.out.println (entry);
         for (File child : entry.listFiles())
            renameAll (child); // recurse into sub-directories
      }
   }

   private static void renameFile (final File file)
   {
      String suffix = FileUtils.getSuffix (file);
      if (suffix != null && (suffix.equals ("gif") || suffix.equals ("png")))
      {
         String name = FileUtils.getNameWithoutSuffix (file);
         String newName = getNewName (name);
         if (name.equals (newName))
            return;
         
         String newPath = file.getParent() + File.separator + newName + "." + suffix;
         File newFile = new File (newPath);
         if (!name.equalsIgnoreCase (newName))
         {
            if (file.renameTo (newFile))
               System.out.println ("  " + name + " > " + newName + "." + suffix);
            else
               System.err.println ("Conflict with " + newName + "; ignoring: " + file);
         }
         else if (FileUtils.renameSafe (file, newFile)) 
            System.out.println ("  " + name + " > " + newName + "." + suffix);
      }
   }

   private static String getNewName (final String name)
   {
      String newName = NAMES.get (name);
      if (newName == null)
         newName = name;
      for (String part : PARTS.keySet())
         if (newName.contains (part))
            newName = newName.replace (part, PARTS.get (part));
      return newName;
   }
   
   public static void main (final String[] args)
   {
      ComponentTools.setDefaults();
      
      String path = "C:/pkgs/workspace/Resources/icons";
      FileChooser fc = new FileChooser ("Select Directory", path);
      fc.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY);
      if (fc.showOpenDialog (null) == JFileChooser.APPROVE_OPTION)
      {
         File dir = fc.getSelectedFile();
         if (dir != null)
            renameAll (dir);
      }      
   }
}
