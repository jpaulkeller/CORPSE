package pr.actions;

import java.net.URL;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;

public class OpenDIRT implements IWorkbenchWindowActionDelegate
{
	private IWorkbenchWindow window;

	public OpenDIRT()
	{
	}

	@Override
	public void run(final IAction action)
	{
	   InputDialog dialog = new InputDialog(window.getShell(), "DIRT Item", null, null, null);
	   int code = dialog.open();
	   if (code == InputDialog.OK)
	   {
	      String dirtId = dialog.getValue();
	      String url = "https://dirt.cville.northgrum.com/view.php?id=" + dirtId;
	      openInBrowser(url);
	   }
	}

   private void openInBrowser(String url)
   {
      IWorkbench workbench = PlatformUI.getWorkbench();
      try
      {
         IWorkbenchBrowserSupport browserSupport = workbench.getBrowserSupport();
         IWebBrowser browser;
         if (browserSupport.isInternalWebBrowserAvailable())
            browser = browserSupport.createBrowser(this.getClass().getName()); // internal browser
         else
            browser = browserSupport.getExternalBrowser(); // external browser
         browser.openURL(new URL(url));
      }
      catch (Exception x)
      {
         x.printStackTrace();
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
