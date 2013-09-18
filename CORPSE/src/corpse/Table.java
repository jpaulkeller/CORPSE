package corpse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.table.DefaultTableModel;

import file.FileUtils;

public class Table extends ArrayList<String>
{
   private static final long serialVersionUID = 1L;
   
   private static final String ENCODING = "UTF8";
   private static final DecimalFormat LINE_NUM = new DecimalFormat ("0000"); 
   
   // TODO: Fix subsets to deal with included tables (such as Color.tbl)
   // TODO: weight based on quantity (without expanding)
   // TODO: filter as expanding
   // TODO: track quantity by line? then use L# for those quantities?
   
   // # {Table}
   private static final Pattern WEIGHTED_LINE = Pattern.compile ("(\\d+) +(.+)");
   // #-# {Table}
   private static final Pattern RANGE_LINE = Pattern.compile ("(\\d+)-(\\d+) +(.+)");
   // + {Table} (or "text{Table}text", but at most one {Table})
   private static final Pattern INCLUDED_TBL = 
         Pattern.compile ("[" + Macros.INCLUDE_CHAR + "] *([^{]*)\\{(.+)\\}([^{]*)");

   static SortedMap<String, Table> tables = new TreeMap<String, Table>();
   
   private File file;
   private String tableName;
   private List<String> title = new ArrayList<String>();
   private List<String> source = new ArrayList<String>();
   
   SortedMap<String, Column> columns = new TreeMap<String, Column>();
   SortedMap<String, Subset> subsets = new TreeMap<String, Subset>();
   int imported = 0; // total number of imported lines (includes weighted lines)
   
   // For example of subset usage, see the MADE_OF.TBL and COLOR.TBL.

   public static void populate (final File dir)
   {
      for (File f : dir.listFiles())
      {
         if (f.isDirectory() && !f.getName().startsWith ("."))
            populate (f);
         else if (f.isFile())
            new Table (f.getPath());
      }
   }
   
   public static Table getTable (final String name)
   {
      Table table = tables.get (name.toUpperCase());
      if (table != null)
      {
         if (table.isEmpty())
            table.importTable();
      }
      else
         System.err.println ("Table not yet loaded: " + name);
      
      return table;
   }
   
   public Table (final String path)
   {
      file = new File (path);
      tableName = FileUtils.getNameWithoutSuffix (file).toUpperCase();
      tables.put (tableName, this);
   }
   
   // for filtered tables
   public Table (final String name, final boolean unused)
   {
      // TODO
      tableName = name;
   }
   
   public String resolve (final String entry, final String filter)
   {
      String resolved = Macros.resolve (entry, filter); // recurse to support embedded tokens

      Matcher m;
      while ((m = Macros.TOKEN.matcher (resolved)).find())
      {
         System.err.println (file + " unsupported token: " + m.group (0));
         resolved = m.replaceFirst (TokenRenderer.INVALID_OPEN + m.group (1) + TokenRenderer.INVALID_CLOSE);
      }

      return resolved;
   }
   
   public String getColumn (final int index, final String columnName, final String filter)
   {
      String entry = get (index);
      if (entry != null)
      {
         entry = getColumnValue (entry, columnName);
         entry = resolve (entry, filter);
      }
      return entry;
   }

   Subset getSubset (final String subsetName)
   {
      String key = subsetName != null ? subsetName.toUpperCase() : tableName;
      Subset subset = subsets.get (key);
      if (subset == null && subsetName != null)
         System.err.println ("Missing " + file + " subset: [" + subsetName + "]");
      return subset;
   }

   private Column getColumn (final String columnName)
   {
      String key = columnName != null ? columnName.toUpperCase() : tableName;
      Column column = columns.get (key);
      if (column == null && columnName != null && !columnName.equals ("*"))
         System.err.println ("Missing " + file + " column: [" + key + "]");
      return column;
   }

   private String getColumnValue (final String unresolvedEntry, final String columnName)
   {
      String unresolved = unresolvedEntry;
      try
      {
         Column column = getColumn (columnName);
         if (column != null)
            unresolved = column.getValue (unresolvedEntry);
      }
      catch (Exception x)
      {
         System.err.println ("File: " + file);
         System.err.println ("Line: " + unresolvedEntry);
         System.err.println ("Column: " + columnName);
         x.printStackTrace (System.err);
      }
      return unresolved;
   }
   
   private void importTable()
   {
      String line = null;
      try
      {
         FileInputStream fis = new FileInputStream (file);
         InputStreamReader isr = new InputStreamReader (fis, ENCODING);
         BufferedReader br = new BufferedReader (isr);
         
         while ((line = br.readLine()) != null)
            parseLine (line);
         fis.close();
         validate();
      }
      catch (Exception x)
      {
         System.err.println ("File: " + file);
         System.err.println ("Line: " + line);
         x.printStackTrace (System.err);
      }
   }

