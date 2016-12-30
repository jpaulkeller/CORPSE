package geoquest;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import str.Token;
import utils.ImageTools;

public class TravelBug extends Component implements Comparable<TravelBug>
{
   static final String TGC_TB_DIR = Factory.TGC + "Tokens/Travel Bugs/";
   
   static final Map<String, TravelBug> BUGS = new TreeMap<>();
   static
   {
      populate();
      populateFR();
   }

   public TravelBug(final String name, final String goal)
   {
      this.name = name;
      this.text = goal.length() > 0 ? goal : CardUtils.BLANK;
   }

   @Override
   public String getName()
   {
      return name;
   }
   
   public int compareTo(final TravelBug other)
   {
      return name.compareTo(other.name);
   }

   @Override
   public boolean equals(final Object other)
   {
      if (other instanceof TravelBug)
         return name.equals(((TravelBug) other).name);
      return false;
   }

   private static void populate()
   {
      add("After You", "Row or\nColumn\nV, W, or Y");
      add("Apropos", "Row or\nColumn with\nCacher's\nletter");
      add("Beast of Burden", "With 5+\nequipment\ncards");
      add("Bridge Too Far", "Within\n3 tiles of\na bridge"); // BRIDGE 
      add("Bushwhacker", "At least\n4 tiles from\nany path"); // PATH
      add("Camper", "Within 3\ntiles of the\ncampsite"); // CAMPSITE
      add("Can't Fool Me", "Search\nroll must\nbe 5+");
      add("Cornered", "In a\ncorner\nof any\nquadrant");
      add("Day Tripper", "Both\nsearch dice\nmust roll\nwhite");
      add("Easy Puzzle", "Level 1\nor 2 Puzzle\ncache");
      add("Footprints", "Already\nfound by\n2+ other\nplayers");
      add("Frosty", "Within\n2 tiles of a\npath fork"); // PATH FORK
      add("Geocacher", "Row or\nColumn\nG, E, O, C,\nA or H");
      add("Get Lucky", "Search\ndice must\nroll\ndoubles");
      add("Grover", "In an\nisolated\nForest tile");
      add("Hook Me Up", "With\nanother\nTravel Bug\nin it");
      add("In Uniform", "With an\nEnsemble");
      add("Kid Friendly", "Level 1\nor 2 in a\nClear tile");
      add("Like I Care", "With an\nevent card\nplayed\non you");
      add("Long Hike", "On the\noutside\nedge of\nthe map");
      add("Looks Like Rain", "While a\nweather\nevent is\nin effect");
      add("Made for Me", "Row or\nColumn\nin your\nname");
      add("Me First", "You must\nbe FTF");
      add("Night Owl", "Both\nsearch dice\nmust roll\nblack");
      add("One More Time", "Multi-\ncache with\n4+ stages");
      add("Only the Best", "Level 5\ncache");
      add("Out of Town", "Farthest\nfrom an\nUrban tile"); // URBAN
      add("Park and Grab", "Level 1 in\nan Urban\ntile"); // URBAN
      add("Picture This", "At or\nnext to a\nScenic\nView"); // SCENIC VIEW
      add("Radical", "Level\n4+ in an\nUrban tile"); // URBAN
      add("Rainbow", "Within\n3 tiles of a\nwaterfall"); // WATERFALL
      add("Rocky Road", "On a\nRocky tile\nwith a path"); // ROCKY, PATH
      add("Safe Zone", "Between\nthe hospital\nand police\nstation"); // HOSPITAL, POLICE
      add("Sloth", "Level\n4+ Puzzle\ncache");
      add("Swimmer", "Next to\na stream"); // STREAM
      add("Travel Light", "With 2\nor fewer\nequipment\ncards");
      add("Tree Hugger", "Level 3+\nin a Forest\ntile"); // FOREST
      add("Trvl Bg", "Row or\nColumn\nA, E, I, O,\nor U");
      add("Will o' Wisp", "On a\nSwamp\ntile"); // SWAMP
      
      // LAKE?
      
      // add("Animal Lover", "Within\n3 tiles of\nthe Zoo");
      // add("Art Critic", "Within 2 tiles of\nthe Museum");
      // add("Bookworm", "Within 2 tiles of\nthe Library");
      // add("Haystack", "Within 2 tiles of\na farm");
      // add("Spelunker", "Within 2 tiles of\na cave");
      // add("Spooky", "In or\nnext to a\ngraveyard");
   }

