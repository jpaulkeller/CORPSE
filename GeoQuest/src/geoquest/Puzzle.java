package geoquest;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import str.Token;
import utils.ImageTools;

public class Puzzle extends Component implements Comparable<Puzzle>
{
   static final List<Puzzle> PUZZLES = new ArrayList<>();
   static final List<Puzzle> PUZZLES_FR = new ArrayList<>();
   static
   {
      populate();
      populateFR();
   }
   
   Puzzle(final String text)
   {
      this.name = text;
   }

   @Override
   public String getName()
   {
      return name;
   }
   
   public int compareTo(final Puzzle other)
   {
      return name.compareTo(other.name);
   }

   @Override
   public boolean equals(final Object other)
   {
      if (other instanceof Puzzle)
         return name.equals(((Puzzle) other).name);
      return false;
   }

   private static void populate()
   {
      PUZZLES.add(new Puzzle("5-letter\nWords"));
      PUZZLES.add(new Puzzle("Anatomy"));
      PUZZLES.add(new Puzzle("Animals"));
      PUZZLES.add(new Puzzle("Art and\nArtists"));
      PUZZLES.add(new Puzzle("Black or\nWhite"));
      PUZZLES.add(new Puzzle("Books &\nAuthors"));
      PUZZLES.add(new Puzzle("Couples"));
      PUZZLES.add(new Puzzle("Dangerous\nThings"));
      PUZZLES.add(new Puzzle("Events")); // or maybe current events? 
      PUZZLES.add(new Puzzle("Famous\nand\nLiving"));
      PUZZLES.add(new Puzzle("Far Away")); // anything far away from where you currently are
      PUZZLES.add(new Puzzle("Fictional\nCharacters"));
      PUZZLES.add(new Puzzle("First\n& Last")); // puzzle letters must be first and last in solution word ("c/h" could be solved by\n"couch")
      PUZZLES.add(new Puzzle("Flowers"));
      PUZZLES.add(new Puzzle("Foreign\nWords"));
      PUZZLES.add(new Puzzle("Games"));
      PUZZLES.add(new Puzzle("Herbs &\nSpices"));
      PUZZLES.add(new Puzzle("Historical\nFigures"));
      PUZZLES.add(new Puzzle("Hot or\nCold"));
      PUZZLES.add(new Puzzle("In a\nLaboratory")); 
      PUZZLES.add(new Puzzle("In This\nRoom")); // anything in the current room
      PUZZLES.add(new Puzzle("Inside\nWord")); // puzzle letters must be in the word, but not first or last ("c/h" could be solved by\n"shock")
      PUZZLES.add(new Puzzle("Movies,\nPlays,\nor TV"));
      PUZZLES.add(new Puzzle("Noisy\nThings"));
      PUZZLES.add(new Puzzle("On a\nfarm"));
      PUZZLES.add(new Puzzle("Opposites")); // each word must have one of the puzzle letters ("c/h" could be solved by "thick/thin" or "cold/hot")
      PUZZLES.add(new Puzzle("Outer\nSpace"));
      PUZZLES.add(new Puzzle("Places"));
      PUZZLES.add(new Puzzle("Quotations"));
      PUZZLES.add(new Puzzle("Round\nThings"));
      PUZZLES.add(new Puzzle("Scary\nThings"));
      PUZZLES.add(new Puzzle("Science\nFiction"));
      PUZZLES.add(new Puzzle("Sea\nCreatures"));
      PUZZLES.add(new Puzzle("Side\nby\nSide")); // puzzle letters must be adjacent ("c/h" could be solved by\n"child")
      PUZZLES.add(new Puzzle("Small\nThings"));
      PUZZLES.add(new Puzzle("Soft\nThings"));
      PUZZLES.add(new Puzzle("Someone\nYou\nKnow")); 
      PUZZLES.add(new Puzzle("Sounds")); 
      PUZZLES.add(new Puzzle("Sports &\nAthletes"));  
      PUZZLES.add(new Puzzle("Things in\na Garden"));
      PUZZLES.add(new Puzzle("Things in\na Kitchen"));
      PUZZLES.add(new Puzzle("Things in\nan Office"));
      PUZZLES.add(new Puzzle("Things\nThat\nFly"));
      PUZZLES.add(new Puzzle("Things\nYou Eat"));
      PUZZLES.add(new Puzzle("Things\nYou Wear"));
   }

