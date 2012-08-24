package gui.form;

import gui.ComponentTools;
import gui.form.valid.StatusEvent;
import gui.form.valid.StatusListener;
import gui.form.valid.Validator;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * This class is an adapter for form component items.
 *
 * Other than the abstract methods, you'll probably want to override
 * the showChangeStatus, stateChanged, setEditable and setTooltipText
 * methods, and perhaps the hasFocus method.
 *
 * @see FormItem */

public abstract class FormItemAdapter
implements ActionListener, ClipboardOwner, DocumentListener, FormItem, MouseListener
{
   private static final long serialVersionUID = 1L;
   
   private Filter filter; // filter/transformer for pre-processing
   private Object initialValue;
   private String label;
   private Validator validator; // post-process validator
   private List<ValueChangeListener> valueChangeListeners =
      new Vector<ValueChangeListener>();

   public FormItemAdapter()
   {
   }

   public abstract JComponent getComponent();
   public abstract void setValue (Object value);
   public abstract Object getValue();

   /**
    * Warning: this method also sets the item's value (as an
    * intentional side-effect).  Therefore, it cannot be called from
    * within a valueChanged callback.  If you just want to indicate
    * that the current value should now be considered the original
    * value (e.g., a save operation has been done), then use the
    * <code>apply</code> method instead. */

   public void setInitialValue (final Object value)
   {
      setInitialValue (value, true);
      showChangeStatus();
   }

   private void setInitialValue (final Object value, final boolean setValue)
   {
      initialValue = value;
      if (setValue)
         setValue (value);
      showChangeStatus();
   }

   public Object getInitialValue()
   {
      return initialValue;
   }

   /** Apply any changes by setting the initial value to the current value. */

   public void apply()
   {
      setInitialValue (getValue(), false);
   }

   public String getLabel()
   {
      return label;
   }

   public void setLabel (final String label)
   {
      if (this.label != null)
      {
         Component parent = getComponent().getParent();
         if (parent instanceof JPanel)
         {
            Border border = ((JPanel) parent).getBorder();
            if (border instanceof TitledBorder)
               if (!label.equals (((TitledBorder) border).getTitle()))
               {
                  ((TitledBorder) border).setTitle (label);
                  parent.repaint();
               }
         }
      }
      this.label = label;
   }

   public void setToolTipText (final String tip)
   {
      getComponent().setToolTipText (tip);
   }

   public Filter getFilter()
   {
      return filter;
   }

   public void setFilter (final Filter filter)
   {
      this.filter = filter;
   }
   
   public final Validator getValidator()
   {
      return validator;
   }

   public void setValidator (final Validator validator)
   {
      if (this.validator != null)
      {
         this.validator.removeStatusListener (this, this);
         removeValueChangeListener (this.validator);
      }

      this.validator = validator;
      if (validator != null)
      {
         validator.addStatusListener (this, this);
         addValueChangeListener (validator);
         // validate the data using the new validator
         validator.valueChanged (new ValueChangeEvent (this, getValue()));
      }
   }

   public void removeValidator (final Validator validatorToRemove)
   {
      validatorToRemove.removeStatusListener (this, this);
      removeValueChangeListener (validatorToRemove);
      this.validator = null;
      // no more validator, assume the data is now valid
      stateChanged (new StatusEvent (this, getValue(), true));
   }

   public void setNullValidity (final boolean status)
   {
      if (validator != null)
      {
         validator.setNullValidity (status);
         validator.validate (this, getValue());
      }
   }

   public boolean isValid()
   {
      return isValid (getValue());
   }

   /**
    * Calls the validtor to determine if the given value is valid.
    * Returns true if there is no validator. */

   public boolean isValid (final Object value)
   {
      return validator != null ? validator.isValid (value) : true;
   }

   public void addStatusListener (final StatusListener listener)
   {
      if (validator != null)
         validator.addStatusListener (this, listener);
      else
         System.err.println ("FormItemAdapter addStatusListener missing required validator: " +
                             getClass().getName());
   }

   public void removeStatusListener (final StatusListener listener)
   {
      if (validator != null)
         validator.removeStatusListener (this, listener);
   }

   /*private boolean emptyValue (Object value)
   {
      return value == null || value.toString().equals ("");
   }*/

   protected void showChangeStatus()
   {
   }

   public boolean hasChanged()
   {
      Object curVal = getValue();
      if ((initialValue == null || initialValue.toString().equals ("")) &&
          (curVal == null || curVal.toString().equals ("")))
         return false;
      else if (initialValue == null || curVal == null)
         return true;
      else
         return !initialValue.equals (curVal);
   }

   public void addValueChangeListener (final ValueChangeListener listener)
   {
      valueChangeListeners.add (listener);
   }

   public void removeValueChangeListener (final ValueChangeListener listener)
   {
      valueChangeListeners.remove (listener);
   }

   public void fireValueChanged()
   {
      showChangeStatus();

      ValueChangeEvent valueChangeEvent =
         new ValueChangeEvent (this, getValue());
      for (int i = valueChangeListeners.size() - 1; i >= 0; i--)
      {
         ValueChangeListener listener = valueChangeListeners.get (i);
         listener.valueChanged (valueChangeEvent);
      }
   }

   // implement DocumentListener
   public void removeUpdate  (final DocumentEvent e) { valueChanged(); }
   public void changedUpdate (final DocumentEvent e) { valueChanged(); }
   public void insertUpdate  (final DocumentEvent e) { valueChanged(); }

   protected void valueChanged()
   {
      fireValueChanged();
   }
   
   public void stateChanged (final StatusEvent e)
   {
   }

   // implement ClipboardOwner
   public void lostOwnership (final Clipboard clipboard, final Transferable contents)
   {
   }

   /**
    * Transfers the contents of the TextItem to the system clipboard,
    * removing the contents.
    */
   public void cutAll()
   {
      copyAll();
      setValue (null);
   }

   /**
    * Transfers the contents of the TextItem to the system clipboard,
    * leaving the contents.
    */
   public void copyAll()
   {
      Object obj = getValue();
      if (obj != null)
      {
         String value = obj.toString();
         Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
         StringSelection contents = new StringSelection (value);
         cb.setContents (contents, this);
      }
   }

   public Object getClipboardContents (final DataFlavor flavor)
   {
      Object value = null;
      Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      Transferable content = clipboard.getContents (this);
      if (content != null)
      {
         try
         {
            value = content.getTransferData (flavor);
         }
         catch (UnsupportedFlavorException ufe)
         {
            System.err.println (ufe + ": " + ufe.getMessage());
         }
         catch (IOException ioe)
         {
            System.err.println (ioe + ": " + ioe.getMessage());
         }
      }
      return value;
   }

   /**
    * Transfers the contents of the system clipboard into the
    * TextItem.  Any value in the cell is replaced with the contents
    * of the clipboard.  If the clipboard is empty, it does
    * nothing. */

   public void replace()        // paste over any old value
   {
      Object value = getClipboardContents (DataFlavor.stringFlavor);
      if (value != null)
         setValue (value);
   }

   /** Restore the original value. */

   public void restore()
   {
      setValue (initialValue);
   }

   // implement MouseListener
   public void mouseClicked  (final MouseEvent e) { }
   public void mouseEntered  (final MouseEvent e) { }
   public void mouseExited   (final MouseEvent e) { }
   public void mousePressed  (final MouseEvent e) { triggerMenu (e); }
   public void mouseReleased (final MouseEvent e) { triggerMenu (e); }

   void triggerMenu (final MouseEvent e)
   {
      // TBD popup
   }

   public boolean hasFocus()
   {
      return getComponent().hasFocus();
   }

   public void addFocusListener (final FocusListener listener)
   {
      getComponent().addFocusListener (listener);
   }

   public void removeFocusListener (final FocusListener listener)
   {
      getComponent().removeFocusListener (listener);
   }

   public void addKeyListener (final KeyListener listener)
   {
      getComponent().addKeyListener (listener);
   }

   public void removeKeyListener (final KeyListener listener)
   {
      getComponent().removeKeyListener (listener);
   }

   public void actionPerformed (final ActionEvent ae)
   {
      // The hasFocus call should not be necessary, but works-around a
      // problem with the ComboBoxItem class.  When you use the
      // Cut/Copy/Paste keys with it (test with FormPanel), they
      // instead affect the first field.
      /* TBD
      if (hasFocus())
         MethodCaller.runMethod (this, ae);
         */
   }

   public String convertToHTML()
   {
      StringBuffer buf = new StringBuffer();

      buf.append ("<tr>\n");
      String lbl = (getLabel() != null) ? getLabel() : "&nbsp;";
      buf.append (" <td>" + lbl + " </td>\n");
      buf.append (" <td><b>" + getValue() + "</b></td>\n");
      buf.append ("</tr>\n");

      return buf.toString();
   }

   @Override
   public String toString()
   {
      StringBuffer buf = new StringBuffer();
      buf.append ("Label: " + getLabel() + "\n");
      buf.append ("Class: " + getClass() + "\n");
      buf.append ("Initial Value: " + getInitialValue() + "\n");
      buf.append ("Current Value: " + getValue() + "\n");
      buf.append ("Has Changed? " + hasChanged() + "\n");
      if (getComponent() != null)
         buf.append ("Component: " + getComponent().getClass() + "\n");
      if (validator != null)
      {
         buf.append ("Validator: " + validator.getClass() + "\n");
         buf.append ("Is Valid? " + isValid() + "\n");
      }
      if (!valueChangeListeners.isEmpty())
         buf.append ("Listeners: " + valueChangeListeners.size() + "\n");

      return buf.toString();
   }

   /**
    * Convenience method for putting the item inside a panel with a
    * titled border (using the item's label). */

   public JPanel getTitledPanel()
   {
      return ComponentTools.getTitledPanel (getComponent(), getLabel());
   }

   /**
    * Convenience method for putting the item inside a panel with a
    * label (Label: [---------]). */

   public JPanel getLabeledPanel()
   {
      JLabel lbl = new JLabel (getLabel());
      lbl.setToolTipText (getComponent().getToolTipText());

      JPanel p = new JPanel (new BorderLayout());
      p.add (lbl, BorderLayout.WEST);
      p.add (getComponent(), BorderLayout.CENTER);
      return p;
   }

   public void setEnabled (final boolean enable)
   {
      getComponent().setEnabled (enable);
   }

   public boolean isEnabled()
   {
      return getComponent().isEnabled();
   }

   // TBD: this should probably be abstract

   public void setEditable (final boolean state)
   {
      System.out.println ("Warning " + getClass().getName() +
                          " does not implement setEditable");
   }

   public boolean isEditable()
   {
      return true;
   }
   
   public boolean isVisible()
   {
      return true;
   }

   protected void test()
   {
      addValueChangeListener (new ValueChangeListener() {
         public void valueChanged (final ValueChangeEvent e)
         {
            FormItem comp = (FormItem) e.getSource();
            System.out.println (comp.getLabel() + ": " + comp.getValue());
         }
      });
      getComponent().setPreferredSize (new Dimension (200, 50));

      ComponentTools.open (getTitledPanel(), getClass().getName());
   }
}
