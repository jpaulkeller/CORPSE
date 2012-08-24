package lotro.views;

import javax.swing.JTextPane;
import javax.swing.text.html.HTMLEditorKit;

import lotro.models.Character;
import lotro.models.CharacterListModel;
import lotro.models.Klass;
import lotro.raid.SignupListener;
import web.GoogleChart;
import web.GoogleChart.ChartType;

public class ClassPie implements SignupListener
{
   private static final long serialVersionUID = 1L;
   
   private CharacterListModel model;
   private StringBuilder html = new StringBuilder();
   private String url;
   private JTextPane rawPane;
   private JTextPane htmlPane;
   private boolean updateNeeded;
   
   public ClassPie (final CharacterListModel model)
   {
      rawPane = new JTextPane();
      rawPane.setBackground (null);
      rawPane.setEditable (false);
      
      htmlPane = new JTextPane();
      htmlPane.setBackground (null);
      htmlPane.setEditable (false);
      htmlPane.setEditorKit (new HTMLEditorKit());
      
      this.model = model;
      if (!model.isEmpty())
      {
         updateNeeded = true;
         update();
      }
         
      model.addListener (this);
   }
   
   public String getURL()
   {
      update();
      return url;
   }
   
   public JTextPane getRawPane()
   {
      return rawPane;
   }
   
   public JTextPane getHtmlPane()
   {
      return htmlPane;
   }
   
   public void characterAdded (final Character ch)
   {
      model.add (ch);
      updateNeeded = true;
   }

   public void characterRemoved (final Character ch)
   {
      model.remove (ch);
      updateNeeded = true;
   }

   public void characterUpdated (final Character ch)
   {
      // TBD model?
      updateNeeded = true;
   }
   
   public void update()
   {
      if (!updateNeeded)
         return;
      
      url = null;
      
      String title = /*kinship.getName() + */" by Class";
      GoogleChart gc = new GoogleChart (title, ChartType.Pie);
      gc.put (GoogleChart.ChartProp.Width, "600");
      gc.put (GoogleChart.ChartProp.Height, "400");
      
      for (Klass c : Klass.FREEPS)
         gc.addValue (c + "s", getQuantity (c), c.getColorBG (""));
      for (Klass c : Klass.CREEPS)
         gc.addValue (c + "s", getQuantity (c), c.getColorBG (""));

      url = gc.buildURL();
      // url = url.replace ("&", "&amp;"); // for embed in <img> tag
      // url = "http://chart.apis.google.com/chart?chs=250x100&amp;chd=t:60,40&amp;cht=p3&amp;chl=Hello|World";
      
      html.setLength (0);
      /*
      html.append ("<html>\n");
      html.append ("<head>\n");
      html.append ("<meta http-equiv=\"content-type\" content=\"text-html; charset=utf-8\">\n");
      html.append ("</head>\n");
      */
      
      html.append ("<body>\n");
      html.append ("<img border=1 src=\"" + url + "\" alt=\"" + title + "\" />");
      html.append ("\n<br/>\n");
      addFooter();
      
      try
      {
         htmlPane.setText (html.toString());
         rawPane.setText (html.toString());
      }
      catch (Exception x) 
      {
         System.err.println (x.getMessage());
      }
      
      updateNeeded = false;
   }
   
   private int getQuantity (final Klass klass)
   {
      int count = 0;
      for (Character ch : model)
         if (ch.getKlass() == klass)
            count++;
      return count;
   }
   
   private void addFooter()
   {
      String mosby = "<a href=\"mailto:mosby.palantiri@gmail.com?subject=KinCharts\">contact Mosby</a>";
      String kinLink = "http://my.lotro.com/kinship-landroval-the_palantiri/";
      String style = "style=\"font-size: small; font-style:italic; text-align:right;\"";
      html.append ("<span " + style + "> Comments or questions? " +
                   "Please " + mosby + " of <a href=\"" + kinLink + 
                   "\">The Palantiri</a> (Landroval)</span>\n");
      
      html.append ("</body>\n");
      html.append ("</html>\n");
   }
}
