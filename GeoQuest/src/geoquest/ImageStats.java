package geoquest;

import java.awt.Color;
import java.awt.Font;

public class ImageStats
{
   // Fonts
   // private static final String FONT_NAME = "Artifika";
   // private static final String FONT_NAME = "Bubblegum Sans";
   // private static final String FONT_NAME = "Eurostile";
   // private static final String FONT_NAME = "Veggieburger";
   // "Enigmatic Regular"
   // "Hattori Hanzo";
   
   int cardW, cardH;
   int centerX, centerY;
   int cutMarginW, cutMarginH;
   int safeMarginW, safeMarginH;
   int safeW, safeH;
   int artW, artH;

   String titleFontName;
   Font titleFont, titleFont2, titleFont3;
   Font textFont, textFont2;
   Color titleBg;

   // Equipment only
   Font comboFont, comboFont2;
   Color comboColor;
   
   // Event only
   Font playFont;
   Color playAnyColor, playNowColor;
   
   public static ImageStats getEquipmentStats()
   {
      ImageStats stats = new ImageStats();
      
      stats.cardW = 600;
      stats.cardH = 825;
      stats.centerX = stats.cardW / 2;
      stats.centerY = stats.cardH / 2;
      
      stats.safeMarginW = Math.round(stats.cardW / 100f * 12.4f);
      stats.safeMarginH = Math.round(stats.cardH / 100f * 9f);
      stats.safeW = stats.cardW - (stats.safeMarginW * 2);
      stats.safeH = stats.cardH - (stats.safeMarginH * 2);

      stats.cutMarginH = Math.round(stats.cardH / 100f * 4.5f);
      stats.cutMarginW = Math.round(stats.cardW / 100f * 6.1f);

      // size of the art on the final card
      stats.artW = 450;
      stats.artH = 275;
      
      stats.titleFontName = "Bree Serif";
      stats.titleFont = new Font(stats.titleFontName, Font.PLAIN, 60);
      stats.titleFont2 = new Font(stats.titleFontName, Font.PLAIN, 50);
      stats.titleFont3 = new Font(stats.titleFontName, Font.PLAIN, 42);
      stats.titleBg = new Color(255, 215, 0); // gold
         
      stats.textFont = new Font("Cabin", Font.PLAIN, 50);
      stats.textFont2 = new Font("Cabin", Font.PLAIN, 40);
      
      stats.comboFont = new Font(stats.titleFontName, Font.BOLD, 60);
      stats.comboFont2 = new Font(stats.titleFontName, Font.BOLD, 50);
      stats.comboColor = new Color(220, 160, 220); // plum
      
      return stats;
   }
   
   public static ImageStats getEventStats()
   {
      ImageStats stats = new ImageStats();
      
      stats.cardW = 825;
      stats.cardH = 1125;
      stats.centerX = stats.cardW / 2;
      stats.centerY = stats.cardH / 2;
      
      stats.safeMarginW = Math.round(stats.cardW / 100f * 9.5f);
      stats.safeMarginH = Math.round(stats.cardH / 100f * 6.7f);
      stats.safeW = stats.cardW - (stats.safeMarginW * 2);
      stats.safeH = stats.cardH - (stats.safeMarginH * 2);

      stats.cutMarginH = Math.round(stats.cardH / 100f * 3.5f);
      stats.cutMarginW = Math.round(stats.cardW / 100f * 4.8f);

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
