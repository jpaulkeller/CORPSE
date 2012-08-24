package model;

import java.util.LinkedHashMap;
import java.util.Map;
 
public class NumericMapLinked<K, V extends Number> extends LinkedHashMap<K, V>
{
   private static final long serialVersionUID = 0;
   
   private final Class<V> numericType;
   private final ArithmeticType arithmeticType;
   
   public NumericMapLinked (final Class<V> numericType)
   {
      this.numericType = numericType;
      this.arithmeticType = ArithmeticType.forClass (numericType);
   }
   
   public NumericMapLinked (final Class<V> numericType, final Map<K, V> map)
   {
      this (numericType);
      putAll (map);
   }
 
   /** Add the given value to the current value, and return the new value. */
   
   public V plus (final K key, final V toAdd)
   {
      if (toAdd == null || toAdd.doubleValue() == 0)
         return get (key);
      
      Number currentValue = get (key);
      if (currentValue == null || currentValue.doubleValue() == 0)
      {
         put (key, toAdd);
         return toAdd;
      }
      
      V v = numericType.cast (arithmeticType.add (currentValue, toAdd));
      put (key, v);
      return v;
   }
   
   /** Subtract the given value from the current value, and return the new value. */
   
   public V minus (final K key, final Number toSub)
   {
      if (toSub == null || toSub.doubleValue() == 0)
         return get (key);
      
      Number currentValue = get (key);
      if (currentValue == null)
         currentValue = Integer.valueOf (0);
      
      V v = numericType.cast (arithmeticType.sub (currentValue, toSub));
      put (key, v);
      return v;
   }
   
   public int getInt (final K key)
   {
      Number value = get (key);
      return value != null ? value.intValue() : 0;
   }
   
   public long getLong (final K key)
   {
      Number value = get (key);
      return value != null ? value.longValue() : 0;
   }
   
   public float getFloat (final K key)
   {
      Number value = get (key);
      return value != null ? value.floatValue() : 0;
   }
   
   public double getDouble (final K key)
   {
      Number value = get (key);
      return value != null ? value.doubleValue() : 0;
   }
   
   public static void main (final String[] args)
   {
      NumericMapLinked<String, Integer> map = 
         new NumericMapLinked<String, Integer> (Integer.class);

      for (int i = 0; i < 3; i++)
      {
         String key = "k" + i;
         map.put (key, i);
         System.out.println (key + " -> " + map.get (key));
         map.plus (key, i);
      }
      System.out.println();
      
      for (int i = 0; i < 4; i++)
      {
         String key = "k" + i;
         System.out.print (key + " -> " + map.getInt (key));
         map.plus (key, i);
         System.out.print (" +" + i + " = " + map.getInt (key));
         map.minus (key, 1);
         System.out.println (" -1 = " + map.getInt (key));
      }
   }
   
   public enum ArithmeticType
   {
      INTEGER
      {
         @Override
         public Class<? extends Number> getNumericType()
         {
            return Integer.class;
         }
         @Override
         public Number add (final Number n1, final Number n2)
         {
            return Integer.valueOf (n1.intValue() + n2.intValue());
         }
         @Override
         public Number sub (final Number n1, final Number n2)
         {
            return Integer.valueOf (n1.intValue() - n2.intValue());
         }
      },
      
      FLOAT
      {
         @Override
         public Class<? extends Number> getNumericType() 
         {
            return Float.class;
         }
         @Override
         public Number add (final Number n1, final Number n2) 
         {
            return Float.valueOf (n1.floatValue() + n2.floatValue());
         }
         @Override
         public Number sub (final Number n1, final Number n2) 
         {
            return Float.valueOf (n1.floatValue() - n2.floatValue());
         }
      },
      
      DOUBLE
      {
         @Override
         public Class<? extends Number> getNumericType() 
         {
            return Double.class;
         }
         @Override
         public Number add (final Number n1, final Number n2) 
         {
            return Double.valueOf (n1.doubleValue() + n2.doubleValue());
         }
         @Override
         public Number sub (final Number n1, final Number n2) 
         {
            return Double.valueOf (n1.doubleValue() - n2.doubleValue());
         }
      };
      
      public abstract Class<? extends Number> getNumericType();
      public abstract Number add (Number n1, Number n2); // n1 + n2
      public abstract Number sub (Number n1, Number n2); // n1 - n2
      
      public static ArithmeticType forClass (final Class<? extends Number> c)
      {
         for (ArithmeticType type : values())
            if (type.getNumericType() == c)
               return type;
         throw new IllegalArgumentException ("Invalid type: " + c.getName());
      }
   }
}
