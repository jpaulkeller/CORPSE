package corpse.popup.actions;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

public class OpenTableAction implements IObjectActionDelegate
{
   private static final String WORKSPACE = 
      ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile().getPath();

   private Shell shell;

   public OpenTableAction()
   {
      super();
   }

   @Override
   public void setActivePart(IAction action, IWorkbenchPart targetPart)
   {
      shell = targetPart.getSite().getShell();
   }

   @Override
   public void run(IAction action)
   {
      String selectedText = getSelectedText(); // should be a table name
      if (selectedText != null)
      {
         // File file = getCurrentFile();
         File f = findTable(new File(WORKSPACE + "/Personal/CORPSE/data/Tables"), selectedText);
         if (f != null)
            openFile(f);
      }
   }

   private String getSelectedText()
   {
      String text = null;
      
      try 
      {
         IWorkbench wb = PlatformUI.getWorkbench();
         IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
         if (win != null)
         {
            ISelectionService svc = win.getSelectionService();
            ISelection selection = svc.getSelection();
            if (selection instanceof ITextSelection && !selection.isEmpty()) 
               text = ((ITextSelection) selection).getText();
         }
      } 
      catch (Exception x) 
      {
         MessageDialog.openWarning(shell, x.getClass().getSimpleName(), x.getLocalizedMessage());
      }      

      return text;
   }

   private File getCurrentFile()
   {
      File file = null;
      
      IWorkbench wb = PlatformUI.getWorkbench();
      IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
            
      if (win != null)
      {
         IWorkbenchPage page = win.getActivePage();
         IEditorPart editor = page.getActiveEditor();
         // get the file from the tab's tool-tip (TODO: there must be a better way...)
         String tip = editor.getTitleToolTip();
         file = new File(WORKSPACE + "/Personal/" + tip);
      }

      return file;
   }

   private File findTable(final File dir, final String table)
   {
      File match = null;
      
      if (dir.isDirectory())
      {
         File[] files = dir.listFiles();
         for (File f : files)
         {
            if (f.isDirectory())
               match = findTable(f, table);
            else if (f.isFile() && f.getName().equalsIgnoreCase(table + ".tbl"))
               match = f;
            if (match != null)
               break;
         }
      }
      
      return match;
   }

   private void openFile(final File f)
   {
      IWorkbench wb = PlatformUI.getWorkbench();
      IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
      if (win != null)
      {
         IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
         IWorkbenchPage page = win.getActivePage();

         try
         {
            Path path = new Path(f.getAbsolutePath());
            IFile file = root.getFileForLocation(path); // warning: case-sensitive!
            if (file.exists())
               IDE.openEditor(page, file, true);
            else
               MessageDialog.openWarning(shell, "Missing Table (update needed?)", "Unable to find: " + path);
         }
         catch (Exception x)
         {
            MessageDialog.openWarning(shell, x.getClass().getSimpleName(), x.getLocalizedMessage());
         }
      }
   }

   /**
    * Selection in the workbench has been changed. We can change the state of the 'real' action here if we want, 
    * but this can only happen after the delegate has been created.
    */
   @Override
   public void selectionChanged(IAction action, ISelection selection)
   {
   }
}
