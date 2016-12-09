package geoquest;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import str.StringUtils;
import utils.ImageTools;

public class Cacher extends Component implements Comparable<Cacher>
{
   static final Map<String, Cacher> CACHERS = new TreeMap<>();
   
   static
   {
      populate();
      populateFR();
      setStats();
   }

   private static void setStats()
   {
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

      stats.titleFontName = "Segoe Print";
      
      stats.titleFont = new Font(stats.titleFontName, Font.BOLD, 60);
      stats.titleFont2 = new Font(stats.titleFontName, Font.BOLD, 50);
      stats.titleBg = new Color(0, 229, 91, 175); // green, translucent
         
      stats.textFont = new Font("Cabin", Font.PLAIN, 70);
      stats.textFont2 = new Font("Cabin", Font.PLAIN, 60);
      
      stats.letterFont = new Font("Days", Font.PLAIN, 80);
   }
   
   public static ImageStats getImageStats()
   {
      return stats;
   }
   
   public Cacher(final String name, final String text)
   {
      this.name = name;
      this.text = text.length() > 0 ? text : CardUtils.BLANK;
   }

   public int compareTo(final Cacher e)
   {
      return name.compareTo(e.name);
   }

   @Override
   public boolean equals(final Object other)
   {
      if (other instanceof Cacher)
         return name.equals(((Cacher) other).name);
      return false;
   }

   @Override
   public int hashCode()
   {
      return name.hashCode();
   }

   private static void populate()
   {
      add("Athletic Alex", "Alex gets +1 to Move for each empty Equipment slot (not counting extra slots).");
      add("Birder Brandon", "Brandon gets +1 when searching for Multi-caches.");
      add("Collector Colin", "Colin starts the game with a random Travel Bug ®.");
      add("Determined Dan", "Once per turn, Dan may re-roll a <em class=dnf>.D.</em> when searching.");
      add("Eager Earl", "Whenever a new cache is placed on the board, Earl may immediately move 4 tiles.");
      add("Fast Fred", "Fred gets +1 to all Move rolls in Clear and Urban tiles.");
      add("Grampa Gary", "Gary can't move more than 5 per turn, but he gets +1 to all Search rolls.");
      add("Hunter Henry", "Henry gets +1 when searching in Forest tiles.  Other players can't play events on Henry if he's on a Forest tile.");
      add("Independent Isabel", "Other players may only play events on Isabel if they are in the same map quadrant.");
      add("Jolly Jamie", "Other players can't play events on Jamie unless she has more points than they do.");
      add("Kindly Kris", "Kris can discard Event cards to gain 1 point each.");
      add("Lucky Lisa", "Lisa gets +1 whenever she rolls doubles.");
      add("Marathon Martin", "Martin may treat any \"end your turn\" effect as -2 instead.");
      add("Nosy Norman", "Whenever another player rolls 5 or higher, Norman may immediately move 1 tile.");
      add("Observant Oscar", "Oscar gets +1 when searching for any cache that has already been found.");
      add("Puzzler Paul", "Paul gets +1 when searching for puzzle caches.");
      add("Quirky Quigly", "Quigly may hold up to 3 Event cards; and he starts the game with a random one.");
      add("Ranger Rachel", "Rachel gets +2 to all Move rolls in Forest tiles.");
      add("Scout Scotty", "Scotty may discard Equipment cards to gain 1 point each.");
      add("Trader Ted", "When Ted finds a cache, he may draw two Equipment cards (but only keep one).");
      add("Unique Ursula", "Whenever Ursula rolls a 1 on either die, she gets +1 on her roll.");
      add("Volunteer Veda", "Veda gains 1 extra point for Event cards that award her points. She must attend all <em class=event>Meet and Greet</em> events.");
      add("Wandering Warren", "If Warren rolls a <em class=find>.F.</em> while moving, he may take an extra turn.");
      add("Yuppie Yuri", "Yuri starts the game with an extra Equipment card.");
      // "Xander the Explorer"
      // "Zealous Zach"
   }
   
