package words;
import gui.ComponentTools;
import gui.form.TextAreaItem;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import org.jdesktop.swingx.JXTable;

public class Cipher implements ActionListener
{
   static final Font FIXED = new Font ("Courier", Font.PLAIN, 12);
   static final DecimalFormat dec = new DecimalFormat ("0.000");
   static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

   static final double[] expected = new double[26];
   static
   {
     expected[ 0] /* a */ = 0.08167; expected[13] /* n */ = 0.06749;
     expected[ 1] /* b */ = 0.01492; expected[14] /* o */ = 0.07507;
     expected[ 2] /* c */ = 0.02782; expected[15] /* p */ = 0.01929;
     expected[ 3] /* d */ = 0.04253; expected[16] /* q */ = 0.00095;
     expected[ 4] /* e */ = 0.12702; expected[17] /* r */ = 0.05987;
     expected[ 5] /* f */ = 0.02228; expected[18] /* s */ = 0.06327;
     expected[ 6] /* g */ = 0.02015; expected[19] /* t */ = 0.09056;
     expected[ 7] /* h */ = 0.06094; expected[20] /* u */ = 0.02758;
     expected[ 8] /* i */ = 0.06966; expected[21] /* v */ = 0.00978;
     expected[ 9] /* j */ = 0.00153; expected[22] /* w */ = 0.02360;
     expected[10] /* k */ = 0.00772; expected[23] /* x */ = 0.00150;
     expected[11] /* l */ = 0.04025; expected[24] /* y */ = 0.01974;
     expected[12] /* m */ = 0.02406; expected[25] /* z */ = 0.00074;
   }

   TextAreaItem cipherText;
   CipherView caesar;
   CipherView freq;
   CipherView cryptogram;
   CipherView filter;

   public Cipher()
   {
     makeGUI();
   }

   void makeGUI()
   {
      cipherText = new TextAreaItem ("Cipher Text", null, 4, 50);
      cipherText.setFont (FIXED);

       // load the plugin views
       JTabbedPane tabs = new JTabbedPane();
       caesar = new CaesarView (tabs);
       freq = new FreqView (tabs);
       cryptogram = new CryptogramView (tabs);
       filter = new FilterView (tabs);

       JPanel buttons = new JPanel();
       addButton (buttons, "Decrypt", this);
       addButton (buttons, "Exit", this);

      JPanel top = new JPanel (new BorderLayout());
      top.add (cipherText.getTitledPanel(), BorderLayout.CENTER);
      top.add (tabs, BorderLayout.SOUTH);

      JPanel main = new JPanel (new BorderLayout());
      main.add (top, BorderLayout.CENTER);
      main.add (buttons, BorderLayout.SOUTH);

      ComponentTools.open (main, "Cipher");
   }

   TextAreaItem addLabeledItem (JPanel labels, JPanel values, String label)
   {
      TextAreaItem item = new TextAreaItem (label, null, 2, 30);
      item.setFont (FIXED);
      labels.add (new JLabel (label));
      values.add (item.getComponent());
      return item;
   }

   void addButton (JPanel buttons, String label, ActionListener listener)
   {
     JButton button = new JButton (label);
     button.addActionListener (listener);
     buttons.add (button);
   }

   public void actionPerformed (ActionEvent e)
   {
     String cmd = e.getActionCommand();
     if (cmd.equals ("Decrypt"))
        decrypt();
     else if (cmd.equals ("Exit"))
        System.exit (0);
   }

   void decrypt()
   {
      String text = (String) cipherText.getValue();

       caesar.decrypt (text);
       freq.decrypt (text);
       cryptogram.decrypt (text);
       filter.decrypt (text);

       cipherText.setInitialValue (cipherText.getValue());
   }

   interface CipherView
   {
      public void decrypt (String text);
   }

   class CaesarView implements CipherView
   {
      TextAreaItem[] caesar;

      CaesarView (JTabbedPane tabs)
      {
          JPanel labels = new JPanel (new GridLayout (0, 1));
          JPanel values = new JPanel (new GridLayout (0, 1));
          caesar = new TextAreaItem[26];
          for (int i = 0; i < 25; i++)
             caesar[i] = addLabeledItem (labels, values, "ROT-" + (i + 1));
          caesar[25] = addLabeledItem (labels, values, "ROT-" + 47);

          JPanel caesars = new JPanel (new BorderLayout());
          caesars.add (labels, BorderLayout.WEST);
          caesars.add (values, BorderLayout.CENTER);

          JScrollPane scroll = new JScrollPane (caesars);
          scroll.setPreferredSize (new Dimension (200, 200));
          // scroll.getVerticalScrollBar().setBlockIncrement (10);

         tabs.addTab ("Caesar", scroll);
      }

      public void decrypt (String text)
      {
         StringBuffer buf = new StringBuffer();

         int len = text.length();
         for (int i = 0; i < 26; i++)
         {
            buf.append (text);
            for (int j = 0; j < len; j++)
            {
               char c = buf.charAt (j);
               if (c >= 'A' && c <= 'Z')
               {
                  int rotate;
                  if (i < 25)
                  {
                     rotate = c + i + 1;
                     if (rotate > 'Z')
                        rotate = rotate - 26;
                  }
                  else // (i == 25)
                  {
                     rotate = c + 47; // ROT-47
                     if (rotate > '~')
                        rotate = rotate - 47;
                  }
                  buf.setCharAt (j, (char) rotate);
               }
            }
            caesar[i].setInitialValue (buf.toString());
            buf.setLength (0);
         }
      }
   }

