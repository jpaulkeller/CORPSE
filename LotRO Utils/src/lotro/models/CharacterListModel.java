package lotro.models;

import gui.comp.DragSite;
import gui.comp.DropSite;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import javax.swing.SwingUtilities;

import lotro.my.xml.CharacterXML;
import lotro.raid.Signup;
import lotro.raid.SignupListener;
import model.CollectionModel;

public class CharacterListModel extends CollectionModel<Character>
implements DragSite, DropSite
{
   private static final long serialVersionUID = 1L;
   
   private Kinship kinship;
   private boolean autoScrape;
   private List<SignupListener> listeners = new ArrayList<SignupListener>();

   public CharacterListModel (final Kinship kinship, final boolean autoScrape)
   {
      super (new TreeSet<Character>());
      this.kinship = kinship;
      this.autoScrape = autoScrape;
   }

   public void addListener (final SignupListener listener)
   {
      listeners.add (listener);
   }
   
   public void drag (final Object item)
   {
      // do nothing
   }
   
   public void drop (final Object item)
   {
      if (item instanceof Player)
         add ((Player) item);
      else if (item instanceof Character)
         add ((Character) item);
      else if (item instanceof Signup)
         add ((Signup) item);
   }
   
   public void add (final Player player)
   {
      if (kinship != null)
         for (Character ch : kinship.getCharacters().values())
            if (ch.getPlayer() == player)
               add (ch);
   }
   
   public void add (final Signup signup)
   {
      for (Character ch : signup.getCharacters())
         add (ch);
   }
   
   @Override
   public boolean add (final Character ch)
   {
      boolean added = super.add (ch);

      if (added)
      {
         for (SignupListener listener : listeners)
            listener.characterAdded (ch);
         if (autoScrape)
            scrapeCharacter (ch);
      }
      
      return added;
   }

   private void scrapeCharacter (final Character ch)
   {
      Thread thread = new Thread (new Runnable()
      {
         public void run()
         {
            CharacterXML.loadCharacter (ch);
            SwingUtilities.invokeLater (new Runnable()
            {
               public void run()
               {
                  fireContentsChanged (CharacterListModel.this, 0, size() - 1);
                  for (SignupListener listener : listeners)
                     listener.characterUpdated (ch);
               }
            });
         }
      });
      thread.start();
   }
   
   @Override
   public boolean remove (final Object o)
   {
      boolean removed = super.remove (o);
      if (removed && o instanceof Character)
         for (SignupListener listener : listeners)
            listener.characterRemoved ((Character) o);
      return removed;
   }   
}
