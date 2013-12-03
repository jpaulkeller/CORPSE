package gui.db;

import gui.ColorField;
import gui.ComponentTools;
import gui.comp.FileChooser;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PrinterException;
import java.io.File;
import java.text.Format;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import model.table.TableToExcel;
import model.table.TableToHTML;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.table.ColumnHeaderRenderer;

import db.Model;

/**
 * A simple wrapper for configuring a view of tabular data.  Static methods
 * are provided to display a given row, and to print the view.
 */
public class TableView
{
   private String name;
   private TableModel model;
   private JXTable view;
   private TableCellRenderer renderer;
   private List<JButton> buttons = new ArrayList<JButton>();
   
   public TableView (final TableModel model, final String name,
                     final TableCellRenderer renderer)
   {
      setName (name);
      setModel (model);
      setRenderer (renderer);
   }
   
   public TableView (final TableModel model, final String name)
   {
      this (model, name, new ColoredTableRenderer());
   }
   
   public TableView (final Model model)
   {
      this (model, new ColoredTableRenderer());
   }
   
   public TableView (final Model model, final TableCellRenderer renderer)
   {
      this (model, null, renderer);
      if (model != null)
         setName (model.getName());
   }
   
   public void setName (final String name)
   {
      this.name = name;
   }
   
   public String getName()
   {
      return name;
   }
   
   public TableModel getModel()
   {
      return model;
   }
   
   public void setModel (final TableModel model)
   {
      this.model = model;
      if (model != null)
         model.addTableModelListener (new ModelListener());
      if (view != null)
         view.setModel (model);
   }
   
   public void setRenderer (final TableCellRenderer renderer)
   {
      this.renderer = renderer;
      if (view != null)
      {
         view.setDefaultRenderer (Boolean.class, renderer);
         view.setDefaultRenderer (ColorField.class, renderer);
         view.setDefaultRenderer (Date.class, renderer);
         view.setDefaultRenderer (Double.class, renderer);
         view.setDefaultRenderer (Number.class, renderer); // for DECIMAL types
         view.setDefaultRenderer (Object.class, renderer);
         view.setDefaultRenderer (String.class, renderer);
      }
   }

   public TableCellRenderer getRenderer()
   {
      return renderer;
   }
   
   public void setFormat (final int modelCol, final Format format)
   {
      if (renderer instanceof ColoredTableRenderer)
         ((ColoredTableRenderer) renderer).setFormat (modelCol, format);
   }
   
   public void setFormat (final Class<?> type, final Format format)
   {
      if (renderer instanceof ColoredTableRenderer)
         ((ColoredTableRenderer) renderer).setFormat (type, format);
   }
   
   public void setCellDecorator (final Class<?> type, final CellDecorator decorator)
   {
      if (renderer instanceof ColoredTableRenderer)
         ((ColoredTableRenderer) renderer).setDecorator (type, decorator);
   }
   
   public JXTable getView()
   {
      if (view == null)
         makeView();
      return view;
   }

   private void makeView()
   {
      view = new JXTable();
      if (model != null)
         view.setModel (model);
      
      view.setEditable (false);
      view.setColumnControlVisible (true);
      view.setHorizontalScrollEnabled (true);

      if (renderer != null)
         setRenderer (renderer);
      
      // center the column headers
      TableCellRenderer hr = view.getTableHeader().getDefaultRenderer();
      if (hr instanceof ColumnHeaderRenderer)
         ((ColumnHeaderRenderer) hr).setHorizontalAlignment (JLabel.CENTER);
         
      view.packAll();
   }
   
   public int findViewColumn (final String columnName)
   {
      if (view != null)
      {
         for (int c = 0; c < view.getColumnCount(); c++)
            if (columnName.equals (view.getColumn (c).getIdentifier()))
               return c;
      }
      return -1;
   }
   
   public int findModelColumn (final String columnName)
   {
      int col = findViewColumn (columnName);
      if (col >= 0)
         return view.convertColumnIndexToModel (col);
      return -1;
   }
   
   public JPanel getPanel (final boolean includeButtons, final int w, final int h)
   {
      JPanel panel = new JPanel (new BorderLayout());
      
      getView();
      if (includeButtons)
         addButtons (panel);

      JScrollPane scroller = new JScrollPane (view);
      scroller.getVerticalScrollBar().setUnitIncrement(16);
      scroller.setPreferredSize (new Dimension (w, h));
      panel.add (scroller, BorderLayout.CENTER);
      
      return panel;
   }

