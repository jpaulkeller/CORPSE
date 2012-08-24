package db;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * The <b>Row</b> class provides a level of abstraction for table
 * rows.  Rows are simply lists of objects.  Convenience methods are
 * provided for constructing rows.
 */

public class Row extends ArrayList<Object> implements Serializable
{
   private static final long serialVersionUID = 4;

   private boolean editable = true;

   /**
    * Constructs an empty Row vector.
    */
   public Row()
   {
      super();
   }

   /**
    * Constructs an empty Row vector with the specified initial capacity.
    *
    * @param   initialCapacity   the initial capacity of the vector.
    */

   public Row (final int initialCapacity)
   {
      super (initialCapacity);
   }

   /**
    * Constructs a Row with values from the given <i>array</i>.
    *
    * <code>
    * new Row (new Object[] {"val1", "val2", ..., "valN"});
    * </code>
    *
    * @param  array  an array containing the values for the new Row
    */

   public Row (final Object[] array)
   {
      super (array.length);
      for (int i = 0; i < array.length; i++)
         add (array[i]);
   }

   /**
    * Returns a String showing the row values terminated by tabs.
    * This is mostly useful for debugging purposes, and will not be
    * appropriate for some rows (with unusual values). */

   public String asString()
   {
      StringBuilder buf = new StringBuilder ("");
      for (Object element : this)
         buf.append (element + "\t");
      return buf.toString();
   }

   public void setEditable (final boolean editable)
   {
      this.editable = editable;
   }

   public boolean isEditable()
   {
      return editable;
   }
}
