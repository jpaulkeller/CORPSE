package gui.comp;

import gui.ComponentTools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.util.Collection;
import java.util.Date;

import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;

/*
 * TipComboBox extends JComboBox to work-around some of its problems.
 * Specifically, it supports ToolTips, and it will allow the width of
 * the pop-up to exceed the width of the combo-box component. */

public class TipComboBox extends JComboBox
{
   private static final long serialVersionUID = 0;

   private int popupWidth;

   /*
    * TipComboBox Constructor - Creates a TipComboBox with a default
    * data model.
    */
   public TipComboBox()
   {
      super();
      init();
   }

   /**
    * TipComboBox Constructor - Creates a TipComboBox that takes its
    * items from an existing ComboBoxModel.
    */
   public TipComboBox (final ComboBoxModel model)
   {
      super (model);
      init();
   }

   /**
    * TipComboBox Constructor - Creates a TipComboBox that contains the
    * elements in the specified array.
    */
   public TipComboBox (final Object[] items)
   {
      super (items);
      init();
   }

   /*
    * TipComboBox Constructor - Creates a TipComboBox that contains the
    * elements in the specified Collection.
    */
   public TipComboBox (final Collection<? extends Object> items)
   {
      super (items.toArray());
      init();
   }

   protected void init()
   {
      // this allows the pop-up items to be wider than the main component
      setUI (new SteppedComboBoxUI());
      setPopupWidth (getPreferredSize().width);
   }

   /*
    * setToolTipText() - Override to set the tool tip for each component.
    * @param  text  String The text to display in the tool tip.
    */
   @Override
   public void setToolTipText (final String text)
   {
      // Set the tool-tip text on the editor component.
      // We set the tool-tip text on the editor component regardless of
      // whether the TipComboBox is editable or not.  This is so it will
      // work if the user sets the TipComboBox to editable after he has
      // added the tool-tip text.
      Component editorComponent = getEditor().getEditorComponent();
      if (editorComponent instanceof JComponent)
         ((JComponent) editorComponent).setToolTipText (text);

      // Set the tool-tip text on the JButton component of the TipComboBox.
      // this is done because the actual mouse movement is over the
      // JButton component of the TipComboBox. This is also done regardless
      // of whether the TipComboBox is editable or not, to allow the user
      // to set/unset editable after he has added the tool-tip text.
      Component[] components = getComponents();
      for (int i = 0; i < components.length; i++)
         if (components[i] instanceof JComponent)
            ((JComponent) components[i]).setToolTipText (text);
   }

   /**
    * Adds a MouseListener to this TipComboBox.
    */
   public void addSpecialMouseListener (final MouseListener listener)
   {
      // adds a mouse listener to the editor component.
      // This is done so the editor component will have the MouseListener
      // even if the user sets the TipComboBox to editable after he
      // adds the pop-up menu listener.
      getEditor().getEditorComponent().addMouseListener (listener);

      // add the mouse listener to the JButton component of the
      // TipComboBox.  This is done so the JButton will have the
      // MouseListener if the TipComboBox is editable or not.
      Component[] components = getComponents();
      for (int i = 0; i < components.length; i++)
         if (components[i] instanceof JButton)
            ((JButton) components[i]).addMouseListener (listener);
   }

   public void setPopupWidth (final int width)
   {
      popupWidth = width;
   }

   public Dimension getPopupSize()
   {
      Dimension preferred = getPreferredSize();
      Dimension actual = getSize (null);
      int width = Math.max (preferred.width, actual.width);
      if (popupWidth < width)
         popupWidth = width;
      return new Dimension (popupWidth, preferred.height);
   }

   @Override
   public void setBackground (final Color color)
   {
      ComboBoxEditor comboEditor = getEditor();
      if (comboEditor != null)
      {
         Component comp = comboEditor.getEditorComponent();
         if (comp instanceof JComponent)
            ((JComponent) comp).setBackground (color);
      }
      else
         super.setBackground (color);
   }

