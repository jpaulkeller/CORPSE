package geoquest;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import file.FileUtils;
import utils.ImageTools;

public class Equipment extends Component implements Comparable<Equipment>
{
   static final Map<String, Equipment> EQUIPMENT = new TreeMap<>();
   
   static
   {
      populate();
      populateFR();
      setStats();
   }

   private static void setStats()
   {
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

      if (Factory.LANGUAGE == Language.FRENCH)
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
   }
   
   public static ImageStats getImageStats()
   {
      return stats;
   }
   
   private String image;
   private String icon;
   private String ensemble;
   private boolean usedByEvent; // true if used by Event

   public Equipment(final String name, final String text, final String imageName, final String icon, final String ensemble)
   {
      this.name = name;
      this.text = text.length() > 0 ? text : CardUtils.BLANK;
      this.ensemble = ensemble;
      this.image = CardUtils.findImage("Art/Equipment", imageName != null ? imageName : name);
      
      if (icon != null)
      {
         this.icon = CardUtils.findImage("Icons", icon);
         if (this.icon == null)
            this.icon = CardUtils.findImage("Icons", "Missing");
      }
   }

   public String getImage()
   {
      return image;
   }

   @Override
   public String getIcon()
   {
      return icon;
   }

   public String getEnsemble()
   {
      if (Factory.LANGUAGE != Language.ENGLISH)
         return Ensemble.ENSEMBLES.get(ensemble).getName();
      return ensemble;
   }

   @Override
   public int compareTo(final Equipment e)
   {
      return name.compareTo(e.name);
   }

   @Override
   public boolean equals(final Object other)
   {
      if (other instanceof Equipment)
         return name.equals(((Equipment) other).name);
      return false;
   }

