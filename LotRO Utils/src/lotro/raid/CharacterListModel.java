package lotro.raid;

import gui.comp.DragSite;
import gui.comp.DropSite;

import java.util.TreeSet;

import lotro.models.Character;
import lotro.models.Kinship;
import lotro.models.Player;
import model.CollectionModel;

public class CharacterListModel extends CollectionModel<Character>
implements DragSite, DropSite
{
   private static final long serialVersionUID = 1L;
   
   private Kinship kinship;

   public CharacterListModel (final Kinship kinship)
   {
      super (new TreeSet<Character>());
      this.kinship = kinship;
   }

   public void drag (final Object item)
   {
      // do nothing
   }
   
   public void drop (final Object item)
   {
      /*
      if (item instanceof Player)
         add ((Player) item);
      else if (item instanceof Character)
         add ((Character) item);
      else if (item instanceof Signup)
         add ((Signup) item);
         */
   }
   
   public void add (final Player player)
   {
      for (Character ch : kinship.getCharacters().values())
         if (ch.getPlayer() == player)
            super.add (ch);
   }
   
   public void add (final Signup signup)
   {
      for (Character ch : signup.getCharacters())
         super.add (ch);
   }
}
