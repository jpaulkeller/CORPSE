/*
 * The Column class represents a column (field) in a table.  There are several ways to define a column, but only one
 * format should be used within any file.
 *
 * For FIXED WIDTH values:
 * 
 * 1) You can just specify the width ". Name 10".  The column will start at 1 + the end of
 *    the previous column.  (Values are trimmed as they are extracted, so extra white-space won't matter.)
 * 
 * 2) You can specify the starting position and width like ". Name 1 20".  This format should only
 *    be used if necessary, since it's harder to maintain.
 *  
 * 3) Fixed width columns can also be defined on one line, aligned with the data (separated by at least 2 spaces):
 *    .Name            Cost      Weight       etc...
 *    Note that if the first column name appears next to the . (as above), it will be treated as starting at 1 (instead of 2).  
 *    This format is easy to use and maintain, but does limit the width of your column names.
 *
 * For DELIMITED values (such as CSV):
 * 
 * 4) You can simply specify all the column names on one line (. First, Second, ...)
 * 
 * 5) Or you can just list the columns in order, one per line. 
 */

package corpse;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Column implements Comparable<Column>
{
   private static final String CC = Constants.COLUMN_CHAR;
   private static final String CN = Constants.COLUMN_NAME;

   private static final Pattern CN_PATTERN = Pattern.compile(CN, Pattern.CASE_INSENSITIVE);

   // . Name
   private static final Pattern COLUMN = Pattern.compile("^\\" + CC + "\\s*" + CN + Constants.COMMENT, Pattern.CASE_INSENSITIVE);
   
   // . Name width
   private static final Pattern COLUMN_FIXED = Pattern.compile("^\\" + CC + "\\s*" + CN + "\\s+(\\d+)" + Constants.COMMENT,
                                                               Pattern.CASE_INSENSITIVE);
   
   // . Name [regex] -- regex must contain a single group, which will be the column's value
   private static final String REGEX = "\\[(.*\\(.+\\).*)\\]";
   private static final Pattern COLUMN_REGEX = 
      Pattern.compile("^\\" + CC + "\\s*" + CN + "\\s+" + REGEX + Constants.COMMENT, Pattern.CASE_INSENSITIVE);
   
   // . Name start width (obsolete format)
   private static final Pattern COLUMN_FULL = Pattern.compile("^" + CC + "\\s*" + CN + "\\s+(\\d+)\\s+(\\d+)" + Constants.COMMENT,
                                                              Pattern.CASE_INSENSITIVE);
   // . First  Second  Third ... (up to 20; min 2-space separation) -- Alteration.tbl
   private static final String HEADER_REGEX = "^\\" + CC + "(\\s*" + CN + "(?:\\s\\s+" + CN + "){1,20})" + Constants.COMMENT;
   private static final Pattern COLUMN_HEADER = Pattern.compile(HEADER_REGEX, Pattern.CASE_INSENSITIVE);
   
   // . Name1, Name2, ... (2 to 20)
   private static final String CSV_REGEX = "^\\" + CC + "\\s*(" + CN + "(?:[,;]\\s*" + CN + "){1,20})" + Constants.COMMENT;
   private static final Pattern COLUMNS_CSV = Pattern.compile(CSV_REGEX, Pattern.CASE_INSENSITIVE);

   // . Name Token (e.g. Quality {quality:})
   private static final Pattern GHOST_COLUMN = Pattern.compile("^\\" + CC + "\\s*" + CN + "\\s+" + Constants.TOKEN + Constants.COMMENT, 
                                                               Pattern.CASE_INSENSITIVE);
   
   // . Name = name + name + name (up to 20)
   private static final String META_COLUMN_REGEX = "^\\" + CC + " *" + CN + " *= *(" + CN + "(?: *\\+ *" + CN + "){0,20})";
   private static final Pattern COMPOSITE_COLUMN = Pattern.compile(META_COLUMN_REGEX, Pattern.CASE_INSENSITIVE);

   // value 1;value 2;...
   private static final Pattern DELIMITED_DATA = Pattern.compile("([^;]*)(?: *; *)?");

   private Table table;
   private String name;
   private int index;
   private int start; // 1-based
   private int width;
   private String token; // for ghost columns
   private Pattern pattern; // for regex columns

   private List<String> composites = new ArrayList<String>();

   public static void parse(final Table table, final String entry)
   {
      Column column = null;
      
      Matcher m;
      if ((m = COMPOSITE_COLUMN.matcher(entry)).find())
      {
         column = new Column(table);
         column.name = m.group(1);
         column.index = table.getColumns().size();
         column.start = 0;
         column.width = 0;

         Matcher nameMatcher = CN_PATTERN.matcher(m.group(2));
         while (nameMatcher.find())
            column.composites.add(nameMatcher.group(1));
         table.addColumn(column);
      }
      else if ((m = COLUMN_FULL.matcher(entry)).find()) // Name start width
      {
         column = new Column(table);
         column.name = m.group(1);
         column.index = table.getColumns().size();
         column.start = Integer.parseInt(m.group(2));
         column.width = Integer.parseInt(m.group(3));
         table.addColumn(column);
         System.err.println("Upgrade " + table.getName() + " column format: " + column.name);
      }
      else if ((m = COLUMN_FIXED.matcher(entry)).find()) // Name width
      {
         column = new Column(table);
         column.name = m.group(1);
         column.index = table.getColumns().size();
         column.start = column.index == 0 ? 1 : Column.getNextStart(table, column);
         column.width = Integer.parseInt(m.group(2));
         table.addColumn(column);
      }
      else if ((m = COLUMN_REGEX.matcher(entry)).find()) // Name [regex]
      {
         column = new Column(table);
         column.name = m.group(1);
         column.index = table.getColumns().size();
         column.start = -1;
         column.width = -1;
         column.pattern = CORPSE.safeCompile(table.getName() + "." + column.name, m.group(2));
         table.addColumn(column);
      }
      else if ((m = GHOST_COLUMN.matcher(entry)).find()) // Name Token
      {
         column = new Column(table);
         column.name = m.group(1);
         column.index = table.getColumns().size();
         column.start = -1;
         column.width = -1;
         column.token = "{" + m.group(2) + "}";
         table.addColumn(column);
      }
      else if ((m = COLUMNS_CSV.matcher(entry)).find()) // First, Second, ...
      {
         Matcher nameMatcher = CN_PATTERN.matcher(m.group(1));
         while (nameMatcher.find())
            table.addColumn(new Column(table, nameMatcher.group(1), table.getColumns().size()));
      }
      else if ((m = COLUMN_HEADER.matcher(entry)).find()) // First Second ...
         parseColumnHeader(table, m.group(1));
      else if ((m = COLUMN.matcher(entry)).find()) // Name width
         table.addColumn(new Column(table, m.group(1), table.getColumns().size()));
      else
         System.err.println("Invalid column in " + table.getFile() + ": " + entry);
   }

   private static void parseColumnHeader(final Table table, final String header)
   {
      if (table.getColumns().isEmpty())
      {
         Matcher m = CN_PATTERN.matcher(header);
         while (m.find())
         {
            Column column = new Column(table);
            column.name = m.group(1);
            column.index = table.getColumns().size();
            column.start = (m.start() == 0 ? 0 : m.start() + Constants.COLUMN_CHAR.length()) + 1;
            table.addColumn(column);
            
            if (column.index > 0)
            {
               Column prev = Column.getColumn(table, column.index - 1);
               prev.width = column.start - prev.start; // determine the width of the previous column
            }
         }
         // note the final column won't have a width, so we'll just go to the end-of-line when we extract it
      }
   }

   private Column(final Table table)
   {
      this.table = table;
   }

   private Column(final Table table, final String name, final int index)
   {
      this(table, name, 0, 0, index);
   }

   private Column(final Table table, final String name, final int start, final int length, final int index)
   {
      this.table = table;
      this.name = name;
      this.start = start;
      this.width = length;
      this.index = index;
   }

   private static Column getColumn(final Table table, final int index)
   {
      Column column = null;
      for (Column c : table.getColumns().values())
         if (c.index == index)
            column = c;
      return column;
   }

   static boolean isComposite(final Table table, final String colName)
   {
      Column col = table.getColumn(colName);
      return col != null && !col.composites.isEmpty();
   }
   
   // determine the start of the given column, based on the start/width of the previous column
   private static int getNextStart(final Table table, final Column column)
   {
      int start = 1;
      for (Column c : table.getColumns().values())
         if (c.index == column.index - 1) // if previous
            start = c.start + c.width;
      return start;
   }

   public String getName()
   {
      return name;
   }

   public String getValue(final String unresolvedEntry)
   {
      String field = "";

      if (!composites.isEmpty())
      {
         for (String composite : composites)
         {
            Column col = table.getColumn(composite);
            if (col != null)
               field += col.getValue(unresolvedEntry);
            else
            {
               if (composite.equals("_")) // hack
                  composite = " ";
               if (!field.isEmpty())
                  field += composite; // support Field1 + _ + Field2
            }
         }
      }
      else if (start == 0)
      {
         Matcher m = DELIMITED_DATA.matcher(unresolvedEntry);
         for (int i = 0; i < index; i++)
            m.find(); // skip the first N-1 entries
         if (m.find())
            field = m.group(1);
      }
      else if (pattern != null) // regex column
      {
         Matcher m = pattern.matcher(unresolvedEntry);
         if (m.matches() && m.groupCount() > 0)
            field = m.group(1);
      }
      else if (token != null) // ghost column
         field = token;
      else // fixed-width
      {
         int from = start - 1;
         int end = from + width;
         int len = unresolvedEntry.length();
         if (end <= len && width > 0)
            field = unresolvedEntry.substring(from, end).trim();
         else if (from < len)
            field = unresolvedEntry.substring(from).trim();
      }

      return field;
   }

   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder();
      sb.append("[" + index + "] " + name);
      if (!composites.isEmpty())
      {
         sb.append(" = ");
         Iterator<String> iter = composites.iterator();
         while (iter.hasNext())
            sb.append(iter.next() + (iter.hasNext() ? " + " : ""));
      }
      else if (token != null)
         sb.append(" (token = " + token + ")");
      else if (pattern != null)
         sb.append(" (pattern = " + pattern + ")");
      else
         sb.append(" " + start + " " + width);
      return sb.toString();
   }

   @Override
   public int compareTo(final Column other)
   {
      return index - other.index;
   }

   public static void main(final String[] args)
   {
      CORPSE.init(true);

      // String test = "EQUIPMENT";
      // String test = "Alteration";
      // String test = "Profession";
      String test = "Fauna";
      
      if (test != null)
      {
         Table table = Table.getTable(test); 
         System.out.println (table);
      }
      else // test all
      {
         for (Table table : Table.getTables())
         {
            table.importTable();
            if (!table.getColumns().isEmpty())
               System.out.println(table);
         }
      }
   }
}
