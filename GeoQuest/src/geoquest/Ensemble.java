package geoquest;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import utils.ImageTools;

public class Ensemble extends Component implements Comparable<Ensemble>
{
   static final Map<String, Ensemble> ENSEMBLES = new TreeMap<>();
   
   static
   {
      populate();
      populateFR();
      setStats();
   }

   private static void setStats()
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

      if (Factory.LANGUAGE == Language.FRENCH)
         stats.titleFontName = "EB Garamond 12";
      else
         stats.titleFontName = "Bree Serif";
      stats.titleFont = new Font(stats.titleFontName, Font.BOLD, 60);
      stats.titleFont2 = new Font(stats.titleFontName, Font.BOLD, 50);
      stats.titleBg = Color.WHITE;
         
      stats.textFont = new Font("Cabin", Font.PLAIN, 70);
      stats.textFont2 = new Font("Cabin", Font.PLAIN, 60);
   }
   
   public static ImageStats getImageStats()
   {
      return stats;
   }
   
   private String trigger;
   String eq1, eq2, eq3;

   public Ensemble(final String name, final String eq1, final String eq2, final String eq3, final String trigger, final String text)
   {
      this.name = name;
      this.trigger = trigger;
      this.text = trigger + (text.length() > 0 ? text : CardUtils.BLANK);
      this.eq1 = eq1;
      this.eq2 = eq2;
      this.eq3 = eq3;
   }

   public int compareTo(final Ensemble c)
   {
      return name.compareTo(c.name);
   }

   @Override
   public String toString()
   {
      return name + " (" + eq1 + " + " + eq2 + " + " + eq3 + ")";
   }

   @Override
   public boolean equals(final Object other)
   {
      if (other instanceof Ensemble)
         return name.equals(((Ensemble) other).name);
      return false;
   }

   private static void populate()
   {
      add("Eagle Scout", "Batteries", "Compass", "Pocket Knife", "When an event would affect you, ",
         "you may discard any Equipment card to ignore it.");
      add("Engineer", "Antenna", "Cell Phone", "Laptop", "Whenever another player rolls a <em class=find>.F.</em>, ",
         "you get +1 on your next roll.  (not limited to once per turn.)");
      add("Event Coordinator", "Geocoin", "Hiking Staff", "Letterbox Stamp",
         "Whenever someone plays an Event card on you, ",
         "you both roll the dice.  If you roll higher, it does not affect you (discard it).");
      add("Lifeguard", "Bandana", "Hat", "Whistle",
         "If a player next to a stream or lake rolls a <em class=roll>1</em> on either die, ",
         "you may immediately jump to their location.");
      add("Hitchhiker", "Lucky Charm", "Survival Strap", "Swag Bag", "Whenever you roll <em class=roll>4+2</em>,",
         "you may jump to any tile as your move.");
      add("MacGyver", "Belt Pack", "Duct Tape", "Utility Tool", "", "You may skip your turn to draw an Equipment card.");
      add("Naturalist", "CITO Bag", "Field Guide", "Walking Stick", "",
         "All wildlife Event cards (drawn, in play, or in a player's hand) are discarded, and you gain 2 points for each one.");
      add("Night Cacher", "Flashlight", "Head Lamp", "Insect Repellent", "",
         "All roll and point penalties against you are treated as -1.");
      add("Paramedic", "First-aid Kit", "Jeep", "Repair Kit", "If an Event card would award you points, ",
         "instead roll the dice to determine how many points you earn.");
      add("Photographer", "Camera", "Mirror", "Safari Vest", "",
         "You get 2 points if you end your turn on a <em class=tile>Scenic View</em> or <em class=tile>Waterfall</em>.  (You may only get the points once each.)");
      add("Road Warrior", "Gorp", "Camel Pack", "Mountain Bike", "If you roll 5 or higher while on a Path, ",
         "you may jump to any tile on that Path.");
      add("Search and Rescue", "Binoculars", "FRS Radio", "Rope",
         "Whenever anyone rolls a <em class=dnf>.D.</em>, ", "you may immediately jump to their location.");
      add("Through Hiker", "Backpack", "Hiking Boots", "Trail Guide", "Each time you roll doubles while on a Path, ",
         "you may take an extra turn (not limited to once per turn).");
      add("Tracker", "Ol' Blue", "Map", "Waders",
         "If a player in your quadrant rolls <em class=find>.F.</em> or <em class=dnf>.D.</em>, ",
         "you may immediately jump to their location.");
      add("Veteran", "Gaiters", "Long Pants", "Water Bottle",
         "If you roll a <em class=find>.F.</em> or <em class=dnf>.D.</em> while moving, ",
         "you may ignore it, and add 2 to your roll.");
      add("Weatherman", "Emergency Radio", "Gloves", "Rain Jacket", "",
         "All weather Event cards (drawn, in play, or in a player's hand) are discarded, and you gain 2 points for each one.");
   }
   
   static void populateFR()
   {
      addFR("Eagle Scout", "Eagle Scout", "Lorsqu'un événement vous affecte, vous pouvez jeter une carte d'équipement pour l'ignorer.");
      addFR("Engineer", "Ingénieur", "Chaque fois qu'un autre joueur lance un <em class=find>.F.</em>, vous obtenez +1 sur votre prochain jet (non limité à une fois par tour)");
      addFR("Event Coordinator", "Coordinateur d'évenements", "Chaque fois que quelqu'un joue une carte Event sur vous, vous lancez tous les deux les dés. Si vous roulez plus haut, cela ne vous affecte pas (jetez-le).");
      addFR("Lifeguard", "Maître Nageur", "Si un joueur à côté d'un ruisseau ou d'un lac lance un <em class=roll>1</em> sur chaque dé, vous pouvez immédiatement sauter à leur emplacement.");
      addFR("Hitchhiker", "Auto Stoppeur", "Chaque fois que vous lancez <em class=roll>4 + 2</em>, vous pouvez passer à n'importe quelle tuile lors de votre déplacement.");
      addFR("MacGyver", "MacGyver", "Vous pouvez sauter votre tour pour dessiner une carte d'équipement.");
      addFR("Naturalist", "Naturaliste", "Toutes les cartes d'événement de la faune (tirées, en jeu ou dans la main d'un joueur) sont éliminées et vous gagnez 2 points pour chacune d'elles.");
      addFR("Night Cacher", "Nuit Cacher", "Toutes les pénalités de roulis et de points contre vous sont traitées comme -1.");
      addFR("Paramedic", "Paramédical", "Si une carte Event vous accorde des points, lancez plutôt les dés pour déterminer combien de points vous gagnez.");
      addFR("Photographer", "Photographe", "Vous obtenez 2 points si vous terminez votre tour sur une <em class=tile>Vue Panoramique</em> ou <em class=tile>Cascade</em>. (Vous ne pouvez obtenir les points qu'une seule fois.)");
      addFR("Road Warrior", "Guerrier de la Route", "Si vous roulez 5 ou plus sur un Chemin, vous pouvez passer à n'importe quelle tuile sur ce Chemin.");
      addFR("Search and Rescue", "Chercher et Sauver", "Chaque fois que quelqu'un lance une <em class=dnf>.D.</em>, vous pouvez immédiatement passer à leur emplacement.");
      addFR("Through Hiker", "Par le Randonneur", "Chaque fois que vous doublez sur un chemin, vous pouvez prendre un tour supplémentaire (non limité à une fois par tour).");
      addFR("Tracker", "Traqueur", "Si un joueur de votre quadrant lance <em class=find>.F.</em> ou <em class=dnf>.D.</em>, vous pouvez immédiatement passer à leur emplacement.");
      addFR("Veteran", "Vétéran", "Si vous lancez une <em class=find>.F.</em> ou <em class=dnf>.D.</em> en déplacement, vous pouvez l'ignorer et ajouter 2 à votre rouleau.");
      addFR("Weatherman", "Météorologue", "Toutes les cartes d'événement météorologiques (tirées, en jeu ou dans la main d'un joueur) sont éliminées et vous gagnez 2 points pour chacune d'elles.");
   }

   private static void add(final String cardName, final String eq1, final String eq2, final String eq3, final String trigger,
                           final String text)
   {
      Ensemble ensemble = new Ensemble(cardName, eq1, eq2, eq3, trigger, text);
      ENSEMBLES.put(ensemble.getName(), ensemble);
   }

   private static void dump(final Ensemble ensemble)
   {
      StringBuilder sb = new StringBuilder();
      sb.append(ensemble.name);
      sb.append(": ");
      sb.append(ensemble.eq1);
      sb.append(", ");
      sb.append(ensemble.eq2);
      sb.append(", ");
      sb.append(ensemble.eq3);
      sb.append(", \"");
      sb.append(ensemble.trigger);
      sb.append(" ");
      sb.append(ensemble.text);
      sb.append("\"");

      String s = sb.toString();
      s = s.replaceAll("<[^>]+>", "");
      System.out.println(s);
   }

   private static void validate()
   {
      System.out.println("Validating Equipment References:");
      System.out.flush();
      for (Ensemble c : ENSEMBLES.values())
      {
         Equipment eq = Equipment.EQUIPMENT.get(c.eq1);
         if (eq == null)
            System.err.println("  Missing equipment for " + c.name + ": " + c.eq1);
         else if (!c.name.equalsIgnoreCase(eq.getEnsemble()))
            System.err.println("  Invalid ensemble for " + c.name + " " + c.eq1 + ": " + eq.getEnsemble());
         
         eq = Equipment.EQUIPMENT.get(c.eq2);
         if (eq == null)
            System.err.println("  Missing equipment for " + c.name + ": " + c.eq2);
         else if (!c.name.equalsIgnoreCase(eq.getEnsemble()))
            System.err.println("  Invalid ensemble for " + c.name + " " + c.eq2 + ": " + eq.getEnsemble());

         eq = Equipment.EQUIPMENT.get(c.eq3);
         if (eq == null)
            System.err.println("  Missing equipment for " + c.name + ": " + c.eq3);
         else if (!c.name.equalsIgnoreCase(eq.getEnsemble()))
            System.err.println("  Invalid ensemble for " + c.name + " " + c.eq3 + ": " + eq.getEnsemble());
         System.err.flush();
      }
      System.out.println();
      System.out.flush();
   }

   public void publish() // in TheGameCrafter format
   {
      ImageGenerator imgGen = Factory.getImageGenerator();
      OutputStream os = null;
      
      try
      {
         String name = getName();
         System.out.println(" > " + name + ": " + getText());
         
         BufferedImage cardImage = new BufferedImage(stats.w, stats.h, BufferedImage.TYPE_INT_ARGB);
         Graphics2D g = (Graphics2D) cardImage.getGraphics();
         
         File face = new File(Factory.ROOT + "Cards/Ensembles/Ensemble Face.png");
         if (face.exists())
         {
            BufferedImage background = ImageIO.read(face);
            g.drawImage(background, 0, 0, null);
         }

         g.setColor(Color.WHITE);
         int titleHeight = imgGen.paintTitle(g, this);
         int top =  stats.safeMarginH + titleHeight - 50;
         int bottom = stats.h - stats.safeMarginH;
         imgGen.paintText(g, this, top, bottom, 4);
         hackIcons(g);

         // safe box
         g.setColor(Color.BLUE);
         g.setStroke(ImageGenerator.DASHED);
         // g.drawRect(stats.safeMarginW, stats.safeMarginH, stats.safeW, stats.safeH);
         
         g.dispose();
         cardImage.flush();
         
         String path = Factory.getRoot() + "Cards/Ensembles";
         File file = new File(path + name.replaceAll("[?!]", "") + ".png");
         os = new FileOutputStream(file);
         ImageTools.saveAs(cardImage, "png", os, 0f);
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
   
   private void hackIcons(final Graphics2D g)
   {
      ImageGenerator imgGen = Factory.getImageGenerator();
      String name = getNameEnglish();
      
      if (name.equals("Engineer"))
      {
         imgGen.addIcon(g, Factory.ART_DIR + "Icons/Roll +1.png", 90, 90, 665, 70);
         imgGen.addIcon(g, Factory.ART_DIR + "Icons/Roll FIND.png", 65, 65, stats.centerX - 135, stats.centerY - 37);
      }
      else if (name.equals("Hitchhiker"))
         imgGen.addIcon(g, Factory.ART_DIR + "Icons/Move.png", 90, 90, 665, 70);
      else if (name.equals("Lifeguard"))
         imgGen.addIcon(g, Factory.ART_DIR + "Icons/Move.png", 90, 90, 665, 70);
      else if (name.equals("MacGyver"))
         imgGen.addIcon(g, Factory.ART_DIR + "Icons/Equip 1.png", 90, 90, 680, 70);
      else if (name.equals("Naturalist"))
         imgGen.addIcon(g, Factory.ART_DIR + "Icons/Point +2.png", 90, 90, 665, 70);
      else if (name.equals("Photographer"))
         imgGen.addIcon(g, Factory.ART_DIR + "Icons/Point +2.png", 90, 90, 665, 70);
      else if (name.equals("Road Warrior"))
         imgGen.addIcon(g, Factory.ART_DIR + "Icons/Move.png", 90, 90, 665, 70);
      else if (name.equals("Search and Rescue"))
      {
         imgGen.addIcon(g, Factory.ART_DIR + "Icons/Move.png", 90, 90, 665, 70);
         imgGen.addIcon(g, Factory.ART_DIR + "Icons/Roll DNF.png", 76, 76, stats.centerX - 86, stats.centerY - 43);
      }
      else if (name.equals("Tracker"))
      {
         imgGen.addIcon(g, Factory.ART_DIR + "Icons/Move.png", 90, 90, 665, 70);
         imgGen.addIcon(g, Factory.ART_DIR + "Icons/Roll FIND.png", 70, 70, stats.centerX + 65, stats.centerY - 40);
         imgGen.addIcon(g, Factory.ART_DIR + "Icons/Roll DNF.png", 70, 70, stats.centerX + 208, stats.centerY - 40);
      }
      else if (name.equals("Veteran"))
      {
         imgGen.addIcon(g, Factory.ART_DIR + "Icons/Roll +2.png", 90, 90, 665, 70);
         imgGen.addIcon(g, Factory.ART_DIR + "Icons/Roll FIND.png", 75, 75, stats.centerX + 48, 175);
         imgGen.addIcon(g, Factory.ART_DIR + "Icons/Roll DNF.png", 75, 75, stats.centerX + 211, 175);
      }
      else if (name.equals("Weatherman"))
         imgGen.addIcon(g, Factory.ART_DIR + "Icons/Point +2.png", 90, 90, 665, 70);
   }
   
   public static void main(final String[] args)
   {
      validate();
      
      /*
      HtmlGenerator htmlGen = new HtmlGenerator(12, 3);
      htmlGen.printEnsembles(ENSEMBLES);
      System.out.println();
      
      ImageStats stats = Ensemble.getStats(Language.ENGLISH);
      ImageGenerator imgGen = new ImageGenerator(stats, false);
      for (Ensemble ensemble : ENSEMBLES.values())
         ensemble.publish(imgGen);
      System.out.println();

      stats = Ensemble.getStats(Language.FRENCH);
      imgGen = new ImageGenerator(stats, false);
      for (Ensemble ensemble : ENSEMBLES.values())
      {
         ensemble.setLanguage(Language.FRENCH);
         ensemble.publish(imgGen);
      }
      System.out.println();
      */
   }
}
