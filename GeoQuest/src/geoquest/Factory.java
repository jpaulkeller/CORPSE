package geoquest;

public class Factory
{
   public static final String ROOT = "G:/pkgs/workspace/GeoQuest/docs/";
   public static final String ART_DIR = ROOT;
   public static Language LANGUAGE = Language.ENGLISH;
   
   private static ImageStats stats;
   private static ImageGenerator imgGen = new ImageGenerator();

   public static void setLanguage(final Language language)
   {
      Factory.LANGUAGE = language;
   }

   public static String getRoot()
   {
      if (LANGUAGE == Language.FRENCH)
         return ROOT + Language.FRENCH + "/";
      return ROOT;
   }

   public static void setImageStats(final ImageStats stats)
   {
      Factory.stats = stats;
      imgGen.setImageStats(stats);
   }
   
   public static ImageStats getImageStats()
   {
      return stats;
   }
   
   public static ImageGenerator getImageGenerator()
   {
      return imgGen;
   }

   private static void generateHTML()
   {
      // HtmlGenerator htmlGen = new HtmlGenerator(80, 8, 0, 0, 0, 0);
      // htmlGen.printPuzzles(PUZZLES);
      
      HtmlGenerator htmlGen = new HtmlGenerator(80, 8);
      htmlGen.printTravelBugs(TravelBug.BUGS);
   }
   
   private void publishCachers()
   {
      Factory.setImageStats(Cacher.getImageStats());
      
      HtmlGenerator htmlGen = new HtmlGenerator(12, 3);
      htmlGen.printCachers(Cacher.CACHERS);

      for (Cacher cacher : Cacher.CACHERS.values())
         cacher.publish();
      System.out.println();

      Factory.setLanguage(Language.FRENCH);
      for (Cacher cacher : Cacher.CACHERS.values())
         cacher.publish();
      System.out.println();
   }
   
   public static void main(String[] args)
   {
      Factory factory = new Factory();
      factory.publishCachers();
   }
}
