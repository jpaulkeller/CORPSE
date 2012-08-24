package lotro.models;

public enum Rank
{
   Unknown, Recruit, Member, Officer, Leader;

   public static Rank parse (final String name)
   {
      if (name != null)
         for (Rank rank : Rank.values())
            if (name.equalsIgnoreCase (rank.toString()))
               return rank;

      System.err.println ("Invalid Rank: " + name);
      return Rank.Unknown;
   }
}