   private static void populateFR()
   {
      PUZZLES_FR.add(new Puzzle("Mots\nde 5\nLettres"));
      PUZZLES_FR.add(new Puzzle("Anatomie"));
      PUZZLES_FR.add(new Puzzle("Animaux"));
      PUZZLES_FR.add(new Puzzle("Art et\nArtistes"));
      PUZZLES_FR.add(new Puzzle("Noir\nou\nBlanc"));
      PUZZLES_FR.add(new Puzzle("Livres\net\nAuteurs"));
      PUZZLES_FR.add(new Puzzle("Des\nCouples"));
      PUZZLES_FR.add(new Puzzle("Choses\nDangereuses"));
      PUZZLES_FR.add(new Puzzle("Événements"));
      PUZZLES_FR.add(new Puzzle("Célèbre\net\nVivant"));
      PUZZLES_FR.add(new Puzzle("Loin"));
      PUZZLES_FR.add(new Puzzle("Personnages\nde\nFiction"));
      PUZZLES_FR.add(new Puzzle("Premier\net\nDernier"));
      PUZZLES_FR.add(new Puzzle("Fleurs"));
      PUZZLES_FR.add(new Puzzle("Mots\nÉtrangers"));
      PUZZLES_FR.add(new Puzzle("Des\nJeux"));
      PUZZLES_FR.add(new Puzzle("Herbes\net\nÉpices"));
      PUZZLES_FR.add(new Puzzle("Personnages\nHistoriques"));
      PUZZLES_FR.add(new Puzzle("Chaud\nou\nFroid"));
      PUZZLES_FR.add(new Puzzle("Dans un\nLaboratoire"));
      PUZZLES_FR.add(new Puzzle("Dans\nCette\nChambre"));
      PUZZLES_FR.add(new Puzzle("Dans\nune\nParole"));
      PUZZLES_FR.add(new Puzzle("Films,\nJeux\nou TV"));
      PUZZLES_FR.add(new Puzzle("Bruyant"));
      PUZZLES_FR.add(new Puzzle("Dans\nune\nFerme"));
      PUZZLES_FR.add(new Puzzle("Opposés"));
      PUZZLES_FR.add(new Puzzle("Cosmos"));
      PUZZLES_FR.add(new Puzzle("Des\nEndroits"));
      PUZZLES_FR.add(new Puzzle("Citations"));
      PUZZLES_FR.add(new Puzzle("Les\nChoses\nRondes"));
      PUZZLES_FR.add(new Puzzle("Choses\nEffrayantes"));
      PUZZLES_FR.add(new Puzzle("Science\nFiction"));
      PUZZLES_FR.add(new Puzzle("Créatures\nde la\nMer"));
      PUZZLES_FR.add(new Puzzle("Cote\nà Cote"));
      PUZZLES_FR.add(new Puzzle("Petites\nChoses"));
      PUZZLES_FR.add(new Puzzle("Choses\nMolles"));
      PUZZLES_FR.add(new Puzzle("Quelqu'un\nque vous\nConnaissez"));
      PUZZLES_FR.add(new Puzzle("Des\nSons"));
      PUZZLES_FR.add(new Puzzle("Sports\net\nAthlètes"));
      PUZZLES_FR.add(new Puzzle("Dans\nun\nJardin"));
      PUZZLES_FR.add(new Puzzle("Dans\nune\nCuisine"));
      PUZZLES_FR.add(new Puzzle("Dans\nun\nBureau"));
      PUZZLES_FR.add(new Puzzle("Les Choses\nqui Volent"));
      PUZZLES_FR.add(new Puzzle("Choses\nque vous\nMangez"));
      PUZZLES_FR.add(new Puzzle("Choses\nque vous\nPortez"));
   }