   static void populateFR()
   {
      addFR("Athletic Alex", /*"Athlétique */ "Amélie", "Amélie obtient +1 à Move pour chaque slot d'équipement vide (sans compter les créneaux supplémentaires).");
      addFR("Birder Brandon", /*"Bénévole */ "Benjamin", "Benjamin gagne 1 point supplémentaire pour les cartes d'événement qui lui attribuent des points. Elle doit assister à tous les événements <em class=event>Rencontrez-nous</em>."); // volunteer
      addFR("Collector Colin", /*"Chanceux */ "Camille", "Camille obtient +1 chaque fois qu'elle roule double."); // lucky  
      addFR("Determined Dan", /*"Déterminé */ "Daniel", "Une fois par tour, Daniel peut relancer une <em class=dnf>.D.</em> lors de la recherche.");
      addFR("Eager Earl", /*"Enthousiaste */ "Étienne", "Chaque fois qu'un nouveau cache est placé sur la carte, Étienne peut immédiatement déplacer 4 tuiles."); 
      addFR("Fast Fred", /*"Forestier */ "François", "François obtient +2 à tous les rouleaux Move en tuiles Forest."); // ranger
      addFR("Grampa Gary", /*"Grand-père */ "Gabriel", "Gabriel ne peut pas déplacer plus de 5 par tour, mais il obtient +1 à tous les rouleaux de recherche.");
      addFR("Hunter Henry", /*"Habile */ "Hugo", "Hugo obtient +1 lors de la recherche de caches de casse-tête."); // puzzler 
      addFR("Independent Isabel", /*"Indépendant */ "Isabelle", "D'autres joueurs ne peuvent jouer des événements sur Isabelle que s'ils sont dans le même quadrant de carte.");
      addFR("Jolly Jamie", /*"Jovial */ "Julie", "D'autres joueurs ne peuvent pas jouer des événements sur Julie à moins qu'elle ait plus de points qu'ils.");
      addFR("Kindly Kris", "Karine", "Karine peut se défaire des cartes Event pour gagner 1 point chacune.");
      addFR("Lucky Lisa", /*"Lettré */ "Léa", "Léa commence le jeu avec un Travel Bug ® aléatoire."); // collector
      addFR("Marathon Martin", /*"Marathon */ "Martin", "Martin peut traiter n'importe quel effet \"finir votre tour\" comme -2 à la place.");
      addFR("Nosy Norman", /*"Nanti */ "Nicole", "Nicole commence le jeu avec une carte d'équipement supplémentaire."); // yuppie
      addFR("Observant Oscar", /*"Observateur */ "Olivier", "Olivier obtient +1 lors de la recherche de tout cache qui a déjà été trouvé.");
      addFR("Puzzler Paul", /*"Poursuivante */ "Priscilla", "Si Priscilla lance une <em class=find>.F.</em> en se déplaçant, il peut prendre un tour supplémentaire."); // wanderer 
      addFR("Quirky Quigly", /*"Québécois */ "Quentin", "Quentin peut contenir jusqu'à 3 cartes d'événement; Et il commence le jeu avec un aléatoire.");
      addFR("Ranger Rachel", /*"Rapide */ "Raphaël", "Raphaël obtient +1 pour tous les rouleaux Move dans les tuiles Clear et Urban."); // fast
      addFR("Scout Scotty", /*"Scout */ "Serge", "Serge peut se défaire des cartes d'équipement pour gagner 1 point chacune.");
      addFR("Trader Ted", "Thomas", "Lorsque Thomas trouve un cache, il peut tirer deux cartes d'équipement (mais n'en garder qu'une).");
      addFR("Unique Ursula", /*"Unique */ "Ulrich", "Chaque fois que Ulrich roule un 1 sur chaque mourir, elle obtient +1 sur son rouleau.");
      addFR("Volunteer Veda", /*"Veneur */ "Victor", "Victor obtient +1 lors de la recherche dans les tuiles Forest. D'autres joueurs ne peuvent pas jouer des événements sur Henry si il est sur une tuile Forest."); // hunter
      addFR("Wandering Warren", "Willam", "Willam obtient +1 lors de la recherche de Multi-caches."); // birder
      addFR("Yuppie Yuri", "Yohan", "Chaque fois qu'un autre joueur roule 5 ou plus, Yohan peut immédiatement déplacer 1 tuile."); // nosy
   }

