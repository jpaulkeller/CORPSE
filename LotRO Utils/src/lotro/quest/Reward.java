package lotro.quest;

public class Reward implements Comparable<Reward>
{
   private String type;
   private String name;
   private String link;
   private boolean option;
   
   public Reward (final String type, final String name, final String link, 
                  final boolean option)
   {
      this.type = type;
      this.name = name;
      this.link = link;
      this.option = option;
   }
   
   public String getName()
   {
      return name;
   }

   public String getLink()
   {
      return link;
   }

   public boolean isOption()
   {
      return option;
   }

   @Override
   public boolean equals (final Object o)
   {
      if (o instanceof Reward)
         return name.equals (((Reward) o).name);
      return false;
   }
   
   public int compareTo (final Reward r)
   {
      return name.compareTo (r.name);
   }
   
   @Override
   public int hashCode()
   {
      return name.hashCode();
   }
   
   @Override
   public String toString()
   {
      return name;
   }
}
