package gui.comp;

import java.awt.AWTEvent;
import java.awt.AWTException;
import java.awt.AWTKeyStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FocusTraversalPolicy;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.ImageCapabilities;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.MenuComponent;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.dnd.DropTarget;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerListener;
import java.awt.event.FocusListener;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyListener;
import java.awt.event.InputMethodListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;
import java.awt.im.InputContext;
import java.awt.im.InputMethodRequests;
import java.awt.image.BufferStrategy;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.awt.image.VolatileImage;
import java.beans.PropertyChangeListener;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.EventListener;
import java.util.Locale;
import java.util.Set;

import javax.accessibility.AccessibleContext;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JMenuBar;
import javax.swing.JRootPane;

import utils.Utils;

/**
 * Delegates to a JDialog (or JFrame in some cases) for all methods.
 * The constructors will accept any Component as the owner (and look
 * up the top level ancestor), instead of requiring a Dialog or
 * Frame. This class overrides all non-deprecated public methods in
 * the JDialog and JFrame hierarchies (even "final" ones). Final and
 * Synchronized methods are not declared as such here.
 *
 * Since calling the setAlwaysOnTop() method has no effect (for
 * JDailog objects on Windows at least), dialogs without owners will
 * be converted into JFrame objects.  This will ensure that they
 * appear on the Windows start bar (even if they become buried).  This
 * requires a hack to the setVisible() method, to simulate modal
 * frames. */

public class SimpleDialog
{
   private Window window;
   private boolean modal;

   public SimpleDialog()
   {
      this (null, null, false);
   }

   public SimpleDialog (final Component owner)
   {
      this (owner, null, false);
   }

   public SimpleDialog (final Component owner, final boolean modal)
   {
      this (owner, null, modal);
   }

   public SimpleDialog (final Component owner, final String title)
   {
      this (owner, title, false);
   }

   public SimpleDialog (final Component comp, final String title, final boolean modal)
   {
      Component owner = comp;
      
      if (comp instanceof Frame)
         owner = comp;
      else if (comp instanceof Dialog)
         owner = comp;
      else if (comp instanceof JComponent)
         owner = ((JComponent) comp).getTopLevelAncestor();
      else if (comp != null)
      {
         Component parent;
         while ((parent = owner.getParent()) != null)
            owner = parent;
      }

      if (owner instanceof Frame)
         window = new JDialog ((Frame) owner, title, modal);
      else if (owner instanceof Dialog)
         window = new JDialog ((Dialog) owner, title, modal);
      else
      {
         System.out.println
            ("Warning -- converting unowned dialog to JFrame: " + title);
         System.out.println (getClass().getName());
         if (owner != null)
            System.out.println ("   Owner: " + owner.getClass().getName());
         window = new JFrame (title);
         this.modal = modal;
      }
   }

   public Window getWindow()
   {
      return window;
   }

   // delegate for all public methods in javax.swing.JDialog

