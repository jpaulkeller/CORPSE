package gui.form;

import gui.comp.TipComboBox;
import gui.form.valid.RegexValidator;
import gui.form.valid.StatusEvent;
import gui.form.valid.Validator;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionListener;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Collection;

import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.MutableComboBoxModel;

/**
 * ComboBoxItem objects are labeled combo-boxes for use in forms,
 * designed to hold constrained data.
 *
 * Note that this class currently generates too many valueChanged
 * events, especially for editable combo boxes. */

public class ComboBoxItem extends FormItemAdapter
implements ItemListener, KeyListener
{
   private static final long serialVersionUID = 7;

   private TipComboBox box;
   private Object selectedItem;

   /**
    * ComboBoxItem Constructor - If this constructor is used, caller
    * must setModel().
    */
   public ComboBoxItem()
   {
      box = new TipComboBox();
      initialize();
   }

   public ComboBoxItem (final String label)
   {
      box = new TipComboBox();
      setLabel (label);
      initialize();
   }

   public ComboBoxItem (final Object[] objects)
   {
      box = new TipComboBox (objects);
      initialize();
   }

   public ComboBoxItem (final String label, final Object[] objects)
   {
      setLabel (label);
      box = new TipComboBox (objects);
      initialize();
   }

   public ComboBoxItem (final String label, final Collection<? extends Object> objects)
   {
      setLabel (label);
      box = new TipComboBox (objects);
      initialize();
   }

   public ComboBoxItem (final String label, final ComboBoxModel model)
   {
      setLabel (label);
      box = new TipComboBox (model);
      initialize();
   }

   public void setModel (final ComboBoxModel model)
   {
      box.setModel (model);
      setInitialValue (getValue());
   }

   /**
    * setEditable() sets the combobox to be editable if true
    * or non-editable if false.
    *
    * @param  state  boolean
    */
   @Override public void setEditable (final boolean state)
   {
      box.setEditable (state);
      // add or remove listener for editable combo box
      ComboBoxEditor editor = box.getEditor();
      JTextField comp = (JTextField) editor.getEditorComponent();
      comp.getDocument().removeDocumentListener (this);
      if (state)
         comp.getDocument().addDocumentListener (this);
   }

   public void setFilter (final String charactersAllowed)
   {
      setFilter (TextFilter.getFilter (charactersAllowed));
   }

   @Override
   public void setFilter (final Filter filter)
   {
      if (getFilter() != null)  // clear any old listeners
         removeKeyListener (this);

      super.setFilter (filter);
      if (filter != null)
         addKeyListener (this);
   }

   public void setWidth (final int w)
   {
      Dimension dim = new Dimension (w, box.getPreferredSize().height);
      box.setMinimumSize (dim);
      box.setPreferredSize (dim);
   }

   @Override
   public void addKeyListener (final KeyListener listener)
   {
      ComboBoxEditor editor = box.getEditor();
      JTextField comp = (JTextField) editor.getEditorComponent();
      comp.addKeyListener (listener);
   }
   
   @Override
   public void removeKeyListener (final KeyListener listener)
   {
      ComboBoxEditor editor = box.getEditor();
      JTextField comp = (JTextField) editor.getEditorComponent();
      comp.removeKeyListener (listener);
   }
   
   public void addActionListener (final ActionListener listener)
   {
      box.addActionListener (listener);
   }
   
   public void removeActionListener (final ActionListener listener)
   {
      box.removeActionListener (listener);
   }
   
   public void addItemListener (final ItemListener listener)
   {
      box.addItemListener (listener);
   }
   
   public void removeItemListener (final ItemListener listener)
   {
      box.removeItemListener (listener);
   }
   
   // implement KeyListener
   public void keyTyped (final KeyEvent e)
   {
      if (getFilter() != null)
      {
         if (e.isActionKey())
            return;
         char c = e.getKeyChar();
         char newChar = getFilter().process (c);
         if (newChar == 0)
            e.consume();        // ignore this keystroke
         else if (c != newChar) // transformed
            e.setKeyChar (newChar);
      }
   }
   public void keyPressed  (final KeyEvent e) { }
   public void keyReleased (final KeyEvent e) { }

   void initialize()
   {
      setInitialValue (getValue());
      box.addItemListener (this);
      box.addSpecialMouseListener (this); // add support for popup menus
   }

   /** Removes list and adds new items to the combo box. */
   public void updateItems (final Object[] items)
   {
      box.removeAllItems();
      for (int i = 0; i < items.length; i++)
         box.addItem (items[i]);
   }

   @Override 
   public JComponent getComponent()
   {
      return box;
   }

   public MutableComboBoxModel getModel()
   {
      return (MutableComboBoxModel) box.getModel();
   }

   @Override
   public void addFocusListener (final FocusListener listener)
   {
      box.addFocusListener (listener);
      if (box.isEditable())
      {
         ComboBoxEditor editor = box.getEditor();
         JTextField comp = (JTextField) editor.getEditorComponent();
         comp.addFocusListener (listener);
      }
   }

   @Override public boolean hasFocus()
   {
      if (box.isEditable())
      {
         ComboBoxEditor editor = box.getEditor();
         JTextField comp = (JTextField) editor.getEditorComponent();
         return comp.hasFocus();
      }
      return box.hasFocus();
   }

   /** Sets the selected value in the combo-box. */
   
   @Override public void setValue (final Object value)
   {
      if (box.isEditable())
      {
         ComboBoxEditor editor = box.getEditor();
         JTextField comp = (JTextField) editor.getEditorComponent();

         if (value == null)
            comp.setText ("");
         else
         {
            String s = value.toString();
            if (getFilter() != null)
               s = getFilter().process (s);
            comp.setText (s);
         }
         box.setSelectedItem (comp.getText());
      }
      else
         box.setSelectedItem (value);
   }

   /**
    * Gets the selected item in the combo-box. Warning: If editable, this method
    * should not be used in callbacks (such as actionPerformed).  The value in
    * the editor will not have been set at that point; use getSelectedItem(). */
   
   @Override 
   public Object getValue()
   {
      if (box.isEditable())
      {
         ComboBoxEditor editor = box.getEditor();
         JTextField comp = (JTextField) editor.getEditorComponent();
         Object selected = box.getSelectedItem();
         // the selection may be null, even with a visible value
         if (selected == null)
         {
            ComboBoxModel model = box.getModel();
            if (model.getSize() > 0)
               selected = model.getElementAt (0);
         }
         if (selected != null && comp.getText().equals (selected.toString()))
            return selected;  // return the object
         return comp.getText(); // return the entered text
      }
      return box.getSelectedItem();
   }

   /** Returns the index (0-based) of the selected item in the combo-box. */

   public int getSelectedIndex()
   {
      return box.getSelectedIndex();
   }

   public Object getSelectedItem()
   {
      return box.getSelectedItem();
   }

   // implement cut/copy/paste

   public void cut()
   {
      if (box.isEditable())
      {
         ComboBoxEditor editor = box.getEditor();
         JTextField comp = (JTextField) editor.getEditorComponent();
         if (comp.getSelectedText() != null)
            comp.cut();
         else
            cutAll();
      }
      else
         copyAll();
   }

   public void copy()
   {
      if (box.isEditable())
      {
         ComboBoxEditor editor = box.getEditor();
         JTextField comp = (JTextField) editor.getEditorComponent();
         if (comp.getSelectedText() != null)
            comp.copy();
         else
            copyAll();
      }
      else
         copyAll();
   }

   public void paste()
   {
      if (box.isEditable())
      {
         ComboBoxEditor editor = box.getEditor();
         JTextField comp = (JTextField) editor.getEditorComponent();

         if (getFilter() != null)
         {
            String value =
               (String) getClipboardContents (DataFlavor.stringFlavor);
            if (value != null)
            {
               String filtered = getFilter().process (value);
               if (!value.equals (filtered))
                  return;          // abort
            }
         }
         comp.paste();
      }
      else
         replace();
   }

   @Override 
   protected void showChangeStatus()
   {
      // set the foreground color based on whether or not the value changed
      if (box.isEditable())
         box.setForeground (hasChanged() ? Color.blue : Color.black);
   }

   public void itemStateChanged (final ItemEvent e)
   {
      if (selectedItem == null || !selectedItem.equals (e.getItem()))
      {
         selectedItem = e.getItem();
         fireValueChanged();
      }
   }

   @Override 
   public void stateChanged (final StatusEvent e)
   {
      // set the background color based on whether or not the value is valid
      if (box.isEditable())
         box.setBackground (e.getStatus() ? Color.white : Validator.INVALID_COLOR);
   }

   public void setMaximumRowCount (final int rows)
   {
      if (box != null)
         box.setMaximumRowCount (rows);
   }

   public static void main (final String[] args)
   {
      // ComboBoxItem
      ComboBoxItem item = new ComboBoxItem
      ("Month", new Object[] {"January", "February", "March", "April",
                              "May", "June", "July", "August", "September",
                              "October", "November", "December" });
      item.setToolTipText ("Select a month");
      item.test();

      // ComboBoxItem (editable and validated)
      item = new ComboBoxItem
         ("Direction", new Object[] { "North", "South", "East", "West" });
      item.setFilter ("NorthSouthEastWest");
      item.setEditable (true);
      item.setToolTipText ("Select/Enter a direction"); // after setEditable!
      RegexValidator rv = 
         new RegexValidator ("^North$|^South$|^East$|^West$|^NW$|^NE$|^SW$|^SE$");
      item.setValidator (rv);
      item.box.setSelectedItem ("East");
      item.test();
   }
}
