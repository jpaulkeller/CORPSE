package lotro.raid;

import gui.comp.DragSite;
import gui.comp.DropSite;

import java.util.TreeSet;

import lotro.models.Player;
import model.CollectionModel;

public class PlayerListModel extends CollectionModel<Player>
implements DragSite, DropSite
{
   private static final long serialVersionUID = 1L;

   public PlayerListModel()
   {
      super (new TreeSet<Player>());
   }

   public void drag (final Object item)
   {
      // do nothing
   }
   
   public void drop (final Object item)
   {
      // do nothing
   }
}
