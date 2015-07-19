package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.TitledBorder;

import str.Token;
import utils.ImageTools;

/**
 * This class provides static methods for creating and manipulating swing components.
 */
public final class ComponentTools
{
   private ComponentTools()
   { /* utility class */
   }

   public static JFrame open(final Component component, final String title)
   {
      return open(title, null, component, null, null, null);
   }

   public static JFrame open(final String title, final Component north, final Component center, final Component south)
   {
      return open(title, north, center, south, null, null);
   }

   public static JFrame open(final String title, final Component north, final Component center, final Component south,
            final Component west, final Component east)
   {
      JFrame frame = new JFrame(title);
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

      if (north != null)
         frame.add(north, BorderLayout.NORTH);
      if (center != null)
         frame.add(center, BorderLayout.CENTER);
      if (south != null)
         frame.add(south, BorderLayout.SOUTH);
      if (west != null)
         frame.add(west, BorderLayout.WEST);
      if (east != null)
         frame.add(east, BorderLayout.EAST);

      frame.pack();
      centerComponent(frame);
      frame.setVisible(true);

      return frame;
   }

   public static JFrame open(final JInternalFrame jif)
   {
      jif.setVisible(true);

      JDesktopPane desk = new JDesktopPane();
      desk.setPreferredSize(jif.getPreferredSize());
      desk.add(jif, JLayeredPane.DEFAULT_LAYER);

      return open(desk, jif.getTitle());
   }

   /**
    * Creates a JMenu with the given arguments.
    * 
    * @param label
    *           - menu text.
    * @param mnemonic
    *           - keyboard mnemonic character
    * @param iconFile
    *           - icon associated with the menu.
    */
   public static JMenu makeMenu(final String label, final char mnemonic, final String iconFile)
   {
      JMenu menu = new JMenu(label);
      ImageIcon icon = ImageTools.getIcon(iconFile);
      if (icon != null)
         menu.setIcon(icon);
      menu.setHorizontalTextPosition(SwingConstants.RIGHT);
      menu.setMnemonic(mnemonic);
      return menu;
   }

   /**
    * Create a JMenu (with icon, if provided) using the given arguments. For this version, the mnemonic will be the first
    * character of the label.
    * 
    * @param label
    *           - menu text.
    * @param iconFile
    *           - icon associated with the menu.
    */
   public static JMenu makeMenu(final String label, final String iconFile)
   {
      return makeMenu(label, label.charAt(0), iconFile);
   }

   /**
    * Create a JMenuItem (with icon, if provided) using the given arguments.
    * 
    * @param label
    *           - text associated with the menu item.
    * @param command
    *           - action to be performed when the menu item is selected.
    * @param mnemonic
    *           - keyboard character mnemonic.
    * @param iconFile
    *           - icon to be displayed in the menu item.
    * @param tipText
    *           - brief hint as to what the menu item does.
    * @param listener
    *           - action listener associated with the menu item.
    */
   public static JMenuItem makeMenuItem(final String label, final String command, final char mnemonic, final String iconFile,
            final String tipText, final ActionListener listener)
   {
      JMenuItem mi;

      ImageIcon icon = ImageTools.getIcon(iconFile);
      if (icon != null)
         mi = new JMenuItem(label, icon);
      else
         mi = new JMenuItem(label);
      mi.setHorizontalTextPosition(SwingConstants.RIGHT);
      if (mnemonic != ' ')
         mi.setMnemonic(mnemonic);
      mi.setActionCommand(command);
      mi.setToolTipText(tipText);
      mi.addActionListener(listener);
      return mi;
   }

   /**
    * Create a JMenuItem (with icon, if provided) using the given arguments. For this method, the actionCommand will be the same
    * as the label.
    * 
    * @param label
    *           - text associated with the menu item.
    * @param mnemonic
    *           - keyboard character mnemonic.
    * @param iconFile
    *           - icon to be displayed in the menu item.
    * @param tipText
    *           - brief hint as to what the menu item does.
    * @param listener
    *           - action listener associated with the menu item.
    */
   public static JMenuItem makeMenuItem(final String label, final char mnemonic, final String iconFile, final String tipText,
            final ActionListener listener)
   {
      return (makeMenuItem(label, label, mnemonic, iconFile, tipText, listener));
   }

