package model.table;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;

import db.Model;
import file.FileUtils;
import gui.ComponentTools;
import gui.comp.FileChooser;
import gui.db.TableView;

public class CSVTable extends Model
{
   // TBD: deal with quoted values
   private static final Pattern FIRST_TOKEN = Pattern.compile ("([^,]*).*");
   private static final Pattern NEXT_TOKEN = Pattern.compile (",([^,]*)");
   
   private File file;
   
   public CSVTable (final File file)
   {
      super (FileUtils.getNameWithoutSuffix (file));
      this.file = file;
   }
   
   public void load() throws IOException
   {
      if (file != null && file.exists())
      {
         BufferedReader br = null;
         try
         {
            FileInputStream fis = new FileInputStream (file);
            InputStreamReader isr = new InputStreamReader (fis);
            br = new BufferedReader (isr);
            
            // assume the first line is a header
            String line = br.readLine();
            Matcher m = FIRST_TOKEN.matcher (line);
            if (m.matches())
            {
               addColumn (m.group (1));
               m = NEXT_TOKEN.matcher (line);
               while (m.find())
                  addColumn (m.group (1));
            }
            
            while ((line = br.readLine()) != null)
            {
               m = FIRST_TOKEN.matcher (line);
               if (m.matches())
               {
                  Vector<String> row = new Vector<String> (m.groupCount());
                  row.add (m.group (1));
                  m = NEXT_TOKEN.matcher (line);
                  while (m.find())
                     row.add (m.group (1));
                  addRow (row);
               }
            }
         }
         finally
         {
            FileUtils.close (br);
         }
      }
   }

   public static void main (final String[] args)
   {
      ComponentTools.setDefaults();
      String dir = "C:/pkgs/workspace/Oberon/data/etass";
      FileChooser fc = new FileChooser ("Select CSV File", dir);
      fc.setRegexFilter (".+[.]csv", "Comma-separated Values (*.csv) files");
      
      if (fc.showOpenDialog (null) == JFileChooser.APPROVE_OPTION)
      {
         File file = fc.getSelectedFile();
         if (file != null)
         {
            CSVTable model = new CSVTable (file);
            try
            {
               model.load();
               TableView.show (model);
            }
            catch (IOException x)
            {
               x.printStackTrace();
            }
         }
      }
   }
}
