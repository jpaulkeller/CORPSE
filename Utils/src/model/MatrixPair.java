package model;

public class MatrixPair<V>
{
   private int count;
   private V value;
   
   public MatrixPair (final int count, final V value)
   {
      this.count = count;
      this.value = value;
   }

   public int getCount()
   {
      return count;
   }

   public V getValue()
   {
      return value;
   }
   
   @Override
   public String toString()
   {
      return count + "x" + value;
   }
}
