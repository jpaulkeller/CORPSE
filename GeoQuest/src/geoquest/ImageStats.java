package geoquest;

import java.awt.Color;
import java.awt.Font;

public class ImageStats
{
   Language language;
   
   // Fonts
   // private static final String FONT_NAME = "Artifika";
   // private static final String FONT_NAME = "Bubblegum Sans";
   // private static final String FONT_NAME = "Eurostile";
   // private static final String FONT_NAME = "Veggieburger";
   // "Enigmatic Regular"
   // "Hattori Hanzo"; 
   
   int w, h; // component (card or token)
   int centerX, centerY;
   int cutMarginW, cutMarginH;
   int safeMarginW, safeMarginH;
   int safeW, safeH;
   int artW, artH;

   String titleFontName;
   Font titleFont, titleFont2, titleFont3;
   Font textFont, textFont2;
   Color titleBg;
   Color titleFg = Color.BLACK;

   // Cacher cards only
   Font letterFont;
   
   // Equipment cards only
   Font ensembleFont, ensembleFont2;
   Color ensembleColor;
   
   // Event cards only
   Font playFont;
   Color playAnyColor, playNowColor;

   public ImageStats()
   {
      this(Language.ENGLISH);
   }

   public ImageStats(final Language language)
   {
      this.language = language;
   }
   
   // TOKENS -----------------------------------------------------------------------------
   
   public static ImageStats getDiceStats() // square
   {
      ImageStats stats = new ImageStats();
      
      stats.w = 188;
      stats.h = 188;
      stats.centerX = stats.w / 2;
      stats.centerY = stats.h / 2;
      
      stats.safeMarginW = Math.round(stats.w / 100f * 12.4f);
      stats.safeMarginH = Math.round(stats.h / 100f * 9f);
      stats.safeW = stats.w - (stats.safeMarginW * 2);
      stats.safeH = stats.h - (stats.safeMarginH * 2);

      stats.cutMarginH = Math.round(stats.h / 100f * 4.5f);
      stats.cutMarginW = Math.round(stats.w / 100f * 6.1f);

      // size of the art on the final card
      stats.artW = 450;
      stats.artH = 275;
      
      return stats;
   }
   
   public static ImageStats getShardStats(final int fontSize) // travel bugs, puzzle tokens, and FTF coins 
   {
      ImageStats stats = new ImageStats();
      
      stats.w = 300;
      stats.h = 300;
      stats.centerX = stats.w / 2;
      stats.centerY = stats.h / 2;
      
      // String fontName = "Cabin";
      String fontName = "Archivo Narrow";
      // String fontName = "Gill Sans MT Condensed";
      // String fontName = "Steelfish";
      // String fontName = "WinterthurCondensed";
      stats.textFont = new Font(fontName, Font.PLAIN, fontSize);

      return stats;
   }
   
   // CARDS -----------------------------------------------------------------------------
   
   public static ImageStats getCacherStats()
   {
      ImageStats stats = new ImageStats();
      
      stats.w = 825;
      stats.h = 600;
      stats.centerX = stats.w / 2;
      stats.centerY = stats.h / 2;
      
      stats.safeMarginW = Math.round(stats.w / 100f * 9f);
      stats.safeMarginH = Math.round(stats.h / 100f * 12.4f);
      stats.safeW = stats.w - (stats.safeMarginW * 2);
      stats.safeH = stats.h - (stats.safeMarginH * 2);

      stats.cutMarginH = Math.round(stats.h / 100f * 6.1f);
      stats.cutMarginW = Math.round(stats.w / 100f * 4.5f);

      // stats.titleFontName = "Artifika";
      // stats.titleFontName = "Architects Daughter";
      // stats.titleFontName = "Condiment";
      stats.titleFontName = "Segoe Print";
      
      stats.titleFont = new Font(stats.titleFontName, Font.BOLD, 60);
      stats.titleFont2 = new Font(stats.titleFontName, Font.BOLD, 50);
      stats.titleBg = new Color(0, 229, 91, 175); // green, translucent
         
      stats.textFont = new Font("Cabin", Font.PLAIN, 70);
      stats.textFont2 = new Font("Cabin", Font.PLAIN, 60);
      
      // stats.letterFont = new Font("Bree Serif", Font.PLAIN, 80);
      stats.letterFont = new Font("Days", Font.PLAIN, 80);
      
      return stats;
   }
   