   public void publishTGC(final int index) // in TheGameCrafter format
   {
      ImageGenerator imgGen = Factory.getImageGenerator();       
      ImageStats stats = Factory.getImageStats();
      
      OutputStream os = null;
      try
      {
         String name = getName().replace("\n", " ");
         System.out.println(" " + index + ") " + name);
         
         BufferedImage image = new BufferedImage(stats.w, stats.h, BufferedImage.TYPE_INT_ARGB);
         Graphics2D g = (Graphics2D) image.getGraphics();

         File face = new File(Factory.TGC + "Tokens/Puzzles/Puzzle Face.png");
         if (face.exists())
         {
            BufferedImage background = ImageIO.read(face);
            g.drawImage(background, 0, 0, null);
         }
         
         if (imgGen.getDrawBoxes())
         {
            BufferedImage background = ImageIO.read(new File(TravelBug.TGC_TB_DIR + "TB Borders.png"));
            g.drawImage(background, 0, 0, null);
         }

         String[] lines = Token.tokenize(getName(), "\n");
         g.setFont(stats.textFont);
         g.setColor(Color.BLACK);
         imgGen.paintText(g, name, lines);

         g.dispose();
         image.flush();
         
         String path = Factory.getTGC() + "Tokens/Puzzles/";
         File file = new File(path + "PZ " + name.replaceAll("[^-A-Za-z0-9& ]", "") + ".png");
         os = new FileOutputStream(file);
         ImageTools.saveAs(image, "png", os, 0f);
      }
      catch (Exception x)
      {
         x.printStackTrace();
      }
      finally
      {
         imgGen.close(os);
      }
   }
   
   public void publishTTS(final int index) // for Tabletop Simulator
   {
      ImageGenerator imgGen = Factory.getImageGenerator();       
      ImageStats stats = Factory.getImageStats();
      
      OutputStream os = null;
      try
      {
         String name = getName().replace("\n", " ");
         System.out.println(" " + index + ") " + name);
         
         BufferedImage image = new BufferedImage(150, 150, BufferedImage.TYPE_INT_ARGB);
         Graphics2D g = (Graphics2D) image.getGraphics();

         File face = new File(Factory.TGC + "Tokens/Puzzles/Puzzle Face.png");
         if (face.exists())
         {
            BufferedImage background = ImageIO.read(face);
            g.drawImage(background, 0, 0, null);
         }
         
         String[] lines = Token.tokenize(getName(), "\n");
         g.setFont(stats.textFont);
         g.setColor(Color.BLACK);
         imgGen.paintTextTTS(g, name, lines);

         g.dispose();
         image.flush();
         
         String path = Factory.TTS + "Tokens/Puzzles/";
         File file = new File(path + "PZ " + name.replaceAll("[^-A-Za-z0-9& ]", "") + ".png");
         os = new FileOutputStream(file);
         ImageTools.saveAs(image, "png", os, 0f);
      }
      catch (Exception x)
      {
         x.printStackTrace();
      }
      finally
      {
         imgGen.close(os);
      }
   }
   
   public static void main(final String[] args)
   {
      Factory.setImageStats(ImageStats.getShardStats(36));
      int i = 1;
      for (Puzzle puzzle : PUZZLES)
      {
         // puzzle.publishTGC(i);
         puzzle.publishTTS(i);
         i++;
      }
      System.out.println();
      
      Factory.setLanguage(Language.FRENCH);
      i = 1;
      for (Puzzle puzzle : PUZZLES_FR)
         ; // puzzle.publishTGC(i++);
   }
}
