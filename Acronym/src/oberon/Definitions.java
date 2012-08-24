package oberon;

import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * This class represents a set of possible definitions for an acronym.
 */
public class Definitions implements Comparable<Definitions>
{
   private SortedSet<Definition> set = new TreeSet<Definition>();
   private Definition selected;
   
   public Definition add (final String text, final Source source)
   {
      Definition newDef = new Definition (text, source);
      Iterator<Definition> iter = set.iterator();
      while (iter.hasNext())
      {
         Definition oldDef = iter.next();
         if (oldDef.getText().equals (text))
            if (oldDef.getSource().ordinal() > source.ordinal())
               iter.remove();
      }
      
      set.add (newDef);
      if (selected == null)
         setSelected (newDef);
      return newDef;
   }

   public Definition getSelected()
   {
      return selected;
   }

   public void setSelected (final Definition selected)
   {
      this.selected = selected;
   }

   public void setSelected (final String text)
   {
      for (Definition definition : set)
         if (definition.getText().equals (text))
         {
            this.selected = definition;
            break;
         }
   }

   public Definition getFirst()
   {
      return set.isEmpty() ? null : set.first(); 
   }
   
   public boolean contains (final Definition definition)
   {
      return isEmpty() ? false : contains (definition.getText());
   }
   
   public boolean contains (final String text)
   {
      for (Definition definition : set)
         if (definition.getText().equals (text))
            return true;
      return false;
   }
   
   public SortedSet<Definition> getDefinitions()
   {
      return set;
   }
   
   public boolean isEmpty()
   {
      return set.isEmpty();
   }
   
   public int size()
   {
      return set.size();
   }
   
   public int size (final Source source)
   {
      int count = 0;
      for (Definition definition : set)
         if (definition.getSource() == source)
            count++;
      return count;
   }
   
   @Override
   public String toString()
   {
      return getSelected() != null ? getSelected().getText() : "";
   }

   public int compareTo (final Definitions d2)
   {
      String s1 = "~", s2 = "~"; // force sort at end
      if (!isEmpty() && getSelected() != null)
         s1 = getSelected().getSource().ordinal() + getSelected().getText();          
      if (d2 != null && !d2.isEmpty() && d2.getSelected() != null)
         s2 = d2.getSelected().getSource().ordinal() + d2.getSelected().getText();          
      return s1.compareTo (s2);
   }
}
