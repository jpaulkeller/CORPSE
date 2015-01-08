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

import utils.Utils;
import corpse.Quantity.Numeric;
import corpse.ui.TokenRenderer;
import file.FileUtils;

public class Table extends ArrayList<String>
{
   public static final String ENCODING = "UTF8";

   private static final long serialVersionUID = 1L;

   private static final DecimalFormat LINE_NUM = new DecimalFormat("0000");

   // the master list of tables by name (names must be unique)
   protected static final SortedMap<String, Table> TABLES = new TreeMap<String, Table>();

   protected String tableName;
   protected File file;
   protected Pattern filter;

   private SortedMap<String, Column> columns = new TreeMap<String, Column>();
   private SortedMap<String, Subset> subsets = new TreeMap<String, Subset>();
   private List<String> imported = new ArrayList<String>();

   public static void populate(final File dir)
   {
      for (File f : dir.listFiles())
      {
         if (f.isDirectory() && !f.getName().startsWith("."))
            populate(f);
         else if (f.isFile() && f.getName().toLowerCase().endsWith(".tbl"))
            new Table(f.getPath());
      }
   }

   public static Table getTable(final String name)
   {
      Table table = TABLES.get(name.toUpperCase());
      if (table != null)
         table.importTable();
      else
      {
         System.err.println("Table not yet loaded: " + name);
         System.err.println(Utils.getStack(null));
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

   private Table(final String path)
   {
      file = new File(path);
      tableName = FileUtils.getNameWithoutSuffix(file).toUpperCase();
      Table table = TABLES.get(tableName);
      if (table == null)
         TABLES.put(tableName, this);
      else
         System.err.println("Ignoring duplicate table name: " + path + " (already loaded: " + table.getFile() + ")");
   }

   // for filtered tables
   public Table(final String name, final String filterRegex)
   {
      Table unfiltered = Table.getTable(name);
      file = new File(unfiltered.file.getAbsolutePath());
      tableName = name + Constants.FILTER_CHAR + filterRegex + Constants.FILTER_CHAR;
      filter = CORPSE.safeCompile("Invalid subset filter", filterRegex);
      importTable();
      TABLES.put(tableName, this);
   }

   public String getName()
   {
      return tableName;
   }

   public File getFile()
   {
      return file;
   }

   public String resolve(final String entry, final String filter)
   {
      String resolved = entry;

      Matcher m = Constants.TOKEN.matcher(resolved);
      if (m.find())
      {
         resolved = Macros.resolve(getName(), entry, filter); // recurse to support embedded tokens
         while ((m = Constants.TOKEN.matcher(resolved)).find())
         {
            System.err.println(file + " unsupported token: " + m.group(0));
            resolved = m.replaceFirst(Matcher.quoteReplacement(TokenRenderer.INVALID_OPEN + m.group(1) + TokenRenderer.INVALID_CLOSE));
         }
      }

      return resolved;
   }

   public String getColumn(final int index, final String columnName, final String filter)
   {
      String entry = get(index);
      if (entry != null)
      {
         entry = getColumnValue(entry, columnName);
         entry = resolve(entry, filter);
      }
      return entry;
   }

   SortedMap<String, Subset> getSubsets()
   {
      return subsets;
   }

   Subset getSubset(final String subsetName)
   {
      Subset subset = null;
      if (subsetName != null)
      {
         String key = subsetName.toUpperCase();
         subset = subsets.get(key);
         if (subset == null && !subsetName.equalsIgnoreCase(tableName))
            System.err.println("Missing " + file + " subset: [" + subsetName + "]");
      }
      return subset;
   }

   SortedMap<String, Column> getColumns()
   {
      return columns;
   }

   Column getColumn(final String columnName)
   {
      Column column = null;
      if (columnName != null)
      {
         String key = columnName.toUpperCase();
         column = columns.get(key);
         if (column == null && !columnName.equalsIgnoreCase("ALL") && 
             !columnName.equalsIgnoreCase(tableName) && 
             !columnName.equals("_")) // hack for composite columns 
            // && !Column.isComposite(this, columnName)) // TODO infinite loop
            System.err.println("Missing " + file + " column: [" + key + "]\n" + Utils.getStack("^corpse[.].*"));
      }
      return column;
   }

   public String getColumnValue(final String unresolvedEntry, final String columnName)
   {
      String unresolved = unresolvedEntry;
      try
      {
         if (columnName != null && !columnName.equalsIgnoreCase("ALL"))
         {
            Column column = getColumn(columnName);
            if (column != null)
               unresolved = column.getValue(unresolvedEntry);
         }
      }
      catch (Exception x)
      {
         System.err.println("File: " + file);
         System.err.println("Line: " + unresolvedEntry);
         System.err.println("Missing Column: " + columnName);
         x.printStackTrace(System.err);
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
         FileInputStream fis = new FileInputStream(file);
         InputStreamReader isr = new InputStreamReader(fis, ENCODING);
         br = new BufferedReader(isr);

         while ((line = br.readLine()) != null)
            if (line.toUpperCase().contains(pattern))
               matches.add(line);
      }
      catch (Exception x)
      {
         System.err.println("File: " + file);
         System.err.println("Line: " + line);
         x.printStackTrace(System.err);
      }
      finally
      {
         FileUtils.close(br);
      }

      return matches;
   }

   protected void importTable()
   {
      if (isEmpty())
      {
         String line = null;
         BufferedReader br = null;
         try
         {
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis, ENCODING);
            br = new BufferedReader(isr);

            while ((line = br.readLine()) != null && !line.startsWith(Constants.EOF))
               parseLine(line);
            Subset.finish(this);
            validate();
         }
         catch (Exception x)
         {
            System.err.println("File: " + file);
            System.err.println("Line: " + line);
            x.printStackTrace(System.err);
         }
         finally
         {
            FileUtils.close(br);
         }
      }
   }

   void parseLine(final String line)
   {
      if (line.length() > 0)
      {
         Numeric numeric;

         if (line.startsWith(Constants.COLUMN_CHAR))
            Column.parse(this, line);
         else if (line.startsWith(Constants.SUBSET_CHAR))
            Subset.parse(this, line);
         else if (line.startsWith(Constants.COMMENT_CHAR))
            ; // do nothing
         else if (line.trim().length() == 0)
            ; // ignore blank lines
         else if (line.startsWith(Constants.INCLUDE_CHAR))
            includeTable(line);
         else if ((numeric = Quantity.startsWith(line)) != null)
            includeWeighted(numeric, line);
         else
            add(line); // add a normal table entry
      }
   }

   // Included tables are used to provide an even distribution of elements from multiple sub-tables of
   // different sizes. Each element has the same chance of selection. See Site.tbl or Flora.tbl as examples.

   private void includeTable(final String line)
   {
      String resolvedline = Macros.resolveExpressions(line);

      Matcher m = Constants.INCLUDED_TBL.matcher(resolvedline);
      if (m.find())
      {
         String prefix = m.group(1);
         String tableRef = m.group(2);
         String suffix = m.group(m.groupCount());

         if (Macros.DEBUG) 
            System.out.println("INCLUDED: " + tableRef + " into " + tableName);

         addSubset(Subset.parseIncludedTableAsSubset(this, tableRef));
         
         String token = "{" + tableRef + "}";
         Table table = TABLES.get(token);
         if (table == null)
         {
            // if (Constants.SIMPLE_TABLE.matcher(tableRef).matches()) table = Table.getTable(tableRef);
            table = new SubTable(token); // resolve the table before including
         }
         for (String entry : table)
            add(prefix + entry + suffix);
         
         Subset.closeSubset(this);
         
         imported.add(tableRef);
      }
   }

   // Weighted lines are used to provide a (possibly random) number of copies of a particular line.

   private void includeWeighted(final Numeric num, final String line)
   {
      int from = num instanceof Quantity.Constant ? 1 : num.getMin();
      int to = num.getMax();
      int brk = line.indexOf(Constants.WEIGHT);
      String text = line.substring(brk + Constants.WEIGHT.length());
      // if (Macros.DEBUG) System.out.println("Include Weighted: " + text + " into " + tableName);
      for (int i = from; i <= to; i++)
         add(text);
   }

   void addColumn(final Column column)
   {
      columns.put(column.getName().toUpperCase(), column);
   }

   void addSubset(final Subset subset)
   {
      subsets.put(subset.getName().toUpperCase(), subset);
   }

   public DefaultTableModel getModel()
   {
      DefaultTableModel model = new DefaultTableModel();
      SortedSet<Column> sorted = new TreeSet<Column>(); // by order in file

      model.addColumn("#");

      if (!subsets.isEmpty())
         model.addColumn("Subset");

      if (columns.isEmpty()) // just one column; each line is the value
         model.addColumn(tableName);
      else
      {
         sorted.addAll(columns.values());
         for (Column column : sorted)
            model.addColumn(column.getName());
      }

      int i = 1;
      for (String unresolvedEntry : this)
         addRow(model, sorted, i++, unresolvedEntry);

      return model;
   }

   private static final Pattern INTEGER = Pattern.compile("-?[0-9]+");
   private static final Pattern FLOAT = Pattern.compile("-?[0-9]+[.][0-9]+");

   private void addRow(final DefaultTableModel model, final SortedSet<Column> sorted, final int i, final String entry)
   {
      Vector<Object> row = new Vector<Object>();
      row.add(i);

      if (!subsets.isEmpty())
      {
         StringBuilder rowSubsets = new StringBuilder();
         for (Subset subset : subsets.values())
            if (subset.includes(this, i, entry))
            {
               if (rowSubsets.length() > 0)
                  rowSubsets.append(", ");
               rowSubsets.append(subset.getName());
            }
         row.add(rowSubsets.toString());
      }

      if (sorted.isEmpty())
         row.add(resolve(entry.trim(), null));
      else
      {
         for (Column column : sorted)
         {
            String value = resolve(column.getValue(entry), null);
            if (value != null && INTEGER.matcher(value.toString()).matches())
               row.add(Integer.parseInt(value)); // so numeric values are right-aligned
            else if (value != null && FLOAT.matcher(value.toString()).matches())
               row.add(Double.parseDouble(value)); // so numeric values are right-aligned
            else
               row.add(value);
         }
      }

      model.addRow(row);
   }

   void validate()
   {
      if (!subsets.isEmpty())
         for (Subset subset : subsets.values())
            if (subset.getMax() > size())
               System.err.println("Invalid subset in " + file + ": " + subset + "; " + subset.getMax() + " > " + size());
   }

   void export()
   {
      if (!subsets.isEmpty())
         for (Subset subset : subsets.values())
            System.out.println("  Subset: " + subset);
      
      if (!columns.isEmpty())
         for (Column column : columns.values())
            System.out.println("  Column: " + column);

      System.out.println();

      int i = 1;
      for (String entry : this)
         System.out.println("  " + LINE_NUM.format(i++) + ") " + entry); // TODO: sprintf?
   }

   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder();
      sb.append(tableName);
      sb.append(" (" + file + ")");
      if (!columns.isEmpty())
         sb.append("\n  Columns: " + columns);
      if (!subsets.isEmpty())
         sb.append("\n  Subsets: " + subsets);
      
      if (!imported.isEmpty())
      {
         sb.append("\n  Imported: " + imported);
         for (String importedTable : imported)
            if (!subsets.containsKey(importedTable.toUpperCase()))
               System.err.println("Warning: possible subset/import conflict in " + getName() + ": " + importedTable);
      }
         
      return sb.toString();
   }