   @Override
   public void setForeground (final Color color)
   {
      ComboBoxEditor comboEditor = getEditor();
      if (comboEditor != null)
      {
         Component comp = comboEditor.getEditorComponent();
         if (comp instanceof JComponent)
            ((JComponent) comp).setForeground (color);
      }
   }

   /** From codeguru.com. */

   static class SteppedComboBoxUI 
   // extends MetalComboBoxUI
   extends com.sun.java.swing.plaf.windows.WindowsComboBoxUI
   {
      @Override
      protected ComboPopup createPopup()
      {
         BasicComboPopup comboPopup = new BasicComboPopup (comboBox)
         {
            private static final long
            serialVersionUID = TipComboBox.serialVersionUID;
            
            @Override
            public void show (final Component invoker, final int x, final int y)
            {
               Dimension size = ((TipComboBox) comboBox).getPopupSize();
               int h = getPopupHeightForRowCount (comboBox.getMaximumRowCount());
               size.setSize (size.width, h);
               Rectangle popupBounds = computePopupBounds
                  (0, comboBox.getBounds().height, size.width, size.height);
               scroller.setMaximumSize   (popupBounds.getSize());
               scroller.setPreferredSize (popupBounds.getSize());
               scroller.setMinimumSize   (popupBounds.getSize());
               
               list.invalidate();
               setLightWeightPopupEnabled (comboBox.isLightWeightPopupEnabled());
               
               super.show (comboBox, popupBounds.x, popupBounds.y);
            }
         };
         comboPopup.getAccessibleContext().setAccessibleParent (comboBox);
         
         return comboPopup;
      }
   }
   
   public static void main (final String[] args)
   {
      ComponentTools.setDefaults();
      
      JPanel boxes = new JPanel (new GridLayout (0, 1));

      // editable ComboBox
      String[] months = { "January", "February", "March", "April", "May",
                          "June", "July", "August", "September", "October",
                          "November", "December" };
      final TipComboBox box1 = new TipComboBox (months);
      box1.setEditable (true);
      box1.setSelectedIndex (8);
      box1.setToolTipText ("Select a month");
      boxes.add (box1);

      // non-editable ComboBox
      String[] days = { "Monday", "Tuesday", "Wednesday", "Thursday",
                        "Friday", "Saturday", "Sunday" };
      final TipComboBox box2 = new TipComboBox (days);
      box2.setToolTipText ("Select a day");
      box2.setSelectedItem ("Friday");
      boxes.add (box2);

      // stepped ComboBox (items are wider than GUI)
      final TipComboBox box3 = new TipComboBox();
      box3.setToolTipText ("Select a wide item");
      boxes.add (box3);

      // empty ComboBox (items are added later)
      final TipComboBox box4 = new TipComboBox();
      box4.setToolTipText ("Select a dynamically added item");
      boxes.add (box4);

      JButton btn = new JButton ("OK");
      btn.addActionListener (new ActionListener() {
         @Override
         public void actionPerformed (final ActionEvent e)
         {
            System.out.println ("Month: " + box1.getSelectedIndex() + " " +
                                box1.getSelectedItem());
            System.out.println ("Day  : " + box2.getSelectedIndex() + " " +
                                box2.getSelectedItem());
            System.out.println ("Wide : " + box3.getSelectedIndex() + " " +
                                box3.getSelectedItem());
            System.out.println ("Date : " + box4.getSelectedIndex() + " " +
                                box4.getSelectedItem());
            System.out.println();

            // add a new option each time OK is clicked
            DefaultComboBoxModel model =
               (DefaultComboBoxModel) box4.getModel();
            model.addElement (new Date());
         }
      });

      JPanel panel = new JPanel (new BorderLayout());
      panel.add (boxes, BorderLayout.CENTER);
      panel.add (btn, BorderLayout.SOUTH);

      ComponentTools.open (panel, "TipComboBox Test");

      DefaultComboBoxModel model = new DefaultComboBoxModel (
         new String[] { "This component uses a Stepped Combo Box",
                        "So even if the main component is narrow",
                        "You should see the full item in the popup" });
      box3.setModel (model);
   }
}

