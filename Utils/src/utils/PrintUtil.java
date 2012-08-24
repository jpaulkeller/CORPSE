package utils;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import javax.swing.JOptionPane;
import javax.swing.RepaintManager;

/*
 * Copied from this tutorial:
 *
 * http://www.apl.jhu.edu/~hall/java/Swing-Tutorial/Swing-Tutorial-Printing.html
 */

public class PrintUtil implements Printable
{
   private Component componentToBePrinted;

   public PrintUtil (final Component componentToBePrinted)
   {
      this.componentToBePrinted = componentToBePrinted;
   }

   public void print() throws PrinterException
   {
      PrinterJob printJob = PrinterJob.getPrinterJob();
      printJob.setPrintable (this);
      if (printJob.printDialog())
         printJob.print();
   }

   public int print (final Graphics g, final PageFormat pf, final int pageIndex)
   {
      int response = NO_SUCH_PAGE;
      Graphics2D g2 = (Graphics2D) g;
      Dimension d = componentToBePrinted.getSize(); //get size of document
      double panelW = d.width; // width in pixels
      double panelH = d.height; // height in pixels
      double pageW = pf.getImageableWidth(); // width of printer page
      double pageH = pf.getImageableHeight(); // height of printer page
      double scale = pageW / panelW;

      //  make sure not to print empty pages
      int totalNumPages = (int) Math.ceil (scale * panelH / pageH);
      if (pageIndex < totalNumPages)
      {
         disableDoubleBuffering (componentToBePrinted); // faster printing
         // shift Graphic to align with beginning of print-imageable region
         g2.translate (pf.getImageableX(), pf.getImageableY());
         // shift Graphic to line up with beginning of next page to print
         g2.translate (0f, -pageIndex * pageH);
         // scale the page so the width fits
         g2.scale (scale, scale);
         componentToBePrinted.paint (g2); // repaint the page for printing
         enableDoubleBuffering (componentToBePrinted);
         response = Printable.PAGE_EXISTS;
      }
      return response;
   }

   public static void disableDoubleBuffering (final Component c)
   {
      RepaintManager currentManager = RepaintManager.currentManager (c);
      currentManager.setDoubleBufferingEnabled (false);
   }

   public static void enableDoubleBuffering (final Component c)
   {
      RepaintManager currentManager = RepaintManager.currentManager (c);
      currentManager.setDoubleBufferingEnabled (true);
   }

   public static void printComponent (final Component c) throws PrinterException
   {
      new PrintUtil (c).print();
   }

   public static void print (final Component owner, final Component toPrint)
   {
      try
      {
         new PrintUtil (toPrint).print();
      }
      catch (PrinterException x)
      {
         JOptionPane.showMessageDialog
            (owner, x.getMessage(), "Error Printing", JOptionPane.WARNING_MESSAGE);
         System.err.println ("Error printing: " + x.getMessage());
         x.printStackTrace (System.err);
      }
   }
}
