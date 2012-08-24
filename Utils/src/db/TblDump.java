package db;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

/**
 * The TblDump class creates SQL drop/create/insert statements that
 * describe the definition of a table (and the data the table
 * contains).  These statements are written to stdout by default, but
 * may be redirected to any PrintStream, or to an ArrayList.  It does
 * this through the JDBC layer so it will work for any database for
 * which a featured JDBC driver exists.
 *
 * Basically, the way TblDump works is it asks the JDBC Layer about a
 * table definition, translates the types to standard JDBC types and
 * then, if a target database was specified, re-translates the types
 * to that database.
 *
 * If it is called from the command line, the format is as follows:
 *
 * <pre>
 * java comet.db.dbtools.TblDump
 *    {-c connection} {-S SQL | -t table}  -- the source data
 *    {-d driver-connection-target}        -- optional target DBMS
 *    {-s (skip schema)}                   -- don't write schema
 *    {-i (ignore inserts)}                -- don't write inserts
 * </pre>
 *
 * Issues:
 *
 * Currently, if the target database has a smaller limit on the size
 * of VARCHARS, the creation SQL is modified to suit the smaller
 * limit.  The data, however, is not checked and truncated although a
 * SQL insert error will occur if the resulting file is run.  */

public class TblDump
{
   private Model table;
   // private String sql;

   private ArrayList<String> arrayList;
   private Writer out;
   // private DatabaseDriver target;

   public TblDump()
   {
      // caller must call setModel()
   }

   public TblDump (final Model table)
   {
      this.table = table;
   }

   public void setModel (final Model model)
   {
      this.table = model;
   }

   /** Set the target DBMS driver.  Types will be translated to this
       this driver's preferences when the schema is output. */

   /*
   public void setTarget (final String connName) throws SQLException
   {
      DatabaseConnection conn = DatabaseConnection.connect (connName);
      this.target = conn.getDatabaseDriver();
   }
   */

   public void setArrayList (final ArrayList<String> list)
   {
      if (list != null)
      {
         this.arrayList = list;
         out = null;
      }
   }

   public ArrayList<String> getArrayList()
   {
      return arrayList;
   }

   public void setWriter (final Writer writer)
   {
      if (writer != null)
      {
         this.out = writer;
         arrayList = null;
      }
   }

   public Writer getWriter()
   {
      return out;
   }

   private void addStatement (final String statement) throws IOException
   {
      if (arrayList != null)
         arrayList.add (statement);
      else if (out != null)
         out.write (statement);
   }

   public void writeAll (final boolean schema, final boolean inserts)
      throws IOException
   {
      if (schema)
         writeSchema();
      if (inserts)
         writeInserts();
   }

   public void writeSchema() // throws IOException
   {
      /*
      ColumnMetaData cmd =
         new ColumnMetaData (table.getConnectionName(), table.getName());

      if (sql == null)
         addStatement ("drop table " + table.getName() + ";\n");
      else // assume subset and comment out drop statement
         addStatement ("# drop table " + table.getName() + ";\n");

      StringBuilder create = null;

      if (sql == null)
         create = new StringBuilder ("create table " + table.getName() + " (\n");
      else // assume subset and comment out create statement
         create = new StringBuilder ("# create table " + table.getName() + " (\n");

      for (int i = 0, colQty = cmd.getRowCount(); i < colQty; i++)
      {
         cmd.jump (i);
         Column column = makeColumn (cmd);
         if (sql != null) // assume subset and add commented
            create.append ("#");

         create.append ("\t" + getColumnDefinition (column));
         create.append (i + 1 < colQty ? ",\n" : "\n");
      }
      if (sql != null)
         create.append ("#"); // assume subset and add commented
      create.append (");\n\n");

      addStatement (create.toString());
      */
   }

   /*
   private Column makeColumn (final ColumnMetaData cmd)
   {
      Column column = cmd.getColumn();
      table.getDriver().translateJdbcType (column);
      return column;
   }
   */

   /*
   private String getColumnDefinition (final Column column)
   {
      String typeName = null;
      int dataType = column.getType();
      StringBuilder sb = new StringBuilder (column.getName() + "\t");

      if (target != null && target.translateJdbcType (column))
         typeName = column.getTypeName();
      else
      {
         typeName = SQLTypes.getCommonType (dataType);
         column.setTypeName (typeName);
      }

      if (dataType == Types.OTHER) // Try to change to Datasource name
      {
         if (column.getTypeName() != null)
         {
            int locType = SQLTypes.toInt (typeName);
            if (locType != 0)   // Translate to known type
               typeName = SQLTypes.toString (locType);
            else if (column.getWidth() > 0)  // unknown type, append size
               typeName = typeName + "(" + column.getWidth() + ")";
         }
      }
      sb.append (typeName);

      if (column.isSizeRequired())
         sb.append ("(" + column.getWidth() + ")");

      return sb.toString();
   }
   */

   public void writeInserts() throws IOException
   {
      int rowQty = table.getRowCount();
      int colQty = table.getColumnCount();
      StringBuilder insert = new StringBuilder();

      for (int row = 0; row < rowQty; row++)
      {
         insert.setLength (0);
         insert.append ("insert into " + table.getName() + " values (");

         for (int col = 0; col < colQty; col++)
         {
            // Object value = table.getValueAt (row, col);
            // TBD
            // insert.append (table.toSQL (table.getColumnName (col), value));
            if (col + 1 < colQty)
               insert.append (", ");
         }
         insert.append (");\n");

         addStatement (insert.toString());
      }
   }

   public static void main (final String[] args) throws Exception
   {
      Model model = null; // TBD
      TblDump td = new TblDump (model);
      // td.setTarget (driver);
      td.writeAll (true, true);
   }
}
