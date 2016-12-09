package geoquest;

import java.awt.Color;
import java.awt.Font;

public class ImageStats
{
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
      
      String fontName = "Archivo Narrow";
      stats.textFont = new Font(fontName, Font.PLAIN, fontSize);

      return stats;
   }
}
