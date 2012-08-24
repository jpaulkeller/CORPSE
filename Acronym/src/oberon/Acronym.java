package oberon;

import java.awt.Color;

public class Acronym implements Comparable<Acronym>
{
   private String abbrev;
   private String context;
   private Definitions definitions;

   public Acronym (final String abbrev)
   {
      this.abbrev = abbrev;
      definitions = new Definitions();
   }
   
   public String getAbbrev()
   {
      return abbrev;
   }
   
   public void setContext (final String newContext) 
   {
      context = newContext;
   }
   
   public String getContext() 
   {
      return context;
   }
   
   public Definition addValue (final String text, final Source source)
   {
      if (definitions.size() == 0)
         definitions.add (Definition.EMPTY, Source.Empty);
      Definition definition = definitions.add (text, source);
      if (getSelected() == null || getSelected().getSource() == Source.Empty)
         setSelected (definition);      
      return definition;
   }
   
   public boolean isDefined()
   {
      return !definitions.isEmpty();
   }
   
   public Definitions getDefinitions()
   {
      return definitions;
   }
   
   public boolean contains (final Definition definition)
   {
      return definitions.contains (definition);
   }
   
   public Definition getSelected()
   {
      return definitions.getSelected();
   }

   public void setSelected (final Definition selected)
   {
      definitions.setSelected (selected);
   }
   
   public Source getSource()
   {
      Source source = null;
      Definition selected = getSelected();
      if (selected != null)
         source = selected.getSource();
      return source;
   }
   
   private static final Color DEFAULT_COLOR = new Color (245, 240, 240);
   
   public Color getBackgroundColor()
   {
      Source source = getSource();
      return source != null ? source.getColor() : DEFAULT_COLOR;
   }
   
   public String getToolTipText() 
   {
      if (context == null)
         return "(searching for context)";

      if (context.equals (""))
         return "(context not found)";
         
      StringBuilder html = new StringBuilder();
      html.append ("<html>");
      html.append ("<body>");
      html.append (context.replaceAll ("(" + getAbbrev() + ")", "<b>$1</b>"));
      html.append ("</body>");
      html.append ("</html>");
      return html.toString(); 
   }
   
   @Override
   public String toString()
   {
      return abbrev;
   }
   
   @Override
   public boolean equals (final Object obj)
   {
      if (obj instanceof Acronym)
         return abbrev.equals (((Acronym) obj).getAbbrev());
      return false;
   }

   @Override
   public int hashCode()
   {
      return abbrev.hashCode();
   }

   public int compareTo (final Acronym other)
   {
      return abbrev.toUpperCase().compareTo (other.getAbbrev().toUpperCase()); 
   }
}
