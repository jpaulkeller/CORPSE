package corpse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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

import corpse.ui.TokenRenderer;
import file.FileUtils;

public class Table extends ArrayList<String>
{
   public static final String ENCODING = "UTF8";
   
   private static final long serialVersionUID = 1L;

   private static final DecimalFormat LINE_NUM = new DecimalFormat ("0000"); 
   
   // the master list of tables by name (names must be unique)
   protected static final SortedMap<String, Table> TABLES = new TreeMap<String, Table>();
      
   protected String tableName;
   protected File file;
   protected Pattern filter;
   private List<String> title = new ArrayList<String>();
   private List<String> source = new ArrayList<String>();
   
   private SortedMap<String, Column> columns = new TreeMap<String, Column>();
   private SortedMap<String, Subset> subsets = new TreeMap<String, Subset>();
   
   int included = 0; // total number of imported lines (includes weighted lines)
   
   // For example of subset usage, see the MADE OF.TBL and COLOR.TBL.

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
      Table table = TABLES.get (name.toUpperCase());
      if (table != null)
      {
         if (table.isEmpty())
            table.importTable();
      }
      else
      {
         System.err.println ("Table not yet loaded: " + name);
         // Thread.dumpStack();
      }
      
      return table;
   }
   
   public static List<Table> getTables()
   {
      return new ArrayList<Table>(TABLES.values());
   }
   
   protected Table()
   {
   }
         
   private Table (final String path)
   {
      file = new File (path);
      tableName = FileUtils.getNameWithoutSuffix (file).toUpperCase();
      Table table = TABLES.get(tableName);
      if (table == null)
         TABLES.put (tableName, this);
      else
         System.err.println("Ignoring duplicate table name: " + path + " (already loaded: " + table.getFile() + ")");
   }
   
   // for filtered tables
   public Table (final String name, final String filterRegex)
   {
      Table unfiltered = Table.getTable(name);
      file = new File(unfiltered.file.getAbsolutePath());
      tableName = name + "#" + filterRegex;
      filter = Pattern.compile(filterRegex);
      importTable();
      TABLES.put (tableName, this);
   }

   public String getName()
   {
      return tableName;
   }
   
   public File getFile()
   {
      return file;
   }
   
   public String resolve (final String entry, final String filter)
   {
      String resolved = entry;
      
      Matcher m = Constants.TOKEN.matcher (resolved);
      if (m.find())
      {
         if (filter == null)
            resolved = Macros.resolve (entry); // recurse to support embedded tokens
         else
         {
            System.out.println("Table.resolve() [" + entry + "]  Filter = [" + filter + "]"); // TODO
            resolved = Macros.resolve (entry, filter); // recurse to support embedded tokens
         }
         
         while ((m = Constants.TOKEN.matcher (resolved)).find())
         {
            System.err.println (file + " unsupported token: " + m.group (0));
            resolved = m.replaceFirst (TokenRenderer.INVALID_OPEN + m.group (1) + TokenRenderer.INVALID_CLOSE);
         }
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

   SortedMap<String, Subset> getSubsets()
   {
      return subsets;
   }
   
   Subset getSubset (final String subsetName)
   {
      String key = subsetName != null ? subsetName.toUpperCase() : tableName;
      Subset subset = subsets.get (key);
      if (subset == null && subsetName != null)
         System.err.println ("Missing " + file + " subset: [" + subsetName + "]");
      return subset;
   }

   SortedMap<String, Column> getColumns()
   {
      return columns;
   }
   
   Column getColumn (final String columnName)
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
         System.err.println ("Missing Column: " + columnName);
         x.printStackTrace (System.err);
      }
      return unresolved;
   }
   
   public List<String> search(final String pattern)
   {
      List<String> matches = new ArrayList<String>();
      
      String line = null;
      BufferedReader br = null;
      try
      {
         FileInputStream fis = new FileInputStream (file);
         InputStreamReader isr = new InputStreamReader (fis, ENCODING);
         br = new BufferedReader (isr);
         
         while ((line = br.readLine()) != null)
            if (line.toUpperCase().contains(pattern))
               matches.add(line);
      }
      catch (Exception x)
      {
         System.err.println ("File: " + file);
         System.err.println ("Line: " + line);
         x.printStackTrace (System.err);
      }
      finally
      {
         if (br != null)
            try { br.close(); } catch (IOException x) { }
      }
      
      return matches;
   }
   
   void importTable()
   {
      String line = null;
      BufferedReader br = null;
      try
      {
         FileInputStream fis = new FileInputStream (file);
         InputStreamReader isr = new InputStreamReader (fis, ENCODING);
         br = new BufferedReader (isr);
         
         while ((line = br.readLine()) != null)
            parseLine (line);
         Subset.finish(this);
         validate();
      }
      catch (Exception x)
      {
         System.err.println ("File: " + file);
         System.err.println ("Line: " + line);
         x.printStackTrace (System.err);
      }
      finally
      {
         if (br != null)
            try { br.close(); } catch (IOException x) { }
      }
   }

   void parseLine (final String line)
   {
      if (line.length() > 0)
      {
         Matcher m;
         if (line.startsWith(Constants.COLUMN_CHAR))
            Column.parse(this, line);
         else if (line.startsWith(Constants.SUBSET_CHAR))
            Subset.parse(this, line);
         else if (line.startsWith (Constants.SOURCE_CHAR))
            source.add (line.substring (1).trim());
         else if (line.startsWith (Constants.TITLE_CHAR))
            title.add (line.substring (1).trim());
         else if (line.startsWith (Constants.COMMENT_CHAR))
            ; // do nothing
         else if (line.startsWith (Constants.FOOTNOTE_CHAR))
            ; // do nothing
         else if (line.startsWith (Constants.HEADER_CHAR))
            ; // do nothing
         else if (line.startsWith (Constants.SEPR_CHAR))
            ; // do nothing

         else if ((m = Constants.INCLUDED_TBL.matcher (line)).find())
            includeTable(m);
         else if ((m = Constants.RANGE_LINE.matcher (line)).find())
            includeRange(m);
         else if ((m = Constants.WEIGHTED_LINE.matcher (line)).find())
            includeWeighted(m);
         
         else if (line.trim().length() > 0) // ignore blank lines
            add (line);
      }
   }

   // Included tables are used to provide an even distribution of elements from multiple sub-tables of 
   // different sizes.  Each element has the same chance of selection.  See Site.tbl for an example.
   
   private void includeTable(final Matcher m)
   {
      String name = m.group(2);
      // if (Macros.DEBUG) System.out.println("INCLUDED: " + name + " into " + tableName);

      String token = "{" + name + "}";
      Table table = TABLES.get (token);
      if (table == null)
      {
         if (Constants.SIMPLE_TABLE.matcher(name).matches())
            table = Table.getTable(name);
         else // resolve any tables with columns/subsets/filters
            table = new SubTable (token); // resolve the table before including
      }
      for (String line : table)
         if (add (m.group(1) + line + m.group(3)))
            included++;
   }

   // Range lines are used to provide a random number of chances for a particular line.
   
   private void includeRange(final Matcher m)
   {
      int from = Integer.parseInt (m.group (1));
      int to = Integer.parseInt (m.group (2));
      String text = m.group(3);
      // if (Macros.DEBUG) System.out.println("RANGE: " + text + " into " + tableName);
      for (int i = from; i <= to; i++)
         if (add (text))
            included++;
   }

   // Weighted lines are used to provide a specified number of chances for a particular line.
   
   private void includeWeighted(final Matcher m)
   {
      int weight = Integer.parseInt (m.group (1));
      String text = m.group(2);
      // if (Macros.DEBUG) System.out.println("WEIGHTED: " + text + " into " + tableName);
      for (int i = 0; i < weight; i++)
         if (add (text))
            included++;
   }
   
   void addColumn (final Column column)
   {
      columns.put (column.getName().toUpperCase(), column);
   }
   
   void addSubset (final Subset subset)
   {
      subsets.put (subset.getName().toUpperCase(), subset);
   }
   
   public DefaultTableModel getModel()
   {
      DefaultTableModel model = new MyTableModel();
      SortedSet<Column> sorted = new TreeSet<Column>(); // by order in file
      
      model.addColumn ("#");
      
      if (!subsets.isEmpty())
         model.addColumn ("Subset");

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
      // System.out.println ("Entry = [" + entry + "]"); // TODO
      Vector<Object> row = new Vector<Object>();
      row.add (i);
      
      if (!subsets.isEmpty())
      {
         StringBuilder rowSubsets = new StringBuilder();
         for (Subset subset : subsets.values())
            if (i >= subset.getMin() && i <= subset.getMax())
            {
               if (rowSubsets.length() > 0)
                  rowSubsets.append(", ");
               rowSubsets.append(subset.getName());
            }
         row.add(rowSubsets.toString());
      }
      
      if (sorted.isEmpty())
         row.add (resolve (entry.trim(), null));
      else for (Column column : sorted)
      {
         // if (i == 1) System.out.println ("Column: " + column); // TODO
         String field = column.getValue (entry);
         row.add (resolve (field, null));
      }
      
      model.addRow (row);
   }
   
   void validate()
   {
      if (!subsets.isEmpty())
      {
         // TODO warn if imported > 0
         // if (included > 0) System.err.println("Warning: Subset with imported files: " + tableName);
         for (Subset subset : subsets.values())
            if (subset.getMax() > size())
               System.err.println ("Invalid subset in " + file + ": " + subset +
                     "; " + subset.getMax() + " > " + size());
      }
   }
   
   void export()
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
   
   @Override
   public boolean add(final String line)
   {
      if (filter == null || filter.matcher(line).matches())
         return super.add(line);
      return false;
   }
   
   public static void main (final String[] args)
   {
      Table.populate (new File ("data/Tables"));
      
      for (String name : new ArrayList<String>(Table.TABLES.keySet()))
      {
         Table table = Table.getTable (name);
         System.out.println (table);
      }
      System.out.println();
      
      Table table;

      // test including a subset
      table = Table.getTable ("METALLIC");
      table.export();
      System.out.println();
      
      // test a filter
      new Table("COLOR", "C.+");
      table = Table.getTable ("COLOR#C.+");
      table.export();
      System.out.println();
   }
}