   /**
    * Create a JMenuItem (with icon, if provided) using the given arguments. For this method, the actionCommand will be the same
    * as the label, and the mnemonic will be the first letter of the label.
    * 
    * @param label
    *           - text associated with the menu item.
    * @param iconFile
    *           - icon to be displayed in the menu item.
    * @param tipText
    *           - brief hint as to what the menu item does.
    * @param listener
    *           - action listener associated with the menu item.
    */
   public static JMenuItem makeMenuItem(final String label, final String iconFile, final String tipText,
            final ActionListener listener)
   {
      return (makeMenuItem(label, label, label.charAt(0), iconFile, tipText, listener));
   }

   /**
    * Create a JCheckBoxMenuItem (with icon, if provided) using the given arguments.
    * 
    * @param label
    *           - text displayed with the JCheckBoxMenuItem.
    * @param mnemonic
    *           - keyboard mnemonic character.
    * @param iconFile
    *           - icon displayed with the JCheckBoxMenuItem.
    * @param tipText
    *           - brief hint as to what the JCheckBoxMenuItem does.
    * @param state
    *           - initial state.
    * @param listener
    *           - action listener.
    */
   public static JCheckBoxMenuItem makeCheckItem(final String label, final char mnemonic, final String iconFile,
            final String tipText, final boolean state, final ActionListener listener)
   {
      JCheckBoxMenuItem mi;
      ImageIcon icon = ImageTools.getIcon(iconFile);
      if (icon != null)
         mi = new JCheckBoxMenuItem(label, icon, state);
      else
         mi = new JCheckBoxMenuItem(label, state);
      mi.setHorizontalTextPosition(SwingConstants.RIGHT);
      mi.setMnemonic(mnemonic);
      mi.setActionCommand(label);
      mi.setToolTipText(tipText);
      mi.addActionListener(listener);
      return mi;
   }

   /**
    * Create a JCheckBoxMenuItem (with icon, if provided) using the given arguments. For this version, the mnemonic will be the
    * first character of the label.
    * 
    * @param label
    *           - text displayed with the JCheckBoxMenuItem.
    * @param iconFile
    *           - icon displayed with the JCheckBoxMenuItem.
    * @param tipText
    *           - brief hint as to what the JCheckBoxMenuItem does.
    * @param state
    *           - initial state.
    * @param listener
    *           - action listener.
    */
   public static JCheckBoxMenuItem makeCheckItem(final String label, final String iconFile, final String tipText,
            final boolean state, final ActionListener listener)
   {
      return (makeCheckItem(label, label.charAt(0), iconFile, tipText, state, listener));
   }

   /**
    * Create a JRadioButtonMenuItem (with icon, if provided) using the given arguments.
    * 
    * @param group
    *           - button group this button gets added to.
    * @param label
    *           - text displayed with this JRadioButtonMenuItem.
    * @param mnemonic
    *           - keyboard mnemonic character
    * @param iconFile
    *           - icon displayed with this JRadioButtonMenuItem.
    * @param tipText
    *           - brief hint as to what this JRadioButtonMenuItem does.
    * @param state
    *           - initial state.
    * @param listener
    *           - action listener.
    */
   public static JRadioButtonMenuItem makeRadioItem(final ButtonGroup group, final String label, final char mnemonic,
            final String iconFile, final String tipText, final boolean state, final ActionListener listener)
   {
      JRadioButtonMenuItem mi;
      ImageIcon icon = ImageTools.getIcon(iconFile);
      if (icon != null)
         mi = new JRadioButtonMenuItem(label, icon, state);
      else
         mi = new JRadioButtonMenuItem(label, state);
      mi.setHorizontalTextPosition(SwingConstants.RIGHT);
      mi.setMnemonic(mnemonic);
      mi.setActionCommand(label);
      mi.setToolTipText(tipText);
      mi.addActionListener(listener);
      group.add(mi);
      return mi;
   }

   /**
    * Create a JRadioButtonMenuItem (with icon, if provided) using the given arguments. For this version, the mnemonic will be the
    * first character of the label.
    * 
    * @param group
    *           - button group this button gets added to.
    * @param label
    *           - text displayed with this JRadioButtonMenuItem.
    * @param iconFile
    *           - icon displayed with this JRadioButtonMenuItem.
    * @param tipText
    *           - brief hint as to what this JRadioButtonMenuItem does.
    * @param state
    *           - initial state.
    * @param listener
    *           - action listener.
    */
   public static JRadioButtonMenuItem makeRadioItem(final ButtonGroup group, final String label, final String iconFile,
            final String tipText, final boolean state, final ActionListener listener)
   {
      return (makeRadioItem(group, label, label.charAt(0), iconFile, tipText, state, listener));
   }