   static void populate()
   {
      add("Antenna", "+1 when Searching on Forest tiles", "Antenna", "Search +1", "Engineer");
      add("Backpack", "Provides 4 extra equipment slots, but you can't move more than 5 per turn", null, "Slot 4", "Through Hiker");
      add("Bandana", "You may trade this for another player's equipment card (if they agree)", null, null, "Lifeguard");
      add("Batteries", "You may reroll any Move roll of 1 or less", null, "Reroll", "Eagle Scout");
      add("Belt Pack", "Provides 2 extra equipment slots", null, "Slot 2", "MacGyver");
      add("Binoculars", "+1 to Move when you roll 4 or higher", null, "Move 1", "Search and Rescue");
      add("Camel Pack", "Provides 2 extra equipment slots", null, "Slot 2", "Road Warrior");
      add("Camera", "Gain 1 point for each other <em class=event>Meet and Greet</em> attendee (if you attend)", null, "Point +1", "Photographer");
      add("Cell Phone", "You may add 1 to your Search roll (if you do, gain 1 less point)", null, "Search +1", "Engineer");
      add("CITO Bag", "You may discard this card to gain 3 points", "CITO", "Point +3", "Naturalist");
      add("Compass", "+1 when Searching for all Multi-caches", null, "Search +1", "Eagle Scout");
      add("Duct Tape", "You may discard this Equipment to ignore any Event played on you", null, null, "MacGyver");
      add("Emergency Radio", "+1 when Moving if any weather Event is in effect", null, "Move 1", "Weatherman");
      add("Field Guide", "You may discard this to solve any puzzle cache", null, "Cache Puzzle", "Naturalist");
      add("First-aid Kit", "Discard to prevent any injury Event; gain 4 points if used on another player", null, "Point +4", "Paramedic");
      add("Flashlight", "+1 when Searching for caches not yet found by anyone", null, "Search +1", "Night Cacher");
      add("FRS Radio", "If you roll <em class=find>.F.</em> while moving, you gain 1 point", null, "Point +1", "Search and Rescue");
      add("Gaiters", "+1 when Moving on Swamp tiles, and when crossing a stream", null, "Move 1", "Veteran");
      add("Geocoin", "This card counts as 3 points, or discard it for 1 point", null, "Point +3", "Event Coordinator");
      add("Gloves", "+1 when Searching for any level 3 or higher cache on a Forest tile", null, "Search +1", "Weatherman");
      add("Gorp", "Discard this card to take another turn", null, null, "Road Warrior");
      add("Hat", "You earn an extra point for every <em class=event>Meet and Greet</em> you attend", null, "Point +1", "Lifeguard");
      add("Head Lamp", "+2 when Moving if both dice are black", null, "Move 2", "Night Cacher");
      add("Hiking Boots", "+1 when Moving on Forest tiles", null, "Move 1", "Through Hiker");
      add("Hiking Staff", "+1 when Moving on Rocky tiles", null, "Move 1", "Event Coordinator");
      add("Insect Repellent", "+1 when Searching on Swamp tiles", null, "Search +1", "Night Cacher");
      add("Jeep", "+2 when Moving on Urban tiles; provides 1 extra equipment slot", null, "Slot 1", "Paramedic");
      add("Laptop", "You may solve any Puzzle cache using only 1 of the letters", null, "Cache Puzzle", "Engineer");
      add("Letterbox Stamp", "+1 point whenever you find a level 3 cache", null, "Point +1", "Event Coordinator");
      add("Long Pants", "Provides 1 extra equipment slot", null, "Slot 1", "Veteran");
      add("Lucky Charm", "You get +1 whenever you roll doubles", null, null, "Hitchhiker");
      add("Map", "+1 when Moving if either die is white", null, "Move 1", "Tracker");
      add("Mirror", "Ignore any <em class=dnf>.D.</em> when searching on Urban tiles", null, "Search", "Photographer");
      add("Mountain Bike", "+1 when Moving if either die is 1", null, "Move 1", "Road Warrior");
      add("Ol' Blue", "You may search for caches in adjacent tiles as if you were on them", "Dog", "Search", "Tracker");
      add("Pocket Knife", "+1 when Searching for any level 5 cache", null, "Search +1", "Eagle Scout");
      add("Rain Jacket", "Provides 1 extra equipment slot", null, "Slot 1", "Weatherman");
      add("Repair Kit", "You may discard to gain 2 points if you are on a cache", null, "Point +2", "Paramedic");
      add("Rope", "No other player may play Events on you if you are on a Rocky tile", null, null, "Search and Rescue");
      add("Safari Vest", "Provides 3 extra equipment slots", null, "Slot 3", "Photographer");
      add("Survival Strap", "You may reroll if both dice are 1", null, "Reroll", "Hitchhiker");
      add("Swag Bag", "Provides 1 extra equipment slot", null, "Slot 1", "Hitchhiker");
      add("Trail Guide", "You may move 1 extra tile along a path each turn", null, "Move 1", "Through Hiker");
      add("Utility Tool", "+1 when Searching on Urban tiles", null, "Search +1", "MacGyver");
      add("Waders", "+2 when crossing a stream", null, "Move 2", "Tracker");
      add("Walking Stick", "+1 when Moving on Clear tiles", null, "Move 1", "Naturalist");
      add("Water Bottle", "+1 when Moving if either die is black", null, "Move 1", "Veteran");
      add("Whistle", "Moving onto a tile occupied by another player costs you no movement points", null, "Move Join", "Lifeguard");

      // add ("Coat", "Protects against some cold-related effects", null, "Insulation");
      // add ("Emergency Blanket", "Discard to ignore any weather-related effect", null, "Just In Case");
      // add ("Metal Detector", "Provides a 50/50 chance to prevent effects that would cause you to lose an Equipment card", null, "");
      // add ("Sunscreen", "Protects against some sun-related effects", null, "");
      // add ("PDA", "You may roll again if your first Search roll fails", null, "Engineer");
      // add ("Umbrella", "Protects against some rain-related effects", null, "");
   }