   class FreqView implements CipherView
   {
      DefaultTableModel tbl;
      JXTable view;

      FreqView (JTabbedPane tabs)
      {
         tbl = new DefaultTableModel();
         tbl.addColumn ("Letter");
         tbl.addColumn ("Count");
         tbl.addColumn ("Percent");
         tbl.addColumn ("Expected");
         tbl.addColumn ("Delta");
         
         view = new JXTable (tbl);
         tabs.addTab ("Frequency", view);
      }

      public void decrypt (String text)
      {
         StringBuffer buf = new StringBuffer (text.toUpperCase());
         
         int[] count = new int[26];
         float total = 0;
         for (int i = 0, len = text.length(); i < len; i++)
         {
            char c = buf.charAt (i);
            if (c >= 'A' && c <= 'Z')
            {
               count [c - 'A']++;
               total++;
            }
         }
         
         while (tbl.getRowCount() > 0)
            tbl.removeRow (0);
         for (int i = 0; i < 25; i++)
            if (count[i] > 0)
            {
               String letter = "" + (char) (i + 'A');
               float freq = count[i] / total;
               
               Vector<Object> row = new Vector<Object>();
               row.add (letter);
               row.add (count[i]);
               row.add (freq > 0 ? dec.format (freq) : "0");
               row.add (dec.format (expected[i]));
               row.add (dec.format (Math.abs (freq - expected[i])));
               tbl.addRow (row);
            }
      }
   }
   
   class CryptogramView implements CipherView
   {
      JTextField pAlpha;
      JTextField cAlpha;
      Map<String, String> map = new HashMap<String, String>();
      TextAreaItem plainText;

      CryptogramView (JTabbedPane tabs)
      {
         JPanel labels = new JPanel (new GridLayout (0, 1));
         labels.add (new JLabel ("Cipher:"));
         labels.add (new JLabel ("Plain:"));

         cAlpha = new JTextField (ALPHABET);
         pAlpha = new JTextField (ALPHABET);
         cAlpha.setFont (FIXED);
         pAlpha.setFont (FIXED);

         JPanel alphabets = new JPanel (new GridLayout (0, 1));
         alphabets.add (cAlpha);
         alphabets.add (pAlpha);

         JPanel mapPanel = new JPanel (new BorderLayout());
         mapPanel.add (labels, BorderLayout.WEST);
         mapPanel.add (alphabets, BorderLayout.CENTER);

         plainText = new TextAreaItem ("Plain Text", null, 4, 50);
         plainText.setFont (FIXED);

         JPanel p = new JPanel (new BorderLayout());
         p.add (ComponentTools.getTitledPanel
            (mapPanel, "Alphabet Mapping"), BorderLayout.NORTH);
         p.add (plainText.getTitledPanel(), BorderLayout.CENTER);

         JScrollPane scroll = new JScrollPane (p);

         tabs.addTab ("Cryptogram", scroll);
      }

      public void decrypt (String text)
      {
         StringBuffer cBuf = new StringBuffer (text.toUpperCase());
         StringBuffer pBuf = new StringBuffer (text.toUpperCase());

         String c = cAlpha.getText();
         String p = pAlpha.getText();
         while (p.length() < c.length()) p += "?";

         for (int i = 0, len = c.length(); i < len; i++)
            map.put (c.substring (i, i + 1), p.substring (i, i + 1));

         for (int i = 0, len = text.length(); i < len; i++)
         {
            String cCh = cBuf.substring (i, i + 1);
            String pCh = map.get (cCh);
            char ch = pCh != null ? pCh.charAt (0) : cCh.charAt (0);
            pBuf.setCharAt (i, ch);
         }

         plainText.setInitialValue (pBuf.toString());
      }
   }

   class FilterView implements CipherView
   {
      JTextField filter;
      TextAreaItem plainText;

      FilterView (JTabbedPane tabs)
      {
         filter = new JTextField (ALPHABET);
         filter.setFont (FIXED);
         
         plainText = new TextAreaItem ("Plain Text", null, 4, 50);
         plainText.setFont (FIXED);
         
         JPanel p = new JPanel (new BorderLayout());
         p.add (ComponentTools.getTitledPanel (filter, "Show Only"),
               BorderLayout.NORTH);
         p.add (plainText.getTitledPanel(), BorderLayout.CENTER);
         JScrollPane scroll = new JScrollPane (p);
         
         tabs.addTab ("Filter", scroll);
      }

      public void decrypt (String text)
      {
          StringBuffer cBuf = new StringBuffer (text.toUpperCase());
          StringBuffer pBuf = new StringBuffer();

          String show = filter.getText().toUpperCase();

           for (int i = 0, len = text.length(); i < len; i++)
              {
              String c = cBuf.substring (i, i + 1);
              if (c.equals ("\n") || show.indexOf (c) >= 0)
                 pBuf.append (c);
              }

          plainText.setInitialValue (pBuf.toString());
      }
   }

   public static void main (String[] args)
   {
       new Cipher();
   }
}