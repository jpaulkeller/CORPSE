package oberon;

import gui.ComponentTools;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import utils.Application;
import utils.ImageTools;

public class Menus
{
   enum States 
   {
      hasDocument,
      extracting,
      hasData,
      acronymSelected, // exactly one selected
      acronymsSelected, // one or more selected
      noDatabase;
   };
   
   static final String ACRONYMS   = "Extract";
   static final String ADD_NEW    = "Add New";
   static final String REMOVE     = "Remove";
   static final String CLEAR      = "Clear";
   
   static final String DEFINE     = "Define";
   static final String DOCUMENT   = "Document";
   static final String WEB        = "Web";
   
   static final String CONNECT_DB = "Connect to Database";
   static final String EDIT_DB    = "View/Edit Database";
   static final String ADD_DB     = "Add to Database";
   
   static final String RTF        = "RTF (MS-Word)";
   static final String HTML       = "HTML";
   static final String EXCEL      = "Excel Spreadsheet";
   static final String PRINT      = "Print";
   static final String PUBLISH    = "Publish";
   
   private static final String ACRONYMS_ICON = "DocumentDiagram.gif";
   private static final String ACRONYMS_TIP =
      "Extract possible acronyms from the document";
   private static final String ADD_NEW_ICON = "RowNew.gif";
   private static final String ADD_NEW_TIP = "Add a new acronym into the list";
   private static final String DOCUMENT_ICON = "DocumentScan.gif"; 
   private static final String DOCUMENT_TIP =
      "<html>Search the document to define acronyms.<br>" +
      "This operation applies to the <b>selected</b> rows only if there<br>" +
      "are any, otherwise it will search for all acroymns.</html>";
   private static final String WEB_ICON = "Magnify.gif"; 
   private static final String WEB_TIP =
      "<html>Search the web to define acronyms.<br>" +
      "This operation applies to the <b>selected</b> rows only if there are<br>" +
      "any, otherwise it will search for all <b>undefined</b> acronyms.<br>" +
      "(For selected rows, it will search for possible definitions in<br>" +
      "any domain (not just government and military.)</html>";

   private ActionListener listener;
   private Map<String, JComponent> invokers = new HashMap<String, JComponent>();
   
   private Application app;
   private JMenuBar menus;
   
   public Menus (final Application app, final ActionListener listener)
   {
      this.app = app;
      this.listener = listener;
      menus = makeMenus();
   }

   JMenuBar getMenus()
   {
      return menus;
   }
   
   private JMenuBar makeMenus()
   {
      JMenuBar menubar = new JMenuBar();
      menubar.add (makeAcronymMenu());
      menubar.add (makeDefinitionMenu());
      menubar.add (makeExportMenu());
      menubar.add (makeDatabaseMenu());
      return menubar;
   }

   private JMenu makeAcronymMenu()
   {
      JMenu menu = new JMenu ("Acronyms");
      menu.setMnemonic ('A');
      
      JMenuItem mi;
      mi = menu.add (makeMenuItem (ACRONYMS, 'E', ACRONYMS_ICON, ACRONYMS_TIP));
      app.enableWhen (mi, States.hasDocument.toString());
      app.disableWhen (mi, States.extracting.toString());
      
      mi = menu.add (makeMenuItem (ADD_NEW, 'A', ADD_NEW_ICON, ADD_NEW_TIP));
      app.disableWhen (mi, States.extracting.toString());
      
      mi = menu.add (makeMenuItem (REMOVE, 'R', "gui/RowDelete.gif", 
                                   "Remove the selected acronyms from the list"));
      app.enableWhen (mi, States.acronymsSelected.toString());
      app.disableWhen (mi, States.extracting.toString());
      
      mi = menu.add (makeMenuItem (CLEAR, 'C', "gui/SheetNew.gif",
                                   "Remove all acronyms from the current list"));
      app.enableWhen (mi, States.hasData.toString());
      app.disableWhen (mi, States.extracting.toString());
      
      return menu;
   }
   
   private JMenu makeDefinitionMenu()
   {
      JMenu menu = new JMenu ("Definitions");
      menu.setMnemonic ('D');
      
      JMenuItem mi;
      mi = menu.add (makeMenuItem (DEFINE, 'D', "documents/DocumentSelect.gif", 
                                   "Define the selected acronym " +
                                   "(using the context from the document)"));
      app.enableWhen (mi, States.acronymSelected.toString());
      app.disableWhen (mi, States.extracting.toString());
      
      menu.addSeparator();
      
      mi = menu.add (makeMenuItem (DOCUMENT, 'o', DOCUMENT_ICON, DOCUMENT_TIP)); 
      app.enableWhen (mi, States.hasData.toString());
      app.disableWhen (mi, States.extracting.toString());
      
      mi = menu.add (makeMenuItem (WEB, 'W', WEB_ICON, WEB_TIP));
      app.enableWhen (mi, States.hasData.toString());
      app.disableWhen (mi, States.extracting.toString());
      
      return menu;
   }
   
