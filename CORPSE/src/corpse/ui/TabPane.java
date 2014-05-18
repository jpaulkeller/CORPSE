package corpse.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import utils.ImageTools;

public class TabPane extends JTabbedPane
{
   private static final long serialVersionUID = 1L;

   private static final ImageIcon CLOSE_ICON = ImageTools.getIcon("icons/16/gui/BoxX.gif");

   private Set<TabListener> listeners = new HashSet<TabListener>();

   public JLabel addToggleTab(final String title, final Icon icon, final Component body, final String tip)
   {
      addTab(title, icon, body, tip);

      int index = indexOfTab(title);
      JPanel header = new JPanel(new BorderLayout());
      header.setOpaque(false);

      JButton closeBtn = new JButton(CLOSE_ICON);
      closeBtn.setMargin(new Insets(0, 0, 0, 0));
      closeBtn.addActionListener(new CloseActionHandler(title));

      JLabel label = new JLabel(title, icon, JLabel.LEFT);
      header.add(label, BorderLayout.CENTER);
      header.add(closeBtn, BorderLayout.EAST);

      setTabComponentAt(index, header);

      return label;
   }

   public void addTabListener(final TabListener listener)
   {
      listeners.add(listener);
   }

   private void fireClosed(final String name)
   {
      for (TabListener listener : listeners)
         listener.tabClosed(name);
   }

   class CloseActionHandler implements ActionListener
   {
      private String tabName;

      public CloseActionHandler(final String tabName)
      {
         this.tabName = tabName;
      }

      public String getTabName()
      {
         return tabName;
      }

      @Override
      public void actionPerformed(final ActionEvent evt)
      {
         int index = TabPane.this.indexOfTab(getTabName());
         if (index >= 0)
         {
            TabPane.this.removeTabAt(index);
            JButton button = (JButton) evt.getSource();
            button.removeActionListener(this);
         }
         fireClosed(getTabName());
      }
   }
}
