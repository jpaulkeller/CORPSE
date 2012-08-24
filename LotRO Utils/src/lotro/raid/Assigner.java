package lotro.raid;

import gui.ComponentTools;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import lotro.models.Character;
import lotro.models.Kinship;
import lotro.models.Klass;
import lotro.models.Player;
import lotro.my.reports.FilterFactory;
import lotro.my.xml.KinshipXML;
import lotro.web.Dropbox;

public final class Assigner
{
   private Group group;
   private List<Character> characters;
   private List<DefaultComboBoxModel> models = new ArrayList<DefaultComboBoxModel>();
   private ActionListener listener;

   private Assigner (final Group group)
   {
      this.group = group;
      this.characters = new ArrayList<Character> (group.getBestGroup());
      this.listener = new ChangeListener();
   }

   private void makeGUI()
   {
      JPanel panel = new JPanel (new GridLayout (0, 1));
      /*
      for (Composition.CompSlot slot : group.getComposition().getSlots())
      {
         Character selected = pop (slot.getKlass());
         panel.add (makeField (slot, selected));
      }
      */
      
      ComponentTools.open (panel, "Assigner");
   }

   /*
   private Component makeField (final CompSlot slot, final Character selected)
   {
      // String color = slot.getColor();
      // slot.getScore()
      
      JPanel field = new JPanel (new BorderLayout());
      field.add (new JLabel (slot.getKlass().toString()), BorderLayout.WEST);
      field.add (makeCombo (slot.getKlass(), selected), BorderLayout.EAST);
      return field;
   }
   */
   
   private JComboBox makeCombo (final Klass klass, final Character selected)
   {
      DefaultComboBoxModel model = new DefaultComboBoxModel();
      model.addElement (null);
      for (Signup signup : group.getSignups())
         for (Character ch : signup.getCharacters())
            if (ch.getKlass() == klass)
               model.addElement (ch);
      models.add (model);

      JComboBox box = new JComboBox (model);
      box.setEditable (true);
      box.setSelectedItem (selected);
      box.addActionListener (listener);
      return box;
   }

   private Character pop (final Klass klass)
   {
      Iterator<Character> iter = characters.iterator();
      while (iter.hasNext())
      {
         Character c = iter.next();
         if (c.getKlass() == klass)
         {
            iter.remove();
            return c;
         }
      }
      return null;
   }

   class ChangeListener implements ActionListener
   {
      public void actionPerformed (final ActionEvent e)
      {
         JComboBox box = (JComboBox) e.getSource();
         ComboBoxModel changedModel = box.getModel();
         Character selected = (Character) changedModel.getSelectedItem();
         if (selected != null)
         {
            for (ComboBoxModel model : models)
               if (model != changedModel && model.getSize() > 1)
               {
                  Character ch = (Character) model.getSelectedItem();
                  if (selected == ch)
                     model.setSelectedItem (null); // TBD choose another?
               }
         }
      }
   }
   
   public static void main (final String[] args)
   {
      KinshipXML xml = new KinshipXML();
      xml.setLookupPlayer (true);
      Kinship kinship = xml.scrapeURL ("Landroval", "The Palantiri");
      kinship.setFilter (FilterFactory.getLevelFilter (48));
      
      String dropbox = Dropbox.get().getPath ("/raids/Test 1.signup");
      Map<Player, Signup> signups = Signup.loadFromFile (kinship, dropbox);
      dropbox = Dropbox.get().getPath ("/raids/Rift Day 3 Balrog.raid");
      Composition comp = Composition.loadFromFile (dropbox);
      Group group = new Group (null);
      group.setSignups (signups);
      group.setComposition (comp);
      group.optimize();
      
      Assigner app = new Assigner (group);
      app.makeGUI();
   }
}
