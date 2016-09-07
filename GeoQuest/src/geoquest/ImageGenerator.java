package geoquest;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
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
import javax.swing.JLabel;

import geoquest.Event.Type;
import gui.ComponentTools;
import str.Token;
import utils.ImageTools;

// TODO
// convert all images to PNG with transparent backgrounds
// color for TBs/Equipment
// slightly lower image?

public class ImageGenerator
{
   private static final boolean DRAW_BOXES = false;
   
   private static final String CARD_DIR = "G:/pkgs/workspace/GeoQuest/docs/TGC/Events/";
   private static final String ART_DIR = "G:/pkgs/workspace/GeoQuest/docs/";

   private static final Pattern EM = Pattern.compile("([^<]+)</em>(.+)?");
   
   private static final int CARD_W = 825;
   private static final int CARD_H = 1125;
   // size of the art on the final card
   private static final int ART_W = 650;
   private static final int ART_H = 400;
   
   private static final int CENTER_X = CARD_W / 2;
   private static final int CENTER_Y = CARD_H / 2;
   private static final int LABEL_H = (CARD_H - (2 * ART_H)) / 2; // TODO - base on font?
   private static final int CUT_MARGIN_H = Math.round(CARD_H / 100f * 3.5f);
   private static final int CUT_MARGIN_W = Math.round(CARD_W / 100f * 4.8f);
   private static final int SAFE_MARGIN_H = Math.round(CARD_H / 100f * 6.7f);
   private static final int SAFE_MARGIN_W = Math.round(CARD_W / 100f * 9.5f);
   private static final int SAFE_W = CARD_W - (SAFE_MARGIN_W * 2);
   private static final int SAFE_H = CARD_H - (SAFE_MARGIN_H * 2);
   
   // private static final Font TITLE_FONT = new Font("Artifika", Font.BOLD, 60);
   // private static final Font TITLE_FONT_NARROW = new Font("Artifika", Font.BOLD, 50);
   // private static final Font TITLE_FONT = new Font("Bree Serif", Font.BOLD, 60);
   // private static final Font TITLE_FONT_NARROW = new Font("Bree Serif", Font.BOLD, 50);
   private static final Font TITLE_FONT = new Font("Bubblegum Sans", Font.PLAIN, 60);
   private static final Font TITLE_FONT_NARROW = new Font("Bubblegum Sans", Font.PLAIN, 50);
   // private static final Font TITLE_FONT = new Font("Eurostile", Font.BOLD, 60);
   // private static final Font TITLE_FONT_NARROW = new Font("Eurostile", Font.BOLD, 50);
   // private static final Font TITLE_FONT = new Font("Lilly", Font.BOLD, 60);
   // private static final Font TITLE_FONT_NARROW = new Font("Lilly", Font.BOLD, 50);
   // private static final Font TITLE_FONT = new Font("Veggieburger", Font.BOLD, 60);
   // private static final Font TITLE_FONT_NARROW = new Font("Veggieburger", Font.BOLD, 50);
   private static final Color TITLE_BG_COLOR = new Color(255, 215, 0); // gold
      
   private static final Font TEXT_FONT = new Font("Cabin", Font.PLAIN, 60);
   private static final Font TEXT_FONT_SM = new Font("Cabin", Font.PLAIN, 54);
   
   // TODO EQUIPMENT
   // private static final Font TEXT_FONT = new Font("Enigmatic Regular", Font.PLAIN, 60); 
   // private static final Font TEXT_FONT = new Font("Hattori Hanzo", Font.PLAIN, 60); 
   
   private static final Font PLAY_FONT = new Font("Bitter", Font.PLAIN, 72);
   private static final Color PLAY_WHENEVER = new Color(152, 251, 152); // light green
   private static final Color PLAY_NOW = new Color(255, 160, 122); // light salmon
   
   private static final Color CACHER_COLOR = new Color(0, 100, 0); // dark green
   
