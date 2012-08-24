package db;

import java.io.Serializable;

/**
 * The <b>Column</b> class provides a level of abstraction for table
 * columns (or fields).  A Column object contains the meta-data information
 * associated with a table field, such as its type.
 *
 * @see comet.db.tbl.Row
 * @see comet.db.tbl.Table
 */

public class Column implements Cloneable, Serializable
{
   private static final long serialVersionUID = 3;

   private String name;
   private int width;                   // Size of the field
   private int type;                    // from SQLTypes (and java.sql.Types)
   private String typeName;             // note: does not correlate 1-to-1 with type
   private boolean sizeRequired;        // some fields like VARCHAR(n) require a size
   private int precision;               // decimal digits
   private boolean editable = true;

   /**
    * Constructs a column The name may be case-sensitive, so callers should use 
    * SQLU.formatName() in general.
    *
    * @param name a name
    * @param type an SQL type (from SQLTypes)
    * @param typeName an SQL type name */

   public Column (final String name, final int type, final String typeName)
   {
      this.name = name;
      this.type = type;
      this.typeName = typeName;
   }
   
   /**
    * @param name a name
    * @param type an SQL type (from SQLTypes)
    * @param typeName an SQL type name
    * @param    width   width of field
    */

   public Column (final String name, final int type,
                  final String typeName, final int width)
   {
      this (name, type, typeName);
      this.width = width;
   }

   /**
    * @param name a name
    * @param type an SQL type (from SQLTypes)
    */

   public Column (final String name, final int type)
   {
      this (name, type, SQLTypes.toString (type));
   }
   /**
    * Constructs a column.
    *
    * @param name a name
    * @param type an SQL type (from SQLTypes)
    * @param    width   width of field
    */

   public Column (final String name, final int type, final int width)
   {
      this (name, type);
      this.width = width;
   }
   
   /** Clone a column. */
      
   @Override
   public Object clone()
   {
      Object copy = null;
      try
      {
         copy = super.clone();
      }
      catch (CloneNotSupportedException x)
      {
         x.printStackTrace();
      }
      return (copy);
   }
   
   /**
    * Returns the name of the column.  The name does not need to be
    * unique within a table (though it usually is).
    *
    * @return the name of the column
    */

   public String getName()
   {
      return (name);
   }

   /**
    * Returns the width of the column.  
    *
    * @return the width of the column
    */

   public int getWidth()
   {
      return (width);
   }
   
   /** Sets the width of the column. */

   public void setWidth (final int width)
   {
      this.width = width;
   }
   
   /** Returns true if this column requires a size. */

   public boolean isSizeRequired()
   {
      return (sizeRequired);
   }
   
   /** Sets whether this column requires a size. */

   public void setSizeRequired (final boolean sizeRequired)
   {
      this.sizeRequired = sizeRequired;
   }
   
   public int getPrecision()
   {
      return (precision);
   }
   
   public void setPrecision (final int decimalDigits)
   {
      this.precision = decimalDigits;
   }
   
   /**
    * Returns the type of the column.  This is an integer value from
    * SQLTypes.
    *
    * @return the column type
    * @see SQLTypes
    * @see java.sql.Types
    */

   public int getType()
   {
      return (type);
   }
   
   /** Sets the type of the column. */

   public void setType (final int type)
   {
      this.type = type;
   }
   
   /**
    * Note: For some reason, the TYPENAME does not correlate with the
    * TYPE.  For example, type 12 can be VARCHAR or TEXT. */
   
   public String getTypeName()
   {
      return typeName != null ? typeName.toUpperCase() : null;
   }
   
   public void setTypeName (final String typeName)
   {
      this.typeName = typeName.toUpperCase();
   }
   
   @Override
   public String toString() 
   {
      return ("Name: " + name +
              "; Type: " + typeName + "/" + type +
              "; Size: " + width + "/" + precision);
   }

   public void setEditable (final boolean isEditable)
   {
      this.editable = isEditable;
   }
   
   public boolean getEditable()
   {
      return (editable);
   }
}
