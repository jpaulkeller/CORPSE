package db;

import gui.ComponentTools;
import gui.db.TableView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.Cursor;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;

public class MDBFile
{
   private File file;
   private Map<String, Table> tables = new HashMap<String, Table>();

   private Database db; // jackcess-1.1.18.jar
   
   /** Open the given MDB file. */

   public MDBFile (final File file)
   {
      this.file = file;
      try
      {
         db = Database.open (file);
         for (String name : db.getTableNames())
            tables.put (name, db.getTable (name));
      }
      catch (IOException x)
      {
         x.printStackTrace();
      }
   }
   
   public File getFile()
   {
      return file;
   }
   
   public Database getDatabase()
   {
      return db;
   }
   
   public Set<String> getTableNames()
   {
      return tables.keySet();
   }
   
   public Table getTable (final String tableName)
   {
      return tables.get (tableName);
   }
   
   public List<String> getFields (final String tableName)
   {
      List<String> names = new ArrayList<String>();
      Table table = tables.get (tableName);
      if (table != null)
      {
         List<Column> columns = table.getColumns();
         for (Column column : columns)
            names.add (column.getName());
      }
      return names;
   }
   
   public Model asTableModel (final String tableName)
   {
      Model model = null;
      
      Table table = tables.get (tableName);
      if (table != null)
      {
         model = new Model (tableName);

         List<Column> columns = table.getColumns();
         for (Column column : columns)
            model.addColumn (column.getName());
         
         Iterator<Map<String, Object>> iter = table.iterator();
         while (iter.hasNext())
            model.addRow (table.asRow (iter.next()));
      }
      
      return model;
   }

   // The fields argument should contain the desired header labels (in order)
   // mapped to the actual Column names (or null for place-holders).
   
   public Model asTableModel (final String tableName, final Map<String, String> fields)
   {
      Model model = null;
      
      Table table = tables.get (tableName);
      if (table != null)
      {
         model = new Model (tableName);
         
         List<String> columns = new ArrayList<String>();
         for (Entry<String, String> entry : fields.entrySet())
         {
            model.addColumn (entry.getKey());
            if (entry.getValue() != null) 
               columns.add (entry.getValue());
         }
         
         Object[] rowArray = new Object [fields.size()];
         Iterator<Map<String, Object>> iter = table.iterator (columns);
         while (iter.hasNext()) // for each row
         {
            Map<String, Object> rowMap = iter.next();
            int i = 0;
            for (Entry<String, String> entry : fields.entrySet())
            {
               String column = entry.getValue();
               rowArray [i++] = column != null ? rowMap.get (column) : null; 
            }
            model.addRow (rowArray);
         }
      }
      
      return model;
   }

   public void addRow (final String tableName, final Object... values)
   {
      Table table = tables.get (tableName);
      if (table != null)
      {
         try
         {
            table.addRow (values);
         }
         catch (IOException x)
         {
            x.printStackTrace();
         }
      }
   }
   
   /** Delete the first row where the given field contains the given value. */
   
   public void deleteRow (final String tableName,
                          final String field, final Object value)
   {
      Table table = tables.get (tableName);
      if (table != null)
      {
         List<String> fieldList = new ArrayList<String>();
         fieldList.add (field);
         
         try
         {
            Cursor cursor = Cursor.createCursor (table);
            while (cursor.moveToNextRow())
            {
               if (!cursor.isCurrentRowDeleted())
               {
                  Map<String, Object> row = cursor.getCurrentRow (fieldList);
                  if (row.get (field).equals (value))
                  {
                     cursor.deleteCurrentRow();
                     break;
                  }
               }
            }
         }
         catch (IOException x)
         {
            x.printStackTrace();
         }
      }
   }
   
   public static void expose (final File file)
   {
      try
      {
         Database db = Database.open (file);
         Set<String> tables = db.getTableNames();
         for (String table : tables)
         {
            Table tbl = db.getTable (table);
            System.out.println (tbl);
            System.out.println();
         }
      }
      catch (IOException x)
      {
         x.printStackTrace();
      }
   }
   
   public static void main (final String[] args)
   {
      String path = "C:/pkgs/workspace/StressTest/StressTest/MsgDataBase.mdb";
      File file = new File (path);
      MDBFile.expose (file);
      System.out.println();
      
      MDBFile mdb = new MDBFile (file);
      Model model = mdb.asTableModel ("tblMessages");
      if (model != null)
      {
         ComponentTools.setDefaults();
         TableView.show (model);
      }
   }
}
