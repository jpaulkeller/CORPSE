package geoquest;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Stroke;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;

import str.Token;
import utils.ImageTools;

// Image Sources
// https://openclipart.org/
// https://pixabay.com/
// https://www.google.com/imghp

public class ImageGenerator
{
   private static final Pattern EM = Pattern.compile("([^<]+)</em>(.+)?");
   
   private static final Color CACHER_COLOR = new Color(0, 100, 0); // dark green
   private static final Color EVENT_COLOR = new Color(200, 30, 30); // dark red
   private static final Color FIND_COLOR = new Color(25, 125, 25); // dark green
   private static final Color FTF_COLOR = new Color(228, 205, 0); // gold
   public static final Color DIFF1_COLOR = new Color(0, 201, 255); // light blue
   private static final Color DIFF3_COLOR = Color.YELLOW;
   private static final Color DIFF5_COLOR = new Color(255, 104, 104); // reddish
   private static final Color TEXT_BG = new Color(255, 255, 255, 128); // translucent white
   
   public static final Stroke STROKE3 = new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, null, 0);
   private static final Stroke STROKE5 = new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, null, 0);
   public static final Stroke DASHED = new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[] { 3, 5 }, 0);

   private ImageStats stats;
   private boolean drawBoxes;
   
   public void setImageStats(final ImageStats stats)
   {
      this.stats = stats;
   }
   
   public void setDrawBoxes(final boolean drawBoxes)
   {
      this.drawBoxes = drawBoxes;
   }
   
   public boolean getDrawBoxes()
   {
      return drawBoxes;
   }
   
   public void paintText(final Graphics2D g, final String name, final String[] lines)
   {
      g.setColor(Color.BLACK);
      g.setFont(stats.textFont);
      FontMetrics fm = g.getFontMetrics(stats.textFont);
      int lineHeight = fm.getHeight() - 5;
      int textHeight = lines.length * lineHeight;
      
      int y = ((stats.h - textHeight) / 2) + lineHeight - 7;
      for (String line : lines)
      {
         int x = (stats.w - fm.stringWidth(line)) / 2;
         g.drawString(line, x, y); // lower-left
         y += lineHeight;
      }
   }

   public void paintTextTTS(final Graphics2D g, final String name, final String[] lines)
   {
      g.setColor(Color.BLACK);
      g.setFont(stats.textFont);
      FontMetrics fm = g.getFontMetrics(stats.textFont);
      int lineHeight = fm.getHeight() - 5;
      int textHeight = lines.length * lineHeight;
      
      int y = ((150 - textHeight) / 2) + lineHeight;
      for (String line : lines)
      {
         int x = (150 - fm.stringWidth(line)) / 2;
         g.drawString(line, x, y); // lower-left
         y += lineHeight;
      }
   }

   public void paintGrid(final Graphics2D g)
   {
      if (drawBoxes)
      {
         // grid lines
         g.setColor(Color.BLACK);
         g.drawLine(stats.centerX, 0, stats.centerX, stats.h);
         g.drawLine(0, stats.centerY, stats.w, stats.centerY);
      
         // cut line
         g.setColor(Color.RED);
         g.drawRect(stats.cutMarginW, stats.cutMarginH,
            stats.w - (stats.cutMarginW * 2), stats.h - (stats.cutMarginH * 2));
      }
   }

   public void paintArt(final Graphics2D g, final String imageName, final int top)
   {
      try
      {
         ImageIcon artImage = new ImageIcon(Factory.ART_DIR + imageName);
         ImageIcon artScaled = ImageTools.scaleImage(artImage, stats.artW, stats.artH, Image.SCALE_SMOOTH, null);
         BufferedImage artBuf = ImageTools.imageToBufferedImage(artScaled.getImage());
         g.drawImage(artBuf, (stats.w - artBuf.getWidth()) / 2, top, null); // upper-left
      }
      catch (Exception x)
      {
         System.err.println("Missing: "+ imageName);
      }
   }

   public int paintIcon(final Graphics2D g, final String imageName, final int titleHeight)
   {
      int h = Math.round(titleHeight * 1.5f);
      ImageIcon icon = new ImageIcon(Factory.ART_DIR + imageName);
      ImageIcon scaled = ImageTools.scaleImage(icon, h, h, Image.SCALE_SMOOTH, null);
      BufferedImage bi = ImageTools.imageToBufferedImage(scaled.getImage());
      int top = stats.safeMarginH + 2; // on title
      // int top = stats.safeMarginH + (h / 2) + 5; // super-imposed on title, shadowed
      int left = stats.w - stats.safeMarginW - bi.getWidth() + 5;
      g.drawImage(bi, left, top, null);
      return h;
   }

   public void addIcon(final Graphics2D g, final String path, final int w, final int h, final int left, final int top)
   {
      try
      {
         ImageIcon icon = new ImageIcon(path);
         ImageIcon scaled = ImageTools.scaleImage(icon, w, h, Image.SCALE_SMOOTH, null);
         BufferedImage bi = ImageTools.imageToBufferedImage(scaled.getImage());
         g.drawImage(bi, left, top, null);
      }
      catch (Exception x)
      {
         System.err.println("Missing: " + path);
         System.err.flush();
      }
   }
   
   public int paintTitle(final Graphics2D g, final Component card)
   {
      String name = card.getName();
      Font font = stats.titleFont;
      
      // hack for long titles with icons
      if (name.equals("Event Coordinator") || name.equals("Search and Rescue"))
         font = stats.titleFont2;
      
      int margin = 10;
      FontMetrics fm = g.getFontMetrics(font);
      g.setFont(font);
      int textHeight = fm.getHeight();
      int textWidth = fm.stringWidth(name);
      if (textWidth + margin > stats.safeW)
      {
         System.out.println(" [NAME TOO WIDE] > " + name);
         fm = g.getFontMetrics(stats.titleFont2);
         g.setFont(stats.titleFont2);
         textHeight = fm.getHeight();
         textWidth = fm.stringWidth(name);
         if (textWidth + margin > stats.safeW)
         {
            System.err.println(" [NAME TOO WIDE] > " + name);
            fm = g.getFontMetrics(stats.titleFont3);
            g.setFont(stats.titleFont3);
            textHeight = fm.getHeight();
            textWidth = fm.stringWidth(name);
         }
      }
      
      int textLeft = (stats.w - textWidth) / 2;
      // g.fillRect(0, 0, stats.cardW, stats.safeMarginH + nameHeight);
      g.setColor(stats.titleBg);
      int left = (stats.w - textWidth) / 2;
      int top = stats.safeMarginH + 2;
      RoundRectangle2D bg = new RoundRectangle2D.Float(left - 10, top + 2, textWidth + 20, textHeight + 4, 20, 20);
      g.fill(bg);
      g.setColor(stats.titleFg);
      Stroke origStroke = g.getStroke();
      g.setStroke(STROKE5);
      g.draw(bg);
      g.setStroke(origStroke);
      if (drawBoxes)
         g.drawRect(textLeft, stats.safeMarginH, textWidth, textHeight);
      g.drawString(name, textLeft, stats.safeMarginH + textHeight - 7); // lower-left
      
      return textHeight + 20;
   }

   public int paintTitleLeft(final Graphics2D g, final Component card)
   {
      String name = card.getName();
      Font font = stats.titleFont;
      
      int margin = 10;
      FontMetrics fm = g.getFontMetrics(font);
      g.setFont(font);
      int textHeight = fm.getHeight();
      int textWidth = fm.stringWidth(name);
      int iconWidth = card.getIcon() != null ? Math.round(textHeight * 1.5f) : 0;
      
      if (textWidth + margin + iconWidth > stats.safeW)
      {
         System.out.println(" [NAME TOO WIDE 1] > " + name);
         fm = g.getFontMetrics(stats.titleFont2);
         g.setFont(stats.titleFont2);
         textHeight = fm.getHeight();
         textWidth = fm.stringWidth(name);
         iconWidth = card.getIcon() != null ? Math.round(textHeight * 1.5f) : 0;
         
         if (textWidth + margin + iconWidth > stats.safeW)
         {
            System.err.println(" [NAME TOO WIDE 2] > " + name);
            fm = g.getFontMetrics(stats.titleFont3);
            g.setFont(stats.titleFont3);
            textHeight = fm.getHeight();
            textWidth = fm.stringWidth(name);
         }
      }
      
      int textLeft = stats.safeMarginW + 10; 
      g.setColor(stats.titleBg);
      int top = stats.safeMarginH + 2;
      g.fillRect(0, top, stats.w, textHeight + 4);
      
      // borders
      g.setColor(Color.BLACK);
      Stroke origStroke = g.getStroke();
      g.setStroke(STROKE5);
      int y = top - 2;
      g.drawLine(0, y, stats.w, y);
      y = top + textHeight + 4 + 2;
      g.drawLine(0, y, stats.w, y);

      // text
      g.setStroke(origStroke);
      g.drawString(name, textLeft, stats.safeMarginH + textHeight - 7); // lower-left
      
      return textHeight + 4;
   }

   public int paintTitleRight(final Graphics2D g, final Component card)
   {
      String name = card.getName();
      Font font = stats.titleFont;
      
      // hack for long titles with icons
      if (name.equals("Independent Isabel") || name.equals("Wandering Warren"))
         font = stats.titleFont2;
      
      int margin = 10;
      FontMetrics fm = g.getFontMetrics(font);
      g.setFont(font);
      int textHeight = fm.getHeight();
      int textWidth = fm.stringWidth(name);
      if (textWidth + margin > stats.safeW)
      {
         System.out.println(" [NAME TOO WIDE] > " + name);
         fm = g.getFontMetrics(stats.titleFont2);
         g.setFont(stats.titleFont2);
         textHeight = fm.getHeight();
         textWidth = fm.stringWidth(name);
         if (textWidth + margin > stats.safeW)
         {
            System.err.println(" [NAME TOO WIDE] > " + name);
            fm = g.getFontMetrics(stats.titleFont3);
            g.setFont(stats.titleFont3);
            textHeight = fm.getHeight();
            textWidth = fm.stringWidth(name);
         }
      }
      
      int textLeft = stats.w - stats.safeMarginW - textWidth - 10; 
      g.setColor(stats.titleBg);
      int top = stats.safeMarginH + 2;
      g.fillRect(0, top + 25, stats.w, textHeight - 25);
      
      // borders
      g.setColor(Color.BLACK);
      Stroke origStroke = g.getStroke();
      g.setStroke(STROKE5);
      int y = top + 25;
      g.drawLine(0, y, stats.w, y);
      y = top + textHeight;
      g.drawLine(0, y, stats.w, y);

      // text
      g.setStroke(origStroke);
      g.drawString(name, textLeft, stats.safeMarginH + textHeight - 20); // lower-left
      
      return textHeight + 4;
   }

   public void paintText(final Graphics2D g, final Component card, final int top, final int bottom, final int maxLines)
   {
      int boxHeight = bottom - top;

      if (drawBoxes) // text box
      {
         g.setColor(Color.GREEN);
         g.drawRect(stats.safeMarginW + 1, top, stats.safeW - 2, boxHeight);
      }
      
      // text
      g.setFont(stats.textFont);
      g.setColor(Color.BLACK);
      FontMetrics fm = g.getFontMetrics(stats.textFont);
      int lineHeight = fm.getHeight();
      List<Line> lines = splitText(card.getTextForImage(), fm, stats.safeW);
      
      if (lines.size() > maxLines) // too much text, use a smaller font
      {
         System.out.println(" [TEXT TOO BIG] > " + card.getName());
         g.setFont(stats.textFont2);
         g.setColor(Color.BLACK);
         fm = g.getFontMetrics(stats.textFont2);
         lineHeight = fm.getHeight();
         lines = splitText(card.getTextForImage(), fm, stats.safeW);
         if (lines.size() > maxLines + 1) // too much text, use a smaller font
            System.err.println(" [TEXT TOO BIG] > " + card.getName());
      }
      
      
      int textHeight = lineHeight * lines.size();
      int textTop = top + ((boxHeight - textHeight) / 2);
      if (card instanceof Cacher)
      {
         g.setColor(TEXT_BG);
         RoundRectangle2D bg = new RoundRectangle2D.Float(stats.safeMarginW + 1, textTop + 15,
            stats.safeW - 2, textHeight, 40, 40);
         g.fill(bg);
      }
      
      g.setColor(Color.BLACK);
      int y = textTop + lineHeight;
      for (Line line : lines)
      {
         paintWords(line, g, fm, y);
         y += lineHeight;
      }
   }

   private List<Line> splitText(final String text, final FontMetrics fm, final int safeWidth)
   {
      List<Line> lines = new ArrayList<>();
      
      String[] tokens = Token.tokenize (new StringTokenizer (text, " "));
      Stack<String> wordStack = new Stack<>();
      for (int i = tokens.length - 1; i >= 0; i--)
         wordStack.push(tokens[i]);

      String cleanLine = "";
      List<String> words = new ArrayList<>();
      
      while (!wordStack.isEmpty())
      {
         String word = wordStack.peek();
         if (word.startsWith("<"))
            word = word.replaceAll("<[^>]+>", "");
         else if (word.contains("</em>"))
            word = word.replace("</em>", "");
         
         boolean hasRoom = fm.stringWidth(cleanLine + " " + word) < safeWidth - 20;
         if (hasRoom || cleanLine.isEmpty()) // is there room for the next word (or the word is too big)?
         {
            if (!cleanLine.isEmpty())
               cleanLine += " ";
            cleanLine += word;
            words.add(wordStack.pop()); // add the (possibly annotated) word to the list
         }

         if (!hasRoom || wordStack.isEmpty())
         {
            Line line = new Line();
            line.line = cleanLine;
            lines.add(line);
            line.words = words;
            cleanLine = "";
            words = new ArrayList<>();
         }
      }
         
      return lines;
   }

   private void paintWords(final Line line, final Graphics2D g, final FontMetrics fm, final int y)
   {
      int lineWidth = fm.stringWidth(line.line);
      int x = (stats.w - lineWidth) / 2;
      boolean resetColor = false;

      Iterator<String> iter = line.words.iterator();
      while (iter.hasNext())
      {
         String word = iter.next();
         word = cleanWord(g, word, "bug", Color.MAGENTA);
         word = cleanWord(g, word, "cacher", CACHER_COLOR); // dark green
         word = cleanWord(g, word, "diff1", DIFF1_COLOR); // light blue
         word = cleanWord(g, word, "diff3", DIFF3_COLOR); // reddish
         word = cleanWord(g, word, "diff5", DIFF5_COLOR); // reddish
         word = cleanWord(g, word, "dnf", Color.RED);
         word = cleanWord(g, word, "equipment", Color.BLUE);
         word = cleanWord(g, word, "event", EVENT_COLOR); // dark pink
         word = cleanWord(g, word, "find", FIND_COLOR); // green
         word = cleanWord(g, word, "ftf", FTF_COLOR); // gold
         word = cleanWord(g, word, "roll", Color.BLUE.darker());
         word = cleanWord(g, word, "tile", CACHER_COLOR);

         Matcher m = EM.matcher(word);
         resetColor = m.matches(); // end of annotation
         if (resetColor)
         {
            word = m.group(1);
            if (m.group(2) != null)
            {
               g.drawString(word, x, y); // lower-left
               x += fm.stringWidth(word);
               word = m.group(2); // punctuation like period, comma, parenthesis 
               g.setColor(Color.BLACK);
               resetColor = false;
            }
         }
         
         if (iter.hasNext())
            word += " ";
         g.drawString(word, x, y); // lower-left
         x += fm.stringWidth(word);
         if (resetColor)
            g.setColor(Color.BLACK);
      }
   }

   private String cleanWord(final Graphics2D g, final String annotatedWord, final String type, final Color color)
   {
      String word = annotatedWord;
      if (word.startsWith("<" + type + ">"))
      {
         word = word.replace("<" + type + ">", "");
         g.setColor(color);
      }
      return word;
   }

   public void close(final OutputStream os)
   {
      try
      {
         if (os != null)
         {
            os.flush();
            os.close();
         }
      }
      catch (IOException x) { }
   }
}
