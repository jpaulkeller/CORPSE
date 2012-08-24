package str;

import gui.ComponentTools;
import gui.VFillLayout;
import gui.form.ComboBoxItem;
import gui.form.TextItem;
import gui.form.ValueChangeEvent;
import gui.form.ValueChangeListener;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public final class RegExpHelper
{
   private JPanel panel;
   private TextItem patternItem;
   private JTextArea textField;
   private JTextField result;
   private JTextArea groups;

   private JCheckBox caseIns;
   private JCheckBox multiLine;
   private JCheckBox dotAll;

   private Matcher matcher;

   public RegExpHelper (final String text, final Map<String, String> patterns)
   {
      textField = new JTextArea (text, 10, 60);
      textField.setFont (new Font ("Courier", Font.PLAIN, 12));

      Vector<String> v;
      if (patterns != null)
         v = new Vector<String> (patterns.keySet());
      else
         v = new Vector<String>();

      final ComboBoxItem patternNameItem =
         new ComboBoxItem ("Pattern Name", new DefaultComboBoxModel (v));
      // patternNameItem.setEditable (false);
      patternNameItem.addValueChangeListener (new ValueChangeListener() {
         public void valueChanged (ValueChangeEvent e)
         {
            String name = (String) patternNameItem.getValue();
            if (name != null && patterns != null)
               patternItem.setInitialValue (patterns.get (name));
         }
      });

      patternItem = new TextItem ("Pattern", 40);
      patternItem.getComponent().setFont (new Font ("Courier", Font.PLAIN, 14));

      if (!v.isEmpty() && patterns != null)
         patternItem.setInitialValue (patterns.get (v.firstElement()));

      result = new JTextField (60);
      groups = new JTextArea ("", 10, 60);

      caseIns   = new JCheckBox ("CASE_INSENSITIVE");
      multiLine = new JCheckBox ("MULTILINE");
      dotAll    = new JCheckBox ("DOTALL");

      JPanel flagPanel = new JPanel();
      flagPanel.add (caseIns);
      flagPanel.add (multiLine);
      flagPanel.add (dotAll);

      JButton compare = new JButton ("Compare");
      JButton next    = new JButton ("Next Match");
      JButton all     = new JButton ("All Matches");
      JButton reset   = new JButton ("Reset");

      ButtonListener listener = new ButtonListener();
      compare.addActionListener (listener);
      next.addActionListener (listener);
      all.addActionListener (listener);
      reset.addActionListener (listener);

      JPanel buttons = new JPanel();
      buttons.add (compare);
      buttons.add (next);
      buttons.add (all);
      buttons.add (reset);

      panel = new JPanel (new VFillLayout());
      panel.add (ComponentTools.getTitledPanel
                 (new JScrollPane (textField), "Source Text"));
      panel.add (patternNameItem.getTitledPanel());
      panel.add (patternItem.getTitledPanel());
      panel.add (ComponentTools.getTitledPanel (flagPanel, "Flags"));
      panel.add (buttons);
      panel.add (ComponentTools.getTitledPanel (result, "Match"));
      panel.add (ComponentTools.getTitledPanel
                 (new JScrollPane (groups), "Groups (or All Matches)"));
   }

   public JPanel getPanel()
   {
      return panel;
   }

   int getFlags()
   {
      int flags = 0;

      if (caseIns.isSelected())
         flags |= Pattern.CASE_INSENSITIVE;
      if (multiLine.isSelected())
         flags |= Pattern.MULTILINE;
      if (dotAll.isSelected())
         flags |= Pattern.DOTALL;

      return flags;
   }

   class ButtonListener implements ActionListener
   {
      public void actionPerformed (final ActionEvent e)
      {
         String command = e.getActionCommand();
         if (command.equals ("Compare"))
            compare();
         else if (command.equals ("Next Match"))
            next();
         else if (command.equals ("All Matches"))
            all();
         else if (command.equals ("Reset"))
            reset();
      }

      void compare()
      {
         String regexp = (String) patternItem.getValue();
         String target = textField.getText();

         try
         {
            Pattern pattern = Pattern.compile (regexp, getFlags());
            matcher = pattern.matcher (target);
            boolean status = matcher.matches();
            result.setBackground (status ? Color.green : Color.pink);
         }
         catch (Exception ex)
         {
            result.setBackground (Color.red);
            result.setText (ex.getMessage());
         }

         matcher = null;
      }

      void next()
      {
         String regexp = (String) patternItem.getValue();
         String target = textField.getText();

         groups.setText ("");

         try
         {
            if (matcher == null)
            {
               Pattern pattern = Pattern.compile (regexp, getFlags());
               matcher = pattern.matcher (target);
            }
            boolean status = matcher.find();
            result.setBackground (status ? Color.green : Color.pink);
            if (status)
            {
               result.setText (matcher.group());
               for (int i = 1, count = matcher.groupCount(); i <= count; i++)
                  groups.append (i + " at " + matcher.start() + ") " + 
                                 matcher.group (i) + "\n");
            }
         }
         catch (Exception x)
         {
            result.setBackground (Color.red);
            result.setText (x.getMessage());
         }
      }

      void all()
      {
         String regexp = (String) patternItem.getValue();
         String target = textField.getText();

         groups.setText ("");

         try
         {
            Pattern pattern = Pattern.compile (regexp, getFlags());
            matcher = pattern.matcher (target);
            int i = 0;
            while (matcher.find())
               groups.append (++i + ") " + matcher.group (0) + "\n");
         }
         catch (Exception ex)
         {
            result.setBackground (Color.red);
            result.setText (ex.getMessage());
         }
      }

      void reset()
      {
         matcher = null;
         result.setText ("");
         result.setBackground (Color.white);
      }
   }

   public static void main (final String[] args)
   {
      RegExpHelper app = new RegExpHelper ("", null);
      ComponentTools.open (app.panel, "Regular Expression Helper");
   }
}
