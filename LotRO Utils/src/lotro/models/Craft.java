package lotro.models;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Craft
{
   private Vocation vocation;
   private Map<Profession, Integer> proficiencies = new HashMap<Profession, Integer>();
   private Map<Profession, Integer> masteries = new HashMap<Profession, Integer>();
   
   public Craft (final Vocation vocation)
   {
      this.vocation = vocation;
   }
   
   public Vocation getVocation()
   {
      return vocation;
   }
   public void setVocation (final Vocation vocation)
   {
      this.vocation = vocation;
   }
   
   public List<Profession> getProfessions()
   {
      return vocation.getProfessions();
   }

   public int getProficiency (final Profession profession) 
   {
      return proficiencies.get (profession);
   }
   public void setProficiency (final Profession profession, final int level)
   {
      proficiencies.put (profession, level);
   }

   public int getMastery (final Profession profession)
   {
      return masteries.get (profession);
   }
   public void setMastery (final Profession profession, final int level)
   {
      masteries.put (profession, level);
   }
   
   public String getTitle (final Profession profession)
   {
      int p = getProficiency (profession);
      int m = getMastery (profession);

      StringBuilder title = new StringBuilder();
      if (p > m)
      {
         switch (p)
         {
         case 1: title.append ("Apprentice"); break;
         case 2: title.append ("Journeyman"); break;
         case 3: title.append ("Expert"); break;
         case 4: title.append ("Artisan"); break;
         case 5: title.append ("Master"); break;
         case 6: title.append ("Supreme"); break;
         default: title.append ("Unknown"); break;
         }
         
         if (m > 0)
            title.append (", ");
      }
      else if (m == 0)
         title.append ("&nbsp;");
      
      switch (m)
      {
      case 1: title.append ("Master Apprentice"); break;
      case 2: title.append ("Master Journeyman"); break;
      case 3: title.append ("Master Expert"); break;
      case 4: title.append ("Master Artisan"); break;
      case 5: title.append ("Grand Master"); break;
      case 6: title.append ("Supreme Master"); break;
      default: title.append ("Unknown"); break;
      }
      
      return title.toString();
   }
   
   public String getTooltipText (final String name)
   {
      StringBuilder sb = new StringBuilder ("<div class=\"htmltooltip\">");
      sb.append (name + ":<br/>");
      for (Profession p : vocation.getProfessions())
      {
         String title = getTitle (p);
         if (title.equals ("&nbsp;"))
            sb.append (p + "<br/>");
         else
            sb.append (title + "&nbsp;" + p + "<br/>");
      }
      sb.append ("</div>");
      return sb.toString();
   }
   
   public String getTooltipLink()
   {
      StringBuilder sb = new StringBuilder ("<a href=\"#\" rel=\"htmltooltip\">");
      sb.append (vocation.toString());
      sb.append ("</a>");
      return sb.toString();
   }
   
   @Override
   public String toString()
   {
      return vocation.toString();
   }
}
