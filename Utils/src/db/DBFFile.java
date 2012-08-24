package db;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.linuxense.javadbf.DBFException;
import com.linuxense.javadbf.DBFField;
import com.linuxense.javadbf.DBFReader;
import com.linuxense.javadbf.DBFWriter;

/**
 * This class reads and writes DBF files.
 *  
 * http://sarovar.org/docman/view.php/32/23/javadbf-tutorial.html
 * 
 *    FIELD_TYPE_C   67 - String (character)
 *    FIELD_TYPE_D   68 - Date
 *    FIELD_TYPE_F   70 - 
 *    FIELD_TYPE_L   76 - 
 *    FIELD_TYPE_M   77 - 
 *    FIELD_TYPE_N   78 - Double (numeric)
 */

public class DBFFile
{
   private File file;
   private InputStream in;
   private DBFReader reader; // javadbf.jar
   private DBFWriter writer;
   private boolean fieldsSet;
   private Map<String, DBFField> fields = new LinkedHashMap<String, DBFField>();
   private List<String> fieldNames = new ArrayList<String>();

   /** Connect to an existing DBF file for reading. */

   public DBFFile (final File file)
   {
      this.file = file;
   }
   
   public void openToRead() throws IOException
   {
      try
      {
         in = new FileInputStream (file.getPath());
         reader = new DBFReader (in);
      
         int fieldCount = reader.getFieldCount();
         for (int f = 0; f < fieldCount; f++)
            addField (reader.getField (f));
      }
      catch (DBFException x)
      {
         System.out.println ("DBFException: " + x.getMessage());
      }
   }
   
   public void openToWrite() throws IOException
   {
      if (reader == null)
         writer = new DBFWriter (file);
      else
         throw new IOException ("File open for reading: " + file);
   }

   public Collection<DBFField> getFields()
   {
      return fields.values();
   }
   
   public Map<String, Object> readNext()
   {
      Map<String, Object> map = null;

      try
      {
         Object[] row = reader.nextRecord();
         if (row != null)
         {
            map = new LinkedHashMap<String, Object>();
            int f = 0;
            for (Object value : row)
               map.put (fieldNames.get (f++), value);
         }
      }
      catch (DBFException x)
      {
         System.out.println ("DBFException readNext: " + x.getMessage());
      }

      return map;
   }

   public static String getString (final Map<String, Object> map, final String field)
   {
      String value = null;
      Object obj = map.get (field);
      if (obj != null)
         value = obj.toString().trim();
      return value;
   }
   
   public static Date getDate (final Map<String, Object> map, final String field)
   {
      Date date = null;
      Object obj = map.get (field);
      if (obj instanceof Date)
         date = (Date) obj;
      return date;
   }

   public static double getDouble (final Map<String, Object> map, final String field)
   {
      double value = 0;
      Object obj = map.get (field);
      if (obj instanceof Number)
         value = ((Number) obj).doubleValue();
      return value;
   }
   
   public static int getInt (final Map<String, Object> map, final String field)
   {
      int value = 0;
      Object obj = map.get (field);
      if (obj instanceof Number)
         value = ((Number) obj).intValue();
      return value;
   }
   
   public void addTextField (final String name, final int length)
   {
      addField (name, DBFField.FIELD_TYPE_C, length);
   }

   public void addField (final String name, final byte type, final int length)
   {
      DBFField field = new DBFField();
      field.setName (name);
      field.setDataType (type);
      field.setFieldLength (length);
      addField (field);
   }
   
   public void addField (final DBFField field)
   {
      fields.put (field.getName(), field);
      fieldNames.add (field.getName());
   }
   
   public DBFField getField (final String name)
   {
      return fields.get (name);
   }
   
   public boolean addRecord (final Map<String, Object> row)
   {
      // The setFields() method must be called exactly once, after the
      // fields have been added, but before the records are added.
      if (!fieldsSet)
      {
         try
         {
            writer.setFields (fields.values().toArray (new DBFField [fields.size()]));
            fieldsSet = true;
         }
         catch (DBFException x)
         {
            System.out.println ("DBFFile setFields: " + x);
         }
      }

      Object[] rowData = new Object [getFields().size()];
      int column = 0;
      for (DBFField f : getFields())
         rowData [column++] = row.get (f.getName());

      try
      {
         writer.addRecord (rowData);
         return true;
      }
      catch (DBFException x)
      {
         System.out.println ("DBFFile addRecord: " + x);
      }

      return false;
   }
   
   public void close()
   {
      try
      {
         if (reader != null)
            in.close();
         else if (writer != null)
            writer.write();
         /*
            FileOutputStream out = new FileOutputStream (file.getPath());
            writer.write (out);
            out.close();
         */
      }
      catch (IOException x)
      {
         System.err.println ("IOException: " + x.getMessage());
         x.printStackTrace (System.err);
      }
   }

   /**
    * Static convenience method to read the file header (to
    * determine the fields). */

   public static List<String> getFieldNames (final File f) throws IOException
   {
      List<String> fieldNames = new ArrayList<String>();

      if (f.exists())
      {
         DBFFile dbf = new DBFFile (f);
         dbf.openToRead();
         for (DBFField field : dbf.getFields())
            fieldNames.add (field.getName());
         dbf.close();
      }

      return fieldNames;
   }
   
   public static void main (final String[] args) throws IOException
   {
      // test reading

      DBFFile dbf = new DBFFile (new File (args[0]));
      dbf.openToRead();

      Map<String, Object> row;
      while ((row = dbf.readNext()) != null)
      {
         for (Map.Entry<String, Object> entry : row.entrySet())
         {
            Object value = entry.getValue();
            System.out.print ("   " + entry.getKey() + " = " + value);
            if (value != null)
               System.out.print (" (" + value.getClass().getName() + ")");
            System.out.println();
         }
         System.out.println();
      }

      dbf.close();

      // test writing
      
      dbf = new DBFFile (new File (args[0] + ".copy"));
      dbf.openToWrite();
      
      dbf.addField ("Field 1", DBFField.FIELD_TYPE_C, 20);
      dbf.addField ("Field 2", DBFField.FIELD_TYPE_C, 20);

      row = new HashMap<String, Object>();
      for (int r = 0; r < 5; r++)
      {
         for (int c = 0; c < 2; c++)
            row.put ("Field " + (c + 1), "Value " + r + "-" + c);
         dbf.addRecord (row);
      }

      dbf.close();
   }
}
