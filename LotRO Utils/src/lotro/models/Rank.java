package lotro.models;

public enum Rank
{
   None, Recruit, Member, Officer, Leader, Unknown;

   @Override
   public String toString()
   {
      return this.equals(Rank.None) ? "" : super.toString();
   }
   
   public static Rank parse (final String name)
   {
      if (name != null)
         for (Rank rank : Rank.values())
            if (name.equalsIgnoreCase (rank.toString()))
               return rank;

      System.err.println ("Invalid Rank: " + name);
      return Rank.None;
   }
}