   @Override
   public boolean add(final String line)
   {
      if (filter == null || filter.matcher(line).matches())
         return super.add(line);
      return false;
   }

   private static void test(final String token, final String test)
   {
      System.out.println("Test: " + test + " - " + token);
      Table table = Table.getTable(token);
      table.export();
      System.out.println();
   }
   
   public static void main(final String[] args)
   {
      CORPSE.init(true);

      /*
      for (String name : new ArrayList<String>(Table.TABLES.keySet()))
      {
         Table table = Table.getTable(name);
         System.out.println(table);
      }
      System.out.println();

      test("Flora", "including a table with a default subset");
      test("Spell", "including a subset");
      test("Fauna", "included subsets");
      test("Mine", "weighted lines");
      test("Profession#.*craftsman.*#", "filter subsets");
      test("METALLIC", "including a subset");
   
      // TODO: there must be a better way to pre-load the filtered table
      new Table("COLOR", "C.+");
      test("COLOR#C.+#", "filter");
      new Table("COLOR", ".*(EE|RO).*");
      test("COLOR#.*(EE|RO).*#", "filter with alteration");
      new Table("SPELL", ".*(WALK|FALL).*");
      test("SPELL#.*(WALK|FALL).*#", "filter with alteration");
      */
      
      test("Metal", "subsets");
   }
}
