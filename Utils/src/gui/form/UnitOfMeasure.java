package gui.form;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * This class supports the conversion of numeric data from between
 * different units of measurement. */

public class UnitOfMeasure implements Serializable
{
   private static final long serialVersionUID = 14;

   private String base;         // the unit in which the data is stored
   private String standard;     // the default unit
   private Map<String, Unit> units; // maps unit key to Unit objects

   /** Client must call setStandard() or loadFromTable(). */

   public UnitOfMeasure()
   {
      units = new Hashtable<String, Unit>();
   }

   /**
    * The "standard" unit is the unit in which the data is stored. The
    * getStandardValue() method in MeasurementItem will return the
    * value converted to the standard unit. */

   public void setStandard (final String standard)
   {
      this.standard = standard; // standard unit key (e.g., "km")
   }

   public String getStandard()
   {
      return standard;
   }

   /**
    * The "base" unit is the unit in which the data is manipulated.
    * This should be the smallest unit, so that no data is lost during
    * conversion.  Conversion factors represent numbers which are
    * multiplied with the value to get a base value. */

   public void setBase (final String base)
   {
      this.base = base;
   }

   public String getBase()
   {
      return base;
   }

   /** Return a set of the unit keys (e.g., km, miles, etc). */

   public Set<String> getUnits()
   {
      return units.keySet();
   }

   /** Return a set of the unit names (e.g., kilometers, miles, etc). */

   public Set<String> getUnitNames()
   {
      Set<String> names = new TreeSet<String>();
      for (String u : getUnits())
         names.add (getName (u));
      return names;
   }

   /** Given the unit name (such as kilometers) return the unit key (km). */

   public String getUnit (final String name)
   {
      for (String key : units.keySet())
         if (name.equalsIgnoreCase (getName (key)))
            return key;
      return null;
   }

   /** Given the unit key (such as km) return the unit name (kilometers). */

   public String getName (final String unitKey)
   {
      String name = null;

      Unit unit = units.get (unitKey);
      if (unit == null)
         System.err.println ("Unsupported unit: " + unitKey);
      else
         name = unit.getName();

      return name;
   }

   public void addUnit (final String unitKey, final String name, final double toBase)
   {
      if (!Double.isNaN (toBase))
      {
         units.put (unitKey, new Unit (name != null ? name : unitKey, toBase));
         if (toBase == 1.0)
            setBase (unitKey);
      }
      else
         System.err.println ("Invalid Unit of Measure:" + unitKey + " " + name);
   }

   /** Converts the given value to the standard unit type. */

   public double convert (final double value, final String from)
   {
      return convert (value, from, standard);
   }

   public double convert (final double value, final String from, final String to)
   {
      if (from == null || to == null)
         throw new IllegalArgumentException
            ("UnitOfMeasure invalid unit conversion: " + from + " to " + to);

      if (from.equalsIgnoreCase (to))
         return value;        // no conversion necessary

      double toValue = Double.NaN;

      Unit unit = units.get (from.toLowerCase());
      if (unit == null)
         System.err.println ("From unit [" + from + "] is not supported");
      else
      {
         double baseValue = unit.convertTo (value);
         if (to.equalsIgnoreCase (base))
            toValue = baseValue;
         else
         {
            unit = units.get (to.toLowerCase());
            if (unit != null)
               toValue = unit.convertFrom (baseValue);
            else
               System.err.println ("To unit [" + to + "] is not supported");
         }
      }

      return toValue;
   }

   static class Unit implements Serializable
   {
      private static final long serialVersionUID = 14;

      private String name;
      private double toBase;    // value * toBase = base unit

      public Unit (final String name, final double toBase)
      {
         setName (name);
         setToBase (toBase);
      }

      public double convertTo (final double value)
      {
         return value * toBase;
      }

      public double convertFrom (final double value)
      {
         return value / toBase;
      }

      public final String getName()
      {
         return name;
      }

      public void setName (final String name)
      {
         this.name = name;
      }

      public final double getToBase()
      {
         return toBase;
      }

      public void setToBase (final double toBase)
      {
         this.toBase = toBase;
      }

      @Override
      public String toString()
      {
         return name + " (" + toBase + ")";
      }
   }

   public static void main (final String[] args) // for testing
   {
      UnitOfMeasure dist = new UnitOfMeasure();
      String standard = "miles";
      dist.setStandard (standard);
      dist.addUnit ("cm", "centimeters", 1.0); // base
      dist.addUnit ("km", "kilometers", 100000.0);
      dist.addUnit ("feet", "feet", 30.48);
      dist.addUnit ("miles", "miles", 160934);

      // test getUnits() and getName()
      System.out.println ("getUnits():");
      for (String unit : dist.getUnits())
         System.out.println ("  " + unit + " (" + dist.getName (unit) + ")");
      System.out.println("");

      // test getUnitNames() and getUnit()
      System.out.println ("getUnitNames():");
      for (String name : dist.getUnitNames())
         System.out.println ("  " + name + " (" + dist.getUnit (name) + ")");
      System.out.println("");

      // test a simple conversion
      System.out.println ("convert (1.0, \"km\") => ");
      System.out.println (dist.convert (1.0, "km") + " " + standard);

      // test a two-step conversion
      System.out.println ("convert (0.5, \"km\", \"feet\") => ");
      System.out.println (dist.convert (0.5, "km", "feet") + " feet");

      // test an unsupported conversion
      System.out.println ("convert (1.0, \"inches\") => ");
      System.out.println (dist.convert (1.0, "inches") + " " + standard);

      System.exit (0);
   }
}
