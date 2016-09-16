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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import geoquest.Event.Type;
import str.Token;
import utils.ImageTools;

// Image Sources
// 
// https://openclipart.org/
// https://pixabay.com/
// https://www.google.com/imghp

// TODO
// convert all images to PNG with transparent backgrounds
// color for TBs/Equipment
// slightly lower image?
// round point coin icon for point gains?
// use dice icon for +# to roll

public class ImageGenerator
{
   private static final String ART_DIR = "G:/pkgs/workspace/GeoQuest/docs/";
   private static final String EVENT_DIR = "G:/pkgs/workspace/GeoQuest/docs/TGC/Events/";
   private static final String EQUIP_DIR = "G:/pkgs/workspace/GeoQuest/docs/TGC/Equipment/";

   private static final Pattern EM = Pattern.compile("([^<]+)</em>(.+)?");
   
   private static final Color CACHER_COLOR = new Color(0, 100, 0); // dark green
   private static final Color EVENT_COLOR = new Color(255, 20, 147); // dark pink
   private static final Color FIND_COLOR = new Color(25, 125, 25); // dark green
   private static final Color FTF_COLOR = new Color(228, 205, 0); // gold
   private static final Color DIFF3_COLOR = Color.YELLOW;
   private static final Color DIFF5_COLOR = new Color(255, 104, 104); // reddish
   