   public AccessibleContext getAccessibleContext()
   {
      if (window instanceof JDialog)
         return ((JDialog) window).getAccessibleContext();
      return ((JFrame) window).getAccessibleContext();
   }
   public Container getContentPane()
   {
      if (window instanceof JDialog)
         return ((JDialog) window).getContentPane();
      return ((JFrame) window).getContentPane();
   }
   public int getDefaultCloseOperation()
   {
      if (window instanceof JDialog)
         return ((JDialog) window).getDefaultCloseOperation();
      return ((JFrame) window).getDefaultCloseOperation();
   }
   public Component getGlassPane()
   {
      if (window instanceof JDialog)
         return ((JDialog) window).getGlassPane();
      return ((JFrame) window).getGlassPane();
   }
   public JMenuBar getJMenuBar()
   {
      if (window instanceof JDialog)
         return ((JDialog) window).getJMenuBar();
      return ((JFrame) window).getJMenuBar();
   }
   public JLayeredPane getLayeredPane()
   {
      if (window instanceof JDialog)
         return ((JDialog) window).getLayeredPane();
      return ((JFrame) window).getLayeredPane();
   }
   public JRootPane getRootPane()
   {
      if (window instanceof JDialog)
         return ((JDialog) window).getRootPane();
      return ((JFrame) window).getRootPane();
   }
   public boolean isDefaultLookAndFeelDecorated()
   {
      if (window instanceof JDialog)
         return JDialog.isDefaultLookAndFeelDecorated();
      return JFrame.isDefaultLookAndFeelDecorated();
   }
   public void remove (final Component comp)
   {
      if (window instanceof JDialog)
         ((JDialog) window).remove (comp);
      else
         ((JFrame) window).remove (comp);
   }
   public void setContentPane (final Container contentPane)
   {
      if (window instanceof JDialog)
         ((JDialog) window).setContentPane (contentPane);
      else
         ((JFrame) window).setContentPane (contentPane);
   }
   public void setDefaultCloseOperation (final int operation)
   {
      if (window instanceof JDialog)
         ((JDialog) window).setDefaultCloseOperation (operation);
      else
         ((JFrame) window).setDefaultCloseOperation (operation);
   }
   public void setDefaultLookAndFeelDecorated (final boolean laf)
   {
      if (window instanceof JDialog)
         JDialog.setDefaultLookAndFeelDecorated (laf);
      else
         JFrame.setDefaultLookAndFeelDecorated (laf);
   }
   public void setGlassPane (final Component glassPane)
   {
      if (window instanceof JDialog)
         ((JDialog) window).setGlassPane (glassPane);
      else
         ((JFrame) window).setGlassPane (glassPane);
   }
   public void setJMenuBar (final JMenuBar menu)
   {
      if (window instanceof JDialog)
         ((JDialog) window).setJMenuBar (menu);
      else
         ((JFrame) window).setJMenuBar (menu);
   }
   public void setLayeredPane (final JLayeredPane layeredPane)
   {
      if (window instanceof JDialog)
         ((JDialog) window).setLayeredPane (layeredPane);
      else
         ((JFrame) window).setLayeredPane (layeredPane);
   }
   public void setLayout (final LayoutManager manager)
   {
      if (window instanceof JDialog)
         ((JDialog) window).setLayout (manager);
      else
         ((JFrame) window).setLayout (manager);
   }
   public void update (final Graphics g)
   {
      if (window instanceof JDialog)
         ((JDialog) window).update (g);
      else
         ((JFrame) window).update (g);
   }

   // Delegate for all public methods in java.awt.Dialog.  Note that
   // some methods are not supported by frames.

   public void addNotify()
   {
      if (window instanceof JDialog)
         ((JDialog) window).addNotify();
      else
         ((JFrame) window).addNotify();
   }
   public String getTitle()
   {
      if (window instanceof JDialog)
         return ((JDialog) window).getTitle();
      return ((JFrame) window).getTitle();
   }
   public boolean isModal()
   {
      if (window instanceof JDialog)
         return ((JDialog) window).isModal();
      return modal;
   }
   public boolean isResizable()
   {
      if (window instanceof JDialog)
         return ((JDialog) window).isResizable();
      return ((JFrame) window).isResizable();
   }
   public boolean isUndecorated()
   {
      if (window instanceof JDialog)
         return ((JDialog) window).isUndecorated();
      return ((JFrame) window).isUndecorated();
   }
   public void setModal (final boolean b)
   {
      if (window instanceof JDialog)
         ((JDialog) window).setModal (b);
      this.modal = b;
   }
   public void setResizable (final boolean resizable)
   {
      if (window instanceof JDialog)
         ((JDialog) window).setResizable (resizable);
      else
         ((JFrame) window).setResizable (resizable);
   }
   public void setTitle (final String title)
   {
      if (window instanceof JDialog)
         ((JDialog) window).setTitle (title);
      else
         ((JFrame) window).setTitle (title);
   }
   public void setUndecorated (final boolean undecorated)
   {
      if (window instanceof JDialog)
         ((JDialog) window).setUndecorated (undecorated);
      else
         ((JFrame) window).setUndecorated (undecorated);
   }

   // delegate for all public java.awt.Window methods