   static void populateFR()
   {
      addFR("Antenna", "Antenne", "+1 en Recherche en Forêt et case Urbaine");
      addFR("Backpack", "Sac à Dos", "Ajoute 4 places  d'équipement, mais restreint le déplacement maximum à 5 par tour");
      addFR("Bandana", "Bandana", "Échangeable contre une carte d'équipement d'un autre joueur (S'il accepte)");
      addFR("Batteries", "Batteries", "Vous pouvez rouler à nouveau un jet de Déplacement de 1 ou moins");
      addFR("Belt Pack", "Sac de Taille", "Ajoute 2 places d'équipement");
      addFR("Binoculars", "Jumelle", "+1 au Déplacement lorsque vous roulez 4 ou plus");
      addFR("Camel Pack", "Sac à Eau", "Ajoute 2 places d'équipement");
      addFR("Camera", "Caméra", "Gagner 1 point pour chaque participant à l'<em class=event>Évènement de Rencontre</em>");
      addFR("Cell Phone", "Mobile", "Vous pouvez ajouter 1 à vos jets de Recherche (Dans ce cas, gagner 1 point de moins)");
      addFR("CITO Bag", "Sac CITO", "Vous pouvez défaussez cette carte pour gagner 3 points");
      addFR("Compass", "Compas", "+1 lors de Recherche pour toutes les Multi-Caches");
      addFR("Duct Tape", "Ruban Adhésif", "Défaussez cet Équipement pour ignorer un Évènement joué contre vous");
      addFR("Emergency Radio", "Radio d'urgence", "+1 au Déplacement lors d'Évènement climatique en jeu");
      addFR("Field Guide", "Guide Pratique", "Défaussez cette carte pour résoudre une cache puzzle");
      addFR("First-aid Kit", "Trousse de Premier Soin", "Défaussez pour prévenir un Évènement de blessure; +4 points si utilisée sur un autre joueur");
      addFR("Flashlight", "Lampe de Poche", "+1 lors de Recherche pour les caches que personne n'a encore trouvées");
      addFR("FRS Radio", "Radio Bidirectionnelle", "Si vous roulez <em class=find>.F.</em> en vous déplaçant, vous gagnez 1 point");
      addFR("Gaiters", "Guêtre", "+1 lors de Déplacement sur case Marécage et lors de traversée de courant d'eau");
      addFR("Geocoin", "Géocoin", "Cette carte compte pour 3 points ou défaussez la pour 1 point");
      addFR("Gloves", "Gants", "+1 lors de Recherche pour les caches sur case Forêt de niveau 3 ou plus");
      addFR("Gorp", "Barre Tendre", "Défaussez cette carte et jouez un autre tour");
      addFR("Hat", "Chapeau", "Vous gagnez 1 point à chaque <em class=event>Évènement de Rencontre</em> que vous participez");
      addFR("Head Lamp", "Lampe Frontale", "+2 lors de Déplacement si les 2 dés sont noirs");
      addFR("Hiking Boots", "Bottes de Randonnée", "+1 lors de Déplacement sur case Forêt");
      addFR("Hiking Staff", "Bâton de Randonnée", "+1 lors de Déplacement sur case Rocheuse");
      addFR("Insect Repellent", "Chasse Moustique", "+1 lors de Recherche sur case Marécageuse");
      addFR("Jeep", "Jeep", "+2 lors de Déplacement sur case Urbaine; ajoute 1 place d'équipement");
      addFR("Laptop", "Portable", "Vous pouvez résoudre les caches Puzzle en utilisant uniquement 1 des lettres");
      addFR("Letterbox Stamp", "Étampe", "+1 point lorsque vous trouvez une cache de niveau 3");
      addFR("Long Pants", "Pantalon Long", "Ajoute 1 place d'équipement");
      addFR("Lucky Charm", "Porte-Bonheur", "Ajouter +1 lorsque vous roulez un double");
      addFR("Map", "Carte", "+1 lors de Déplacement si un dé est blanc");
      addFR("Mirror", "Miroir", "Ignorez <em class=dnf>.D.</em> lors de Recherche sur case Urbaine");
      addFR("Mountain Bike", "Vélo de Montagne", "+1 lors de Déplacement si un des dés est 1");
      addFR("Ol' Blue", "Chien Pisteur", "Vous pouvez rechercher des caches sur des cases adjacentes comme si vous y étiez");
      addFR("Pocket Knife", "Couteau de Poche", "+1 lors de Recherche sur des caches niveau 5");
      addFR("Rain Jacket", "Imperméable", "Ajoute 1 place d'équipement");
      addFR("Repair Kit", "Trousse de Réparation", "Sur une cache, vous pouvez défausser pour gagner 2 points");
      addFR("Rope", "Corde", "Sur une case Rocheuse, aucun autre joueur ne peut jouer d'Évènements sur vous");
      addFR("Safari Vest", "Gilet Safari", "Ajoute 3 places d'équipement");
      addFR("Survival Strap", "Bracelet de Corde", "Vous pouvez rouler à nouveau les doubles 1");
      addFR("Swag Bag", "Sac au Trésors", "Ajout 1 place d'équipement");
      addFR("Trail Guide", "Guide des Sentiers", "Vous pouvez ajouter 1 case de déplacement le long d'un chemin chaque tour");
      addFR("Utility Tool", "Pince Multi-usage", "+1 lors de Recherche sur case Urbaine");
      addFR("Waders", "Bottes-Pantalon", "+2 lors de traversée de courant d'eau");
      addFR("Walking Stick", "Bâton de Marche", "+1 lors de Déplacement sur case Claire");
      addFR("Water Bottle", "Bouteille d'Eau", "+1 lors de Déplacement si un dé est noir");
      addFR("Whistle", "Sifflet", "Se déplacer sur une case occupée par un autre joueur ne coute aucun point de déplacement.");
   }