   private void parseLine (final String line)
   {
      if (line.length() > 0)
      {
         Matcher m;
         if ((m = Column.COLUMN_FIXED.matcher (line)).matches())
            addColumn (m);
         else if ((m = Column.COLUMN_CSV.matcher (line)).matches())
            addColumn (m);
         else if (Subset.PATTERN.matcher (line).matches()) 
            addSubset (line);
         else if (line.startsWith (Macros.SOURCE_CHAR))
            source.add (line.substring (1).trim());
         else if (line.startsWith (Macros.TITLE_CHAR))
            title.add (line.substring (1).trim());
         else if (line.startsWith (Macros.COMMENT_CHAR))
            ; // do nothing
         else if (line.startsWith (Macros.FOOTNOTE_CHAR))
            ; // do nothing
         else if (line.startsWith (Macros.HEADER_CHAR))
            ; // do nothing
         else if (line.startsWith (Macros.SEPR_CHAR))
            ; // do nothing

         // Included tables are used to provide an even distribution of elements from multiple sub-tables of 
         // different sizes.  Each element has the same chance of selection.  See Site.tbl for an example.
         else if ((m = INCLUDED_TBL.matcher (line)).matches())
         {
            String name = m.group(2);
            if (Macros.DEBUG) System.out.println("INCLUDED: " + name + " into " + tableName);
            Table subTable = Table.getTable (name);
            for (String subLine : subTable)
            {
               add (m.group(1) + subLine + m.group(3));
               imported++;
            }
            /*
            int weight = subTable.size();
            for (int i = 0; i < weight; i++)
               add ("{" + name + "}");
               */
         }
         
         // Range lines are used to provide a random number of chances for a particular line.
         else if ((m = RANGE_LINE.matcher (line)).matches())
         {
            String text = m.group(3);
            if (Macros.DEBUG) System.out.println("RANGE: " + text + " into " + tableName);
            int from = Integer.parseInt (m.group (1));
            int to   = Integer.parseInt (m.group (2));
            for (int i = from; i <= to; i++)
            {
               add (text);
               imported++;
            }
         }
         
         // Weighted lines are used to provide a specified number of chances for a particular line.
         else if ((m = WEIGHTED_LINE.matcher (line)).matches())
         {
            String text = m.group(2);
            if (Macros.DEBUG) System.out.println("WEIGHTED: " + text + " into " + tableName);
            int weight = Integer.parseInt (m.group (1));
            for (int i = 0; i < weight; i++)
            {
               add (text);
               imported++;
            }
         }
         
         else if (line.trim().length() > 0) // ignore blank lines
            add (line);
      }
   }
   
   private void addColumn (final Matcher m)
   {
      Column column;
      if (m.groupCount() == 1)
         column = new Column (m.group (1), columns.size());
      else
         column = new Column (m, columns.size());
      columns.put (column.getName().toUpperCase(), column);
   }
   
   private void addSubset (final String line)
   {
      Subset subset = new Subset (this, line);
      subsets.put (subset.getName().toUpperCase(), subset);
   }
   
   public DefaultTableModel getModel()
   {
      DefaultTableModel model = new MyTableModel();
      
      /* TBD
      // clear the model
      while (model.getRowCount() > 0)
         model.removeRow (0);
      model.setColumnCount (0);
      */

      SortedSet<Column> sorted = new TreeSet<Column>(); // by order in file
      
      model.addColumn ("#");
      if (columns.isEmpty()) // just one column; each line is the value
         model.addColumn (tableName);
      else
      {
         sorted.addAll (columns.values());
         for (Column column : sorted)
            model.addColumn (column.getName());
      }

      int i = 1;
      for (String unresolvedEntry : this)
         addRow (model, sorted, i++, unresolvedEntry);
      
      return model;
   }
   
   private void addRow (final DefaultTableModel model, 
                        final SortedSet<Column> sorted,
                        final int i, final String entry)
   {
      // System.out.println ("Entry = [" + entry + "]"); // TBD
      Vector<Object> row = new Vector<Object>();
      row.add (i);
      
      if (sorted.isEmpty())
         row.add (resolve (entry.trim(), null));
      else for (Column column : sorted)
      {
         // if (i == 1) System.out.println ("Column: " + column); // TBD
         String field = column.getValue (entry);
         row.add (resolve (field, null));
      }
      
      model.addRow (row);
   }
   
   private void validate()
   {
      if (!subsets.isEmpty())
      {
         // TODO warn if imported > 0
         if (imported > 0)
            System.err.println("Warning: Subset with imported files: " + tableName);
         for (Subset subset : subsets.values())
            if (subset.getMax() > size())
               System.err.println ("Invalid subset in " + file + ": " + subset +
                     "; " + subset.getMax() + " > " + size());
      }
   }
   
   private void export()
   {
      if (source.size() > 0)
         for (String line : source)
            System.out.println ("  Source: " + line);
      if (!subsets.isEmpty())
         for (Subset subset : subsets.values())
            System.out.println ("  Subset: " + subset);
      if (!columns.isEmpty())
         for (Column column : columns.values())
            System.out.println ("  Column: " + column);

      System.out.println();
      if (title.size() > 0)
         for (String line : title)
            System.out.println ("      " + line);
      
      int i = 1;
      for (String entry : this)
         System.out.println ("  " + LINE_NUM.format (i++) + ") " + entry);
   }
   
   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder();
      sb.append (tableName);
      sb.append (" (" + file + ")");
      if (!columns.isEmpty())
         sb.append ("\n  Columns: " + columns);
      if (!subsets.isEmpty())
         sb.append ("\n  Subsets: " + subsets);
      return sb.toString();
   }
   
   private static class MyTableModel extends DefaultTableModel
   {
      private static final long serialVersionUID = 1L;
      
      @Override
      public Class<?> getColumnClass (final int c)
      {
         return getValueAt (0, c).getClass();
      }
   }
   
   public static void main (final String[] args)
   {
      Table.populate (new File ("data/Tables"));
      
      for (String name : Table.tables.keySet())
      {
         Table table = Table.getTable (name);
         System.out.println (table);
      }
      System.out.println();
      
      Table table = Table.getTable ("DEPENDS");
      table.export();
   }
}