   private void addButtons (final JPanel panel)
   {
      JPanel grid = new JPanel (new GridLayout (1, 0));
      for (JButton button : getStandardButtons())
         grid.add (button);
      
      final JPanel buttonBar = new JPanel();
      ((FlowLayout) buttonBar.getLayout()).setVgap (1);
      buttonBar.add (grid);
      
      if (name != null)
      {
         TitledBorder border = new TitledBorder (name)
         {
            private Insets customInsets = new Insets (15, 5, 5, 5);
            @Override
            public Insets getBorderInsets (final Component c)
            {
               return customInsets;
            }
         };
         
         panel.setBorder (border);
      }
      
      panel.add (buttonBar, BorderLayout.NORTH);
   }

   public List<JButton> getStandardButtons()
   {
      ActionListener listener = new ButtonListener (view, name);
      
      boolean hasData = model != null && model.getRowCount() > 0;
      buttons.add (ComponentTools.makeButton
                   ("HTML", "icons/20/documents/SaveAsHTML.gif", hasData, listener,
                    "Export to HTML"));
      buttons.add (ComponentTools.makeButton
                   ("Excel", "icons/20/documents/SaveAsEXCEL.gif", hasData, listener,
                    "Export to Excel"));
      buttons.add (ComponentTools.makeButton
                   ("Print", "icons/20/objects/Printer.gif", hasData, listener,
                    "Print this table"));
      return buttons;
   }
   
   public JFrame show()
   {
      JPanel panel = getPanel (true, 400, 250);
      JFrame window = ComponentTools.open (panel, name);
      window.setDefaultCloseOperation (JFrame.DISPOSE_ON_CLOSE);
      return window;
   }
   
   // utility methods
   
   public static JFrame show (final Model model)
   {
      return new TableView (model).show();
   }
   
   public static int getViewColumnIndex (final JTable view, final String name)
   {
      for (int col = 0; col < view.getColumnCount(); col++)
         if (name.equals (view.getColumnName (col)))
            return col;
      return -1;
   }
   
   public static void selectAndShow (final JTable view, final int row)
   {
      view.setRowSelectionInterval (row, row);
      scrollTo (view, row);
   }

   public static void scrollTo (final JTable view, final int row)
   {
      // scroll the table so the matching row will be visible
      if (view.getParent() instanceof JViewport)
      {
         JViewport vp = (JViewport) view.getParent();
         java.awt.Rectangle rect = view.getCellRect (row, 0, true);
         java.awt.Point pos = vp.getViewPosition();
         rect.translate (-pos.x, -pos.y);
         vp.scrollRectToVisible (rect);
      }
   }
   
   public static void print (final JTable view)
   {
      // Note: Java provides other more powerful print methods that
      // will allow the table to span multiple pages across, add
      // headers or footers, etc.  This version simply squeezes the
      // table to fit on one column of pages.
      try
      {
         view.print();
      }
      catch (PrinterException x)
      {
         x.printStackTrace (System.err);
      }
   }
   
   public static void exportToExcel (final JTable view, final String name)
   {
      String user = System.getProperty ("user.name");
      String outName = "C:/Documents and Settings/" + user + "/Desktop/" + name + ".xls";
      FileChooser fc = new FileChooser ("Select File", ".");
      fc.setRegexFilter (".+[.]xls", "Excel (*.xls) files");
      fc.setSelectedFile (new File (outName));
      
      if (fc.showOpenDialog (view) == JFileChooser.APPROVE_OPTION)
         TableToExcel.export (view, fc.getSelectedFile());
   }
      
   public static void exportToHTML (final JTable view, final String name)
   {
      String user = System.getProperty ("user.name");
      String outName = "C:/Documents and Settings/" + user + "/Desktop/" + name + ".html";
      FileChooser fc = new FileChooser ("Select File", ".");
      fc.setRegexFilter (".+[.]html", "HTML files");
      fc.setSelectedFile (new File (outName));

      if (fc.showOpenDialog (view) == JFileChooser.APPROVE_OPTION)
         TableToHTML.export (view, fc.getSelectedFile());
   }
      
   static class HeaderRenderer extends DefaultTableCellRenderer
   {
      private static final long serialVersionUID = 1L;
      
      public HeaderRenderer()
      {
         setHorizontalAlignment (JLabel.CENTER);
      }
   }
   
   private class ModelListener implements TableModelListener
   {
      public void tableChanged (final TableModelEvent e)
      {
         boolean hasData = model.getRowCount() > 0;
         for (JButton button : buttons)
            button.setEnabled (hasData);
      }
   }
   
   private static class ButtonListener implements ActionListener
   {
      private JTable view;
      private String name;
      
      public ButtonListener (final JTable view, final String name)
      {
         this.view = view;
         this.name = name;
      }
      
      public void actionPerformed (final ActionEvent e)
      {
         String cmd = e.getActionCommand();
         if (cmd.equals ("Excel"))
            TableView.exportToExcel (view, name);
         else if (cmd.equals ("HTML"))
            TableView.exportToHTML (view, name);
         else if (cmd.equals ("Print"))
            TableView.print (view);
      }
   }
}