   private static void add(final String cardName, final String cardText, final String image, 
                           final String icon, final String ensemble)
   {
      Equipment equip = new Equipment(cardName, cardText, image, icon, ensemble);
      EQUIPMENT.put(equip.getName(), equip);
   }

   private static void show()
   {
      for (Equipment eq : EQUIPMENT.values())
         System.out.println(eq.getName() + ": " + eq.getText());
      System.out.println();

      System.out.println("\nASSOCIATED CACHERS/EVENTS/ENSEMBLES");
      for (Equipment eq : EQUIPMENT.values())
      {
         System.out.println(eq.getName() + ":");

         System.out.print("  Ensemble: ");
         for (Ensemble ensemble : Ensemble.ENSEMBLES.values())
            if (eq.getName().equals(ensemble.eq1) || eq.getName().equals(ensemble.eq2) || eq.getName().equals(ensemble.eq3))
               System.out.print(ensemble.getName());
         System.out.println();

         System.out.print("  Events: ");
         for (Event ev : Event.EVENTS.values())
            if (ev.getEquipment().contains(eq.name) || ev.getText().toString().contains(eq.name))
            {
               eq.usedByEvent = true;
               System.out.print(ev.getName() + ", ");
            }
         System.out.println();
      }
      System.out.println();
      System.out.flush();

      System.out.println("Equipment not referenced by any event:");
      for (Equipment eq : EQUIPMENT.values())
         if (!eq.usedByEvent)
            System.out.println("  " + eq.name);
      System.out.println();
      System.out.flush();
      System.out.println();
   }

   private static void validate()
   {
      for (Equipment eq : EQUIPMENT.values())
      {
         if (eq.text == null || eq.text.equals("") || eq.text.equals(CardUtils.BLANK))
            System.err.println("No text for: " + eq);

         Ensemble ensemble = Ensemble.ENSEMBLES.get(eq.ensemble);
         if (ensemble == null)
            System.err.println("Missing ensemble for " + eq.name + ": " + eq.ensemble);
         else if (!ensemble.eq1.equals(eq.name) && !ensemble.eq2.equals(eq.name) && !ensemble.eq3.equals(eq.name))
            System.err.println("Invalid ensemble for " + eq.name + ": " + eq.ensemble);
      }
      System.err.flush();
   }

