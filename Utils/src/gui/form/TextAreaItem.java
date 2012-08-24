package gui.form;

import gui.form.valid.StatusEvent;
import gui.form.valid.Validator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.FocusListener;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.TransferHandler;

/**
 * TextAreaItem objects are simple multi-line form items for entering
 * textual data.
 */
public class TextAreaItem extends FormItemAdapter
{
   private static final long serialVersionUID = 7;

   private JPanel panel;                // main component
   private TextArea textArea;

   public TextAreaItem (final String label, final String text, 
                        final int rows, final int columns)
   {
      super();
      textArea = new TextArea (text, rows, columns);
      setInitialValue (text);
      initialize (label);
   }

   public TextAreaItem (final String label, final String text)
   {
      super();
      textArea = new TextArea (text);
      setInitialValue (text);
      initialize (label);
   }

   public TextAreaItem (final String label)
   {
      super();
      textArea = new TextArea();
      initialize (label);
   }

   public void initialize (final String itemLabel)
   {
      setLabel (itemLabel);

      panel = new JPanel (new BorderLayout());
      JScrollPane scroll = new JScrollPane (textArea);
      panel.add (scroll, BorderLayout.CENTER);

      textArea.setLineWrap (true);
      textArea.setWrapStyleWord (true);

      textArea.getDocument().addDocumentListener (this);
      textArea.addMouseListener (this); // add support for popup menus
   }

   @Override
   public void setValue (final Object value)
   {
      textArea.setText (value != null ? value.toString() : "");
      textArea.setCaretPosition (0);
   }

   @Override
   public Object getValue()
   {
      return textArea.getText();
   }

   @Override
   public JComponent getComponent()
   {
      return panel;
   }

   /*
   public PrintStream getStream()
   {
      return new TextAreaStream (textArea);
   }
   */

   @Override
   public void setToolTipText (final String tip)
   {
      textArea.setToolTipText (tip);
   }

   public void setFont (final Font font)
   {
      textArea.setFont (font);
   }

   public void setLineWrap (final boolean wrap)
   {
      textArea.setLineWrap (wrap);
   }

   public int getCaretPosition()
   {
      return textArea.getCaretPosition();
   }

   public void setCaretPosition (final int pos)
   {
      textArea.setCaretPosition (pos);
   }

   public void insert (final String text, final int pos)
   {
      textArea.insert (text, pos);
      setCaretPosition (pos);
   }

   @Override
   public void setEditable (final boolean editable)
   {
      textArea.setEditable (editable);
   }

   @Override
   public boolean hasFocus()
   {
      return textArea.hasFocus();
   }

   public void setTransferHandler (final TransferHandler h)
   {
      textArea.setTransferHandler (h);
   }

   public void setColumns (final int columns)
   {
      textArea.setColumns (columns);
   }

   public int getColumns()
   {
      return textArea.getColumns();
   }

   public void setRows (final int rows)
   {
      textArea.setRows (rows);
   }

   public int getRows()
   {
      return textArea.getRows();
   }

   public void cut()
   {
      if (textArea.getSelectedText() != null)
         textArea.cut();
      else
         cutAll();
   }

   public void copy()
   {
      if (textArea.getSelectedText() != null)
         textArea.copy();
      else
         copyAll();
   }

   public void paste()
   {
      if (getFilter() != null)
      {
         String value = (String) getClipboardContents (DataFlavor.stringFlavor);
         if (value != null)
         {
            String filtered = getFilter().process (value);
            if (!value.equals (filtered))
               return;          // abort
         }
      }

      textArea.pasteFiltered();
   }

   @Override
   public void addFocusListener (final FocusListener listener)
   {
      textArea.addFocusListener (listener);
   }

   @Override
   public void removeFocusListener (final FocusListener listener)
   {
      textArea.removeFocusListener (listener);
   }

   @Override
   protected void showChangeStatus()
   {
      // set the foreground color based on whether or not the value changed
      if (hasChanged())
         textArea.setForeground (Color.blue);
      else
         textArea.setForeground (Color.black);
   }

   @Override
   public void stateChanged (final StatusEvent e)
   {
      // set the background color based on whether or not the value is valid
      if (e.getStatus())
         textArea.setBackground (Color.white);
      else
         textArea.setBackground (Validator.INVALID_COLOR);
   }

   @Override
   public String convertToHTML()
   {
      StringBuilder buf = new StringBuilder();

      buf.append ("<tr>\n");
      String lbl = (getLabel() != null) ? getLabel() : "&nbsp;";
      buf.append (" <td>" + lbl + "</td>\n");
      buf.append (" <td><b><pre>" + getValue() + "</pre></b></td>\n");
      buf.append ("</tr>\n");

      return buf.toString();
   }

   class TextArea extends JTextArea
   {
      private static final long serialVersionUID = 4;

      public TextArea() { }
      public TextArea (final String value) 
      {
         super (value);
      }
      public TextArea (final String value, final int rows, final int columns)
      {
         super (value, rows, columns);
      }

      // override paste to support filtering
      @Override
      public void paste()
      {
         TextAreaItem.this.paste();
      }

      public void pasteFiltered()
      {
         super.paste();
      }
   }

   public static void main (final String[] args) // for testing
   {
      TextAreaItem item = new TextAreaItem ("Enter Text", "", 3, 30);
      item.setFont (new Font ("Courier", Font.ITALIC, 20));
      item.test();
    }
}
