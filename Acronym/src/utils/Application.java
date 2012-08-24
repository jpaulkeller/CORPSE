package utils;

import gui.ComponentTools;
import gui.comp.ProgressBar;
import gui.tree.AutoTree;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;

import state.ComponentEnabler;
import state.StateModel;

// TBD: state handler

public abstract class Application implements Runnable
{
   private String appName;
   private String version;
   private Options options;
   private AutoTree warnings;
   
   private JMenuBar menus;
   private Map<Object, Component> components = new HashMap<Object, Component>();

   private JFrame frame;
   private ProgressBar progress;

   private StateModel stateModel;
   private ComponentEnabler enabler;
   
   public Application (final String appName, final String optionsDir,
                       final String version)
   {
      this.appName = appName;
      this.version = version;
      
      if (optionsDir != null)
      {
         options = new Options (optionsDir);
         options.read();
      }

      warnings = new AutoTree();
      warnings.setSeparator (":");

      progress = new ProgressBar();
      
      stateModel = new StateModel();
      enabler = new ComponentEnabler (stateModel);
   }
   
   protected void add (final Component comp, final Object constraints)
   {
      if (constraints == BorderLayout.SOUTH)
         System.out.println ("Warning: SOUTH will be replaced with a ProgressBar!");
      if (frame == null)
         components.put (constraints, comp);
      else
         frame.add (comp, constraints);
   }

   protected void setMenus (final JMenuBar menus)
   {
      this.menus = menus;
   }
   
   protected void open()
   {
      String title = appName + " (version " + version + ")";
      frame = ComponentTools.open (title,
                                   components.get (BorderLayout.NORTH),
                                   components.get (BorderLayout.CENTER),
                                   progress,
                                   components.get (BorderLayout.WEST),
                                   components.get (BorderLayout.EAST));
      if (menus != null)
         frame.setJMenuBar (menus);
      
      frame.addWindowListener (new WindowAdapter()
      {
         @Override
         public void windowClosing (final WindowEvent e)
         {
            close();
         }
      });
      
      stateModel.fireStateChanged();
   }
   
   protected void close()
   {
   }
   
   public JFrame getFrame()
   {
      return frame;
   }

   public Options getOptions()
   {
      return options;
   }

   public AutoTree getWarnings()
   {
      return warnings;
   }

   public ProgressBar getProgress()
   {
      return progress;
   }

   public void run()
   {
      try
      {
         progress.setIndeterminate (true);
         process();
      }
      catch (Throwable x)
      {
         x.printStackTrace (System.err);
         while (x.getCause() != null)
            x = x.getCause();
         progress.setIndeterminate (false);
         String trace = Utils.getExceptionText (x);
            
         JOptionPane.showMessageDialog (frame, trace, appName, JOptionPane.ERROR_MESSAGE, null);
      }
   }
   
   protected void process() throws Exception
   {
      warnings.clear(); // sub-classes may or may not want to do this here
   }

   public StateModel getStateModel()
   {
      return stateModel;
   }
   
   public void enableWhen (final Component comp, final String... enabled)
   {
      enabler.enableWhen (comp, enabled);
   }
   
   public void disableWhen (final Component comp, final String... disabled)
   {
      enabler.disableWhen (comp, disabled);
   }
   
   public void updateState (final String state, final boolean active)
   {
      stateModel.updateState (state, active);
   }
   
   protected void showProperties()
   {
      ComponentTools.showProperties (frame);
   }
}
