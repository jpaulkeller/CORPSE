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
   // Token format: {Table:Subset.Column#Filter#}

   public SubTable(final String token)
   {
      Matcher m = Constants.TABLE_XREF.matcher(token);
      if (m.matches())
      {
         String xrefTbl = m.group(1);
         String xrefSub = null;
         String xrefCol = null;
         String xrefFil = null;
         
         if (Constants.ALL_CHAR.equals(m.group(5))) // {Table!#Filter#} don't default subsets/columns
         {
            xrefFil = m.group(6);
            System.out.println("SubTable.SubTable(): " + token + " = " + xrefFil); // TODO
         }
         else // {Table:Subset.Column#Filter#}
         {
            xrefSub = m.group(2);
            xrefCol = m.group(3);
            xrefFil = m.group(4);
            
            // TODO: must handle Table:.# (see also Macros.java)
            
            // support default subsets and columns
            if (xrefCol == null)
               xrefCol = xrefTbl;
            if (xrefSub == null)
               xrefSub = xrefTbl;
         }

         importTable(token, xrefTbl, xrefSub, xrefCol, xrefFil);
      }
      else
         System.out.println("Invalid table token: " + token);
   }

   private void importTable(final String token, String xrefTbl, String xrefSub, String xrefCol, String xrefFil)
   {
      // System.out.println("SubTable [" + token + "] T[" + xrefTbl + "] S[" + xrefSub + "] C[" + xrefCol + "] F[" + xrefFil + "]");

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

   @Override
   public boolean add(final String line)
   {
      count++;
      if (subset == null || subset.includes(count, line))
      {
         String entry = line;
         if (column != null)
            entry = column.getValue(line);
         if (filter == null)
            return super.add(entry);
         
         Matcher m = filter.matcher(entry); 
         if (m.matches())
            return m.groupCount() > 0 ? super.add(m.group(1)) : super.add(entry);
      }
      return false;
   }

   @Override
   void validate()
   {
      // We don't want to validate subsets here, since the table is filtered. Columns could be validated.
   }

   private static void test(final String token, final String test)
   {
      System.out.println("Test: " + test + " - " + token);
      SubTable table = new SubTable(token);
      table.export();
      System.out.println();
   }
   
   public static void main(final String[] args)
   {
      CORPSE.init(true);

      // test("{Profession.#G.*#}", "column and filter");
      // test("{Color:Basic#C.*#}", "subset and filter");
      // test("{Metallic}", "included file");
      // test("{Calendar:Astronomical}", "subset");
      // test("{Gender}", "default column");
      // test("{Gender!}", "full line (don't use default column)");
      // test("{Quality}", "default subset");
      // test("{Quality!}", "full line (don't use default subset)");
      // test("{DiffTest#.+(?<!Common)#}", "negative look-ahead regex to prevent collision");
      // test("{Color#.*(ee|ro).*#}", "filter with alteration");
      // test("{Profession+#.*craftsman.*#}", "filter subsets");
      // test("{Profession:Criminal}", "subset filter");
      test("{Profession.Job}", "composite column");
   }
}
