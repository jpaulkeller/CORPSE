package gui.db;

import gui.ComponentTools;
import gui.wizard.Wizard;

import java.awt.Component;
import java.lang.reflect.Method;
import java.util.List;

import db.Model;

public class FormattedDataImporter
{
   private Wizard wiz = null;

   private FilePanel filePanel;         // file chooser
   private TextPanel textPanel;         // raw text data
   private LinePanel linePanel;         // split into lines
   private FieldPanel delimPanel;       // split into fields
   // private SchemaPanel schemaPanel;     // new table schema
   // private ColumnPanel ccPanel;         // supports mapping columns

   public FormattedDataImporter (final Component owner)
   {
      wiz = new Wizard (owner);
      wiz.setTitle ("Formatted Data Import Wizard");
      
      Method onNext = getOnNextMethod ("loadTextPanel");
      filePanel = new FilePanel (wiz, this, onNext);
      wiz.addPanel (filePanel);
      // TBD: support other data sources (URL)
      wiz.validate();
   }

   public void open()
   {
      wiz.startWizard();
   }

   public void loadTextPanel()
   {
      if (textPanel == null)
      {
         Method onNext = getOnNextMethod ("loadLinePanel", String.class);
         textPanel = new TextPanel (wiz, this, onNext);
         wiz.addPanel (textPanel);
      }
      textPanel.update (filePanel.getFile());
   }

   public void loadLinePanel (final String data)
   {
      if (linePanel == null)
      {
         Method onNext = getOnNextMethod ("loadDelimPanel", Model.class);
         linePanel = new LinePanel (wiz, this, onNext);
         wiz.addPanel (linePanel);
      }
      linePanel.update (data);
   }

   public void loadDelimPanel (final Model lines)
   {
      if (delimPanel == null)
      {
         // Method onNext = getOnNextMethod ("loadSchemaPanel", Model.class);
         Method onNext = null;
         delimPanel = new FieldPanel (filePanel.getFile(), wiz, this, onNext);
         wiz.addPanel (delimPanel);
      }
      delimPanel.update (lines);
   }
   
   protected void loadSchemaPanel (final Model data)
   {
      /*
      if (schemaPanel == null)
      {
         Method onNext = getOnNextMethod ("loadMapPanel", Model.class, String.class, 
                                          String.class, List.class);
         schemaPanel = new SchemaPanel (wiz, this, onNext);
         wiz.addPanel (schemaPanel);
      }
      schemaPanel.update (data);
      */
   }
   
   protected void loadMapPanel (final Model data, final String connName,
                                final String tableName, final List<String> columnNames)
   {
      /*
      if (ccPanel == null)
      {
         ccPanel = new ColumnPanel (data);
         wiz.addPanel (ccPanel);
      }
      ccPanel.update (connName, tableName);
      */
   }

   private Method getOnNextMethod (final String name, final Class<?>... args)
   {
      Method onNext = null;
      try
      {
         onNext = getClass().getMethod (name, args);
      }
      catch (Exception x)
      {
         x.printStackTrace();
      }
      return onNext;
   }

   public static void main (final String[] args)
   {
      ComponentTools.setDefaults();
      FormattedDataImporter wiz = new FormattedDataImporter (null);
      wiz.open();
   }
}