   private static final Stroke STROKE3 = new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, null, 0);
   private static final Stroke STROKE5 = new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, null, 0);
   private static final Stroke DASHED = 
      new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[] { 3, 5 }, 0);

   private ImageStats stats;
   private boolean drawBoxes;
   
   public ImageGenerator(final ImageStats stats, final boolean drawBoxes)
   {
      this.stats = stats;
      this.drawBoxes = drawBoxes;
   }
   
   public void publish(final Equipment eq) // in TheGameCrafter format
   {
      OutputStream os = null;
      try
      {
         String name = eq.getName().replace ("&nbsp;", " ");
         System.out.println(" > " + name + ": " + eq.getText());
         
         BufferedImage cardImage = new BufferedImage(stats.cardW, stats.cardH, BufferedImage.TYPE_INT_ARGB);
         Graphics2D g = (Graphics2D) cardImage.getGraphics();
         
         BufferedImage background = ImageIO.read(new File(EQUIP_DIR + "Equipment Face.png"));
         g.drawImage(background, 0, 0, null);

         paintGrid(g);
         int titleHeight = paintTitleLeft(g, name, eq.getText());
         int titleBottom =  stats.safeMarginH + 2 + titleHeight;
         paintArt(g, eq.getImage(), titleBottom + 40);
         if (eq.getIcon() != null)
            paintIcon(g, eq.getIcon(), titleHeight);
         int comboHeight = paintCombo(g, eq);
         int top = (stats.cardH / 2);
         int bottom = stats.cardH - stats.safeMarginH - comboHeight;
         paintText(g, eq, name, top, bottom, 4);
         hackIcons(g, name);

         // safe box
         g.setColor(Color.BLUE);
         g.setStroke(DASHED);
         g.drawRect(stats.safeMarginW, stats.safeMarginH, stats.safeW, stats.safeH);
         
         g.dispose();
         cardImage.flush();
         
         File path = new File(EQUIP_DIR + name.replaceAll("[?!]", "") + ".png");
         os = new FileOutputStream(path);
         ImageTools.saveAs(cardImage, "png", os, 0f);
      }
      catch (Exception x)
      {
         x.printStackTrace();
      }
      finally
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
   
   public void publish(final Event event) // in TheGameCrafter format
   {
      OutputStream os = null;
      try
      {
         String name = event.getName().replace ("&nbsp;", " ");
         System.out.println(" > " + name + ": " + event.getText());
         
         BufferedImage cardImage = new BufferedImage(stats.cardW, stats.cardH, BufferedImage.TYPE_INT_ARGB);
         Graphics2D g = (Graphics2D) cardImage.getGraphics();
         
         BufferedImage background = ImageIO.read(new File(EVENT_DIR + "Event Face.png"));
         g.drawImage(background, 0, 0, null);

         paintGrid(g);
         int playHeight = paintPlayRule(g, event);
         int titleHeight = paintTitle(g, name, event.getText());
         int titleBottom =  stats.safeMarginH + 2 + titleHeight;
         int top = titleBottom + (event.getType() == Type.STD ? 75 : 40);
         paintArt(g, event.getImage(), top);
         top = (stats.cardH / 2) + playHeight + 1;
         int bottom = stats.cardH - stats.safeMarginH;
         paintText(g, event, name, top, bottom, 5);

         // safe box
         g.setColor(Color.BLUE);
         g.drawRect(stats.safeMarginW, stats.safeMarginH, stats.safeW, stats.safeH);
         
         g.dispose();
         cardImage.flush();
         
         File path = new File(EVENT_DIR + name.replaceAll("[?!]", "") + ".png");
         os = new FileOutputStream(path);
         ImageTools.saveAs(cardImage, "png", os, 0f);

         // if (name.contains("Archive")) gui.ComponentTools.open(new javax.swing.JLabel(new ImageIcon(cardImage)), name); // UI trace
      }
      catch (Exception x)
      {
         x.printStackTrace();
      }
      finally
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

   private void paintGrid(final Graphics2D g)
   {
      if (drawBoxes)
      {
         // grid lines
         g.setColor(Color.BLACK);
         g.drawLine(stats.centerX, 0, stats.centerX, stats.cardH);
         g.drawLine(0, stats.centerY, stats.cardW, stats.centerY);
      
         // cut line
         g.setColor(Color.RED);
         g.drawRect(stats.cutMarginW, stats.cutMarginH,
            stats.cardW - (stats.cutMarginW * 2), stats.cardH - (stats.cutMarginH * 2));
      }
   }

   private void paintArt(final Graphics2D g, final String imageName, final int top)
   {
      ImageIcon artImage = new ImageIcon(ART_DIR + imageName);
      ImageIcon artScaled = ImageTools.scaleImage(artImage, stats.artW, stats.artH, Image.SCALE_SMOOTH, null);
      BufferedImage artBuf = ImageTools.imageToBufferedImage(artScaled.getImage());
      g.drawImage(artBuf, (stats.cardW - artBuf.getWidth()) / 2, top, null); // upper-left
   }

   private void paintIcon(final Graphics2D g, final String imageName, final int titleHeight)
   {
      int h = Math.round(titleHeight * 1.5f);
      ImageIcon icon = new ImageIcon(ART_DIR + imageName);
      ImageIcon scaled = ImageTools.scaleImage(icon, h, h, Image.SCALE_SMOOTH, null);
      BufferedImage bi = ImageTools.imageToBufferedImage(scaled.getImage());
      int top = stats.safeMarginH + 2; // on title
      // int top = stats.safeMarginH + (h / 2) + 5; // super-imposed on title, shadowed
      int left = stats.cardW - stats.safeMarginW - bi.getWidth();
      g.drawImage(bi, left, top, null);
   }

   private void hackIcons(final Graphics2D g, final String name)
   {
      if (name.equals("Backpack"))
         addIcon(g, ART_DIR + "TGC/Icons/Move 5 Cap.png", 90, 90, stats.centerX + 60, stats.safeMarginH + 10);
      else if (name.equals("FRS Radio"))
         addIcon(g, ART_DIR + "TGC/Dice/Dice FIND.png", 68, 68, stats.centerX + 27, stats.centerY + 40);
      else if (name.equals("Gloves"))
         addIcon(g, ART_DIR + "TGC/Icons/Cache 3.png", 95, 95, stats.centerX + 25, stats.centerY + 75);
      else if (name.equals("Jeep"))
         addIcon(g, ART_DIR + "TGC/Icons/Move 2.png", 90, 90, stats.centerX + 60, stats.safeMarginH + 10);
      else if (name.equals("Letterbox Stamp"))
         addIcon(g, ART_DIR + "TGC/Icons/Cache 3.png", 95, 95, stats.centerX + 120, stats.centerY + 105);
      else if (name.equals("Mirror"))
         addIcon(g, ART_DIR + "TGC/Dice/Dice DNF.png", 68, 68, stats.centerX + 19, stats.centerY + 64);
      else if (name.equals("Pocket Knife"))
         addIcon(g, ART_DIR + "TGC/Icons/Cache 5.png", 95, 95, stats.centerX - 12, stats.centerY + 128);
      // else if (name.equals("Utility Tool")) addIcon(g, ART_DIR + "TGC/Icons/Cache 3.png", 95, 95, stats.centerX + 25, stats.centerY + 75);
      // else if (name.equals("Waders")) addIcon(g, ART_DIR + "TGC/Icons/Move 2.png", 90, 90, stats.centerX + 60, stats.safeMarginH + 10);
   }

   private void addIcon(final Graphics2D g, final String path, final int w, final int h, final int left, final int top)
   {
      ImageIcon icon = new ImageIcon(path);
      ImageIcon scaled = ImageTools.scaleImage(icon, w, h, Image.SCALE_SMOOTH, null);
      BufferedImage bi = ImageTools.imageToBufferedImage(scaled.getImage());
      g.drawImage(bi, left, top, null);
   }
   
   private int paintPlayRule(final Graphics2D g, final Event event)
   {
      int textHeight = 0;
      
      if (event.getType() != Type.STD)
      {
         String text = event.getType() == Type.NOW ? "PLAY NOW" : "DISCARD TO PLAY";
         g.setFont(stats.playFont);
         FontMetrics fm = g.getFontMetrics(stats.playFont);
         textHeight = fm.getHeight();
         int textWidth = fm.stringWidth(text);
         // background
         g.setColor(event.getType() == Type.NOW ? stats.playNowColor : stats.playAnyColor);
         int left = (stats.cardW - textWidth) / 2;
         int top = (stats.cardH / 2) + 50;
         RoundRectangle2D bg = new RoundRectangle2D.Float(left - 10, top, textWidth + 20, textHeight, 20, 20);
         g.fill(bg);
         g.setColor(Color.BLACK);
         Stroke origStroke = g.getStroke();
         g.setStroke(STROKE3);
         g.draw(bg);
         g.setStroke(origStroke);
         // text
         int bottom = top + textHeight - 10; // 15;
         g.drawString(text, left, bottom); // lower-left
      }
      
      return textHeight;
   }

   private int paintCombo(final Graphics2D g, final Equipment eq)
   {
      String combo = eq.getCombo().replace("&nbsp;", " ");
      g.setFont(stats.comboFont);
      FontMetrics fm = g.getFontMetrics(stats.comboFont);
      int textHeight = fm.getHeight();
      int textWidth = fm.stringWidth(combo);
      if (textWidth > stats.safeW)
      {
         System.out.println(" [COMBO TOO WIDE] > " + eq.getName() + ": " + combo);
         g.setFont(stats.comboFont2);
         fm = g.getFontMetrics(stats.comboFont2);
         textHeight = fm.getHeight();
         textWidth = fm.stringWidth(combo);
      }
      
      // background
      g.setColor(stats.comboColor);
      int top = stats.cardH - stats.safeMarginH - textHeight - 2;
      g.fillRect(0, top, stats.cardW, textHeight);
      // borders
      g.setColor(Color.BLACK);
      Stroke origStroke = g.getStroke();
      g.setStroke(STROKE3);
      int y = top - 1;
      g.drawLine(0, y, stats.cardW, y);
      y = stats.cardH - stats.safeMarginH - 1;
      g.drawLine(0, y, stats.cardW, y);
      g.setStroke(origStroke);
      
      // text
      g.setColor(Color.BLACK);
      int left = (stats.cardW - textWidth) / 2;
      int bottom = top + textHeight - 15;
      g.drawString(combo, left, bottom); // lower-left
      
      return textHeight;
   }

   private int paintTitle(final Graphics2D g, final String title, final String text)
   {
      String name = title; // .toUpperCase();
      
      int margin = 10;
      FontMetrics fm = g.getFontMetrics(stats.titleFont);
      g.setFont(stats.titleFont);
      int textHeight = fm.getHeight();
      int textWidth = fm.stringWidth(name);
      if (textWidth + margin > stats.safeW)
      {
         System.out.println(" [NAME TOO WIDE] > " + name + ": " + text);
         fm = g.getFontMetrics(stats.titleFont2);
         g.setFont(stats.titleFont2);
         textHeight = fm.getHeight();
         textWidth = fm.stringWidth(name);
         if (textWidth + margin > stats.safeW)
         {
            System.err.println(" [NAME TOO WIDE] > " + name + ": " + text);
            fm = g.getFontMetrics(stats.titleFont3);
            g.setFont(stats.titleFont3);
            textHeight = fm.getHeight();
            textWidth = fm.stringWidth(name);
         }
      }
      
      int textLeft = (stats.cardW - textWidth) / 2;
      // g.fillRect(0, 0, stats.cardW, stats.safeMarginH + nameHeight);
      g.setColor(stats.titleBg);
      int left = (stats.cardW - textWidth) / 2;
      int top = stats.safeMarginH + 2;
      RoundRectangle2D bg = new RoundRectangle2D.Float(left - 10, top + 2, textWidth + 20, textHeight + 4, 20, 20);
      g.fill(bg);
      g.setColor(Color.BLACK);
      Stroke origStroke = g.getStroke();
      g.setStroke(STROKE5);
      g.draw(bg);
      g.setStroke(origStroke);
      if (drawBoxes)
         g.drawRect(textLeft, stats.safeMarginH, textWidth, textHeight);
      g.drawString(name, textLeft, stats.safeMarginH + textHeight - 7); // lower-left
      
      return textHeight + 20;
   }

   private int paintTitleLeft(final Graphics2D g, final String title, final String text)
   {
      String name = title; // .toUpperCase();
      Font font = stats.titleFont;
      
      // hack for long titles with icons
      if (name.equals("Mountain Bike") || 
          name.equals("Survival Strap") || 
          name.equals("Walking Stick"))
         font = stats.titleFont2;
      else if (name.equals("Emergency Radio") || name.equals("Letterbox Stamp"))
         font = stats.titleFont3;
      
      int margin = 10;
      FontMetrics fm = g.getFontMetrics(font);
      g.setFont(font);
      int textHeight = fm.getHeight();
      int textWidth = fm.stringWidth(name);
      if (textWidth + margin > stats.safeW)
      {
         System.out.println(" [NAME TOO WIDE] > " + name + ": " + text);
         fm = g.getFontMetrics(stats.titleFont2);
         g.setFont(stats.titleFont2);
         textHeight = fm.getHeight();
         textWidth = fm.stringWidth(name);
         if (textWidth + margin > stats.safeW)
         {
            System.err.println(" [NAME TOO WIDE] > " + name + ": " + text);
            fm = g.getFontMetrics(stats.titleFont3);
            g.setFont(stats.titleFont3);
            textHeight = fm.getHeight();
            textWidth = fm.stringWidth(name);
         }
      }
      
      int textLeft = stats.safeMarginW + 10; 
      g.setColor(stats.titleBg);
      int top = stats.safeMarginH + 2;
      g.fillRect(0, top, stats.cardW, textHeight + 4);
      
      // borders
      g.setColor(Color.BLACK);
      Stroke origStroke = g.getStroke();
      g.setStroke(STROKE5);
      int y = top - 2;
      g.drawLine(0, y, stats.cardW, y);
      y = top + textHeight + 4 + 2;
      g.drawLine(0, y, stats.cardW, y);

      // text
      g.setStroke(origStroke);
      g.drawString(name, textLeft, stats.safeMarginH + textHeight - 7); // lower-left
      
      return textHeight + 4;
   }

   private void paintText(final Graphics2D g, final Card card, final String name, final int top, final int bottom,
                          final int maxLines)
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
         System.out.println(" [TEXT TOO BIG] > " + name + ": " + card.getText());
         g.setFont(stats.textFont2);
         g.setColor(Color.BLACK);
         fm = g.getFontMetrics(stats.textFont2);
         lineHeight = fm.getHeight();
         lines = splitText(card.getTextForImage(), fm, stats.safeW);
         if (lines.size() > maxLines + 1) // too much text, use a smaller font
            System.err.println(" [TEXT TOO BIG] > " + name + ": " + card.getText());
      }
      int textHeight = lineHeight * lines.size();
      
      int textBottom = top + lineHeight + ((boxHeight - textHeight) / 2);
      // System.out.println("top/btm: " + top + "/" + bottom + "; boxH: " + boxHeight + " txt: " + textHeight + "/" + lineHeight + "/" + textBottom); // TODO
      for (Line line : lines)
      {
         paintWords(line, g, fm, textBottom);
         textBottom += lineHeight;
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
         // if (text.contains("Blue")) System.out.println("[" + word + "]"); // TODO
         
         // if (word.startsWith("<equipment"))
         if (word.startsWith("<"))
            word = word.replaceAll("<[^>]+>", "");
         else if (word.contains("</em>"))
            word = word.replace("</em>", "");
         
         boolean hasRoom = fm.stringWidth(cleanLine + " " + word) < safeWidth - 20;
         if (hasRoom) // is there room for the next word?
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
      int x = (stats.cardW - lineWidth) / 2;
      boolean resetColor = false;

      Iterator<String> iter = line.words.iterator();
      while (iter.hasNext())
      {
         String word = iter.next();
         word = cleanWord(g, word, "bug", Color.MAGENTA);
         word = cleanWord(g, word, "cacher", CACHER_COLOR); // dark green
         word = cleanWord(g, word, "diff3", DIFF3_COLOR); // reddish
         word = cleanWord(g, word, "diff5", DIFF5_COLOR); // reddish
         word = cleanWord(g, word, "dnf", Color.RED);
         word = cleanWord(g, word, "equipment", Color.BLUE);
         word = cleanWord(g, word, "event", EVENT_COLOR); // dark pink
         word = cleanWord(g, word, "find", FIND_COLOR); // green
         word = cleanWord(g, word, "ftf", FTF_COLOR); // gold

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
}
