package lotro.sk;

import file.FileUtils;
import gui.ComponentTools;
import gui.comp.ListOrderable;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;

import lotro.models.Character;
import lotro.web.Dropbox;
import model.ListModel;

public final class SuicideKingsList
{
   // private SortedListModel<RiftPlayer> model;
   private ListModel<RiftPlayer> model;
   private JList view;
   private JPanel panel;
   
   private SuicideKingsList()
   {
      loadModel();
      view = new JList (model);

      JButton button = new JButton ("Save");
      button.addActionListener (new ButtonListener());
      
      ListOrderable<RiftPlayer> lo = new ListOrderable<RiftPlayer> (view, "");
      panel = new JPanel (new BorderLayout()); 
      panel.add (button, BorderLayout.NORTH);
      panel.add (lo, BorderLayout.CENTER);
   }
   
   private void loadModel()
   {
      List<RiftPlayer> players = new ArrayList<RiftPlayer>();
      RiftPlayer.load();
      for (RiftPlayer player : RiftPlayer.PLAYERS.values())
         players.add (player);
         
      // model = new SortedListModel<RiftPlayer>();
      model = new ListModel<RiftPlayer> (players);
   }
   
   private class ButtonListener implements ActionListener
   {
      public void actionPerformed (final ActionEvent e) // Save
      {
         String cmd = e.getActionCommand();
         if (cmd.equals ("Save"))
         {
            List<String> lines = new ArrayList<String>();
            for (int i = 0; i < model.size(); i++)
            {
               RiftPlayer player = model.getElementAt (i);
               player.setOrder (i + 1);
               lines.add (player.getName());
            }
            panel.repaint();
            FileUtils.writeList (lines, RiftPlayer.SK_LIST, false);
         }
         else
            System.out.println ("Unsupported command: " + cmd);
      }
   }
   
   public static void main (final String[] args)
   {
      Character.load (Dropbox.CHAR_PATH);
      SuicideKingsList app = new SuicideKingsList();
      ComponentTools.open (app.panel, "Suicide Kings");
   }
}
