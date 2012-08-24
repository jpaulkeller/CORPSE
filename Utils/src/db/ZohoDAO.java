package db;

import gui.db.TableView;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import web.Firewall;
import web.Zoho;

// jdom.jar

public class ZohoDAO implements DAO
{
   private String app;
   private String view;
   
   public ZohoDAO (final String application, final String view)
   {
      this.app = application;
      this.view = view;
   }
   
   public int execute (final CharSequence sql) throws SQLException
   {
      return 0;
   }
   
   public List<String> getList  (final CharSequence sql) throws SQLException
   {
      List<String> list = new ArrayList<String>();
      
      try
      {
         String field = sql.toString(); // TODO
         String xml = Zoho.getRecords (app, view);
         Reader reader = new StringReader (xml);
         SAXBuilder sax = new SAXBuilder();
         Document doc = sax.build (reader);
         Element root = doc.getRootElement();
         
         Element recordsBranch = root.getChild ("records");
         List<Element> records = recordsBranch.getChildren ("record");
         for (Element record : records)
         {
            List<Element> columns = record.getChildren ("column");
            for (Element column : columns)
               if (field.equals (column.getAttributeValue ("name")))
                  list.add (column.getValue());
         }
         
      }
      catch (JDOMException x)
      {
         x.printStackTrace();
      }
      catch (IOException x)
      {
         x.printStackTrace();
      }
      
      return list;
   }
   
   public Model getModel (final CharSequence sql) throws SQLException
   {
      Model model = new Model (app);
      
      try
      {
         String xml = Zoho.getRecords (app, view);
         Reader reader = new StringReader (xml);
         SAXBuilder sax = new SAXBuilder();
         Document doc = sax.build (reader);
         Element root = doc.getRootElement();
         
         Element recordsBranch = root.getChild ("records");
         List<Element> records = recordsBranch.getChildren ("record");
         
         // determine columns from the first XML record
         for (Object column : records.get (0).getChildren ("column"))
            model.addColumn (((Element) column).getAttributeValue ("name"));

         // populate the model
         for (Element record : records)
         {
            List<Element> columns = record.getChildren ("column");
            Vector<String> row = new Vector<String>();
            // assumes consistent order; may need to check:
            // model.findColumn (column.getAttributeValue ("name"));
            for (Element column : columns)
               row.add (column.getValue());
            model.addRow (row);
         }
      }
      catch (JDOMException x)
      {
         x.printStackTrace();
      }
      catch (IOException x)
      {
         x.printStackTrace();
      }
      
      return model;
   }
   
   public void close()
   {
   }
   
   public static void main (final String[] args)
   {
      Firewall.defineProxy();
      DAO dao = new ZohoDAO ("deed-list", "Deed_List_Form_View");
      try
      {
         // List<String> deeds = dao.getList ("Deed_Name");
         Model model = dao.getModel ("");
         TableView.show (model);
      }
      catch (SQLException x)
      {
         x.printStackTrace();
      }
   }
}
