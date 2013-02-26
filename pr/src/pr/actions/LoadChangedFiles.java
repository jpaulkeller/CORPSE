package pr.actions;

import java.io.File;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import pr.Activator;

public class LoadChangedFiles implements IWorkbenchWindowActionDelegate
{
	private String workspace;

	private Pattern changePatternDIRT;

	private Pattern changePatternJIRA;

	private IWorkbenchWindow window;

	public LoadChangedFiles()
	{
		// apply the user preferences
		workspace = Activator.getDefault().getPreferenceStore().getString(Activator.PREF_WORKSPACE);
		String pattern = Activator.getDefault().getPreferenceStore().getString(Activator.PREF_DIRT_PATTERN);
		changePatternDIRT = Pattern.compile(pattern, Pattern.DOTALL);
		pattern = Activator.getDefault().getPreferenceStore().getString(Activator.PREF_JIRA_PATTERN);
		changePatternJIRA = Pattern.compile(pattern, Pattern.DOTALL);
	}

	@Override
	public void run(final IAction action)
	{
		String html = null;
		// html = getSelectedText();
		if (html == null)
			html = askUser();
		if (html != null)
			openChangedFiles(html);
	}
	
	/*
	private String getSelectedText()
	{
		String text = null;
		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
		if (win != null)
		{
			IWorkbenchPage page = win.getActivePage();
			if (page != null)
			{
				// ISelectionService ss = win.getSelectionService();
				ISelection selection = page.getSelection();
				if (selection instanceof ITextSelection)
				{
					ITextSelection textSelection = (ITextSelection) selection;
					text = textSelection.getText();
				}
			}
		}
		return text;
	}
	*/

	private String askUser()
	{
		String text = null;
		InputDialog dialog = new MultiLineInputDialog();
		int code = dialog.open();
		if (code == InputDialog.OK)
			text = dialog.getValue();
		return text;
	}

	private void openChangedFiles(String html)
	{
		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
		if (win != null)
		{
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IWorkbenchPage page = win.getActivePage();

			StringBuilder message = new StringBuilder();
			Set<String> filesChanged = new TreeSet<String>();
			
			// check DIRT
			Matcher m = changePatternDIRT.matcher(html);
			while (m.find())
			{
				message.append(m.group() + "\n");
				filesChanged.add(m.group(1));
			}
			
			// check JIRA
			if (filesChanged.isEmpty())
			{
				m = changePatternJIRA.matcher(html);
				while (m.find())
				{
					message.append(m.group() + "\n");
					filesChanged.add(m.group(1));
				}
			}

			MessageDialog.openInformation(window.getShell(), "Changed Files", message.toString());
			message.setLength(0);

			try
			{
				for (String fileName : filesChanged)
				{
					String fullPath = workspace + File.separator + fileName;
					Path path = new Path(fullPath);
					IFile file = root.getFileForLocation(path);
					if (file.exists())
						IDE.openEditor(page, file, true);
					else
						message.append(fileName + "\n");
				}

				if (message.length() > 0)
					MessageDialog.openWarning(window.getShell(), "Missing Files (update needed?)",
						"Unable to find these files in: " + workspace + "\n\n" + message.toString());
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

	class MultiLineInputDialog extends InputDialog
	{
		Text text = null;

		public MultiLineInputDialog()
		{
			super(window.getShell(), "Paste DIRT or JIRA Item", "Copy/Paste DIRT or JIRA revisions", null, null);
		}

		@Override
		protected int getInputTextStyle()
		{
			return SWT.MULTI | SWT.BORDER | SWT.V_SCROLL;
		}

		@Override
		protected Control createDialogArea(Composite parent)
		{
			Control res = super.createDialogArea(parent);
			((GridData) this.getText().getLayoutData()).heightHint = 100;
			return res;
		}
	}
}
