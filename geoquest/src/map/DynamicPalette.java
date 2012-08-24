package map;

import gui.ComponentTools;
import gui.form.TextItem;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import model.queue.ScheduleQueue;
import model.queue.Task;
import utils.Utils;

/**
 * The DynamicPalette class is a Visio-like GUI that provides a palette of
 * icons matching user-entered search terms.
 */
public class DynamicPalette
{
   static final int MAX_ICONS = 200;

   // private static final Icon SEARCH_ICON =
   //    ComponentTools.loadImageIcon ("icons/buttons/Magnify.gif", null);

   private String path;
   private JPanel panel;
   // private JButton searchButton;
   // private JPopupMenu popupMenu;
   private TextItem searchItem;
   private JLabel countLabel;
   private JPanel iconPanel;
   private ActionListener buttonListener;
   
   private ScheduleQueue queue;
   private Task finished;
   
   private BlockingQueue<PaletteTile> iconQueue;
   private Thread scanner;
   
   private int total;
   private Set<String> urls = new HashSet<String>();
   private Set<String> searches = new HashSet<String>();
   private Pattern highlightPattern;
   
   public DynamicPalette (final String title,
                          final String path, final String searchTerm,
                          final ActionListener buttonListener)
   {
      this.path = path;
      this.buttonListener = buttonListener;
      
      iconPanel = new JPanel (new GridLayout (0, 4));
      JPanel top = new JPanel (new BorderLayout());
      top.add (iconPanel, BorderLayout.NORTH); // so icons don't stretch
      JScrollPane scroll = new JScrollPane (top);
      scroll.setHorizontalScrollBarPolicy
         (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
      scroll.getVerticalScrollBar().setUnitIncrement (32);
      scroll.addComponentListener (new ResizeListener());

      panel = new JPanel (new BorderLayout());
      panel.setBorder (BorderFactory.createTitledBorder (title));
      panel.setPreferredSize (new Dimension (50, 200));
      panel.add (makeSearchPanel(), BorderLayout.NORTH);
      panel.add (scroll, BorderLayout.CENTER);
      
      iconQueue = new LinkedBlockingQueue<PaletteTile>();
      scanner = new PaletteScanner();
      scanner.start();

      // create one task that's called whenever a search if finished
      finished = new FinishedTask();
      finished.setRequiresSwing (true);
      finished.setPriority (Integer.MAX_VALUE);

      searchItem.setInitialValue (searchTerm);
      queue = new ScheduleQueue (500, 1000);
      queue.start();
      
      spawnSearch (false);
   }

   private JPanel makeSearchPanel()
   {
      ActivationListener listener = new ActivationListener();
      
      /*
      searchButton = new JButton (SEARCH_ICON);
      searchButton.setToolTipText
         ("Click to find matching symbols (right-click for more options)");
      searchButton.setMargin (new Insets (0, 0, 0, 0));
      searchButton.setFocusable (false);
      searchButton.addActionListener (listener);
      
      PopupListener popupListener = new PopupListener();
      popupMenu = getPopup (popupListener);
      searchButton.addMouseListener (popupListener);
      */
      
      searchItem = new TextItem (null, 15);
      searchItem.getComponent().addKeyListener (listener);

      countLabel = new JLabel ("");
      countLabel.setToolTipText ("Number of matching symbols");
      
      JPanel searchPanel = new JPanel (new BorderLayout());
      // searchPanel.add (searchButton, BorderLayout.WEST);
      searchPanel.add (new JLabel ("Filter"), BorderLayout.WEST);
      searchPanel.add (searchItem.getComponent(), BorderLayout.CENTER);
      searchPanel.add (countLabel, BorderLayout.EAST);
      return searchPanel;
   }
   
   public JPanel getPanel()
   {
      return panel;
   }
   
   public Set<String> getURLs()
   {
      return urls;
   }
   
   public void queueTile (final PaletteTile tile)
   {
      iconQueue.add (tile);
   }
   
   public ActionListener getButtonListener()
   {
      return buttonListener;
   }
   
   // set the number of columns based on current size of the scroll panel
   class ResizeListener extends ComponentAdapter
   {
      @Override
      public void componentResized (final ComponentEvent e)
      {
         JScrollPane scroll = (JScrollPane) e.getSource();
         Dimension dim = scroll.getSize();
         int width = dim.width - scroll.getVerticalScrollBar().getWidth();
         int columns = Math.max (4, (width / 34) - 1);
         iconPanel.setLayout (new GridLayout (0, columns));
      }
   }

   class ActivationListener extends KeyAdapter implements ActionListener
   {
      @Override
      public void keyReleased (final KeyEvent e)
      {
         int keyCode = e.getKeyCode();
         if (keyCode == KeyEvent.VK_ENTER)
         {
            clearMatches();
            spawnSearch (false);
         }
      }
      
      public void actionPerformed (final ActionEvent e)
      {
         clearMatches();
         spawnSearch (false);
      }
   }

   /*
   private JPopupMenu getPopup (ActionListener listener)
   {
      JPopupMenu menu = new JPopupMenu ("Search Modes");
         
      JMenuItem mi = new JMenuItem ("Append");
      mi.setToolTipText ("Append matching symbols into the current palette");
      mi.addActionListener (listener);
      menu.add (mi);
         
      mi = new JMenuItem ("Refine");
      mi.setToolTipText 
         ("Find symbols matching ALL of the search terms (\"AND\" search)");
      mi.addActionListener (listener);
      menu.add (mi);
      
      mi = new JMenuItem ("Replace");
      mi.setToolTipText
         ("Find symbols matching ANY of the search terms (\"OR\" search)");
      mi.addActionListener (listener);
      menu.add (mi);
      
      menu.addSeparator();
      
      mi = new JMenuItem ("Clear");
      mi.setToolTipText ("Empty the current palette");
      mi.addActionListener (listener);
      menu.add (mi);
      
      return menu;
   }
   
   class PopupListener extends MouseAdapter implements ActionListener
   {
      @Override public void mousePressed  (MouseEvent e) { maybeShowPopup(e); }
      @Override public void mouseReleased (MouseEvent e) { maybeShowPopup(e); }

      private void maybeShowPopup (MouseEvent e)
      {
          if (e.isPopupTrigger())
             popupMenu.show (e.getComponent(), e.getX(), e.getY());
      }
      
      public void actionPerformed (ActionEvent e)
      {
         String cmd = e.getActionCommand();
         if (cmd.equals ("Append"))
            spawnSearch (false);
         else if (cmd.equals ("Refine"))
            spawnSearch (true);
         else if (cmd.equals ("Replace"))
         {
            clearMatches();
            spawnSearch (false);
         }
         else if (cmd.equals ("Clear"))
            clearMatches();
      }
   }
   */
   
   private void spawnSearch (final boolean refine)
   {
      searchItem.setEnabled (false);
      
      String searchTerms = (String) searchItem.getValue();
      if (searchTerms != null && !searchTerms.equals (""))
      {
         String[] terms = searchTerms.split ("[,\\s]+");
         for (int term = 0; term < terms.length; term++)
         {
            boolean refineThisPass = refine && (term > 0 || !urls.isEmpty());
            queue.add (new SearchTask (terms[term], refineThisPass));
         }
      }
      else
         queue.add (new SearchTask (null, false));
      
      queue.add (finished);
   }
   
   Pattern getHighlightPattern()
   {
      if (highlightPattern == null)
      {
         StringBuilder sb = new StringBuilder();
         for (String term : searches)
         {
            if (sb.length() > 0)
               sb.append ("|");
            sb.append (Pattern.quote (term));
         }
         highlightPattern = Pattern.compile
            (sb.toString(), Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
      }
      return highlightPattern;
   }

   private void clearMatches()
   {
      searches.clear();
      highlightPattern = null;
      total = 0;
      iconPanel.removeAll();
      urls.clear();
      panel.validate();
      panel.repaint();
   }
   
   class SearchTask extends Task
   {
      private static final long serialVersionUID = 1;

      private String searchTermUpper;
      private boolean refine;
      private IconSource source;

      SearchTask (final String searchTerm, final boolean refine)
      {
         if (searchTerm != null)
         {
            this.searchTermUpper = searchTerm.toUpperCase();
            if (!searchTerm.equals (""))
            {
               searches.add (searchTerm);
               highlightPattern = null;
            }
         }
         this.refine = refine;
         source = new IconSource (DynamicPalette.this, searchTermUpper);
      }

      @Override public String getID() { return searchTermUpper; }

      @Override public void run()
      {
         countLabel.setText ("Searching...");
         panel.repaint();
         
         // wait here until the previous search thread is done queuing
         while (!iconQueue.isEmpty())
            Utils.sleep (100);
         
         if (refine)
         {
            if (iconPanel.getComponentCount() > 0) // if there are any left
               total = refineSearch();
         }
         else
            total += source.searchFiles (path);
      }
      
      private int refineSearch()
      {
         Pattern refinePattern = Pattern.compile
            (searchTermUpper, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

         Component[] components = iconPanel.getComponents();
         List<PaletteTile> icons = new ArrayList<PaletteTile>();
         for (Component pi : components)
            icons.add ((PaletteTile) pi);
            
         // if the icon doesn't match the new search pattern, remove it
         Iterator<PaletteTile> iter = icons.iterator();
         while (iter.hasNext())
         {
            PaletteTile pi = iter.next();
            if (!pi.matches (refinePattern))
            {
               iconPanel.remove (pi);
               iter.remove();
               panel.repaint();
            }
         }
         
         return iconPanel.getComponentCount(); // return the number left
      }
   }

   class FinishedTask extends Task
   {
      private static final long serialVersionUID = 1;

      @Override public void run()
      {
         int count = iconPanel.getComponentCount();
         updateCount (count);
         searchItem.setEnabled (true);
      }
   }

   // The number of symbols (URLs) shown may be less than the total
   // found because of invalid data, or because there were too many (more
   // than MAX_ICONS).
   private void updateCount (final int count)
   {
      if (total > count)
         countLabel.setText (count + " of " + total);
      else if (total > 0)
         countLabel.setText ("" + total);
      else
         countLabel.setText ("No matches");
      panel.repaint();
   }
   
   // This thread is responsible for processing PaletteTile objects as they
   // are added to the iconQueue.

   class PaletteScanner extends Thread
   {
      public PaletteScanner()
      {
         setDaemon (true);
      }

      @Override public void run()
      {
         // loop forever; blocking if the queue is empty
         try
         {
            while (true)
               addIcon (iconQueue.take());
         }
         catch (InterruptedException x) { }
      }
      
      private void addIcon (final PaletteTile icon)
      {
         // do the following on the GUI event-dispatching thread
         SwingUtilities.invokeLater (new Runnable() {
            public void run()
            {
               iconPanel.add (icon);
               countLabel.setText (iconPanel.getComponentCount() + "");
               panel.validate();
               panel.repaint();
            }
         });
      }
   }
   
   public static void main (final String[] args)
   {
      DynamicPalette dp = new DynamicPalette ("Dynamic Palette", ".", null, null);
      ComponentTools.setDefaults();
      ComponentTools.open (dp.getPanel(), "Palette");
   }
}
