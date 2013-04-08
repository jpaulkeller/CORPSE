package lotro.raid;

import gui.ComponentTools;
import gui.comp.DragDropList;
import gui.comp.RangeSlider;
import gui.form.ComboBoxItem;
import gui.form.TextItem;
import gui.form.ValueChangeEvent;
import gui.form.ValueChangeListener;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.dnd.DnDConstants;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionListener;

import lotro.models.Character;
import lotro.models.CharacterListModel;
import lotro.models.Kinship;
import lotro.models.Klass;
import lotro.models.Player;
import lotro.models.Rank;
import lotro.my.xml.CharacterXML;
import lotro.my.xml.KinshipXML;
import model.CollectionModel;
import model.ObservableDelegate;
import model.ObservableDelegator;

public class CharacterSelector implements ObservableDelegator 
{
   private JPanel panel;
   private JPanel charPanel;
   private JList charList;
   private String worldGiven;
   private String kinGiven;
   private boolean includePlayers;
   private ObservableDelegate observable;
   
   // models
   private Kinship kinLoaded;
   private CollectionModel<String> kinships;
   private SortedSet<Character> allCharacters;
   private CharacterListModel filteredCharacters;
   private CollectionModel<Player> playerModel;
   
   // filters
   private ComboBoxItem worldItem;
   private ComboBoxItem kinItem;
   private TextItem charItem;
   private ComboBoxItem playerItem;
   private ComboBoxItem classItem;
   private ComboBoxItem rankItem;
   private RangeSlider levelSlider;

   public CharacterSelector (final String world, // optional
                             final String kinName, // optional
                             final boolean includePlayers)
   {
      observable = new ObservableDelegate();
      
      this.includePlayers = includePlayers;
      if (includePlayers)
         playerModel = new CollectionModel<Player> (new TreeSet<Player>());
      
      allCharacters = new TreeSet<Character>();
      filteredCharacters = new CharacterListModel (null, false);
      
      makeWorldItem (world);
      makeKinItem (kinName);
      
      MyChangeListener listener = new MyChangeListener();
      
      makeCharacterItem (listener);
      if (includePlayers)
         makePlayerItem (listener);
      makeClassItem (listener);
      makeRankItem (listener);
      makeLevelSlider (listener);

      Border up = BorderFactory.createRaisedBevelBorder();
      Border down = BorderFactory.createLoweredBevelBorder();
      Border border = BorderFactory.createCompoundBorder (up, down);
      
      JPanel filters = new JPanel (new GridLayout(0, 1));
      filters.add (charItem.getTitledPanel());
      if (includePlayers)
         filters.add (playerItem.getTitledPanel());
      filters.add (classItem.getTitledPanel());
      filters.add (rankItem.getTitledPanel());
      filters.setBorder (border);
      
      JPanel controls = new JPanel (new BorderLayout());
      controls.setBorder (new TitledBorder ("Filters"));
      controls.add (levelSlider, BorderLayout.WEST);
      controls.add (filters, BorderLayout.CENTER);
      
      JPanel kinPanel = new JPanel (new GridLayout(0, 1));
      if (world == null)
         kinPanel.add (worldItem.getTitledPanel());
      if (kinName == null)
         kinPanel.add (kinItem.getTitledPanel());

      JPanel top = new JPanel (new BorderLayout());
      top.add (kinPanel, BorderLayout.NORTH);
      top.add (controls, BorderLayout.CENTER);
      
      charList = new DragDropList (filteredCharacters, DnDConstants.ACTION_COPY);
      
      charPanel  = new JPanel (new BorderLayout());
      charPanel.setBorder (new TitledBorder ("Filtered Characters"));
      charPanel.add(new JScrollPane (charList), BorderLayout.CENTER);
      
      panel = new JPanel (new BorderLayout());
      panel.add (top, BorderLayout.NORTH);
      panel.add (charPanel, BorderLayout.CENTER);
   }

   private void makeWorldItem (final String world)
   {
      if (world != null)
         this.worldGiven = world;
      else
      {
         List<String> servers = new ArrayList<String>();
         servers.add ("Arkenstone");
         servers.add ("Brandywine");
         servers.add ("Elendilmir");
         servers.add ("Firefoot");
         servers.add ("Gladden");
         servers.add ("Landroval");
         servers.add ("Meneldor");
         servers.add ("Nimrodel");
         servers.add ("Silverlode");
         servers.add ("Vilya");
         servers.add ("Windfola");
         servers.add ("Bullroarer");
         
         worldItem = new ComboBoxItem ("Server", servers);
         worldItem.setToolTipText ("Select a LOTRO server");
         worldItem.setInitialValue ("Landroval");
         worldItem.addValueChangeListener (new WorldListener());
      }
   }
   
