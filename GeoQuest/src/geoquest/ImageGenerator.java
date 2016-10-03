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
   private static final String CACHER_DIR = "G:/pkgs/workspace/GeoQuest/docs/Cards/Cachers/";
   private static final String ENSEMBLE_DIR = "G:/pkgs/workspace/GeoQuest/docs/Cards/Ensembles/";
   private static final String EVENT_DIR = "G:/pkgs/workspace/GeoQuest/docs/Cards/Events/";
   private static final String EQUIP_DIR = "G:/pkgs/workspace/GeoQuest/docs/Cards/Equipment/";
   private static final String PUZZLE_DIR = "G:/pkgs/workspace/GeoQuest/docs/Tokens/Puzzles/";
   private static final String TB_DIR = "G:/pkgs/workspace/GeoQuest/docs/Tokens/Travel Bugs/";

   private static final Pattern EM = Pattern.compile("([^<]+)</em>(.+)?");
   
   private static final Color CACHER_COLOR = new Color(0, 100, 0); // dark green
   private static final Color EVENT_COLOR = new Color(200, 30, 30); // dark red
   private static final Color FIND_COLOR = new Color(25, 125, 25); // dark green
   private static final Color FTF_COLOR = new Color(228, 205, 0); // gold
   public static final Color DIFF1_COLOR = new Color(0, 201, 255); // light blue
   private static final Color DIFF3_COLOR = Color.YELLOW;
   private static final Color DIFF5_COLOR = new Color(255, 104, 104); // reddish
   private static final Color TEXT_BG = new Color(255, 255, 255, 128); // translucent white
   
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
   
   public void publish(final String puzzle, final int index) // in TheGameCrafter format
   {
      OutputStream os = null;
      try
      {
         String name = puzzle.replace("\n", " ");
         System.out.println(" " + index + ") " + name);
         
         BufferedImage image = new BufferedImage(stats.w, stats.h, BufferedImage.TYPE_INT_ARGB);
         Graphics2D g = (Graphics2D) image.getGraphics();

         File face = new File(PUZZLE_DIR + "Puzzle Face.png");
         if (face.exists())
         {
            BufferedImage background = ImageIO.read(face);
            g.drawImage(background, 0, 0, null);
         }
         
         if (drawBoxes)
         {
            BufferedImage background = ImageIO.read(new File(TB_DIR + "TB Borders.png"));
            g.drawImage(background, 0, 0, null);
         }

         String[] lines = Token.tokenize(puzzle, "\n");
         g.setFont(stats.textFont);
         g.setColor(Color.BLACK);
         paintText(g, name, lines);

         g.dispose();
         image.flush();
         
         File path = new File(PUZZLE_DIR + name.replaceAll("[^A-Za-z0-9& ]", "") + ".png");
         os = new FileOutputStream(path);
         ImageTools.saveAs(image, "png", os, 0f);
      }
      catch (Exception x)
      {
         x.printStackTrace();
      }
      finally
      {
         close(os);
      }
   }
   
   public void publish(final TravelBug tb, final int index) // in TheGameCrafter format
   {
      OutputStream os = null;
      try
      {
         String name = tb.getName();
         System.out.println(" " + index + ") " + name + ": " + tb.getText().replaceAll("\n", " "));
         
         BufferedImage image = new BufferedImage(stats.w, stats.h, BufferedImage.TYPE_INT_ARGB);
         Graphics2D g = (Graphics2D) image.getGraphics();

         File face = new File(TB_DIR + "TB Face.png");
         if (face.exists())
         {
            BufferedImage background = ImageIO.read(face);
            g.drawImage(background, 0, 0, null);
         }
         
         if (drawBoxes)
         {
            BufferedImage background = ImageIO.read(new File(TB_DIR + "TB Borders.png"));
            g.drawImage(background, 0, 0, null);
         }

         String[] lines = Token.tokenize(tb.getText(), "\n");
         g.setFont(stats.textFont);
         g.setColor(Color.BLACK);
         paintText(g, name, lines);

         g.dispose();
         image.flush();
         
         File path = new File(TB_DIR + name.replaceAll("[^A-Za-z ]", "") + ".png");
         os = new FileOutputStream(path);
         ImageTools.saveAs(image, "png", os, 0f);
      }
      catch (Exception x)
      {
         x.printStackTrace();
      }
      finally
      {
         close(os);
      }
   }
   
   private void paintText(final Graphics2D g, final String name, final String[] lines)
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

   public void publish(final Cacher cacher) // in TheGameCrafter format
   {
      OutputStream os = null;
      try
      {
         String name = cacher.getName();
         System.out.println(" > " + name + ": " + cacher.getText());
         
         BufferedImage cardImage = new BufferedImage(stats.w, stats.h, BufferedImage.TYPE_INT_ARGB);
         Graphics2D g = (Graphics2D) cardImage.getGraphics();
         
         File face = new File(CACHER_DIR + "Cacher Face.png");
         if (face.exists())
         {
            BufferedImage background = ImageIO.read(face);
            g.drawImage(background, 0, 0, null);
         }

         paintGrid(g);
         int titleHeight = paintTitleRight(g, name, cacher.getText());
         int titleBottom =  stats.safeMarginH + 2 + titleHeight;
         addIcon(g, ART_DIR + "Cards/Cachers/Letter Box.png", 150, 150, stats.safeMarginW + 5, stats.safeMarginH + 5);
         
         // letter
         g.setFont(stats.letterFont);
         g.setColor(Color.BLACK);
         FontMetrics fm = g.getFontMetrics(stats.letterFont);
         String letter = name.substring(0, 1);
         int letterWidth = fm.stringWidth(letter);
         g.drawString(letter, stats.safeMarginW + 60 - (letterWidth / 2), 180); // TODO
         
         int bottom = stats.h - stats.safeMarginH;
         paintText(g, cacher, name, titleBottom, bottom, 4);
         hackIcons(g, name);

         // safe box
         g.setColor(Color.BLUE);
         g.setStroke(DASHED);
         // g.drawRect(stats.safeMarginW, stats.safeMarginH, stats.safeW, stats.safeH);
         
         g.dispose();
         cardImage.flush();
         
         File path = new File(CACHER_DIR + name.replaceAll("[?!]", "") + ".png");
         os = new FileOutputStream(path);
         ImageTools.saveAs(cardImage, "png", os, 0f);
      }
      catch (Exception x)
      {
         x.printStackTrace();
      }
      finally
      {
         close(os);
      }
   }
   
   public void publish(final Ensemble ensemble) // in TheGameCrafter format
   {
      OutputStream os = null;
      try
      {
         String name = ensemble.getName();
         System.out.println(" > " + name + ": " + ensemble.getText());
         
         BufferedImage cardImage = new BufferedImage(stats.w, stats.h, BufferedImage.TYPE_INT_ARGB);
         Graphics2D g = (Graphics2D) cardImage.getGraphics();
         
         File face = new File(ENSEMBLE_DIR + "Ensemble Face.png");
         if (face.exists())
         {
            BufferedImage background = ImageIO.read(face);
            g.drawImage(background, 0, 0, null);
         }

         int titleHeight = paintTitle(g, name, ensemble.getText());
         int top =  stats.safeMarginH + titleHeight - 50;
         int bottom = stats.h - stats.safeMarginH;
         paintText(g, ensemble, name, top, bottom, 4);
         hackIcons(g, name);

         // safe box
         g.setColor(Color.BLUE);
         g.setStroke(DASHED);
         // g.drawRect(stats.safeMarginW, stats.safeMarginH, stats.safeW, stats.safeH);
         
         g.dispose();
         cardImage.flush();
         
         File path = new File(ENSEMBLE_DIR + name.replaceAll("[?!]", "") + ".png");
         os = new FileOutputStream(path);
         ImageTools.saveAs(cardImage, "png", os, 0f);
      }
      catch (Exception x)
      {
         x.printStackTrace();
      }
      finally
      {
         close(os);
      }
   }
   
   public void publish(final Equipment eq) // in TheGameCrafter format
   {
      OutputStream os = null;
      try
      {
         String name = eq.getName();
         System.out.println(" > " + name + ": " + eq.getText());
         
         BufferedImage cardImage = new BufferedImage(stats.w, stats.h, BufferedImage.TYPE_INT_ARGB);
         Graphics2D g = (Graphics2D) cardImage.getGraphics();

         File face = new File(EQUIP_DIR + "Equipment Face.png");
         if (face.exists())
         {
            BufferedImage background = ImageIO.read(face);
            g.drawImage(background, 0, 0, null);
         }

         paintGrid(g);
         int titleHeight = paintTitleLeft(g, name, eq.getText());
         int titleBottom =  stats.safeMarginH + 2 + titleHeight;
         if (eq.getImage() != null)
            paintArt(g, eq.getImage(), titleBottom + 40);
         if (eq.getIcon() != null)
            paintIcon(g, eq.getIcon(), titleHeight);
         int ensembleHeight = paintEnsemble(g, eq);
         int top = (stats.h / 2);
         int bottom = stats.h - stats.safeMarginH - ensembleHeight;
         paintText(g, eq, name, top, bottom, 4);
         hackIcons(g, name);

         // safe box
         g.setColor(Color.BLUE);
         g.setStroke(DASHED);
         // g.drawRect(stats.safeMarginW, stats.safeMarginH, stats.safeW, stats.safeH);
         
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
         close(os);
      }
   }
   
   public void publish(final Event event) // in TheGameCrafter format
   {
      OutputStream os = null;
      try
      {
         String name = event.getName();
         System.out.println(" > " + name + ": " + event.getText());
         
         BufferedImage cardImage = new BufferedImage(stats.w, stats.h, BufferedImage.TYPE_INT_ARGB);
         Graphics2D g = (Graphics2D) cardImage.getGraphics();
         
         File face = new File(EVENT_DIR + "Event Face.png");
         if (face.exists())
         {
            BufferedImage background = ImageIO.read(face);
            g.drawImage(background, 0, 0, null);
         }

         paintGrid(g);
         int playHeight = paintPlayRule(g, event);
         int titleHeight = paintTitleLeft(g, name, event.getText());
         int titleBottom =  stats.safeMarginH + 2 + titleHeight;
         int top = titleBottom + (event.getType() == Type.STD ? 75 : 40);
         if (event.getImage() != null)
            paintArt(g, event.getImage(), top);
         if (event.getIcon() != null)
            paintIcon(g, event.getIcon(), titleHeight);
         top = (stats.h / 2) + playHeight + 1;
         int bottom = stats.h - stats.safeMarginH;
         paintText(g, event, name, top, bottom, 5);
         hackIcons(g, name);

         // safe box
         g.setColor(Color.BLUE);
         g.setStroke(DASHED);
         // g.drawRect(stats.safeMarginW, stats.safeMarginH, stats.safeW, stats.safeH);
         
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
         close(os);
      }
   }

   private void paintGrid(final Graphics2D g)
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

   private void paintArt(final Graphics2D g, final String imageName, final int top)
   {
      try
      {
         ImageIcon artImage = new ImageIcon(ART_DIR + imageName);
         ImageIcon artScaled = ImageTools.scaleImage(artImage, stats.artW, stats.artH, Image.SCALE_SMOOTH, null);
         BufferedImage artBuf = ImageTools.imageToBufferedImage(artScaled.getImage());
         g.drawImage(artBuf, (stats.w - artBuf.getWidth()) / 2, top, null); // upper-left
      }
      catch (Exception x)
      {
         System.err.println("Missing: "+ imageName);
      }
   }

   private void paintIcon(final Graphics2D g, final String imageName, final int titleHeight)
   {
      int h = Math.round(titleHeight * 1.5f);
      ImageIcon icon = new ImageIcon(ART_DIR + imageName);
      ImageIcon scaled = ImageTools.scaleImage(icon, h, h, Image.SCALE_SMOOTH, null);
      BufferedImage bi = ImageTools.imageToBufferedImage(scaled.getImage());
      int top = stats.safeMarginH + 2; // on title
      // int top = stats.safeMarginH + (h / 2) + 5; // super-imposed on title, shadowed
      int left = stats.w - stats.safeMarginW - bi.getWidth();
      g.drawImage(bi, left, top, null);
   }

   private void hackIcons(final Graphics2D g, final String name)
   {
      // Cachers
      if (name.equals("Determined Dan"))
         addIcon(g, ART_DIR + "Icons/Roll DNF.png", 90, 90, 473, 325);
      else if (name.equals("Wandering Warren"))
         addIcon(g, ART_DIR + "Icons/Roll FIND.png", 80, 80, 620, 257);
      
      // Equipment
      else if (name.equals("Backpack"))
         addIcon(g, ART_DIR + "Icons/Move 5 Cap.png", 90, 90, stats.centerX + 60, stats.safeMarginH + 10);
      else if (name.equals("FRS Radio"))
         addIcon(g, ART_DIR + "Icons/Roll FIND.png", 68, 68, stats.centerX + 27, stats.centerY + 40);
      else if (name.equals("Jeep"))
         addIcon(g, ART_DIR + "Icons/Move 2.png", 90, 90, stats.centerX + 60, stats.safeMarginH + 10);
      else if (name.equals("Mirror"))
         addIcon(g, ART_DIR + "Icons/Roll DNF.png", 60, 60, stats.centerX + 22, stats.centerY + 64);
      
      // Events
      else if (name.equals("All About the Numbers"))
      {
         addIcon(g, ART_DIR + "Icons/Move Run.png", 120, 120, stats.safeW - 50, stats.safeMarginH + 45);
         addIcon(g, ART_DIR + "Icons/Cache 1.png", 100, 100, stats.safeMarginW + 5, stats.centerY + 148);
         addIcon(g, ART_DIR + "Icons/Cache 2.png", 70, 70, stats.safeMarginW + 160, stats.centerY + 163);
      }
      else if (name.equals("Bragging Rights"))
         addIcon(g, ART_DIR + "Icons/Point FTF.png", 90, 90, 84, 712);
      else if (name.equals("Bushwhacked"))
         addIcon(g, ART_DIR + "Icons/Roll -2.png", 125, 125, 630, 160);
      else if (name.equals("Equipment Rental"))
         addIcon(g, ART_DIR + "Icons/Roll DNF.png", 70, 70, 555, 878);
      else if (name.equals("In a Hurry"))
         addIcon(g, ART_DIR + "Icons/Point -1.png", 125, 125, 620, 170);
      else if (name.equals("Is That Venomous?"))
         addIcon(g, ART_DIR + "Icons/Roll DNF.png", 65, 65, 383, 726);
      else if (name.equals("Meet and Greet"))
         addIcon(g, ART_DIR + "Icons/Move Join.png", 100, 100, 550, 75);
      else if (name.equals("Not About the Numbers"))
         addIcon(g, ART_DIR + "Icons/Points +1.png", 120, 120, stats.safeW - 50, stats.safeMarginH + 45);
      else if (name.equals("Parking Ticket"))
         addIcon(g, ART_DIR + "Icons/Point -1.png", 100, 100, 650, 170);
      else if (name.equals("Stick Race"))
         addIcon(g, ART_DIR + "Icons/Point +1.png", 100, 100, 640, 170);
      else if (name.equals("Suspicious Activity"))
         addIcon(g, ART_DIR + "Icons/Roll DNF.png", 65, 65, 514, 720);
      else if (name.equals("Trade Up"))
         addIcon(g, ART_DIR + "Icons/Equip -1.png", 100, 100, 660, 170);
      else if (name.equals("Yellow Jackets"))
         addIcon(g, ART_DIR + "Icons/Move 2.png", 100, 100, 580, 75);

      // Ensembles
      else if (name.equals("Engineer"))
         addIcon(g, ART_DIR + "Icons/Roll FIND.png", 65, 65, stats.centerX - 135, stats.centerY - 37);
      else if (name.equals("Search and Rescue"))
         addIcon(g, ART_DIR + "Icons/Roll DNF.png", 76, 76, stats.centerX - 86, stats.centerY - 43);
      else if (name.equals("Tracker"))
      {
         addIcon(g, ART_DIR + "Icons/Roll FIND.png", 70, 70, stats.centerX + 65, stats.centerY - 40);
         addIcon(g, ART_DIR + "Icons/Roll DNF.png", 70, 70, stats.centerX + 208, stats.centerY - 40);
      }
      else if (name.equals("Veteran"))
      {
         addIcon(g, ART_DIR + "Icons/Roll FIND.png", 75, 75, stats.centerX + 48, 175);
         addIcon(g, ART_DIR + "Icons/Roll DNF.png", 75, 75, stats.centerX + 211, 175);
      }
   }

   private void addIcon(final Graphics2D g, final String path, final int w, final int h, final int left, final int top)
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
   
   private int paintPlayRule(final Graphics2D g, final Event event)
   {
      int textHeight = 0;
      
      if (event.getType() != Type.STD)
      {
         String text = event.getType() == Type.NOW ? "PLAY NOW" : "PLAY ANY TIME";
         g.setFont(stats.playFont);
         FontMetrics fm = g.getFontMetrics(stats.playFont);
         textHeight = fm.getHeight();
         int textWidth = fm.stringWidth(text);
         // background
         g.setColor(event.getType() == Type.NOW ? stats.playNowColor : stats.playAnyColor);
         int left = (stats.w - textWidth) / 2;
         int top = (stats.h / 2) + 50;
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

   private int paintEnsemble(final Graphics2D g, final Equipment eq)
   {
      String ensemble = eq.getEnsemble();
      g.setFont(stats.ensembleFont);
      FontMetrics fm = g.getFontMetrics(stats.ensembleFont);
      int textHeight = fm.getHeight();
      int textWidth = fm.stringWidth(ensemble);
      if (textWidth > stats.safeW)
      {
         System.out.println(" [ENSEMBLE TOO WIDE] > " + eq.getName() + ": " + ensemble);
         g.setFont(stats.ensembleFont2);
         fm = g.getFontMetrics(stats.ensembleFont2);
         textHeight = fm.getHeight();
         textWidth = fm.stringWidth(ensemble);
     }
      
      // background
      g.setColor(stats.ensembleColor);
      int top = stats.h - stats.safeMarginH - textHeight - 2;
      g.fillRect(0, top, stats.w, textHeight);
      // borders
      g.setColor(Color.BLACK);
      Stroke origStroke = g.getStroke();
      g.setStroke(STROKE3);
      int y = top - 1;
      g.drawLine(0, y, stats.w, y);
      y = stats.h - stats.safeMarginH - 1;
      g.drawLine(0, y, stats.w, y);
      g.setStroke(origStroke);
      
      // text
      g.setColor(Color.BLACK);
      int left = (stats.w - textWidth) / 2;
      int bottom = top + textHeight - 15;
      g.drawString(ensemble, left, bottom); // lower-left
      
      return textHeight;
   }

   private int paintTitle(final Graphics2D g, final String title, final String text)
   {
      String name = title; // .toUpperCase();
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
      
      int textLeft = (stats.w - textWidth) / 2;
      // g.fillRect(0, 0, stats.cardW, stats.safeMarginH + nameHeight);
      g.setColor(stats.titleBg);
      int left = (stats.w - textWidth) / 2;
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
      if (name.equals("Insect Repellent") || 
          name.equals("Mountain Bike") || 
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

   private int paintTitleRight(final Graphics2D g, final String title, final String text)
   {
      String name = title;
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

   private void paintText(final Graphics2D g, final Component card, final String name, final int top, final int bottom,
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

   private void close(final OutputStream os)
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