   public void addPropertyChangeListener (final PropertyChangeListener listener)
   {
      window.addPropertyChangeListener (listener);
   }
   public void addPropertyChangeListener (final String propertyName,
                                          final PropertyChangeListener listener)
   {
      window.addPropertyChangeListener (propertyName, listener);
   }
   public void addWindowFocusListener (final WindowFocusListener l)
   {
      window.addWindowFocusListener (l);
   }
   public void addWindowListener (final WindowListener l)
   {
      window.addWindowListener (l);
   }
   public void addWindowStateListener (final WindowStateListener l)
   {
      window.addWindowStateListener (l);
   }
   public void createBufferStrategy (final int numBuffers)
   {
      window.createBufferStrategy (numBuffers);
   }
   public void dispose()
   {
      window.dispose();
   }
   public BufferStrategy getBufferStrategy()
   {
      return window.getBufferStrategy();
   }
   public boolean getFocusableWindowState()
   {
      return window.getFocusableWindowState();
   }
   public Container getFocusCycleRootAncestor() // final
   {
      return window.getFocusCycleRootAncestor();
   }
   public Component getFocusOwner()
   {
      return window.getFocusOwner();
   }
   public Set<AWTKeyStroke> getFocusTraversalKeys (final int id)
   {
      return window.getFocusTraversalKeys (id);
   }
   public GraphicsConfiguration getGraphicsConfiguration()
   {
      return window.getGraphicsConfiguration();
   }
   public InputContext getInputContext()
   {
      return window.getInputContext();
   }
   public <T extends EventListener> T[] getListeners (final Class<T> listenerType)
   {
      return window.getListeners (listenerType);
   }
   public Locale getLocale()
   {
      return window.getLocale();
   }
   public Component getMostRecentFocusOwner()
   {
      return window.getMostRecentFocusOwner();
   }
   public Window[] getOwnedWindows()
   {
      return window.getOwnedWindows();
   }
   public Window getOwner()
   {
      return window.getOwner();
   }
   public Toolkit getToolkit()
   {
      return window.getToolkit();
   }
   public String getWarningString() // final
   {
      return window.getWarningString();
   }
   public WindowFocusListener[] getWindowFocusListeners()
   {
      return window.getWindowFocusListeners();
   }
   public WindowListener[] getWindowListeners()
   {
      return window.getWindowListeners();
   }
   public WindowStateListener[] getWindowStateListeners()
   {
      return window.getWindowStateListeners();
   }
   public boolean isActive()
   {
      return window.isActive();
   }
   public boolean isAlwaysOnTop() // final
   {
      return window.isAlwaysOnTop();
   }
   public boolean isFocusableWindow() // final
   {
      return window.isFocusableWindow();
   }
   public boolean isFocusCycleRoot() // final
   {
      return window.isFocusCycleRoot();
   }
   public boolean isFocused()
   {
      return window.isFocused();
   }
   public boolean isLocationByPlatform()
   {
      return window.isLocationByPlatform();
   }
   public boolean isShowing()
   {
      return window.isShowing();
   }
   public void pack()
   {
      window.pack();
   }
   public void removeWindowFocusListener (final WindowFocusListener l)
   {
      window.removeWindowFocusListener (l);
   }
   public void removeWindowListener (final WindowListener l)
   {
      window.removeWindowListener (l);
   }
   public void removeWindowStateListener (final WindowStateListener l)
   {
      window.removeWindowStateListener (l);
   }
   public void setAlwaysOnTop (final boolean b)
   {
      window.setAlwaysOnTop (b);
   }
   public void setBounds (final int x, final int y, final int width, final int height)
   {
      window.setBounds (x, y, width, height);
   }
   public void setCursor (final Cursor cursor)
   {
      window.setCursor (cursor);
   }
   public void setFocusableWindowState (final boolean focusableWindowState)
   {
      window.setFocusableWindowState (focusableWindowState);
   }
   public void setFocusCycleRoot (final boolean focusCycleRoot) // final
   {
      window.setFocusCycleRoot (focusCycleRoot);
   }
   public void setLocationByPlatform (final boolean locationByPlatform)
   {
      window.setLocationByPlatform (locationByPlatform);
   }
   public void setLocationRelativeTo (final Component c)
   {
      window.setLocationRelativeTo (c);
   }
   public void toBack()
   {
      window.toBack();
   }
   public void toFront()
   {
      window.toFront();
   }

   // delegate for all public java.awt.Container methods

