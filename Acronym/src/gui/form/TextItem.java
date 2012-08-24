package gui.form;

import gui.form.valid.RegexValidator;
import gui.form.valid.StatusEvent;
import gui.form.valid.Validator;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;

import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.TransferHandler;
import javax.swing.text.Document;

/**
 * TextItem objects are simple one-line form items for entering
 * textual data. */

public class TextItem extends FormItemAdapter implements KeyListener
{
   private static final long serialVersionUID = 10;

   private TextField textField; // main component

   public TextItem (final int size)
   {
      if (size > 0)
         setTextField (new FilteredTextField (size));
      else
         setTextField (new FilteredTextField());
   }

   public TextItem (final String label)
   {
      this (label, 0);
   }

   public TextItem (final String label, final int size)
   {
      this (size);
      setLabel (label);
   }

   public TextItem (final String label, final Object value)
   {
      this (label, 0, value);
   }

   public TextItem (final String label, final int size, final Object value)
   {
      this (size);
      setLabel (label);
      setInitialValue (value);
   }
   
   protected TextItem()
   {
      // for sub-classes that create their own TextItem  
   }

   protected TextField getTextField()
   {
      return textField;
   }
   
   protected void setTextField (final TextField textField)
   {
      this.textField = textField;
      initialize();
   }
   
   void initialize()
   {
      textField.getDocument().addDocumentListener (this);
      textField.addMouseListener (this); // add support for popup menus
   }

   @Override
   public void setValue (final Object value)
   {
      if (value == null)
         textField.setText ("");
      else
      {
         String s = value.toString();
         if (getFilter() != null)
            s = getFilter().process (s);
         textField.setText (s);
      }
   }

   @Override
   public Object getValue()
   {
      return textField.getText();
   }

   @Override
   public JComponent getComponent()
   {
      return textField.getComponent();
   }

   @Override
   public void setToolTipText (final String tip)
   {
      textField.setToolTipText (tip);
   }

   @Override
   public void setEditable (final boolean state)
   {
      textField.setEditable (state);
   }

   @Override
   public boolean isEditable()
   {
      return textField.isEditable();
   }

   public void setFocusable (final boolean state)
   {
      textField.setFocusable (state);
   }

   public void setFont (final Font font)
   {
      textField.setFont (font);
   }
   
   public void setTransferHandler (final TransferHandler h)
   {
      textField.setTransferHandler (h);
   }
   
   public void cut()
   {
      if (textField.isEditable())
      {
         if (textField.getSelectedText() != null)
            textField.cut();
         else
            cutAll();
      }
      else
         copy();
   }

   public void copy()
   {
      if (textField.getSelectedText() != null)
         textField.copy();
      else
         copyAll();
   }

   public void paste()
   {
      if (textField.isEditable())
      {
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
         
         textField.pasteFiltered();
      }
   }

   @Override
   public void replace()        // paste over any old value
   {
      if (textField.isEditable())
         super.replace();
   }
   
   public void setValidLength (final int min, final int max)
   {
      Validator v = new RegexValidator (".{" + min + "," + max + "}");
      if (min == 0)
         v.setNullValidity (true);
      setValidator (v);
   }
   
   public void setFilter (final String charactersAllowed)
   {
      setFilter (TextFilter.getFilter (charactersAllowed));
   }

   @Override
   public void setFilter (final Filter filter)
   {
      if (this.getFilter() != null)  // clear any old listeners
         textField.removeKeyListener (this);

      super.setFilter (filter);
      if (filter != null)
         textField.addKeyListener (this);
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

   @Override
   protected void showChangeStatus()
   {
      if (!textField.isEditable())
         textField.setForeground (Color.black);
      // set the foreground color based on whether or not the value changed
      else if (hasChanged())
         textField.setForeground (Color.blue);
      else
         textField.setForeground (Color.black);
   }
   
   @Override
   public void stateChanged (final StatusEvent e)
   {
      if (!textField.isEditable() && (textField.getParent() != null))
         textField.setBackground (textField.getParent().getBackground());
      // set the background color based on whether or not the value is valid
      else if (e.getStatus())   // if valid
         textField.setBackground (Color.white);
      else
         textField.setBackground (Validator.INVALID_COLOR);
   }

   protected interface TextField
   {
      // JTextField methods that we need
      Document getDocument();
      void setText (String text);
      String getText();
      String getSelectedText();
      void setToolTipText (String text);
      void setEditable (boolean editable);
      void setFocusable (boolean focusable);
      boolean isEditable();
      Container getParent();
      void cut();
      void copy();
      void paste();
      void addFocusListener (FocusListener listener);
      void removeFocusListener (FocusListener listener);
      void addMouseListener (MouseListener listener);
      void addKeyListener (KeyListener listener);
      void removeKeyListener (KeyListener listener);
      void setForeground (Color color);
      void setBackground (Color color);
      void setHorizontalAlignment (int alignment);
      void setTransferHandler (TransferHandler h);
      void setFont (Font font);

      // extra methods we need
      JComponent getComponent();
      void pasteFiltered();
   }

   protected class FilteredTextField extends JTextField implements TextField
   {
      private static final long serialVersionUID = 10;

      public FilteredTextField() { }
      public FilteredTextField (final int size)
      {
         super (size); 
      }

      public JComponent getComponent()
      {
         return this;
      }
      
      // override paste to support filtering
      @Override
      public void paste()
      {
         TextItem.this.paste();
      }

      public void pasteFiltered()
      {
         super.paste();
      }
   }
   
   public static void main (final String[] args) // for testing
   {
      TextItem item = new TextItem ("Enter Text");
      item.setInitialValue ("Initial Value");
      item.setFilter ("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz ");
      item.setFont (new Font ("Courier", Font.ITALIC, 20));
      item.test();
   }
}