   private void makeKinItem (final String kinName)
   {
      if (kinName != null)
         this.kinGiven = kinName;
      else
      {
         kinships = new CollectionModel<String> (new TreeSet<String>());
         kinships.add ("Creepshow");
         kinships.add ("Irony and Spite");
         kinships.add ("Knights of the White Lady");
         kinships.add ("The Dark Blade");
         kinships.add ("The Palantiri");
         kinships.add ("Valar Guild");
         
         kinItem = new ComboBoxItem ("Kinship / Tribe");
         kinItem.setToolTipText ("Select or enter a Kinship (or Tribe) name");
         kinItem.setModel (kinships);
         kinItem.setEditable (true);
         KinListener kinListener = new KinListener();
         kinItem.addActionListener (kinListener);
         kinItem.addFocusListener (kinListener);
      }
   }
   
   private void makeCharacterItem (final MyChangeListener listener)
   {
      charItem = new TextItem ("Character");
      charItem.setToolTipText ("<html>Enter a pattern to filter the list of characters,<br/>" +
                               "or enter any name (for the selected server) to<br/>" +
                               "include a non-kin character in your report");
      charItem.addKeyListener (new CharListener()); // support non-kin characters
      charItem.addValueChangeListener (listener);
   }

   private void makePlayerItem (final MyChangeListener listener)
   {
      playerItem = new ComboBoxItem ("Player"); 
      playerItem.setToolTipText ("Select a Player to filter the Character list");
      playerItem.setModel (playerModel);
      playerItem.setEditable (true);
      playerItem.addValueChangeListener (listener);
   }

   private void makeClassItem (final MyChangeListener listener)
   {
      classItem = new ComboBoxItem ("Class");
      classItem.setToolTipText ("Select a class to filter the Character list");
      List<Klass> classes = new ArrayList<Klass>();
      classes.add (Klass.None);
      classes.addAll (Klass.FREEPS);
      classes.add (Klass.Unknown);
      classItem.setModel (new CollectionModel<Klass> (classes));
      classItem.addValueChangeListener (listener);
   }

   private void makeRankItem (final MyChangeListener listener)
   {
      rankItem = new ComboBoxItem ("Rank");
      rankItem.setToolTipText ("Select a kinship rank to filter the Character list");
      List<Rank> ranks = new ArrayList<Rank>();
      ranks.addAll (Arrays.asList(Rank.values()));
      rankItem.setModel (new CollectionModel<Rank> (ranks));
      rankItem.addValueChangeListener (listener);
   }

   private void makeLevelSlider (final MyChangeListener listener)
   {
      levelSlider = new RangeSlider (1, Character.MAX_LEVEL, 1, Character.MAX_LEVEL,
                                     RangeSlider.VERTICAL);
      levelSlider.setToolTipText ("Select a Minimum and/or Maximum Level to filter the Character list");
      levelSlider.setForeground (Color.YELLOW);
      levelSlider.setPreferredSize (16, 100);
      levelSlider.setUseLabels (true);
      levelSlider.addChangeListener (listener);
   }

   public void add (final Character ch)
   {
      synchronized (allCharacters)
      {
         allCharacters.add (ch);
      }
      SwingUtilities.invokeLater (new Runnable()
      {
         public void run()
         {
            synchronized (filteredCharacters)
            {
               filteredCharacters.add (ch);
            }
            
            String title = "Filtered Characters (" + size() + "/" + total() + ")";
            ((TitledBorder) charPanel.getBorder()).setTitle (title);
            charPanel.repaint();
            
            if (includePlayers && ch.getPlayer() != null)
               playerModel.add (ch.getPlayer());
         }
      });
   }
   
   public JComponent getComponent()
   {
      return panel;
   }
   
   public int size()
   {
      synchronized (filteredCharacters)
      {
         return filteredCharacters.size();
      }
   }
   
   public int total()
   {
      synchronized (allCharacters)
      {
         return allCharacters.size();
      }
   }
   
   public void setLevelRange (final int minLevel, final int maxLevel)
   {
      levelSlider.setLowValue (minLevel);
      levelSlider.setHighValue (maxLevel);
   }
   
   public void addListSelectionListener (final ListSelectionListener listener) 
   {
      charList.addListSelectionListener (listener);
   }
   
   public void addMouseListener (final MouseListener listener)
   {
      charList.addMouseListener (listener);
   }
   
   public Character getSelected()
   {
      if (charList.getSelectedValues().length == 1)
         return (Character) charList.getSelectedValue();
      return null;
   }

   private String getWorld()
   {
      return worldGiven != null ? worldGiven : (String) worldItem.getValue();
   }
   
   private String getKin()
   {
      return kinGiven != null ? kinGiven : (String) kinItem.getValue();
   }
   
