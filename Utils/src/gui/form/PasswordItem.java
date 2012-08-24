package gui.form;

import javax.swing.JComponent;
import javax.swing.JPasswordField;

/**
 * PasswordItem objects are simple JPassword fields for entering
 * passwords.  Characters allowed include a-z, A-Z, 0-9, and most
 * special characters.  Spaces are not allowed. */

public class PasswordItem extends TextItem
{
   private static final long serialVersionUID = 1;

   static final String CHARS_ALLOWED = "abcdefghijklmnopqrstuvwxyz" +
   "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()-=+/?'][}{,.<>~`|_:;";

   public PasswordItem (final int size)
   {
      if (size > 0)
         setTextField (new FilteredPasswordField (size));
      else
         setTextField (new FilteredPasswordField());

      JPasswordField pf = (JPasswordField) getTextField().getComponent();
      pf.setEchoChar ('*');
      setFilter (CHARS_ALLOWED);
   }

   public PasswordItem()
   {
      this (null, 0, null);
   }

   public PasswordItem (final String label)
   {
      this (label, 0, null);
   }

   public PasswordItem (final String label, final int size)
   {
      this (label, size, null);
   }

   public PasswordItem (final String label, final Object value)
   {
      this (label, 0, value);
   }

   public PasswordItem (final String label, final int size, final Object value)
   {
      this (size);
      if (label != null)
         setLabel (label);
      if (value != null)
         setInitialValue (value);
   }

   protected class FilteredPasswordField extends JPasswordField
      implements TextField
   {
      private static final long
         serialVersionUID = PasswordItem.serialVersionUID;

      public FilteredPasswordField() { }
      public FilteredPasswordField (final int size) 
      { 
         super (size); 
      }

      public JComponent getComponent()
      {
         return (this);
      }
      
      // override paste to support filtering
      @Override
      public void paste()
      {
         PasswordItem.this.paste();
      }

      public void pasteFiltered()
      {
         super.paste();
      }
   }

   public static void main (final String[] args) // for testing
   {
      PasswordItem item = new PasswordItem ("Enter Password", 20, "TeSt");
      item.test();
      System.out.println ("Password: " + item.getValue());
   }
}
