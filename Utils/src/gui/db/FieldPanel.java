package gui.db;

import gui.form.ComboBoxItem;
import gui.form.NumericSpinner;
import gui.form.ValueChangeEvent;
import gui.form.ValueChangeListener;
import gui.wizard.Wizard;
import gui.wizard.WizardPanel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import db.Model;

//TODO: handle fixed-width data (split at positions)

public class FieldPanel extends WizardPanel implements ItemListener
{
   private TableView tv;
   private Model lines;
   private Model model;
   private JCheckBox headerFlag;
   private JCheckBox trim;

   private ComboBoxItem elemSpec;
   private Pattern elemPattern;
   private RegexValidator elemValidator;

   private ComboBoxItem lineSpec;
   private Pattern linePattern;
   private RegexValidator lineValidator;

   private NumericSpinner spinner;
   private SpinnerListener spinnerListener;
   private int columnCount; // manual override

   private Wizard wiz;
   private Object callback;
   private Method onNextMethod;

   public FieldPanel(final File source, final Wizard wiz, final Object callback, final Method onNextMethod)
   {
      this.wiz = wiz;
      this.callback = callback;
      this.onNextMethod = onNextMethod;

      TitledBorder border = new TitledBorder("Separated Data")
      {
         private Insets customInsets = new Insets(15, 5, 5, 5);

         @Override
         public Insets getBorderInsets(final Component c)
         {
            return customInsets;
         }
      };
      setBorder(border);

      tv = new TableView(model, source != null ? source.getName() : "Imported");

      headerFlag = new JCheckBox("Header Line");
      headerFlag.setToolTipText("Turn on if the first line of your data contains column names");
      headerFlag.addActionListener(new HeaderListener());
      headerFlag.setSelected(false);

      Collection<String> elemRegex = new ArrayList<String>();
      elemRegex.add("");
      elemRegex.add("([^,]*),|$"); // comma-separated values (CSV)
      elemRegex.add("([^\\t]*)\\t?"); // tab-separated values
      elemRegex.add("(?sm)<t([hd])[^>]*>(.*?)</t\\1>"); // HTML td/th elems
      elemRegex.add("(?sm)<[^>]+>(.*?)<[^>]+>"); // strip HTML tags

      // TODO: try to determine the actual delimiter, and setValue here
      // TODO: assume "," for *.CSV files

      elemSpec = new ComboBoxItem("Field Spec ", elemRegex);
      elemSpec.setEditable(true);
      elemSpec.addValueChangeListener(new ElemListener());
      elemValidator = new RegexValidator(false, 0);
      elemSpec.setValidator(elemValidator);
      elemSpec.setToolTipText("Select or enter data elem specification");
      elemPattern = elemValidator.getPattern();

      Collection<String> lineRegex = new ArrayList<String>();
      lineRegex.clear();
      lineRegex.add("");
      // TODO

      lineSpec = new ComboBoxItem("Line Spec ", lineRegex);
      lineSpec.setEditable(true);
      lineSpec.addValueChangeListener(new LineListener());
      lineValidator = new RegexValidator(false, 0);
      lineSpec.setValidator(lineValidator);
      lineSpec.setToolTipText("Select or enter regex for the entire row (group each field)");
      linePattern = lineValidator.getPattern();

      spinner = new NumericSpinner("Columns", 5);
      spinner.setToolTipText("Manually specify the number of values to extract per row");
      spinner.setValue(0);
      spinnerListener = new SpinnerListener();
      spinner.addValueChangeListener(spinnerListener);

      // checkbox to auto-trim tokens
      trim = new JCheckBox("Trim tokens?", true);
      trim.setToolTipText("Remove leading and trailing white space from each value");
      trim.addItemListener(this);

      // TODO: flag to auto-trim quotes

      // TODO: flag to auto-strip HTML tags

      JPanel cont1 = new JPanel(new FlowLayout(FlowLayout.LEADING));
      cont1.add(elemSpec.getLabeledPanel());
      cont1.add(spinner.getLabeledPanel());
      cont1.add(trim);
      cont1.add(headerFlag);

      JPanel cont2 = new JPanel(new BorderLayout());
      cont2.add(lineSpec.getLabeledPanel(), BorderLayout.CENTER);

      JPanel controls = new JPanel(new BorderLayout());
      controls.add(cont1, BorderLayout.CENTER);
      controls.add(cont2, BorderLayout.SOUTH);

      add(tv.getPanel(true, 400, 300), BorderLayout.CENTER);
      add(controls, BorderLayout.SOUTH);
   }

   void update(final Model inputLines)
   {
      lines = inputLines;
      model = new Model("Separated Data");

      String firstLine = (String) lines.getValueAt(0, 1);
      determineColumnCount(firstLine);
      addColumns(firstLine);
      addRows();

      tv.setModel(model);
      // tv.setName (null); TODO
      tv.getView().packAll();
   }

