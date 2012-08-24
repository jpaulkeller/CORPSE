package model;

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Data structure for storing raster (2D matrix) data. The value of
 * each cell in the matrix can by any object if type T. */

public class ObjectMatrix<T>
{
   private int rows;
   private int cols;
   private Object[][] data;

   public ObjectMatrix (final int rows, final int cols)
   {
      this.rows = rows;
      this.cols = cols;
      this.data = new Object[rows][cols];
   }

   public ObjectMatrix (final int rows, final int cols, final T[] data)
   {
      this.rows = rows;
      this.cols = cols;
      setData (data);
   }

   public ObjectMatrix (final ObjectMatrix<T> matrix)
   {
      this (matrix.rows, matrix.cols);
      for (int row = 0; row < rows; row++)
         for (int col = 0; col < cols; col++)
            setValue (row, col, matrix.getValue (row, col));
   }

   public void fill (final T value)
   {
      for (int row = 0; row < rows; row++)
         Arrays.fill (data[row], value);
   }

   public int getRowCount()
   {
      return rows;
   }

   public int getColumnCount()
   {
      return cols;
   }

   @SuppressWarnings("unchecked")
   public T getValue (final int row, final int col)
   {
      return (T) data[row][col];
   }

   public void setValue (final int row, final int col, final T value)
   {
      data[row][col] = value;
   }

   /** Replace the model with the given data. */
   
   protected void setData (final T[][] newData)
   {
      this.data = newData;
      this.rows = data.length;
      this.cols = data[0].length;
   }

   /** Populates the 2D array from a 1D array. */

   public void setData (final T[] objects)
   {
      for (int i = 0, row = 0; row < rows; row++)
         for (int col = 0; col < cols; col++)
            setValue (row, col, objects[i++]);
   }

   /**
    * Uses simple run-length encoding to compress the data.  Returns the data
    * as a List of MatrixPair objects.  Each pair consists of a quantity and
    * a value.  For example, a 10x10 matrix of containing all equal values V 
    * would be returned as a List containing one MatrixPair, where count = 100,
    * and value = V. */
   
   @SuppressWarnings("unchecked")
   public List<MatrixPair<T>> getDataCompressed()
   {
      List<MatrixPair<T>> compressed = new ArrayList<MatrixPair<T>>();
      T prev = null;
      int count = 0;

      for (int row = 0; row < rows; row++)
         for (int col = 0; col < cols; col++)
         {
            if (data[row][col] == prev) // both or either may be null
               count++;
            else if (prev != null && prev.equals (data[row][col]))
               count++;
            else
            {
               if (count > 0)
                  compressed.add (new MatrixPair<T> (count, prev));
               count = 1;
               prev = (T) data[row][col];
            }
         }

      compressed.add (new MatrixPair<T> (count, prev)); // add the last pair

      return compressed;
   }
   
   /**
    * Uncompressed the given 1D (compressed) data to populate the 2D
    * array. */

   public void setDataCompressed (final List<MatrixPair<T>> objects)
   {
      int i = 0;
      for (MatrixPair<T> pair : objects)
      {
         int count = pair.getCount();
         for (int j = 0; j < count; j++)
         {
            int row = i / cols;
            int col = i % cols;
            setValue (row, col, pair.getValue()); 
            i++;
         }
      }
   }

   /**
    * Displays the matrix to stdout by invoking the given (optional)
    * null-argument method on each object. */

   public void show (final PrintStream out, final String methodName)
   {
      for (int row = 0; row < rows; row++)
      {
         for (int col = 0; col < cols; col++)
         {
            try
            {
               String s = "?";
               Object o = data[row][col];
               if (o == null)
                  s = "";
               else
               {
                  Method method = null;
                  if (methodName != null) 
                     method = o.getClass().getMethod (methodName);
                  if (method != null)
                     o = method.invoke (o);
                  if (o != null)
                     s = o.toString();
               }
               out.print (s);
            }
            catch (Exception x)
            {
               System.err.println (x);
            }
         }
         out.println();
      }
      out.println();
   }

   public static void main (final String[] args)
   {
      int rows = 8;
      int cols = 12;
      ObjectMatrix<String> matrix = new ObjectMatrix<String> (rows, cols);

      matrix.fill ("-- ");
      for (int row = 0; row < rows; row++)
         for (int col = 0; col < cols; col++)
            if (row * col > 9)
               matrix.setValue (row, col, (row * col) + " ");
      matrix.show (System.out, null);

      matrix.fill ("1");
      for (int row = 0; row < rows; row++)
         for (int col = 0; col < cols; col++)
            if (row == col)
               matrix.setValue (row, col, row + "");
      matrix.show (System.out, null);

      System.out.println ("getDataCompressed (mixed):");
      List<MatrixPair<String>> data = matrix.getDataCompressed();
      for (MatrixPair<String> pair : data)
         System.out.println ("  " + pair);
      System.out.println();

      matrix = new ObjectMatrix<String> (20, 20);
      matrix.fill ("1");

      System.out.println ("getDataCompressed (all 1s):");
      List<MatrixPair<String>> other = matrix.getDataCompressed();
      for (MatrixPair<String> pair : other)
         System.out.println ("  " + pair);
      System.out.println();

      System.out.println ("setDataCompressed (mixed):");
      matrix = new ObjectMatrix<String> (rows, cols);
      matrix.setDataCompressed (data);
      matrix.show (System.out, null);
      System.out.println();
   }
}