   private static void populateFR()
   {
      TEXT_FR.put("After You", "Ligne ou\nColonne\nV, W ou Y");
      TEXT_FR.put("Apropos", "Ligne ou\nColonne avec\nla lettre\nde Cacher");
      TEXT_FR.put("Beast of Burden", "Avec\n5+ cartes\nd'équipement");
      TEXT_FR.put("Bridge Too Far", "Dans\n3 tuiles\nd'un pont");
      TEXT_FR.put("Bushwhacker", "Au moins\n4 tuiles\nde n'importe\nquel chemin");
      TEXT_FR.put("Camper", "Dans\n3 tuiles\ndu camp");
      TEXT_FR.put("Can't Fool Me", "Le rôle\nde recherche\ndoit être\nde 5+");
      TEXT_FR.put("Cornered", "Dans un coin\nde n'importe\nquel quadrant");
      TEXT_FR.put("Day Tripper", "Les deux dés\nde recherche\ndoivent rouler\nen blanc");
      TEXT_FR.put("Easy Puzzle", "Niveau\n1 ou 2\nPuzzle Cache");
      TEXT_FR.put("Footprints", "Déjà trouvé\npar 2+ autres\njoueurs");
      TEXT_FR.put("Frosty", "Dans 2\ntuiles\nd'un chemin\nfourchette");
      TEXT_FR.put("Geocacher", "Ligne\nou Colonne\nG, E, O, C,\nA ou H");
      TEXT_FR.put("Get Lucky", "Rechercher\ndés doit\nrouler double");
      TEXT_FR.put("Grover", "Dans\nune tuile\nforestière\nisolée");
      TEXT_FR.put("Hook Me Up", "Avec\nun autre\nTravel Bug\ndedans");
      TEXT_FR.put("In Uniform", "Avec\nun\nEnsemble");
      TEXT_FR.put("Kid Friendly", "Niveau\n1 ou 2\ndans une\ntuile Clear");
      TEXT_FR.put("Like I Care", "Avec une\ncarte\nd'événement\njouée sur vous");
      TEXT_FR.put("Long Hike", "Sur le bord\nextérieur\nde la carte");
      TEXT_FR.put("Looks Like Rain", "Pendant qu'un\névénement\nmétéorologique\nest en vigueur");
      TEXT_FR.put("Made for Me", "Ligne ou\nColonne\ndans votre\nnom");
      TEXT_FR.put("Me First", "Vous\ndevez\nêtre\nFTF");
      TEXT_FR.put("Night Owl", "Les deux dés\nde recherche\ndoivent rouler\nnoir");
      TEXT_FR.put("One More Time", "Multi-cache\navec 4+\nétapes");
      TEXT_FR.put("Only the Best", "Cache de\nniveau 5");
      TEXT_FR.put("Out of Town", "Plus loin\nd'une\nmosaïque\nUrbaine");
      TEXT_FR.put("Park and Grab", "Niveau 1\ndans une\ntuile Urbaine");
      TEXT_FR.put("Picture This", "À ou\nà côté\nd'une Vue\nPanoramique");
      TEXT_FR.put("Radical", "Niveau 4+\ndans unen\ntuile\nUrbaine");
      TEXT_FR.put("Rainbow", "Dans les\n3 tuiles\nd'une\ncascade");
      TEXT_FR.put("Rocky Road", "Sur une\ntuile Rocky\navec un\nchemin");
      TEXT_FR.put("Safe Zone", "Entre\nl'hôpital\net le Poste\nde Police");
      TEXT_FR.put("Sloth", "Niveau\n4+\nCache");
      TEXT_FR.put("Swimmer", "À côté\nd'un\nruisseau");
      TEXT_FR.put("Travel Light", "Avec 2\ncartes\nd'Équipement\nou moins");
      TEXT_FR.put("Tree Hugger", "Niveau 3+\ndans une\ntuile\nForestière");
      TEXT_FR.put("Trvl Bg", "Ligne ou\nColonne\nA, E, I,\nO ou U");
      TEXT_FR.put("Will o' Wisp", "Sur une\ntuile de\nMarécage");
   }
   
   private static void add(final String name, final String goal)
   {
      BUGS.put(name, new TravelBug(name, goal));
   }

   public void publishTGC(final int index) // in TheGameCrafter format
   {
      ImageGenerator imgGen = Factory.getImageGenerator();
      ImageStats stats = Factory.getImageStats();
      OutputStream os = null;
      
      try
      {
         String name = getName();
         System.out.println(" " + index + ") " + name + ": " + getText().toString().replaceAll("\n", " "));
         
         BufferedImage image = new BufferedImage(stats.w, stats.h, BufferedImage.TYPE_INT_ARGB);
         Graphics2D g = (Graphics2D) image.getGraphics();

         File face = new File(Factory.TGC + "Tokens/Travel Bugs/TB Face.png");
         if (face.exists())
         {
            BufferedImage background = ImageIO.read(face);
            g.drawImage(background, 0, 0, null);
         }
         
         if (imgGen.getDrawBoxes())
         {
            BufferedImage background = ImageIO.read(new File(Factory.ROOT + "Tokens/Travel Bugs/TB Borders.png"));
            g.drawImage(background, 0, 0, null);
         }

         String[] lines = Token.tokenize(getText().toString(), "\n");
         g.setFont(stats.textFont);
         g.setColor(Color.BLACK);
         imgGen.paintText(g, name, lines);

         g.dispose();
         image.flush();

         String path = Factory.getTGC() + "Tokens/Travel Bugs/";
         File file = new File(path + "TB " + name.replaceAll("[^A-Za-z ]", "") + ".png");
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
         String name = getName();
         System.out.println(" " + index + ") " + name + ": " + getText().toString().replaceAll("\n", " "));
         
         BufferedImage image = new BufferedImage(150, 150, BufferedImage.TYPE_INT_ARGB);
         Graphics2D g = (Graphics2D) image.getGraphics();

         File face = new File(TGC_TB_DIR + "TB Face.png");
         if (face.exists())
         {
            BufferedImage background = ImageIO.read(face);
            g.drawImage(background, 0, 0, null);
         }
         
         String[] lines = Token.tokenize(getText().toString(), "\n");
         g.setFont(stats.textFont);
         g.setColor(Color.BLACK);
         imgGen.paintTextTTS(g, name, lines);

         g.dispose();
         image.flush();

         String path = Factory.TTS + "Tokens/Travel Bugs/";
         File file = new File(path + "TB " + name.replaceAll("[^A-Za-z ]", "") + ".png");
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
      Factory.setImageStats(ImageStats.getShardStats(32));
      int i = 1;
      for (TravelBug tb : BUGS.values())
      {
         // tb.publishTGC(i);
         tb.publishTTS(i);
         i++;
      }
      System.out.println();
      
      Factory.setLanguage(Language.FRENCH);
      i = 1;
      for (TravelBug tb : BUGS.values())
         if (tb.getText() != null)
            ; // tb.publishTGC(i++);
      System.out.println();
   }
}
