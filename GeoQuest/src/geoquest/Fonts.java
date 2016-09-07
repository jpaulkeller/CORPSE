package geoquest;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class Fonts
{
   private static final SortedMap<String, Font> FONTS = new TreeMap<>();
   static
   {
      GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
      Font[] fonts = ge.getAllFonts();
      for (Font font : fonts)
         FONTS.put(font.getName(), font);
   }
   
   public static Font derive(final String name, final float size)
   {
      return FONTS.get(name).deriveFont(size);
   }
   
   public static void main(final String[] args)
   {
      JPanel panel = new JPanel(new GridLayout(0, 1));

      for (String name : Fonts.FONTS.keySet())
      {
         JLabel label = new JLabel("AaBbCcDdEeFf - " + name);
         label.setFont(Fonts.derive(name, 20f));
         panel.add(label);
      }
      
      JFrame frame = new JFrame();
      frame.setSize(600, 800);
      frame.add(new JScrollPane(panel));
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.setVisible(true);
   }
}
