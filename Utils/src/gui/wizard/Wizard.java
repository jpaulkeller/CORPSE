package gui.wizard;

import gui.ComponentTools;
import gui.comp.SimpleDialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

/**
 * Class Wizard is the base Wizard Class to be used for the creation
 * of all wizards. All event handlers for a particular panel must be
 * implemented by the developer.
 */
public class Wizard extends SimpleDialog
{
   public static final String FINISH_ACTION = "FINISH";
   public static final String CANCEL_ACTION = "CANCEL";
   public static final String NEXT_ACTION = "NEXT";
   public static final String PREV_ACTION = "PREV";

   static final int BORDER_WIDTH = 5;
   static final int BUTTON_SPACE = 5;

   private JPanel mainPanel;
   private WizardPanel currentPanel;
   private JButton nextButton;
   private JButton cancelButton;
   private JButton prevButton;
   private JButton finishButton;

   private int currentIndex = 0;

   private List<WizardPanel> panelDeck;

   private boolean okToRemove = false;
   private boolean finished = false;
   private boolean standAlone = false;

   public Wizard (final Component owner)
   {
      super (owner);
      panelDeck = new ArrayList<WizardPanel>();
      init();
   }

   private void init()
   {
      // does not work on Solaris
      // setResizable (false);

      setSize (600, 400);
      setDefaultCloseOperation (WindowConstants.DO_NOTHING_ON_CLOSE);
      addWindowListener (new WindowHandler());

      mainPanel = new JPanel (new BorderLayout());

      mainPanel.setBorder (new EmptyBorder (BORDER_WIDTH,
                                            BORDER_WIDTH,
                                            BORDER_WIDTH,
                                            BORDER_WIDTH));
      setContentPane (mainPanel);

      JPanel entryArea = new JPanel();

      mainPanel.add (entryArea, BorderLayout.CENTER);

      Box buttonArea = Box.createHorizontalBox();
      buttonArea.add (Box.createHorizontalGlue());

      ActionHandler actionHandler = new ActionHandler();

      cancelButton = new JButton ("Cancel");
      cancelButton.setActionCommand (CANCEL_ACTION);
      cancelButton.addActionListener (actionHandler);
      buttonArea.add (cancelButton);

      buttonArea.add (Box.createHorizontalStrut (BUTTON_SPACE));

      prevButton = new JButton ("< Prev");
      prevButton.setActionCommand (PREV_ACTION);
      prevButton.addActionListener (actionHandler);
      buttonArea.add (prevButton);

      buttonArea.add (Box.createHorizontalStrut (BUTTON_SPACE));

      nextButton = new JButton ("Next >");
      nextButton.setActionCommand (NEXT_ACTION);
      nextButton.addActionListener (actionHandler);
      nextButton.setEnabled (false);
      buttonArea.add (nextButton);

      buttonArea.add (Box.createHorizontalStrut (BUTTON_SPACE));

      finishButton = new JButton ("Finish");
      finishButton.setActionCommand (FINISH_ACTION);
      finishButton.addActionListener (actionHandler);
      buttonArea.add (finishButton);

      Box separatorPanel = Box.createVerticalBox();
      separatorPanel.add (Box.createVerticalStrut (5));
      separatorPanel.add (new JSeparator());
      separatorPanel.add (Box.createVerticalStrut (5));
      separatorPanel.add (buttonArea);

      mainPanel.add (separatorPanel, BorderLayout.SOUTH);
      ComponentTools.centerComponent (getWindow());
   }

   /** Sets whether the wizard is a stand alone application
       or part of something else.
       @param isStandAlone true if standalone, false if part of application
   */
   public void isStandAlone (final boolean isStandAlone)
   {
      this.standAlone = isStandAlone;
   }

   // used to display the panel inside the wizard window
   private void displayPanel (final WizardPanel panel)
   {
      if (okToRemove)
         mainPanel.remove (2);
      else
         okToRemove = true;

      mainPanel.add (panel, BorderLayout.CENTER);
      currentPanel = panel;
      //panel.onEntry();

      validate();
      repaint();
   }

   public void refresh()
   {
      WizardPanel panel = panelDeck.get (currentIndex);
      displayPanel (panel);
      panel.onEntry();
   }

