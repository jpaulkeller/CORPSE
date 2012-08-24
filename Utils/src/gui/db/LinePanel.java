package gui.db;

import gui.form.ComboBoxItem;
import gui.form.TextItem;
import gui.form.ValueChangeEvent;
import gui.form.ValueChangeListener;
import gui.wizard.Wizard;
import gui.wizard.WizardPanel;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import db.Model;

public class LinePanel extends WizardPanel implements ItemListener, ValueChangeListener
{
   private static final Pattern BLANK = Pattern.compile ("\\s*");
   private static final Pattern LINE_PATTERN = 
      Pattern.compile ("^.*$", Pattern.MULTILINE);
   
   private String data;
   private Model model;
   private TableView tv;
   private JCheckBox blank;
   
   private ComboBoxItem rowRegex;
   private RegexValidator rowValidator;
   private Pattern rowPattern;

   private TextItem mustMatchRegex;
   private RegexValidator mmValidator;
   private Pattern mustMatchPattern;

   private TextItem cantMatchRegex;
   private RegexValidator cmValidator;
   private Pattern cantMatchPattern;

   private Wizard wiz;
   private Object callback;
   private Method onNextMethod;
   
   public LinePanel (final Wizard wiz, 
                     final Object callback,
                     final Method onNextMethod)
   {
      this.wiz = wiz;
      this.callback = callback;
      this.onNextMethod = onNextMethod;
      
      setBorder (BorderFactory.createTitledBorder ("Data Records"));

      model = new Model ("Data Records");
      model.addColumn ("Line", Integer.class);
      model.addColumn ("Data", String.class);

      tv = new TableView (model);
      tv.getView().getTableHeader().setReorderingAllowed (false);

      blank = new JCheckBox ("Ignore Blanks", true);
      blank.setToolTipText ("Select this option to ignore blank lines");
      blank.addItemListener (this);
      
      Collection<String> patterns = new ArrayList<String>();
      patterns.add (""); // one line == one row
      patterns.add ("(?-sm)^.*$"); // one line == one row
      patterns.add ("<tr[^>]*>(.*?)</tr>"); // HTML rows
      rowRegex = new ComboBoxItem ("Row Spec ", patterns);
      rowRegex.setEditable (true);
      rowRegex.addValueChangeListener (this);
      rowValidator = new RegexValidator (false, Pattern.MULTILINE | Pattern.DOTALL);
      rowRegex.setValidator (rowValidator);
      rowRegex.setToolTipText ("Select or enter a pattern to specify a data row");

      // support filter-in and filter-out
      mustMatchRegex = new TextItem ("Must Match ", 6);
      mustMatchRegex.addValueChangeListener (this);
      mmValidator = new RegexValidator (true, Pattern.MULTILINE | Pattern.DOTALL);
      mustMatchRegex.setValidator (mmValidator);
      mustMatchRegex.setToolTipText
         ("Only lines matching this pattern will be imported");

      cantMatchRegex = new TextItem ("Can't Match ", 6);
      cantMatchRegex.addValueChangeListener (this);
      cmValidator = new RegexValidator (true, Pattern.MULTILINE | Pattern.DOTALL);
      cantMatchRegex.setValidator (cmValidator);
      cantMatchRegex.setToolTipText
         ("Lines matching this pattern will NOT be imported");

      JPanel controls = new JPanel (new FlowLayout (FlowLayout.LEADING));
      controls.add (blank);
      controls.add (rowRegex.getLabeledPanel());
      controls.add (mustMatchRegex.getLabeledPanel());
      controls.add (cantMatchRegex.getLabeledPanel());

      add (tv.getPanel (false, 400, 300), BorderLayout.CENTER);
      add (controls, BorderLayout.SOUTH);
   }

   // load the data into a 2-column simple table (row number and line)
   void update (final String inputText)
   {
      data = inputText;

      model.setRowCount (0); // discard any old data

      Matcher m = rowPattern != null ? rowPattern.matcher (data) : LINE_PATTERN.matcher (data);
      int count = 1;
      while (m.find())
      {
         count++;
         String row = m.group (m.groupCount());
         if (blank.isSelected() && BLANK.matcher (row).matches())
            continue;
         if (mustMatchPattern != null && !mustMatchPattern.matcher (row).matches())
            continue;
         if (cantMatchPattern != null && cantMatchPattern.matcher (row).matches())
            continue;

         model.addRow (new Object[] { count, row });
      }
      
      tv.getView().packAll();
   }

   // implement ItemListener
   public void itemStateChanged (final ItemEvent e)
   {
      update (data);
   }

   // implement ValueChangeListener
   public void valueChanged (final ValueChangeEvent e)
   {
      rowPattern = rowRegex.isValid() ? rowValidator.getPattern() : null;
      mustMatchPattern = mustMatchRegex.isValid() ? mmValidator.getPattern() : null;
      cantMatchPattern = cantMatchRegex.isValid() ? cmValidator.getPattern() : null;
      update (data);
   }

   @Override
   public void onEntry()
   {
      wiz.enablePrev (true);
      wiz.enableNext (true);
   }

   @Override
   public void onNext()
   {
      if (onNextMethod != null)
         try
         {
            onNextMethod.invoke (callback, model);
         }
         catch (Exception x)
         {
            x.printStackTrace();
         }
   }
}