   /**
    * Create a JButton using the given icon, tooltip, and listener.
    * 
    * @param command
    *           - action command tied to this button.
    * @param iconFile
    *           - image icon to be displayed with this button
    * @param tipText
    *           - brief textual hint as to what this button does.
    * @param listener
    *           - action listener tied to this button.
    */
   public static JButton makeTool(final String command, final String iconFile, final String tipText, final ActionListener listener)
   {
      ImageIcon icon = ImageTools.getIcon(iconFile);
      JButton button = new JButton(icon);
      button.setMargin(new Insets(0, 0, 0, 0));
      button.setActionCommand(command);
      button.addActionListener(listener);
      button.setToolTipText(tipText);
      return button;
   }

   public static JButton makeButton(final String label, final String icon, final boolean enabled, final ActionListener listener,
            final String tip)
   {
      final JButton button = new JButton(label);
      if (icon != null)
         button.setIcon(ImageTools.getIcon(icon));
      button.setActionCommand(label);
      // button.setFocusable (false);
      button.setToolTipText(tip);
      button.setEnabled(enabled);
      button.addActionListener(listener);
      return button;
   }

   public static JButton makeButtonNarrow(final String label, final String icon, final boolean enabled,
            final ActionListener listener, final String tip)
   {
      JButton button = makeButton(label, icon, enabled, listener, tip);
      button.setHorizontalTextPosition(JButton.CENTER);
      button.setVerticalTextPosition(JButton.BOTTOM);
      return button;
   }

   /**
    * Create a JToggleButton using the given icon, tooltip, and listener.
    * 
    * @param command
    *           - action command tied to this button.
    * @param iconFile
    *           - image icon to be displayed with this button
    * @param tipText
    *           - brief textual hint as to what this button does.
    * @param state
    *           - initial selection state of this button.
    * @param listener
    *           - action listener tied to this button.
    */
   public static JToggleButton makeToggleTool(final String command, final String iconFile, final String tipText,
            final boolean state, final ActionListener listener)
   {
      ImageIcon icon = ImageTools.getIcon(iconFile);
      JToggleButton button = new JToggleButton(icon);
      // button.setMargin (new Insets (0, 0, 0, 0));

      button.setFocusable(false);
      button.setActionCommand(command);
      button.addActionListener(listener);
      button.setToolTipText(tipText);
      button.setSelected(state);

      return button;
   }

   /**
    * Centers the given component on the screen.
    * 
    * @param comp
    *           - component to be centered.
    */
   public static void centerComponent(final Component comp)
   {
      comp.setLocation(findCenter(comp));
   }

   private static Point findCenter(final Component comp)
   {
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      Dimension compSize = comp.getSize();
      int x = (screenSize.width - compSize.width) / 2;
      int y = (screenSize.height - compSize.height) / 2;
      return new Point(Math.max(x, 0), Math.max(y, 0));
   }

   /**
    * Returns the best upper-left point at which to display the popup component, near the given target component. If target is
    * null, Point(0, 0) is returned.
    * 
    * NOTE: This only works if target is currently displayed. Also, note that the point returned is in screen coordinates (which
    * works well with the PopupFactory, but not with the Component setLocation() method. You may want to use moveNear() instead.
    */
   public static Point findBestLocationFor(final Component target, final Component popup)
   {
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      Point point = new Point(0, 0);
      if (popup == null)
         return point;
      if (target == null)
         return findCenter(popup);

      Point pPoint = target.getLocationOnScreen();
      Dimension popupSize = popup.getMinimumSize();
      int width = popupSize.width;
      int height = popupSize.height;
      int x = 0;
      int y = 0;

      // determine how much space is available around the parent
      int topSpace = pPoint.y;
      int leftSpace = pPoint.x;
      int bottomSpace = screenSize.height - (pPoint.y + target.getHeight());
      int rightSpace = screenSize.width - (pPoint.x + target.getWidth());

      // it won't fit, return (0, 0)
      if (width > rightSpace && width > leftSpace && height > topSpace && height > bottomSpace)
         return point;

      // TBD: hack to pad for the Windows Start bar (40 is somewhat arbitrary)
      // int startBarHeight = Util.isWindowsPlatform() ? 40 : 0;
      int startBarHeight = 40;

      if (width < rightSpace) // try to the right
         x = pPoint.x + target.getWidth();
      else if (width < leftSpace) // try to the left
         x = pPoint.x - width;
      else if (pPoint.getX() + width < screenSize.width)
         x = pPoint.x;
      else if (screenSize.width > width)
         x = screenSize.width - width;

      if (height < topSpace) // try above
         y = pPoint.y - height;
      else if (height < bottomSpace - startBarHeight) // try below
         y = pPoint.y + target.getHeight();
      else if (pPoint.getY() + height < screenSize.height - startBarHeight)
         y = pPoint.y;
      else if (screenSize.height - startBarHeight > height)
         y = (screenSize.height - height) - startBarHeight;

      point.setLocation(x, y);

      return point;
   }