   private static void add(final String cardName, final String cardText)
   {
      Cacher cacher = new Cacher(cardName, cardText);
      CACHERS.put(cacher.getName(), cacher);
   }

   private static void show()
   {
      for (Cacher cacher : CACHERS.values())
         if (cacher.name != CardUtils.BLANK)
         {
            System.out.print(StringUtils.pad(cacher.toString(), 20));
            for (Event event : Event.EVENTS.values())
               if (event.getText().toString().contains(cacher.name))
                  System.out.print(StringUtils.pad(event.getName(), 20) + ", ");
            System.out.println();
            System.out.println("  > " + cacher.text);
         }
      System.out.println();
   }

   public void publish() // in TheGameCrafter format
   {
      ImageGenerator imgGen = Factory.getImageGenerator();
      OutputStream os = null;
      try
      {
         String cardName = getName();
         System.out.println(" > " + cardName + ": " + getText());
         
         BufferedImage cardImage = new BufferedImage(stats.w, stats.h, BufferedImage.TYPE_INT_ARGB);
         Graphics2D g = (Graphics2D) cardImage.getGraphics();
         
         File face = new File(Factory.ROOT + "Cards/Cachers/Cacher Face.png"); // use ROOT to ignore language
         if (face.exists())
         {
            BufferedImage background = ImageIO.read(face);
            g.drawImage(background, 0, 0, null);
         }

         imgGen.paintGrid(g);
         int titleHeight = imgGen.paintTitleRight(g, this);
         int titleBottom =  stats.safeMarginH + 2 + titleHeight;
         imgGen.addIcon(g, Factory.ART_DIR + "Cards/Cachers/Letter Box.png", 150, 150, stats.safeMarginW + 5, stats.safeMarginH + 5);
         
         // letter
         g.setFont(stats.letterFont);
         g.setColor(Color.BLACK);
         FontMetrics fm = g.getFontMetrics(stats.letterFont);
         String letter = name.substring(0, 1);
         int letterWidth = fm.stringWidth(letter);
         g.drawString(letter, stats.safeMarginW + 60 - (letterWidth / 2), 180); // TODO
         
         int bottom = stats.h - stats.safeMarginH;
         imgGen.paintText(g, this, titleBottom, bottom, 4);
         hackIcons(g);

         // safe box
         g.setColor(Color.BLUE);
         g.setStroke(ImageGenerator.DASHED);
         // g.drawRect(stats.safeMarginW, stats.safeMarginH, stats.safeW, stats.safeH);
         
         g.dispose();
         cardImage.flush();
         
         String path = Factory.getRoot() + "Cards/Cachers/";
         File file = new File(path + cardName.replaceAll("[?!]", "") + ".png");
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
      String name = getName();
      
      if (name.equals("Determined Dan"))
         imgGen.addIcon(g, Factory.ART_DIR + "Icons/Roll DNF.png", 90, 90, 473, 325);
      else if (name.equals("Daniel")) // French
         imgGen.addIcon(g, Factory.ART_DIR + "Icons/Roll DNF.png", 77, 77, 297, 365);
      else if (name.equals("Priscilla")) // French
         imgGen.addIcon(g, Factory.ART_DIR + "Icons/Roll FIND.png", 80, 80, 110, 290);
      else if (name.equals("Wandering Warren"))
         imgGen.addIcon(g, Factory.ART_DIR + "Icons/Roll FIND.png", 80, 80, 620, 257);
   }
   
   public static void main(final String[] args)
   {
      show();
   }
}
