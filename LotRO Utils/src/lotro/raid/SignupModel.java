package lotro.raid;

import gui.ComponentTools;
import gui.comp.DropSite;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JList;
import javax.swing.SwingUtilities;

import lotro.models.Character;
import lotro.models.Kinship;
import lotro.models.Player;
import lotro.my.reports.FilterFactory;
import lotro.my.xml.CharacterXML;
import lotro.my.xml.KinshipXML;
import model.ListModel;

public class SignupModel extends ListModel<Signup> implements DropSite
{
   private static final long serialVersionUID = 1L;

   private Kinship kinship;
   private List<SignupListener> listeners = new ArrayList<SignupListener>();
   
   public SignupModel()
   {
      super (new ArrayList<Signup>());
   }
   
   public void setKinship (final Kinship kinship) // TBD
   {
      this.kinship = kinship;
   }
   
   public void addListener (final SignupListener listener)
   {
      listeners.add (listener);
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
      for (Character ch : kinship.getCharacters().values())
         if (ch.getPlayer() == player)
            add (ch);
   }
   
   public void add (final Character ch)
   {
      Signup owner = getOwner (ch);
      owner.addCharacter (ch);
      add (owner);
      
      for (SignupListener listener : listeners)
         listener.characterAdded (ch);
      
      Thread thread = new Thread (new Runnable()
      {
         public void run()
         {
            CharacterXML.loadCharacter (ch);
            SwingUtilities.invokeLater (new Runnable()
            {
               public void run()
               {
                  fireContentsChanged (SignupModel.this, 0, size() - 1); // update label
                  for (SignupListener listener : listeners)
                     listener.characterUpdated (ch);
               }
            });
         }
      });
      thread.start();
   }

   public boolean remove (final Signup signup)
   {
      boolean removed = super.remove (signup);
      if (removed)
         for (Character ch : signup.getCharacters())
            for (SignupListener listener : listeners)
               listener.characterRemoved (ch);
      return removed;
   }

   // public boolean remove (final Character ch) TBD

   private Signup getOwner (final Character ch)
   {
      Signup owner = null;
      Iterator<Signup> iter = iterator();
      while (iter.hasNext())
      {
         Signup signup = iter.next();
         if (ch.getPlayer() != null && ch.getPlayer().equals (signup.getPlayer()))
         {
            owner = signup;
            iter.remove(); // TBD: will this mess up the order?
            break;
         }
      }
      if (owner == null)
         owner = new Signup();
      return owner;
   }
   
   public Set<Character> getCharacters()
   {
      Set<Character> characters = new HashSet<Character>();
      for (Signup signup : this)
         for (Character ch : signup.getCharacters())
            characters.add (ch);
      return characters;
   }
   
   public static void main (final String[] args)
   {
      KinshipXML xml = new KinshipXML();
      xml.setIncludeDetails (true);
      xml.setLookupPlayer (true);
      Kinship kinship = xml.scrapeURL ("Landroval", "The Palantiri");
      kinship.setFilter (FilterFactory.getLevelFilter (48));

      SignupModel model = new SignupModel();
      model.setKinship (kinship);
      ComponentTools.open (new JList (model), "SignupModel");
   }
}
