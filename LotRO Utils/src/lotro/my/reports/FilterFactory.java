package lotro.my.reports;

import lotro.models.Character;
import lotro.models.Klass;
import lotro.models.Rank;

public final class FilterFactory
{
   private FilterFactory() { }
   
   public static CharacterFilter getLevelFilter (final int minLevel)
   {
      return new LevelFilter (minLevel);
   }

   public static CharacterFilter getLevelFilter (final int minLevel, final int maxLevel)
   {
      return new LevelFilter (minLevel, maxLevel);
   }

   public static CharacterFilter getClassFilter (final Klass klass)
   {
      return new ClassFilter (klass);
   }

   public static CharacterFilter getKinRankFilter (final Rank minRank)
   {
      return new KinRankFilter (minRank);
   }

   static class LevelFilter implements CharacterFilter
   {
      private int minLevel = 0;
      private int maxLevel = Character.MAX_LEVEL;
      
      public LevelFilter (final int minLevel)
      {
         this.minLevel = minLevel;
      }
      
      public LevelFilter (final int minLevel, final int maxLevel)
      {
         setLevels(minLevel, maxLevel);
      }
      
      public void setLevels(final int minLevel, final int maxLevel)
      {
         this.minLevel = minLevel;
         this.maxLevel = maxLevel;
      }
      
      public boolean include (final Character ch)
      {
         return ch.getLevel() >= minLevel && ch.getLevel() <= maxLevel;
      }
   }

   static class ClassFilter implements CharacterFilter
   {
      private Klass klass;
      
      public ClassFilter (final Klass klass)
      {
         this.klass = klass;
      }
      
      public boolean include (final Character ch)
      {
         return ch.getKlass() == klass;
      }
   }

   static class KinRankFilter implements CharacterFilter
   {
      private Rank minRank;
      
      public KinRankFilter (final Rank minRank)
      {
         this.minRank = minRank;
      }
      
      public boolean include (final Character ch)
      {
         return ch.getRank().ordinal() >= minRank.ordinal();
      }
   }
}