   /**
    * Positions the given popup component adjacent to the given target component, so that the popup will be fully on screen (if
    * possible). Note that the given target does not have to be the actual parent containing the popup.
    */
   public static void moveNear(final Component target, final Component popup)
   {
      Point popupPoint = findBestLocationFor(target, popup);
      Component parent = popup.getParent();
      if (parent != null)
      {
         Point parentPoint = parent.getLocationOnScreen();
         popupPoint.x -= parentPoint.x;
         popupPoint.y -= parentPoint.y;
      }
      popup.setLocation(popupPoint);
   }

   /**
    * Shrinks the given component if necessary.
    * 
    * @param comp
    *           - component to shrink.
    * @param maxWidth
    *           - maximum possible width of the component.
    * @param maxHeight
    *           - maximum possible height of the component.
    */
   public static void shrink(final Component comp, final int maxWidth, final int maxHeight)
   {
      Dimension dim = comp.getSize();
      comp.setSize(Math.min(dim.width, maxWidth), Math.min(dim.height, maxHeight));
   }

   /** Creates a panel containing of buttons with the given labels. */

   public static JPanel createButtonPanel(final ActionListener listener, final String[] buttonLabels)
   {
      JPanel panel = new JPanel();

      for (int i = 0; i < buttonLabels.length; i++)
      {
         JButton button = new JButton(buttonLabels[i]);
         button.setActionCommand(buttonLabels[i].toUpperCase());
         button.addActionListener(listener);
         panel.add(button);
      }

      return panel;
   }

   /** Creates a button panel with OK and Cancel buttons. */

   public static JPanel createOkCancel(final ActionListener listener)
   {
      return createButtonPanel(listener, new String[] { "OK", "Cancel" });
   }

   public static JPanel createOk(final ActionListener listener)
   {
      return createButtonPanel(listener, new String[] { "OK" });
   }

   public static Font getFontBold(final Component c)
   {
      Font f = c.getFont();
      return f.isBold() ? f : new Font(f.getName(), f.getStyle() + Font.BOLD, f.getSize());
   }

   public static Font getFontUnBold(final Component c)
   {
      Font f = c.getFont();
      return !f.isBold() ? f : new Font(f.getName(), f.getStyle() - Font.BOLD, f.getSize());
   }

   /** Convenience method for wrapping the component inside a frame. */

   public static JFrame getFrame(final Component component, final String title)
   {
      JFrame frame = new JFrame(title);
      frame.getContentPane().add(component);
      frame.pack();
      return frame;
   }

   /**
    * Convenience method for putting the component inside a panel with a titled border (using the given title).
    */
   public static JPanel getTitledPanel(final Component component, final String title)
   {
      TitledBorder border = new TitledBorder(title);
      JPanel panel = new JPanel(new BorderLayout());
      panel.setBorder(border);
      panel.add(component, BorderLayout.CENTER);
      return panel;
   }

   public static JPanel getLabeledPanel(final Component component, final String label)
   {
      JLabel lbl = new JLabel(label);
      JPanel panel = new JPanel(new BorderLayout());
      panel.add(lbl, BorderLayout.WEST);
      panel.add(component, BorderLayout.CENTER);
      return panel;
   }

   /** Finds the parent frame containing the component. */

   public static Container findFrame(final Component component)
   {
      Container container = null;

      if (component != null)
      {
         container = component.getParent();
         while (container != null)
         {
            if ((container instanceof Window) || (container instanceof JInternalFrame))
               return container;
            container = container.getParent();
         }
      }

      return container;
   }

   /**
    * Packs the given component's frame (or internal frame). Useful if the component changes size and does not have a reference to
    * the window it belongs to.
    */
   public static void packFrame(final Component component)
   {
      Container container = findFrame(component);
      if (container == null)
         return;
      if (container instanceof Window)
         ((Window) container).pack();
      else if (container instanceof JInternalFrame)
         ((JInternalFrame) container).pack();
   }