   public static ImageStats getEnsembleStats(final Language language)
   {
      ImageStats stats = new ImageStats(language);
      
      stats.w = 825;
      stats.h = 600;
      stats.centerX = stats.w / 2;
      stats.centerY = stats.h / 2;
      
      stats.safeMarginW = Math.round(stats.w / 100f * 9f);
      stats.safeMarginH = Math.round(stats.h / 100f * 12.4f);
      stats.safeW = stats.w - (stats.safeMarginW * 2);
      stats.safeH = stats.h - (stats.safeMarginH * 2);

      stats.cutMarginH = Math.round(stats.h / 100f * 6.1f);
      stats.cutMarginW = Math.round(stats.w / 100f * 4.5f);

      stats.titleFontName = "Bree Serif";
      stats.titleFont = new Font(stats.titleFontName, Font.BOLD, 60);
      stats.titleFont2 = new Font(stats.titleFontName, Font.BOLD, 50);
      stats.titleBg = Color.WHITE;
         
      stats.textFont = new Font("Cabin", Font.PLAIN, 70);
      stats.textFont2 = new Font("Cabin", Font.PLAIN, 60);
      
      return stats;
   }
   
   public static ImageStats getEquipmentStats(final Language language)
   {
      ImageStats stats = new ImageStats(language);
      
      stats.w = 600;
      stats.h = 825;
      stats.centerX = stats.w / 2;
      stats.centerY = stats.h / 2;
      
      stats.safeMarginW = Math.round(stats.w / 100f * 12.4f);
      stats.safeMarginH = Math.round(stats.h / 100f * 9f);
      stats.safeW = stats.w - (stats.safeMarginW * 2);
      stats.safeH = stats.h - (stats.safeMarginH * 2);

      stats.cutMarginH = Math.round(stats.h / 100f * 4.5f);
      stats.cutMarginW = Math.round(stats.w / 100f * 6.1f);

      // size of the art on the final card
      stats.artW = 450;
      stats.artH = 275;

      if (language == Language.FRENCH)
         stats.titleFontName = "EB Garamond 12";
      else
         stats.titleFontName = "Bree Serif";
         
      stats.titleFont = new Font(stats.titleFontName, Font.PLAIN, 60);
      stats.titleFont2 = new Font(stats.titleFontName, Font.PLAIN, 50);
      stats.titleFont3 = new Font(stats.titleFontName, Font.PLAIN, 44);
      // stats.titleBg = new Color(255, 215, 0); // gold
      stats.titleBg = new Color(255, 121, 0); // orange
         
      stats.textFont = new Font("Cabin", Font.PLAIN, 50);
      stats.textFont2 = new Font("Cabin", Font.PLAIN, 40);
      
      stats.ensembleFont = new Font(stats.titleFontName, Font.BOLD, 60);
      stats.ensembleFont2 = new Font(stats.titleFontName, Font.BOLD, 50);
      stats.ensembleColor = new Color(150, 150, 150); // grey
      
      return stats;
   }
   
   public static ImageStats getEventStats()
   {
      ImageStats stats = new ImageStats();
      
      stats.w = 825;
      stats.h = 1125;
      stats.centerX = stats.w / 2;
      stats.centerY = stats.h / 2;
      
      stats.safeMarginW = Math.round(stats.w / 100f * 9.5f);
      stats.safeMarginH = Math.round(stats.h / 100f * 6.7f);
      stats.safeW = stats.w - (stats.safeMarginW * 2);
      stats.safeH = stats.h - (stats.safeMarginH * 2);

      stats.cutMarginH = Math.round(stats.h / 100f * 3.5f);
      stats.cutMarginW = Math.round(stats.w / 100f * 4.8f);

      // size of the art on the final card
      stats.artW = 650;
      stats.artH = 400;
      
      stats.titleFontName = "Bree Serif";
      stats.titleFont = new Font(stats.titleFontName, Font.PLAIN, 60);
      stats.titleFont2 = new Font(stats.titleFontName, Font.PLAIN, 50);
      stats.titleFont3 = new Font(stats.titleFontName, Font.PLAIN, 42);
      stats.titleBg = new Color(255, 215, 0); // gold
         
      stats.textFont = new Font("Cabin", Font.PLAIN, 60);
      stats.textFont2 = new Font("Cabin", Font.PLAIN, 54);
      
      stats.playFont = new Font(stats.titleFontName, Font.BOLD, 60);
      stats.playAnyColor = new Color(152, 251, 152); // light green
      stats.playNowColor = new Color(255, 160, 122); // light salmon
      
      return stats;
   }
}
