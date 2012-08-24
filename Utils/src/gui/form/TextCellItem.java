package gui.form;

import gui.ComponentTools;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;

import utils.ImageTools;

/**
 * TextCellItem objects provide a pop-up interface for editing text in small
 * GUIs (such as table cells).
 */
public class TextCellItem extends TextAreaItem
{
   private static final long serialVersionUID = 0;

   private JDialog dialog;
   private TextItem embed;
   private JPanel panel;
   private JButton button;

   public TextCellItem (final JComponent parent, 
                        final String label, final String text,
                        final int rows, final int columns)
   {
      super (label, text, rows, columns);

      embed = new TextItem (label, columns, text);
      embed.setEditable (false);

      ActionListener listener = new ButtonListener ();

      Icon icon = ImageTools.getIcon ("icons/20/documents/DocumentEdit.gif");
      button = new JButton (icon);
      button.setActionCommand ("Popup");
      button.setFocusable (false);
      button.setPreferredSize (new Dimension (24, 24));
      button.addActionListener (listener);

      panel = new JPanel (new BorderLayout ());
      panel.add (button, BorderLayout.EAST);
      panel.add (embed.getComponent(), BorderLayout.CENTER);

      Window window = parent != null ? (Window) parent.getTopLevelAncestor() : null;
      dialog = new JDialog (window, getLabel(), Dialog.ModalityType.APPLICATION_MODAL);
      dialog.add (super.getComponent(), BorderLayout.CENTER);
      dialog.add (ComponentTools.createOkCancel (listener), BorderLayout.SOUTH);
      dialog.pack ();
   }

   @Override
   public JComponent getComponent()
   {
      return panel;
   }

   public void setIcon (final String name)
   {
      setIcon (ImageTools.getIcon (name));
   }

   public void setIcon (final Icon icon)
   {
      button.setIcon (icon);
   }

   @Override
   public void setValue (final Object value)
   {
      super.setValue (value);
      if (embed != null)
         embed.setValue (value);
   }

   class ButtonListener implements ActionListener
   {
      public void actionPerformed (final ActionEvent e)
      {
         String cmd = e.getActionCommand ();
         if (cmd.equals ("Popup"))
            popupEditor ();
         else // OK or CANCEL
         {
            if (cmd.equals ("OK"))
               embed.setValue (TextCellItem.super.getValue().toString());
            dialog.setVisible (false);
         }
      }

      public void popupEditor()
      {
         ComponentTools.moveNear (getComponent(), dialog);
         dialog.setVisible (true);
      }
   }
   
   @Override
   public void addValueChangeListener (final ValueChangeListener listener)
   {
      super.addValueChangeListener (listener);
      embed.addValueChangeListener (listener);
   }

   public static void main (final String[] args)
   {
      ComponentTools.setDefaults ();
      TextCellItem item = new TextCellItem (null, "TextCellItem Test", "some text", 5, 40);
      item.test();
   }
}