   /**
    * Closes the given component's frame (or internal frame), and disposes all of its resources.
    */
   public static void disposeFrame(final Component component)
   {
      Container container = findFrame(component);
      if (container instanceof Window)
         ((Window) container).dispose();
      else if (container instanceof JInternalFrame)
      {
         JInternalFrame frame = (JInternalFrame) container;
         try
         {
            frame.setSelected(false);
            frame.setClosed(true);
            frame.setVisible(false);
            frame.dispose();
         }
         catch (PropertyVetoException x)
         {
         }
      }
   }

   /** Hides the given component's frame (or internal frame). */

   public static void hideFrame(final Component component)
   {
      Container container = findFrame(component);
      if (container instanceof Window)
         ((Window) container).setVisible(false);
      else if (container instanceof JInternalFrame)
      {
         JInternalFrame frame = (JInternalFrame) container;
         try
         {
            frame.setSelected(false);
            frame.setClosed(true);
            frame.setVisible(false);
         }
         catch (PropertyVetoException x)
         {
         }
      }
   }

   /**
    * Sets the title of the given component's frame (or internal frame).
    */
   public static void setFrameTitle(final Component component, final String title)
   {
      Container container = findFrame(component);
      if (container == null)
         return;
      if (container instanceof Frame)
         ((Frame) container).setTitle(title);
      else if (container instanceof JInternalFrame)
         ((JInternalFrame) container).setTitle(title);
   }

   public static void showProperties(final Component owner)
   {
      StringBuilder sb = new StringBuilder();
      appendProperty(sb, "java.version");
      appendProperty(sb, "java.vendor");
      appendProperty(sb, "java.home");
      // appendProperty (sb, "java.vm.specification.version");
      // appendProperty (sb, "java.vm.version");
      // appendProperty (sb, "java.specification.version");
      // appendProperty (sb, "java.class.version");
      appendProperty(sb, "java.ext.dirs");
      appendProperty(sb, "os.name");
      appendProperty(sb, "os.arch");
      appendProperty(sb, "os.version");
      appendProperty(sb, "user.name");
      appendProperty(sb, "user.home");
      appendProperty(sb, "user.dir");

      sb.append("defaultCharset = " + Charset.defaultCharset().name() + "\n");

      appendPropertyList(sb, "java.class.path");
      appendPropertyList(sb, "java.library.path");
      // appendPropertyList (sb, "java.io.tmpdir");

      JTextArea text = new JTextArea(sb.toString(), 40, 40);
      JScrollPane scroll = new JScrollPane(text);
      JOptionPane.showMessageDialog(owner, scroll, "System Properties", JOptionPane.INFORMATION_MESSAGE);
   }

   private static void appendProperty(final StringBuilder sb, final String property)
   {
      sb.append(property);
      sb.append(" = ");
      sb.append(System.getProperty(property));
      sb.append("\n");
   }

   private static void appendPropertyList(final StringBuilder sb, final String property)
   {
      sb.append(property);
      sb.append("\n");
      for (String token : Token.tokenize(System.getProperty(property), ";"))
         sb.append("     > " + token + "\n");
   }

   public static void setDefaults()
   {
      setLookAndFeel();

      UIManager.put("ToolTip.background", new Color(255, 255, 175)); // light yellow
      UIManager.put("ToolTip.foreground", Color.black);
      UIManager.put("ToolTip.backgroundInactive", new Color(255, 255, 200));
      UIManager.put("ToolTip.foregroundInactive", Color.gray);

      // leave tool tips up for 10 seconds (default is 4)
      ToolTipManager.sharedInstance().setDismissDelay(10000);
   }

   public static void setLookAndFeel()
   {
      try
      {
         String plaf = UIManager.getSystemLookAndFeelClassName();
         UIManager.setLookAndFeel(plaf);
      }
      catch (Throwable x)
      {
      }
   }

   public static void setLookAndFeelNimbus()
   {
      try
      {
         // save the default progress bar settings
         HashMap<Object, Object> progressDefaults = new HashMap<>();
         for (Map.Entry<Object, Object> entry : UIManager.getDefaults().entrySet())
            if (entry.getKey().getClass() == String.class && ((String) entry.getKey()).startsWith("ProgressBar"))
               progressDefaults.put(entry.getKey(), entry.getValue());

         for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels())
            if ("Nimbus".equals(info.getName()))
            {
               UIManager.setLookAndFeel(info.getClassName());
               break;
            }

         // apply the default progress bar settings (Nimbus ones are ugly)
         for (Map.Entry<Object, Object> entry : progressDefaults.entrySet())
            UIManager.getDefaults().put(entry.getKey(), entry.getValue());
      }
      catch (Throwable x)
      {
      }
   }

   public static void main(final String[] args)
   {
      ComponentTools.showProperties(null);
   }
}