   public Component add (final Component comp)
   {
      return window.add (comp);
   }
   public Component add (final Component comp, final int index)
   {
      return window.add (comp, index);
   }
   public void add (final Component comp, final Object constraints)
   {
      window.add (comp, constraints);
   }
   public void add (final Component comp, final Object constraints, final int index)
   {
      window.add (comp, constraints, index);
   }
   public Component add (final String name, final Component comp)
   {
      return window.add (name, comp);
   }
   public void addContainerListener (final ContainerListener l)
   {
      window.addContainerListener (l);
   }
   public void applyComponentOrientation (final ComponentOrientation o)
   {
      window.applyComponentOrientation (o);
   }
   public boolean areFocusTraversalKeysSet (final int id)
   {
      return window.areFocusTraversalKeysSet (id);
   }
   public void doLayout()
   {
      window.doLayout();
   }
   public Component findComponentAt (final int x, final int y)
   {
      return window.findComponentAt (x, y);
   }
   public Component findComponentAt (final Point p)
   {
      return window.findComponentAt (p);
   }
   public float getAlignmentX()
   {
      return window.getAlignmentX();
   }
   public float getAlignmentY()
   {
      return window.getAlignmentY();
   }
   public Component getComponent (final int n)
   {
      return window.getComponent (n);
   }
   public Component getComponentAt (final int x, final int y)
   {
      return window.getComponentAt (x, y);
   }
   public Component getComponentAt (final Point p)
   {
      return window.getComponentAt (p);
   }
   public int getComponentCount()
   {
      return window.getComponentCount();
   }
   public Component[] getComponents()
   {
      return window.getComponents();
   }
   public int getComponentZOrder (final Component comp) // final
   {
      return window.getComponentZOrder (comp);
   }
   public ContainerListener[] getContainerListeners()
   {
      return window.getContainerListeners();
   }
   public FocusTraversalPolicy getFocusTraversalPolicy()
   {
      return window.getFocusTraversalPolicy();
   }
   public Insets getInsets()
   {
      return window.getInsets();
   }
   public LayoutManager getLayout()
   {
      return window.getLayout();
   }
   public Dimension getMaximumSize()
   {
      return window.getMaximumSize();
   }
   public Dimension getMinimumSize()
   {
      return window.getMinimumSize();
   }
   public Point getMousePosition (final boolean allowChildren)
      throws HeadlessException
   {
      return window.getMousePosition (allowChildren);
   }
   public Dimension getPreferredSize()
   {
      return window.getPreferredSize();
   }
   public void invalidate()
   {
      window.invalidate();
   }
   public boolean isAncestorOf (final Component c)
   {
      return window.isAncestorOf (c);
   }
   public boolean isFocusCycleRoot (final Container container)
   {
      return window.isFocusCycleRoot (container);
   }
   public boolean isFocusTraversalPolicyProvider() // final
   {
      return window.isFocusTraversalPolicyProvider();
   }
   public boolean isFocusTraversalPolicySet()
   {
      return window.isFocusTraversalPolicySet();
   }
   public void list (final PrintStream out, final int indent)
   {
      window.list (out, indent);
   }
   public void list (final PrintWriter out, final int indent)
   {
      window.list (out, indent);
   }
   public void paint (final Graphics g)
   {
      window.paint (g);
   }
   public void paintComponents (final Graphics g)
   {
      window.paintComponents (g);
   }
   public void print (final Graphics g)
   {
      window.print (g);
   }
   public void printComponents (final Graphics g)
   {
      window.printComponents (g);
   }
   public void remove (final int index)
   {
      window.remove (index);
   }
   public void removeAll()
   {
      window.removeAll();
   }
   public void removeContainerListener (final ContainerListener l)
   {
      window.removeContainerListener (l);
   }
   public void removeNotify()
   {
      window.removeNotify();
   }
   public void setComponentZOrder (final Component comp, final int index) // final
   {
      window.setComponentZOrder (comp, index);
   }
   public void setFocusTraversalKeys (final int id,
                                      final Set<? extends AWTKeyStroke> keystrokes)
   {
      window.setFocusTraversalKeys (id, keystrokes);
   }
   public void setFocusTraversalPolicy (final FocusTraversalPolicy policy)
   {
      window.setFocusTraversalPolicy (policy);
   }
   public void setFocusTraversalPolicyProvider (final boolean provider) // final
   {
      window.setFocusTraversalPolicyProvider (provider);
   }
   public void setFont (final Font f)
   {
      window.setFont (f);
   }
   public void transferFocusBackward()
   {
      window.transferFocusBackward();
   }
   public void transferFocusDownCycle()
   {
      window.transferFocusDownCycle();
   }
   public void validate()
   {
      window.validate();
   }

