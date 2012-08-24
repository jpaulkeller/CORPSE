package lotro.sk;

import gui.ComponentTools;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;

import org.jdesktop.swingx.JXTable;

import utils.Utils;

public final class SK1
{
   private DefaultTableModel model;
   private JXTable view;
   private JPanel panel;
   
   private SK1()
   {
      loadModel();

      JButton button = new JButton ("Save");
      button.addActionListener (new ButtonListener());
      
      JScrollPane scroll = new JScrollPane (view);
      scroll.setBorder (Utils.BORDER);
      panel = new JPanel (new BorderLayout()); 
      panel.add (button, BorderLayout.NORTH);
      panel.add (scroll, BorderLayout.CENTER);
   }
   
   private void loadModel()
   {
      model = new MyTableModel();
      model.addColumn ("Raiding");
      model.addColumn ("Player");
      model.addColumn ("Order");
      model.addColumn ("Boots");
      model.addColumn ("Gloves");
      model.addColumn ("Pants");
      model.addColumn ("Chest");
      model.addColumn ("Helm");
      model.addColumn ("Shoulders");

      RiftPlayer.load();
      for (RiftPlayer player : RiftPlayer.PLAYERS.values())
      {
         Vector<Object> row = new Vector<Object>();
         row.add (Boolean.FALSE);
         row.add (player.getName());
         row.add (player.getOrder());
         //for (boolean gem : player.getGems())
         // row.add (gem);
         model.addRow (row);
      }
      
      view = new JXTable (model);
      view.getTableHeader().setReorderingAllowed (false);
      view.setColumnControlVisible (false);
      view.setPreferredScrollableViewportSize (new Dimension (400, 600));
      view.packAll();
   }
   
   private class ButtonListener implements ActionListener
   {
      public void actionPerformed (final ActionEvent e)
      {
         //RiftPlayer.publish();
      }
   }
   
   private static class MyTableModel extends DefaultTableModel
   {
      private static final long serialVersionUID = 1L;
      
      @Override
      public Class<?> getColumnClass (final int c)
      {
         return getValueAt (0, c).getClass();
      }
   }
   
   public static void main (final String[] args)
   {
      SK1 app = new SK1();
      ComponentTools.open (app.panel, "SK");
   }
}