   protected void cleanup()
   {
      if (finished)
      {
         setVisible (false);
         currentIndex = 0;
         updatePanel();
         dispose();
         if (standAlone)
            System.exit (0);
      }
   }

   /** Goes to the next slide in order. */
   private void next()
   {
      currentIndex++;
      updatePanel();
   }

   public void stop()
   {
      if (finished)
         finished = false;
      else
         currentIndex--;
   }

   // goes to the previous slide
   private void prev()
   {
      currentIndex--;
      updatePanel();
   }
   
   protected void updatePanel()
   {
      WizardPanel panel = panelDeck.get (currentIndex);
      displayPanel (panel);

      enablePrev (currentIndex > 0);
      enableNext (currentIndex < panelDeck.size() - 1);
      disableFinish();
      panel.onEntry(); // let the implementation decide what to enable
   }

   public int getPanelCount()
   {
      return panelDeck.size();
   }
   
   public void addPanel (final WizardPanel panel)
   {
      addPanel (panel, getPanelCount());
   }

   /** Adds a new Panel to the Wizard.
       @param panel The Panel to be added
   */
   public void addPanel (final WizardPanel panel, final int location)
   {
      if (panelDeck.size() > location)
         panelDeck.set (location, panel);
      else if (panelDeck.size() < location)
         System.out.println ("Wizard: Adding Panel to Wrong Location ");
      else
         panelDeck.add (panel);
   }

   public void removePanel (final int index)
   {
      if (panelDeck.size() > index)
         panelDeck.remove (index);
      else
         System.out.println ("Wizard -- unable to remove panel: " + index);
   }

   /** Enables the Next button on the wizard. */
   public void enableNext()
   {
      nextButton.setEnabled (true);
   }

   /** Disables the Next button on the wizard. */
   public void disableNext()
   {
      nextButton.setEnabled (false);
   }

   /** Sets the Next button state on the Wizard. */
   public void enableNext (final boolean state)
   {
      nextButton.setEnabled (state);
   }

   /** Enables the Previous button on the Wizard. */
   public void enablePrev()
   {
      prevButton.setEnabled (true);
   }

   /** Disables the Previous button on the Wizard. */
   public void disablePrev()
   {
      prevButton.setEnabled (false);
   }

   /** Sets the Prev button state on the Wizard. */
   public void enablePrev (final boolean state)
   {
      prevButton.setEnabled (state);
   }

   /** Enables the Finish button on the Wizard. */
   public void enableFinish()
   {
      finishButton.setEnabled (true);
   }

   /** Disables the Finish button on the Wizard. */
   public void disableFinish()
   {
      finishButton.setEnabled (false);
   }

   /** Sets the Finish button state on the Wizard. */
   public void enableFinish (final boolean state)
   {
      finishButton.setEnabled (state);
   }

   public void startWizard()
   {
      WizardPanel panel = panelDeck.get (currentIndex);
      displayPanel (panel);
      disablePrev();
      disableFinish();
      panel.onEntry();
      setVisible (true);
   }

   public void start (final int whichPanel)
   {
      currentIndex = whichPanel;
      startWizard();
   }

   class ActionHandler implements ActionListener
   {
      // The events from JButtons are sent to the listeners in
      // reverse order.  Therefore, since we add the ActionHandler
      // as the first listener to the buttons, we will receive the
      // event last and not change steps until after the other
      // listeners are notified.
      public void actionPerformed (final ActionEvent event)
      {
         String action = event.getActionCommand();

         if (action.equals (NEXT_ACTION))
         {
            currentPanel.onNext();
            next();
         }
         else if (action.equals (PREV_ACTION))
         {
            currentPanel.onPrev();
            prev();
         }
         else if (action.equals (FINISH_ACTION))
         {
            finished = true;
            currentPanel.onFinish();
            cleanup();
         }
         else if (action.equals (CANCEL_ACTION))
         {
            finished = true;
            currentPanel.onCancel();
            cleanup();
         }
      }
   }

   class WindowHandler extends WindowAdapter
   {
      @Override
      public void windowClosing (final WindowEvent event)
      {
         cancelButton.doClick();
      }
   }
}