   private JMenu makeDatabaseMenu()
   {
      JMenu menu = new JMenu ("Database");
      menu.setMnemonic ('b');
      
      JMenuItem mi;
      mi = menu.add (makeMenuItem 
                     (CONNECT_DB, 'C', "hardware/DataStore.gif", "Connect to the DBMS"));
      app.enableWhen (mi, States.noDatabase.toString());
      
      menu.addSeparator();
      
      mi = menu.add (makeMenuItem 
                     (ADD_DB, 'A', "hardware/DataStore.gif", 
                      "<html>Export acronyms to the database.<br>" +
                      "This operation applies to the <b>selected</b> rows only if there are<br>" +
                      "any, otherwise it will save all <b>Approved</b> acroymns.</html>"));
      app.enableWhen (mi, States.hasData.toString());
      app.disableWhen (mi, States.extracting.toString());
      app.disableWhen (mi, States.noDatabase.toString());
      
      mi = menu.add (makeMenuItem 
                     (EDIT_DB, 'E', "hardware/DataStore.gif", 
                      "View/edit all acronyms stored in the database"));
      app.disableWhen (mi, States.extracting.toString());
      app.disableWhen (mi, States.noDatabase.toString());
      
      return menu;
   }
   
   private JMenu makeExportMenu()
   {
      JMenu menu = new JMenu ("Export");
      menu.setMnemonic ('E');
      
      JMenuItem mi;
      mi = menu.add (makeMenuItem 
                     (RTF, 'R', "documents/DocumentList.gif", 
                      "<html>Exports the <b>Approved</b> acronyms as an RTF file<br>" +
                     "This file can be copy/pasted into a MS-Word document.</html>"));
      app.enableWhen (mi, States.hasData.toString());
      app.disableWhen (mi, States.extracting.toString());
      
      mi = menu.add (makeMenuItem (HTML, 'H', "documents/SaveAsHTML.gif", 
         "Export the acronyms as an HTML file."));
      app.enableWhen (mi, States.hasData.toString());
      app.disableWhen (mi, States.extracting.toString());
      
      mi = menu.add (makeMenuItem (EXCEL, 'E', "documents/SaveAsEXCEL.gif", 
         "Export the acronyms as an EXCEL spreadsheet"));
      app.enableWhen (mi, States.hasData.toString());
      app.disableWhen (mi, States.extracting.toString());

      mi = menu.add (makeMenuItem (PRINT, 'P', "objects/Printer.gif", 
         "Print the table of acronyms"));
      app.enableWhen (mi, States.hasData.toString());
      app.disableWhen (mi, States.extracting.toString());
      
      return menu;
   }
   
   private JMenuItem makeMenuItem (final String label, final char mnemonic, 
                                   final String icon, final String tip)
   {
      JMenuItem mi = new JMenuItem (label);
      mi.setMnemonic (mnemonic);
      mi.setToolTipText (tip);
      if (icon != null)
         mi.setIcon (ImageTools.getIcon ("icons/20/" + icon));
      mi.addActionListener (listener);

      invokers.put (label, mi);
      return mi;
   }
   
   public JPanel getButtonPanel()
   {
      List<JButton> buttons = new ArrayList<JButton>();
      
      JButton b = ComponentTools.makeButton
         (Menus.ACRONYMS, "icons/" + ACRONYMS_ICON, false, listener, ACRONYMS_TIP);
      app.enableWhen (b, Menus.States.hasDocument.toString());
      app.disableWhen (b, Menus.States.extracting.toString());
      buttons.add (b);
      
      b = ComponentTools.makeButton
         (Menus.DOCUMENT, "icons/" + DOCUMENT_ICON, false, listener, DOCUMENT_TIP);
      app.enableWhen (b, Menus.States.hasData.toString());
      app.disableWhen (b, Menus.States.extracting.toString());
      buttons.add (b);

      b = ComponentTools.makeButton 
         (Menus.WEB, "icons/" + WEB_ICON, false, listener, WEB_TIP);
      app.enableWhen (b, Menus.States.hasData.toString());
      app.disableWhen (b, Menus.States.extracting.toString());
      buttons.add (b);

      b = ComponentTools.makeButton
         (Menus.PUBLISH, "icons/DocumentIn.gif", false, listener,
          "<html>Saves the <b>Approved</b> acronyms in the database,<br>" +
          "and exports them as an RTF file (for import to MS-Word).</html>");
      app.enableWhen (b, Menus.States.hasData.toString());
      app.disableWhen (b, Menus.States.extracting.toString());
      buttons.add (b);

      JPanel grid = new JPanel (new GridLayout (1, 0));
      for (JButton button : buttons)
         grid.add (button);
      
      JPanel panel = new JPanel();
      ((FlowLayout) panel.getLayout()).setVgap (1);
      panel.add (grid);
      
      return panel;
   }
}