   private void determineColumnCount(final String line)
   {
      columnCount = spinner.getValueAsInt();
      if (columnCount == 0) // determine # of columns based on first row
      {
         if (linePattern != null)
         {
            Matcher m = linePattern.matcher(line);
            columnCount = m.groupCount();
         }
         else if (elemPattern != null)
         {
            Matcher m = elemPattern.matcher(line);
            while (m.find())
               columnCount++;
         }
         else
            columnCount = 1;

         /*
          spinner.removeValueChangeListener (spinnerListener); spinner.setValue (columnCount); 
          spinner.addValueChangeListener(spinnerListener);
         */
      }
   }

   private void addColumns(final String firstLine)
   {
      model.addColumn("Line", Integer.class);

      Matcher mEach = null; // pattern matches each field
      Matcher mAll = null; // pattern matches all fields
      if (headerFlag.isSelected())
      {
         if (elemPattern != null)
            mEach = elemPattern.matcher(firstLine);
         else if (linePattern != null)
         {
            mAll = linePattern.matcher(firstLine);
            if (!mAll.find())
               mAll = null;
         }
      }

      for (int col = 1; col <= columnCount; col++)
      {
         if (mAll != null && mAll.groupCount() >= col)
            model.addColumn(mAll.group(col));
         else if (mEach != null && mEach.find())
            model.addColumn(mEach.group(mEach.groupCount()));
         else
            model.addColumn("Column " + col);
      }
   }

   private void addRows()
   {
      Matcher mEach = null; // pattern matches each field
      Matcher mAll = null; // pattern matches all fields

      int firstRow = headerFlag.isSelected() ? 1 : 0;
      int rowCount = lines.getRowCount();
      for (int r = firstRow; r < rowCount; r++)
      {
         Vector<Object> row = new Vector<Object>();
         row.add(lines.getValueAt(r, 0)); // Source Line #
         String line = (String) lines.getValueAt(r, 1);

         if (elemPattern != null)
            mEach = elemPattern.matcher(line);
         else if (linePattern != null)
         {
            mAll = linePattern.matcher(line);
            if (!mAll.find())
               mAll = null;
         }

         if (columnCount == 1)
            row.add(line);
         else
         {
            for (int col = 1; col <= columnCount; col++)
            {
               if (mAll != null && mAll.groupCount() >= col)
                  row.add(getValue(mAll.group(col)));
               else if (mEach != null && mEach.find())
                  row.add(getValue(mEach.group(mEach.groupCount())));
               else
                  row.add(""); // null?
            }
         }

         model.addRow(row);
      }
   }

   private static final Pattern NUMBER = Pattern.compile("-?[0-9]+");
   private static final Pattern FOOTNOTE = Pattern.compile("<sup>(.+)</sup>");
   private static final Pattern HTML_TOKEN = Pattern.compile("<.+?>");

   // TODO: Double, Date, etc

   private Object getValue(final String s)
   {
      if (s == null)
         return "";
      String value = trim.isSelected() ? s.trim() : s;
      if (NUMBER.matcher(value).matches())
         return new Integer(value);
      
      Matcher m = FOOTNOTE.matcher(value); // footnote
      if (m.find())
         value = m.replaceFirst("[" + m.group(1) + "]");

      m = HTML_TOKEN.matcher(value); // extra HTML tokens
      if (m.find())
         value = m.replaceAll("");
      
      // TODO replace HTML codes &#160 etc
      
      return value;
   }

   // implement ItemListener
   @Override
   public void itemStateChanged(final ItemEvent e)
   {
      update(lines);
   }

   class HeaderListener implements ActionListener
   {
      @Override
      public void actionPerformed(final ActionEvent e)
      {
         update(lines);
      }
   }

   class ElemListener implements ValueChangeListener
   {
      @Override
      public void valueChanged(final ValueChangeEvent e)
      {
         System.out.println("FieldPanel.ElemListener.valueChanged(): " + elemSpec.isValid()); // TODO
         if (elemSpec.isValid())
         {
            elemPattern = elemValidator.getPattern();
            update(lines);
         }
      }
   }

   class LineListener implements ValueChangeListener
   {
      @Override
      public void valueChanged(final ValueChangeEvent e)
      {
         if (lineSpec.isValid())
         {
            linePattern = lineValidator.getPattern();
            update(lines);
         }
      }
   }

   class SpinnerListener implements ValueChangeListener
   {
      @Override
      public void valueChanged(final ValueChangeEvent e)
      {
         update(lines);
      }
   }

   @Override
   public void onEntry()
   {
      wiz.enablePrev(true);
      // wiz.enableNext (true);
   }

   @Override
   public void onNext()
   {
      if (onNextMethod != null)
         try
         {
            onNextMethod.invoke(callback);
         }
         catch (Exception x)
         {
            x.printStackTrace();
         }
   }
}