   private void loadKinship (final String world, final String kinName)
   {
      // if the kinship (or world) changed, scrape the new kinship
      if (kinName != null && !kinName.equals ("") &&
          (kinLoaded == null || !kinName.equals (kinLoaded.getName()) ||
           !world.equals (kinLoaded.getWorld())))
      {
         observable.registerChange ("Loading " + kinName + " kinship...");
         
         Thread thread = new Thread (new Runnable()
         {
            public void run()
            {
               synchronized (allCharacters)
               {
                  allCharacters.clear();
               }
               synchronized (filteredCharacters)
               {
                  filteredCharacters.clear();
               }
               
               KinshipXML xml = new KinshipXML();
               xml.setLookupPlayer (includePlayers);
               kinLoaded = xml.scrapeURL (world, kinName);
               if (!kinLoaded.getCharacters().isEmpty())
               {
                  if (kinships != null)
                     kinships.add (kinName);
                  for (Character ch : kinLoaded.getCharacters().values())
                     add (ch);
               }
              
               observable.registerChange ("Loaded " + kinName + " kinship");
            }
         });
         
         thread.start();
      }
   }
   
   public void addObserver (final Observer observer)
   {
      observable.addObserver (observer);
   }

   public void deleteObserver (final Observer observer)
   {
      observable.deleteObserver (observer);
   }

   public Observable getObservable()
   {
      return observable;
   }
   
   class WorldListener implements ValueChangeListener
   {
      public void valueChanged (final ValueChangeEvent e)
      {
         loadKinship ((String) worldItem.getValue(), getKin());
         applyFilters();
      }
   }
   
   class KinListener extends FocusAdapter implements ActionListener
   {
      public void actionPerformed (final ActionEvent e)
      {
         loadKinship (getWorld(), (String) kinItem.getSelectedItem());
         applyFilters();
      }
      
      @Override
      public void focusLost (final FocusEvent e)
      {
         loadKinship (getWorld(), (String) kinItem.getValue());
         applyFilters();
      }
   }
   
   class MyChangeListener implements ValueChangeListener, ChangeListener
   {
      public void valueChanged (final ValueChangeEvent e)
      {
         applyFilters();
      }
      
      public void stateChanged (final ChangeEvent e)
      {
         applyFilters();
      }
   }
   
   private void applyFilters()
   {
      synchronized (filteredCharacters)
      {
         filteredCharacters.clear();
         
         synchronized (allCharacters)
         {
            for (Character ch : allCharacters)
            {
               if (ch.getLevel() < levelSlider.getLowValue())
                  continue;
               else if (ch.getLevel() > levelSlider.getHighValue())
                  continue;
               
               Klass klass = (Klass) classItem.getValue();
               if (klass != null && klass != Klass.None && klass != ch.getKlass())
                  continue;
               
               Rank rank = (Rank) rankItem.getValue();
               if (rank != null && rank != Rank.None && ch.getRank().ordinal() < rank.ordinal())
                  continue;
               
               String charFilter = (String) charItem.getValue();
               if (charFilter != null && !charFilter.equals (""))
                  if (!ch.getName().toLowerCase().contains (charFilter.toLowerCase()))
                     continue;
               
               if (includePlayers && !matchesPlayer (ch))
                  continue;
               
               filteredCharacters.add (ch);
            }
         }
         
         SwingUtilities.invokeLater (new Runnable()
         {
            public void run()
            {
               String title = "Filtered Characters (" + size() + "/" + total() + ")";
               ((TitledBorder) charPanel.getBorder()).setTitle (title);
               charPanel.repaint();
            }
         });
      }
   }
      
   private boolean matchesPlayer (final Character ch)
   {
      Object p = playerItem.getValue();
      if (p == null)
         return true;
      
      if (p instanceof String) // filter based on partial match
      {
         String playerPart = ((String) p).toLowerCase();
         if (playerPart.equals (""))
            return true;
         if (ch.getPlayer().getName().toLowerCase().contains (playerPart))
            return true;
      }
      else if (p instanceof Player) // must match exactly
         if (ch.getPlayer().equals (p))
            return true;
      
      return false;
   }
   
   class CharListener extends KeyAdapter 
   {
      @Override
      public void keyPressed (final KeyEvent e)
      {
         if (e.getKeyCode() == KeyEvent.VK_ENTER)
         {
            Thread thread = new Thread (new Runnable()
            {
               public void run()
               {
                  String name = charItem.getValue().toString();
                  if (name != null && !name.trim().equals (""))
                  {
                     String world = getWorld();
                     final Character ch = CharacterXML.getCharacter (world, name);
                     if (ch != null)
                     {
                        SwingUtilities.invokeLater (new Runnable()
                        {
                           public void run()
                           {
                              add (ch);
                           }
                        });
                     }
                  }
               }
            });
            thread.start();
         }
      }
   }
   
   public static void main (final String[] args)
   {
      // CharacterSelector cs = new CharacterSelector (null, null, false);
      CharacterSelector cs = new CharacterSelector ("Landroval", "The Palantiri", true);
      
      ComponentTools.setDefaults();
      ComponentTools.open (cs.getComponent(), "CharacterSelector");
      cs.setLevelRange (50, Character.MAX_LEVEL);
   }
}
