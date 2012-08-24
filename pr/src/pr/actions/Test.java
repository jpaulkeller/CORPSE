package pr.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

public class Test implements IWorkbenchWindowActionDelegate
{
   private IWorkbenchWindow window;

   public Test()
   {
   }

   @Override
   public void run(final IAction action)
   {
      try
      {
         traceFiles();
      }
      catch (Exception x)
      {
         x.printStackTrace();
      }
   }

   private void traceFiles() throws CoreException
   {
      IWorkspace workspace = ResourcesPlugin.getWorkspace();
      IProject[] projects = workspace.getRoot().getProjects();
      for (IProject project : projects)
      {
         System.out.println("P:" + project); // TODO
         for (IResource resource : project.members())
         {
            traceProject(resource, "  ");
            break;
         }
         break;
      }
   }

   private void traceProject(final IResource resource, final String indent) throws CoreException
   {
      if (resource instanceof IFile)
      {
         IFile file = (IFile) resource;
         System.out.println(indent + "F:" + file); // TODO
         MessageDialog.openInformation(window.getShell(), "Peer Review", file.getName());
         openFile(file);
      }
      else if (resource instanceof IFolder)
      {
         IFolder folder = (IFolder) resource;
         System.out.println(indent + "D:" + folder); // TODO
         MessageDialog.openInformation(window.getShell(), "Peer Review D", folder.toString());
         for (IResource r : folder.members())
            ; // traceProject(r, indent + "  ");
      }
      else
         System.out.println("  " + indent + "*:" + resource); // TODO

      // if (folder.getName().equalsIgnoreCase("src"))
      // Resource[] fileResources = folder.members();
      // if (fileResources[k] instanceof IFile &&
   }

   private void openFile(final IFile file)
   {
      IWorkbench wb = PlatformUI.getWorkbench();
      IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
      if (win != null)
      {
         IWorkbenchPage page = win.getActivePage();
         // page.openEditor(new FileEditorInput(file), id);
         try
         {
            IDE.openEditor(page, file, true);
         }
         catch (Exception x)
         {
            x.printStackTrace();
         }
      }
   }

   @Override
   public void init(final IWorkbenchWindow w)
   {
      this.window = w;
   }

   @Override
   public void dispose()
   {
   }

   @Override
   public void selectionChanged(IAction arg0, ISelection arg1)
   {
   }
}