   public static void publish(final Event event) // in TheGameCrafter format
   {
      OutputStream os = null;
      try
      {
         String name = event.getName().replace ("&nbsp;", " ");
         System.out.println(" > " + name + ": " + event.getText()); // TODO
         
         BufferedImage cardImage = new BufferedImage(CARD_W, CARD_H, BufferedImage.TYPE_INT_ARGB);
         Graphics2D g = (Graphics2D) cardImage.getGraphics();
         
         BufferedImage background = ImageIO.read(new File(CARD_DIR + "Event.png"));
         g.drawImage(background, 0, 0, null);

         paintGrid(g);
         paintArt(g, event);
         int playHeight = paintPlayRule(g, event);
         paintTitle(g, event, name);
         paintText(g, event, name, playHeight);

         // safe box
         g.setColor(Color.BLUE);
         g.drawRect(SAFE_MARGIN_W, SAFE_MARGIN_H, SAFE_W, SAFE_H);
         
         g.dispose();
         cardImage.flush();
         
         File path = new File(CARD_DIR + name.replaceAll("[?!]", "") + ".png");
         os = new FileOutputStream(path);
         ImageTools.saveAs(cardImage, "png", os, 0f);

         if (name.contains("Archive")) ComponentTools.open(new JLabel(new ImageIcon(cardImage)), name); // UI trace
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

   private static void paintGrid(final Graphics2D g)
   {
      if (DRAW_BOXES)
      {
         // grid lines
         g.setColor(Color.BLACK);
         g.drawLine(CENTER_X, 0, CENTER_X, CARD_H);
         g.drawLine(0, CENTER_Y, CARD_W, CENTER_Y);
      
         // cut line
         g.setColor(Color.RED);
         g.drawRect(CUT_MARGIN_W, CUT_MARGIN_H, CARD_W - (CUT_MARGIN_W * 2), CARD_H - (CUT_MARGIN_H * 2));
      }
   }

   private static void paintArt(final Graphics2D g, final Event event)
   {
      ImageIcon artImage = new ImageIcon(ART_DIR + event.getImageName());
      ImageIcon artScaled = ImageTools.scaleImage(artImage, ART_W, ART_H, Image.SCALE_SMOOTH, null);
      BufferedImage artBuf = ImageTools.imageToBufferedImage(artScaled.getImage());
      g.drawImage(artBuf, (CARD_W - artBuf.getWidth()) / 2, LABEL_H + 25, null); // upper-left
   }

   private static int paintPlayRule(final Graphics2D g, final Event event)
   {
      int textHeight = 0;
      
      if (event.getType() != Type.Std)
      {
         g.setFont(PLAY_FONT);
         FontMetrics fm = g.getFontMetrics(PLAY_FONT);
         textHeight = fm.getHeight();
         // background
         g.setColor(event.getType() == Type.Now ? PLAY_NOW : PLAY_WHENEVER);
         // g.setStroke(BOX_STROKE);
         int top = CARD_H - SAFE_MARGIN_H - textHeight;
         // RoundRectangle2D bg = new RoundRectangle2D.Float(SAFE_MARGIN_W, top, SAFE_W, textHeight, 20, 20);
         // g.fill(bg);
         g.fillRect(0, top, CARD_W, CARD_H - top);
         // text
         g.setColor(Color.BLACK);
         String text = event.getType() == Type.Now ? "PLAY RIGHT NOW" : "PLAY ANY TIME";
         int textWidth = fm.stringWidth(text);
         int textLeft = (CARD_W - textWidth) / 2;
         g.drawString(text, textLeft, CARD_H - SAFE_MARGIN_H - 12); // lower-left
      }
      return textHeight;
   }

   private static void paintTitle(final Graphics2D g, final Event event, final String title)
   {
      String name = title; // .toUpperCase();
      // title (and box)
      FontMetrics fm = g.getFontMetrics(TITLE_FONT);
      g.setFont(TITLE_FONT);
      int nameHeight = fm.getHeight();
      int textWidth = fm.stringWidth(name); // TODO
      if (textWidth > SAFE_W)
      {
         System.out.println(" [NAME TOO WIDE] > " + name + ": " + event.getText());
         fm = g.getFontMetrics(TITLE_FONT_NARROW);
         g.setFont(TITLE_FONT_NARROW);
         nameHeight = fm.getHeight();
         textWidth = fm.stringWidth(name);
         if (textWidth > SAFE_W)
            System.err.println(" [NAME TOO WIDE] > " + name + ": " + event.getText());
      }
      
      int textLeft = (CARD_W - textWidth) / 2;
      g.setColor(TITLE_BG_COLOR);
      // RoundRectangle2D bg = new RoundRectangle2D.Float(SAFE_MARGIN_W, SAFE_MARGIN_H, SAFE_W, nameHeight, 20, 20);
      // g.fill(bg);
      g.fillRect(0, 0, CARD_W, SAFE_MARGIN_H + nameHeight);
      g.setColor(Color.BLACK);
      if (DRAW_BOXES)
         g.drawRect(textLeft, SAFE_MARGIN_H, textWidth, nameHeight);
      g.drawString(name, textLeft, SAFE_MARGIN_H + nameHeight - 10); // lower-left
   }

   private static void paintText(final Graphics2D g, final Event event, final String name, final int playHeight)
   {
      FontMetrics fm;
      if (DRAW_BOXES) // text box
      {
         g.setColor(Color.GREEN);
         g.drawRect(SAFE_MARGIN_W + 1, (CARD_H / 2) + 1, SAFE_W - 2, (SAFE_H / 2) - 2);
      }
      // text
      g.setFont(TEXT_FONT);
      g.setColor(Color.BLACK);
      fm = g.getFontMetrics(TEXT_FONT);
      int lineHeight = fm.getHeight();
      List<Line> lines = splitText(event.getTextForImage(), fm, SAFE_W);
      
      if (lines.size() > 5) // too much text, use a smaller font
      {
         System.out.println(" [TEXT TOO BIG] > " + name + ": " + event.getText());
         g.setFont(TEXT_FONT_SM);
         g.setColor(Color.BLACK);
         fm = g.getFontMetrics(TEXT_FONT_SM);
         lineHeight = fm.getHeight();
         lines = splitText(event.getTextForImage(), fm, SAFE_W);
         if (lines.size() > 6) // too much text, use a smaller font
            System.err.println(" [TEXT TOO BIG] > " + name + ": " + event.getText());
      }
      int textHeight = lineHeight * lines.size();
      
      int y = (CARD_H / 2) + ((ART_H - textHeight - playHeight) / 2) + lineHeight + 25;
      for (Line line : lines)
      {
         paintWords(line, g, fm, y);
         y += lineHeight;
      }
   }

   private static List<Line> splitText(final String text, final FontMetrics fm, final int safeWidth)
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

   private static void paintWords(final Line line, final Graphics2D g, final FontMetrics fm, final int y)
   {
      int lineWidth = fm.stringWidth(line.line);
      int x = (CARD_W - lineWidth) / 2;
      boolean resetColor = false;

      Iterator<String> iter = line.words.iterator();
      while (iter.hasNext())
      {
         String word = iter.next();
         word = cleanWord(g, word, "equipment", Color.BLUE);
         word = cleanWord(g, word, "dnf", Color.RED);
         word = cleanWord(g, word, "cacher", CACHER_COLOR); // dark green

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

   private static String cleanWord(final Graphics2D g, final String annotatedWord, final String type, final Color color)
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