   public void publishTGC() // in TheGameCrafter format
   {
      ImageGenerator imgGen = Factory.getImageGenerator();
      OutputStream os = null;
      try
      {
         String name = getName();
         System.out.println(" > " + name + ": " + getText());
         
         BufferedImage cardImage = new BufferedImage(stats.w, stats.h, BufferedImage.TYPE_INT_ARGB);
         Graphics2D g = (Graphics2D) cardImage.getGraphics();

         File face = new File(Factory.ROOT + "Cards/Equipment/Equipment Face.png");
         if (face.exists())
         {
            BufferedImage background = ImageIO.read(face);
            g.drawImage(background, 0, 0, null);
         }

         imgGen.paintGrid(g);
         int titleHeight = imgGen.paintTitleLeft(g, this);
         int titleBottom =  stats.safeMarginH + 2 + titleHeight;
         if (getImage() != null)
            imgGen.paintArt(g, getImage(), titleBottom + 35);
         if (getIcon() != null)
            imgGen.paintIcon(g, getIcon(), 70);
         int ensembleHeight = paintEnsemble(g);
         int top = (stats.h / 2);
         int bottom = stats.h - stats.safeMarginH - ensembleHeight;
         imgGen.paintText(g, this, top, bottom, 4);
         hackIcons(g);

         // safe box
         g.setColor(Color.BLUE);
         g.setStroke(ImageGenerator.DASHED);
         // g.drawRect(stats.safeMarginW, stats.safeMarginH, stats.safeW, stats.safeH);
         
         g.dispose();
         cardImage.flush();
         
         String path = Factory.getRoot() + "Cards/Equipment/";
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
   
   private int paintEnsemble(final Graphics2D g)
   {
      String ensemble = getEnsemble();
      g.setFont(stats.ensembleFont);
      FontMetrics fm = g.getFontMetrics(stats.ensembleFont);
      int textHeight = fm.getHeight();
      int textWidth = fm.stringWidth(ensemble);
      if (textWidth > stats.safeW)
      {
         System.out.println(" [ENSEMBLE TOO WIDE] > " + getName() + ": " + ensemble);
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
      g.setStroke(ImageGenerator.STROKE3);
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

   private void hackIcons(final Graphics2D g)
   {
      ImageGenerator imgGen = Factory.getImageGenerator();
      String name = getNameEnglish();
      
      if (name.equals("Backpack"))
         imgGen.addIcon(g, Factory.ART_DIR + "Icons/Move 5 Cap.png", 100, 100, stats.centerX + 130, stats.safeMarginH + 105);
      else if (name.equals("FRS Radio"))
      {
         int x = Factory.LANGUAGE == Language.FRENCH ? 390 : 315;
         imgGen.addIcon(g, Factory.ART_DIR + "Icons/Roll FIND.png", 58, 58, x, stats.centerY + 68);
      }
      else if (name.equals("Jeep"))
         imgGen.addIcon(g, Factory.ART_DIR + "Icons/Move 2.png", 100, 100, stats.centerX + 50, stats.safeMarginH + 5);
      else if (name.equals("Lucky Charm"))
         imgGen.addIcon(g, Factory.ART_DIR + "Icons/Roll +1.png", 90, 90, stats.centerX + 150, stats.safeMarginH);
      else if (name.equals("Mirror"))
      {
         int x = Factory.LANGUAGE == Language.FRENCH ? 280 : 322;
         imgGen.addIcon(g, Factory.ART_DIR + "Icons/Roll DNF.png", 60, 60, x, stats.centerY + 64);
      }
   }
   
   private static void publishTTS() // for Tabletop Simulator
   {
      int columns = 8;
      int rows = EQUIPMENT.size() / columns;
      
      List<String> lines = new ArrayList<>();
      lines.add("# Save File");
      lines.add("cardsx=" + columns);
      lines.add("cardsy=" + rows);
      lines.add("card-width=600");
      lines.add("card-height=825");
      lines.add("zoom=0.25");
      lines.add("background-color=-16777216");

      String path = Factory.TGC.replace(":", "\\:").replace("/", "\\\\");
      int col = 0, row = 0;
      
      for (Equipment eq : EQUIPMENT.values())
      {
         lines.add(col + "_" + row + "=" + path + "Cards\\\\Equipment\\\\" + eq.getName() + ".png");
         col++;
         if (col == columns)
         {
            col = 0;
            row++;
         }
      }
      
      path = Factory.TTS + "Cards/Equipment.tsdb";
      FileUtils.writeList(lines, path, false);
      System.out.println("Tabletop Simulator: " + path);
      System.out.println();
   }
   
   public static void publish()
   {
      Factory.setImageStats(Equipment.getImageStats());
      
      HtmlGenerator htmlGen = new HtmlGenerator(12, 4);
      htmlGen.printEquipment(EQUIPMENT);

      // for (Equipment eq : EQUIPMENT.values())
      //    eq.publishTGC();
      System.out.println();
      
      publishTTS();

      /*
      Factory.setLanguage(Language.FRENCH);
      for (Equipment eq : EQUIPMENT.values())
         eq.publishTGC();
      System.out.println();
      */
   }

   public static void main(final String[] args)
   {
      show();
      validate();
      publish();
   }
}
