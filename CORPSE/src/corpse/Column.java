package corpse;

import java.io.File;
import java.util.regex.Matcher;

public class Column implements Comparable<Column>
{
   private String name;
   private int index;
   private int start;
   private int length;

   public Column (final String name, final int index)
   {
      this (name, 0, 0, index);
   }
   
   public Column (final Matcher m, final int index)
   {
      this (m.group (1),
            Integer.parseInt (m.group (2)),
            Integer.parseInt (m.group (3)),
            index);
   }

   public Column (final String name, final int start, 
                  final int length, final int index)
   {
      this.name = name;
      this.start = start;
      this.length = length;
      this.index = index;
   }

   public String getName()
   {
      return name;
   }
   
   public String getValue (final String unresolvedEntry)
   {
      String field = "";
      
      if (start == 0)
      {
         Matcher m = Constants.COLUMN_SEMI.matcher (unresolvedEntry);
         for (int i = 0; i < index; i++)
            m.find(); // skip the first N-1 entries
         if (m.find())
            field = m.group (1);
      }
      else
      {
         int from = start - 1;
         int end = from + length;
         int len = unresolvedEntry.length();
         if (end <= len)
            field = unresolvedEntry.substring (from, end).trim();
         else if (from < len)
            field = unresolvedEntry.substring (from).trim();
      }
      
      return field;
   }
   
   @Override
   public String toString()
   {
      return "[" + index + "] " + name + " " + start + " " + length;
   }

   public int compareTo (final Column other)
   {
      return index - other.index;
   }
   
   
   public static void main (final String[] args)
   {
      Table.populate (new File ("data/Tables"));
      
      for (Table table : Table.getTables())
      {
         table.importTable();
         if (!table.columns.isEmpty())
            System.out.println (table);
      }
   }
}