   // delegate for all public java.awt.Component methods

   public void add (final PopupMenu popup)
   {
      window.add (popup);
   }
   public void addComponentListener (final ComponentListener l)
   {
      window.addComponentListener (l);
   }
   public void addFocusListener (final FocusListener l)
   {
      window.addFocusListener (l);
   }
   public void addHierarchyBoundsListener (final HierarchyBoundsListener l)
   {
      window.addHierarchyBoundsListener (l);
   }
   public void addHierarchyListener (final HierarchyListener l)
   {
      window.addHierarchyListener (l);
   }
   public void addInputMethodListener (final InputMethodListener l)
   {
      window.addInputMethodListener (l);
   }
   public void addKeyListener (final KeyListener l)
   {
      window.addKeyListener (l);
   }
   public void addMouseListener (final MouseListener l)
   {
      window.addMouseListener (l);
   }
   public void addMouseMotionListener (final MouseMotionListener l)
   {
      window.addMouseMotionListener (l);
   }
   public void addMouseWheelListener (final MouseWheelListener l)
   {
      window.addMouseWheelListener (l);
   }
   public int checkImage (final Image image, final ImageObserver observer)
   {
      return window.checkImage (image, observer);
   }
   public int checkImage (final Image image, final int w, final int h, 
                          final ImageObserver observer)
   {
      return window.checkImage (image, w, h, observer);
   }
   public boolean contains (final int x, final int y)
   {
      return window.contains (x, y);
   }
   public boolean contains (final Point p)
   {
      return window.contains (p);
   }
   public Image createImage (final ImageProducer producer)
   {
      return window.createImage (producer);
   }
   public Image createImage (final int width, final int height)
   {
      return window.createImage (width, height);
   }
   public VolatileImage createVolatileImage (final int width, final int height)
   {
      return window.createVolatileImage (width, height);
   }
   public VolatileImage createVolatileImage (final int width, final int height,
                                             final ImageCapabilities caps)
      throws AWTException
   {
      return window.createVolatileImage (width, height, caps);
   }
   public void dispatchEvent (final AWTEvent e) // final
   {
      window.dispatchEvent (e);
   }
   public void enableInputMethods (final boolean enable)
   {
      window.enableInputMethods (enable);
   }
   public void firePropertyChange (final String name, final byte oldValue, final byte newValue)
   {
      window.firePropertyChange (name, oldValue, newValue);
   }
   public void firePropertyChange (final String name, final char oldValue, final char newValue)
   {
      window.firePropertyChange (name, oldValue, newValue);
   }
   public void firePropertyChange (final String name, final short oldValue, final short newValue)
   {
      window.firePropertyChange (name, oldValue, newValue);
   }
   public void firePropertyChange (final String name, final long oldValue, final long newValue)
   {
      window.firePropertyChange (name, oldValue, newValue);
   }
   public void firePropertyChange (final String name, final float oldValue, final float newValue)
   {
      window.firePropertyChange (name, oldValue, newValue);
   }
   public void firePropertyChange (final String name, final double oldValue, final double newValue)
   {
      window.firePropertyChange (name, oldValue, newValue);
   }
   public Color getBackground()
   {
      return window.getBackground();
   }
   public Rectangle getBounds()
   {
      return window.getBounds();
   }
   public ColorModel getColorModel()
   {
      return window.getColorModel();
   }
   public ComponentListener[] getComponentListeners()
   {
      return window.getComponentListeners();
   }
   public ComponentOrientation getComponentOrientation()
   {
      return window.getComponentOrientation();
   }
   public Cursor getCursor()
   {
      return window.getCursor();
   }
   public DropTarget getDropTarget()
   {
      return window.getDropTarget();
   }
   public FocusListener[] getFocusListeners()
   {
      return window.getFocusListeners();
   }
   public Font getFont()
   {
      return window.getFont();
   }
   public FontMetrics getFontMetrics (final Font font)
   {
      return window.getFontMetrics (font);
   }
   public Color getForeground()
   {
      return window.getForeground();
   }
   public Graphics getGraphics()
   {
      return window.getGraphics();
   }
   public int getHeight()
   {
      return window.getHeight();
   }
   public HierarchyBoundsListener[] getHierarchyBoundsListeners()
   {
      return window.getHierarchyBoundsListeners();
   }
   public HierarchyListener[] getHierarchyListeners()
   {
      return window.getHierarchyListeners();
   }
   public boolean getIgnoreRepaint()
   {
      return window.getIgnoreRepaint();
   }
   public InputMethodListener[] getInputMethodListeners()
   {
      return window.getInputMethodListeners();
   }
   public InputMethodRequests getInputMethodRequests()
   {
      return window.getInputMethodRequests();
   }
   public KeyListener[] getKeyListeners()
   {
      return window.getKeyListeners();
   }
   public Point getLocation()
   {
      return window.getLocation();
   }
   public Point getLocationOnScreen()
   {
      return window.getLocationOnScreen();
   }
   public MouseListener[] getMouseListeners()
   {
      return window.getMouseListeners();
   }
   public MouseMotionListener[] getMouseMotionListeners()
   {
      return window.getMouseMotionListeners();
   }
   public Point getMousePosition() throws HeadlessException
   {
      return window.getMousePosition();
   }
   public MouseWheelListener[] getMouseWheelListeners()
   {
      return window.getMouseWheelListeners();
   }
   public String getName()
   {
      return window.getName();
   }
   public Container getParent()
   {
      return window.getParent();
   }
   public PropertyChangeListener[] getPropertyChangeListeners()
   {
      return window.getPropertyChangeListeners();
   }
   public PropertyChangeListener[] getPropertyChangeListeners (final String property)
   {
      return window.getPropertyChangeListeners (property);
   }
   public Dimension getSize()
   {
      return window.getSize();
   }
   public Dimension getSize (final Dimension rv)
   {
      return window.getSize (rv);
   }
   public Object getTreeLock()  // final
   {
      return window.getTreeLock();
   }
   public int getWidth()
   {
      return window.getWidth();
   }
   public int getX()
   {
      return window.getX();
   }
   public int getY()
   {
      return window.getY();
   }
   public boolean hasFocus()
   {
      return window.hasFocus();
   }
   public boolean imageUpdate (final Image img, final int infoFlags,
                               final int x, final int y, final int w, final int h)
   {
      return window.imageUpdate (img, infoFlags, x, y, w, h);
   }
   public boolean isBackgroundSet()
   {
      return window.isBackgroundSet();
   }
   public boolean isCursorSet()
   {
      return window.isCursorSet();
   }
   public boolean isDisplayable()
   {
      return window.isDisplayable();
   }
   public boolean isDoubleBuffered()
   {
      return window.isDoubleBuffered();
   }
   public boolean isEnabled()
   {
      return window.isEnabled();
   }
   public boolean isFocusable()
   {
      return window.isFocusable();
   }
   public boolean isFocusOwner()
   {
      return window.isFocusOwner();
   }
   public boolean isFontSet()
   {
      return window.isFontSet();
   }
   public boolean isForegroundSet()
   {
      return window.isForegroundSet();
   }
   public boolean isLightweight()
   {
      return window.isLightweight();
   }
   public boolean isMaximumSizeSet()
   {
      return window.isMaximumSizeSet();
   }
   public boolean isMinimumSizeSet()
   {
      return window.isMinimumSizeSet();
   }
   public boolean isOpaque()
   {
      return window.isOpaque();
   }
   public boolean isPreferredSizeSet()
   {
      return window.isPreferredSizeSet();
   }
   public boolean isValid()
   {
      return window.isValid();
   }
   public boolean isVisible()
   {
      return window.isVisible();
   }
   public void list()
   {
      window.list();
   }
   public void list (final PrintStream out)
   {
      window.list (out);
   }
   public void list (final PrintWriter out)
   {
      window.list (out);
   }
   public void paintAll (final Graphics g)
   {
      window.paintAll (g);
   }
   public boolean prepareImage (final Image image, final ImageObserver observer)
   {
      return window.prepareImage (image, observer);
   }
   public boolean prepareImage (final Image image, final int w, final int h, 
                                final ImageObserver obs)
   {
      return window.prepareImage (image, w, h, obs);
   }
   public void printAll (final Graphics g)
   {
      window.printAll (g);
   }
   public void remove (final MenuComponent popup)
   {
      window.remove (popup);
   }
   public void removeComponentListener (final ComponentListener l)
   {
      window.removeComponentListener (l);
   }
   public void removeFocusListener (final FocusListener l)
   {
      window.removeFocusListener (l);
   }
   public void removeHierarchyBoundsListener (final HierarchyBoundsListener l)
   {
      window.removeHierarchyBoundsListener (l);
   }
   public void removeHierarchyListener (final HierarchyListener l)
   {
      window.removeHierarchyListener (l);
   }
   public void removeInputMethodListener (final InputMethodListener l)
   {
      window.removeInputMethodListener (l);
   }
   public void removeKeyListener (final KeyListener l)
   {
      window.removeKeyListener (l);
   }
   public void removeMouseListener (final MouseListener l)
   {
      window.removeMouseListener (l);
   }
   public void removeMouseMotionListener (final MouseMotionListener l)
   {
      window.removeMouseMotionListener (l);
   }
   public void removeMouseWheelListener (final MouseWheelListener l)
   {
      window.removeMouseWheelListener (l);
   }
   public void removePropertyChangeListener (final PropertyChangeListener l)
   {
      window.removePropertyChangeListener (l);
   }
   public void removePropertyChangeListener (final String propertyName,
                                             final PropertyChangeListener listener)
   {
      window.removePropertyChangeListener (propertyName, listener);
   }
   public void repaint()
   {
      window.repaint();
   }
   public void repaint (final int x, final int y, final int width, final int height)
   {
      window.repaint (x, y, width, height);
   }
   public void repaint (final long tm, final int x, final int y, 
                        final int width, final int height)
   {
      window.repaint (tm, x, y, width, height);
   }
   public boolean requestFocusInWindow()
   {
      return window.requestFocusInWindow();
   }
   public void setBackground (final Color c)
   {
      window.setBackground (c);
   }
   public void setBounds (final Rectangle r)
   {
      window.setBounds (r);
   }
   public void setComponentOrientation (final ComponentOrientation o)
   {
      window.setComponentOrientation (o);
   }
   public void setDropTarget (final DropTarget dt)
   {
      window.setDropTarget (dt);
   }
   public void setEnabled (final boolean b)
   {
      window.setEnabled (b);
   }
   public void setFocusable (final boolean focusable)
   {
      window.setFocusable (focusable);
   }
   public void setFocusTraversalKeysEnabled (final boolean focusTraversalKeysEnabled)
   {
      window.setFocusTraversalKeysEnabled (focusTraversalKeysEnabled);
   }
   public void setForeground (final Color c)
   {
      window.setForeground (c);
   }
   public void setIgnoreRepaint (final boolean ignoreRepaint)
   {
      window.setIgnoreRepaint (ignoreRepaint);
   }
   public void setLocale (final Locale l)
   {
      window.setLocale (l);
   }
   public void setLocation (final int x, final int y)
   {
      window.setLocation (x, y);
   }
   public void setLocation (final Point p)
   {
      window.setLocation (p);
   }
   public void setMaximumSize (final Dimension size)
   {
      window.setMaximumSize (size);
   }
   public void setMinimumSize (final Dimension size)
   {
      window.setMinimumSize (size);
   }
   public void setName (final String name)
   {
      window.setName (name);
   }
   public void setPreferredSize (final Dimension size)
   {
      window.setPreferredSize (size);
   }
   public void setSize (final Dimension d)
   {
      window.setSize (d);
   }
   public void setSize (final int width, final int height)
   {
      window.setSize (width, height);
   }

   public void setVisible (final boolean visible)
   {
      window.setVisible (visible);

      // Hack to simulate modal frame: sleep until the user closes the dialog
      if (visible && window instanceof JFrame && isModal())
         while (isVisible()) Utils.sleep (1000);
   }

   @Override
   public String toString()
   {
      return window.toString();
   }
   public void transferFocus()
   {
      window.transferFocus();
   }
   public void transferFocusUpCycle()
   {
      window.transferFocusUpCycle();
   }
}
