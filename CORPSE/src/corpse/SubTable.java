package corpse;

import java.io.File;
import java.util.regex.Matcher;

public final class SubTable extends Table
{
   private static final long serialVersionUID = 1L;

   private Subset subset;
   private Column column;
   private int count;

   // Get a populated/resolved table (with subset, column, and filter applied).
   // Token format: {# Table:Subset@Column#Filter}
   // Note: The numeric prefix is ignored.

   public SubTable(final String token)
   {
      Matcher m = Constants.TABLE_XREF.matcher(token);
      if (m.matches())
      {
         // String qty = m.group (1);
         String xrefTbl = m.group(2);
         String xrefSub = m.group(3);
         String xrefCol = m.group(4);
         String xrefFil = m.group(5);

         if (xrefSub == null && token.contains(Constants.SUBSET_CHAR)) // e.g., Metal:
            xrefSub = xrefTbl;
         if (xrefCol == null && token.contains(Constants.COLUMN_CHAR)) // e.g., Job@
            xrefCol = xrefTbl;

         // System.out.println("[" + token + "] T[" + xrefTbl + "] S[" + xrefSub + "] C[" + xrefCol + "] F[" + xrefFil + "]");

         Table unfiltered = Table.getTable(xrefTbl);
         tableName = token;
         file = new File(unfiltered.file.getAbsolutePath());
         if (xrefSub != null)
            subset = unfiltered.getSubset(xrefSub);
         if (xrefCol != null)
            column = unfiltered.getColumn(xrefCol);
         if (xrefFil != null)
            filter = CORPSE.safeCompile("Invalid filter in " + token, xrefFil);

         count = 0;
         importTable();
         TABLES.put(tableName, this);
      }
      else
         System.out.println("Invalid table token: " + token);
   }

   @Override
   public boolean add(final String line)
   {
      count++;
      if (subset == null || subset.includes(count, line))
      {
         String entry = line;
         if (column != null)
            entry = column.getValue(line);
         if (filter == null || filter.matcher(entry).matches())
            return super.add(entry);
      }
      return false;
   }

   @Override
   void validate()
   {
      // We don't want to validate subsets here, since the table is filtered. Columns could be validated.
   }

   public static void main(final String[] args)
   {
      CORPSE.init(true);

      SubTable table;

      // test column and filter
      table = new SubTable("{Job@#G.*}");
      table.export();
      System.out.println();

      // test subset and filter
      table = new SubTable("{Color:Basic#C.*}");
      table.export();
      System.out.println();

      table = new SubTable("{Metallic}");
      table.export();
      System.out.println();
   }
}
