package lotro.raid;

import gui.ComponentTools;
import gui.form.ComboBoxItem;

import java.util.SortedSet;
import java.util.TreeSet;

import lotro.models.Character;
import lotro.models.Kinship;
import lotro.models.Player;
import lotro.my.reports.FilterFactory;
import lotro.my.xml.KinshipXML;
import model.CollectionModel;

public class CharacterCombo extends ComboBoxItem
{
   private CollectionModel<Character> model; 

   public CharacterCombo()
   {
      super ("Characters");
      SortedSet<Character> characters = new TreeSet<Character>();
      model = new CollectionModel<Character> (characters);
      setModel (model);
      setEditable (true);
   }
   
   public CharacterCombo (final Kinship kinship)
   {
      super ("Characters");
      SortedSet<Character> characters = 
         new TreeSet<Character> (kinship.getCharacters().values());
      model = new CollectionModel<Character> (characters);
      setModel (model);
      setEditable (true);
   }
   
   public Character add (final String name)
   {
      // check to see if it's already in the model
      for (Character ch : model)
         if (name.equals (ch.getName()))
            return ch;
      
      Character ch = new Character (name); 
      ch.setPlayer (new Player ("[" + name + "]"));
      model.add (ch);
      return ch;
   }
   
   public void add (final Character ch)
   {
      model.add (ch);
   }
   
   public boolean isEmpty()
   {
      return model.isEmpty();
   }
   
   public int size()
   {
      return model.size();
   }
   
   public static void main (final String[] args)
   {
      KinshipXML xml = new KinshipXML();
      xml.setLookupPlayer (true);
      Kinship kinship = xml.scrapeURL ("Landroval", "The Palantiri");
      kinship.setFilter (FilterFactory.getLevelFilter (48));
      
      CharacterCombo cc = new CharacterCombo (kinship);
      ComponentTools.setDefaults();
      ComponentTools.open (cc.getComponent(), "CharacterCombo: " + kinship.getName());
   }
}
