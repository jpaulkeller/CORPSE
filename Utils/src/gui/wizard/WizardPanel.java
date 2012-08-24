package gui.wizard;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.LayoutManager;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import utils.ImageTools;

public class WizardPanel extends JPanel
{
   private int wizardPanelNumber;
   private JLabel imageLabel; // the wizard image
   private TitledBorder titledBorder; // titled wrapper for client panel

   public WizardPanel()
   {
      super (new BorderLayout());
      wizardPanelNumber++;
   }

   public WizardPanel (final LayoutManager layout)
   {
      super (layout);
   }

   /** May be Overridden to perform some action as the panel is displayed.
   */
   public void onEntry()
   {
   }

   /** Must be Overridden to perform some action after the Next button
       is clicked.  Wizard calls this method before loading up the next
       Panel. Any validation should be done here
   */
   public void onNext()
   {
   }

   /** Must be Overridden to perform some action after the Prev button
       is clicked.  Wizard calls this method before loading up the previous
       Panel. Any validation should be done here
   */

   public void onPrev()
   {
   }

   /** Must be Overridden to perform some action after the Finish button
       is clicked.  Wizard calls this method before Exiting. Any validation
       should be done here.
   */

   public void onFinish()
   {
   }

   /** Can be Overridden to perform some action after the Cancel button
       is clicked. */

   public void onCancel()
   {
   }

   /** Sub-classes should override to return the results the produce. */

   public Object getResults()
   {
      return null;
   }
 
   /** Sets the description of the Panel.
    *
    * @param description Description or Instructions
    */
   public void setDescription (final String description)
   {
      if (titledBorder == null)
      {
         Border eb = BorderFactory.createEmptyBorder (10, 10, 10, 10);
         titledBorder = BorderFactory.createTitledBorder (eb, description);
      }
      else
      {
         titledBorder.setTitle (description);
         repaint();
      }
   }

   /** Sets The Wizard Image. */

   public void setImage (final ImageIcon icon)
   {
      if (imageLabel == null)
      {
         imageLabel = new JLabel (icon);
         imageLabel.setBorder (new BevelBorder (BevelBorder.LOWERED));
      }
      else
      {
         imageLabel.setText (null);
         imageLabel.setIcon (icon);
      }
   }

   public void setImage (final ImageIcon icon, final int width, final int height)
   {
      BufferedImage img =
         new BufferedImage (width, height, BufferedImage.TYPE_INT_BGR);
      Graphics g = img.getGraphics();
      g.drawImage (icon.getImage(), 0, 0, width, height, this);
      setImage (new ImageIcon (img));
   }

   public ImageIcon setImage (final String imageName)
   {
      ImageIcon ii = null;

      if (imageName != null)
      {
         ii = ImageTools.getIcon (imageName);
         if (ii != null)
            setImage (ii);
         else
            imageLabel = new JLabel (imageName + " not found");
      }

      return (ii);
   }

   public ImageIcon setImage (final String imageName, final int width, final int height)
   {
      if (imageLabel == null)
         setImage (imageName);
      ImageIcon icon = ImageTools.getIcon (imageName);
      if (icon != null)
         setImage (icon, width, height);
      else
         System.err.println ("setImage unable to load: " + imageName);

      return (icon);
   }

   /** Continues the creation of the panel. */
   protected void cont (final Container pane)
   {
      cont (pane, true);
   }

   /** Continues the creation of the panel. */
   protected void cont (final Container pane, final boolean useScrollbars)
   {
      // add the image to the left side of the WizardPanel
      if (imageLabel != null)
         add (imageLabel, BorderLayout.WEST);

      // add the client's panel to the right side of the WizardPanel,
      // wrapped in a titled, scrolling panel
      JPanel titledPanel = new JPanel (new BorderLayout());
      titledPanel.add (pane, BorderLayout.CENTER);
      if (titledBorder != null)
         titledPanel.setBorder (titledBorder);

      if (useScrollbars)
      {
         JScrollPane scrollPanel = new JScrollPane (titledPanel);
         add (scrollPanel, BorderLayout.CENTER);
      }
      else
         add (titledPanel, BorderLayout.CENTER);

      validate();
   }
}
