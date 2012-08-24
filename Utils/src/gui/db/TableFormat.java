package gui.db;

import java.io.Serializable;

import db.Model;

/** The <b>TableFormat<\b> class formats tabular data for use in reports.  */

public final class TableFormat implements Serializable
{
   private static final String columnSeparator = "  ";

   private Model model;
   private int max = 0;
   private boolean allowEmpty;  // true to show columns with no values

   /**
    * Creates an instance of TableFormat.
    *
    * @param model The table model.
    * @param maxFieldWidth The maximum size of any column.
    *      If 0 is specified It will use the longest data value
    *      for that column.  If -1 is specified, it will allow empty columns.
    */
   public TableFormat (Model model, int maxFieldWidth)
   {
      this.model = model;
      if (maxFieldWidth > 0)
         this.max = maxFieldWidth;
      else if (maxFieldWidth < 0)
         allowEmpty = true;
   }

   /**
    * Creates an instance of TableFormat.
    * @param model The table model.
    */
   public TableFormat (Model model)
   {
      this.model = model;
   }

   /**
    * Gets the maximum width for each column.  If the caller did not
    * specify a maximum, then this width will be large enough to hold
    * every value in that column, as well as the column header.  If the
    * caller did specify a valid (> 0) maximum, then the width will not
    * exceed that (and the values may get truncated).
    *
    * @return : An array of lengths.
    */
   public int[] getMaxLength()
   {
      int rowCount = model.getRowCount();
      int colCount = model.getColumnCount();
      int colWidth[] = new int[colCount];

      for (int col = 0; col < colCount; col++) // initialize
         colWidth[col] = 0;

      // check the width of each value, and increase colWidth if necessary
      for (int col = 0; col < colCount; col++)
      {
         for (int row = 0; row < rowCount; row++)
         {
            String val = (String) model.getValueAt (row, col);
            if (val != null)
               colWidth[col] = Math.max (colWidth [col], val.trim().length());
            if ((max > 0) && (colWidth[col] >= max))
               break; // we must truncate, so don't bother to check all values
         }

         if (allowEmpty || (colWidth[col] > 0)) // if there were any values
         {
            // ensure the colWidth is wide enough to hold the column name
            String header = model.getColumnName (col);
            colWidth[col] = Math.max (colWidth[col], header.length());
            // if there is a caller-set maximum, enforce it
            if (max > 0)
               colWidth[col] = Math.min (max, colWidth[col]);
         }
      }

      return (colWidth);
   }

   /**
    * Writes formatted table model to a StringBuilder.
    * @return The formatted StringBuilder.
    */
   public StringBuilder getFormattedBuffer()
   {
      StringBuilder buff = new StringBuilder();

      int maxData[] = getMaxLength();
      StringBuilder header = printHeaders (maxData);
      buff.append (header.toString());
      StringBuilder separator = writeSeparators (maxData);
      buff.append (separator);

      if (model.getRowCount() > 0)
      {
         StringBuilder body = printBody (maxData);
         buff.append (body);
      }

      return (buff);
   }

   /**
    * Formats the Column Headers.
    * @param lens : An array of lengths.
    * @return A StringBuilder containing the formated headers.
    */
   private StringBuilder printHeaders (int[] lens)
   {
      StringBuilder buff = new StringBuilder ("");
      int colCount = model.getColumnCount();

      for (int col = 0; col < colCount; col++)
         if (lens[col] > 0)
         {
            String sepr = (col == colCount - 1) ? "" : columnSeparator;
            String name = model.getColumnName (col);
            name = name.trim();

            if (name.length() < lens[col])
            {
               int offset = lens[col] - name.length();
               buff.append (name + append (offset, " ") + sepr);
            }
            else if (name.length() > lens[col])
               buff.append (name.substring (0, lens[col]) + sepr);
            else
               buff.append (name + sepr);
         }
      buff.append ("\n");
      return (buff);
   }

   /**
    * Creates a String consisting of <numOf> <str> strings.
    * @param numOf : The number of <str> strings to concatenate.
    * @return A new String with the specified number of <str> strings.
    */
   private String append (int numOf, String str)
   {
      StringBuilder result = new StringBuilder();
      for (int i = 0; i < numOf; i++)
         result.append (str);
      return (result.toString());
   }

   // pad or truncate as needed

   private String fitValue (String value, int maxWidth, int col)
   {
      String fit;
      int len = value.length();

      if (len == maxWidth)           // the simple case, just copy it
         fit = value;
      else if (len > maxWidth)       // truncate
         fit = value.substring (0, (maxWidth - 1)) + "+";
      /* TBD
      else if (model.isNumeric (col)) // pad on the left (right-justify)
         fit = append (maxWidth - len, " ") + value;
      */
      else                      // pad on the right (left-justify)
         fit = value + append (maxWidth - len, " ");

      return (fit);
   }

   /**
    * Formats the body of the table model or the data
    * @param lens : An of array of lengths
    * @return A StringBuilder containing the formatted model.
    */
   private StringBuilder printBody (int[] lens)
   {
      StringBuilder buff = new StringBuilder ("");
      int rowCount = model.getRowCount();
      int colCount = model.getColumnCount();

      for (int row = 0; row < rowCount; row++)
      {
         for (int col = 0; col < colCount; col++)
         {
            if (lens[col] > 0)
            {
               String value = (String) model.getValueAt (row, col);
               String sepr = (col == colCount - 1) ? "" : columnSeparator;
               if (value != null)
                  buff.append (fitValue (value.trim(), lens[col], col) + sepr);
               else
                  buff.append (append (lens[col], " ") + sepr);
            }
         }
         buff.append ("\n");
      }
      return (buff);
   }

   /**
    * Formats the separators.
    * @param lens : The Column Lengths.
    * @return A StringBuilder containing the separators.
    */
   private StringBuilder writeSeparators (int[] lens)
   {
      StringBuilder buff = new StringBuilder ("");
      int colCount = model.getColumnCount();
      for (int col = 0; col < colCount; col++)
         if (lens[col] > 0)
         {
            String sepr = (col == colCount - 1) ? "" : columnSeparator;
            buff.append (append (lens[col], "-") + sepr);
         }
      buff.append ("\n");
      return (buff);
   }
}
