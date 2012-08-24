package model.table;

import java.awt.Color;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Date;
import java.util.Vector;

import javax.swing.JFileChooser;

import str.Token;
import utils.DateUtils;
import web.HTMLUtils;
import db.Model;
import file.FileUtils;
import gui.ComponentTools;
import gui.TranslucentColor;
import gui.comp.FileChooser;
import gui.db.TableView;

/**
 * This class provides a disk-based table model (backed by a file).
 */

public class FileTable extends Model
{
   private static final String ENCODING = "UTF8";

   private File file;
   
   public FileTable (final File file, final String name)
   {
      super (name);
      setFile (file);
   }
   
   public void setFile (final File file)
   {
      this.file = file;
   }
   
   public File getFile()
   {
      return file;
   }

   public void load()
   {
      if (file.exists())
      {
         FileInputStream fis = null;
         String line = null;
         try
         {
            fis = new FileInputStream (file);
            InputStreamReader isr = new InputStreamReader (fis, ENCODING);
            BufferedReader br = new BufferedReader (isr);
            
            while ((line = br.readLine()) != null)
               if (line.length() > 0)
                  readRow (line);
         }
         catch (Exception x)
         {
            System.err.println ("File: " + file);
            System.err.println ("Line: " + line);
            x.printStackTrace (System.err);
         }
         finally
         {
            FileUtils.close (fis);
         }
      }
   }

   private void readRow (final String line)
   {
      Vector<Object> row = new Vector<Object>();

      int col = 0;
      for (String s : Token.tokenizeQuoted (line, "\"", ","))
      {
         s = HTMLUtils.decode (s);
         Class<?> type = getColumnClass (col);
         Object value;
         if (type == String.class)
            value = s;
         else if (s == null || s.equals (""))
            value = null;
         else if (type == Integer.class)
            value = Integer.parseInt (s);
         else if (type == Date.class)
            value = DateUtils.parse (s, null);
         else if (type == File.class)
            value = new File (s);
         else if (type == Color.class)
            value = TranslucentColor.parse (s, 1.0f);
         else
            value = s;
         row.add (value);
         col++;
      }
      
      addRow (row);   
   }
   
   public void save()
   {
      PrintStream out = null;
      try
      {
         FileOutputStream fos = new FileOutputStream (file); 
         BufferedOutputStream buf = new BufferedOutputStream (fos);
         out = new PrintStream (buf, true, ENCODING);

         for (int r = 0; r < getRowCount(); r++)
         {
            for (int c = 0; c < getColumnCount(); c++)
            {
               if (c > 0)
                  out.print (",");
               Object value = getValueAt (r, c);
               String s = value != null ? value.toString() : "";
               out.print ("\"" + HTMLUtils.encode (s) + "\"");
            }
            out.println();
         }
         
         out.flush();
      }
      catch (Exception x)
      {
         System.err.println ("File: " + file);
         x.printStackTrace (System.err);
      }
      finally
      {
         FileUtils.close (out);
      }
   }

   public static void main (final String[] args)
   {
      ComponentTools.setDefaults();
      String home = System.getProperty ("user.home");
      String dir = home + "/Local Settings/Application Data/Oberon"; 
      FileChooser fc = new FileChooser ("Select File Table", dir);
      fc.setRegexFilter (".+[.]log", "File Tables (*.log) files");
      
      if (fc.showOpenDialog (null) == JFileChooser.APPROVE_OPTION)
      {
         File file = fc.getSelectedFile();
         if (file != null)
         {
            FileTable table = new FileTable (file, file.getPath());
            table.load();
            TableView.show (table);
         }
      }
   }
}
