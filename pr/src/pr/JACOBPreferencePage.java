package pr;

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class JACOBPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage
{
	public JACOBPreferencePage()
	{
	}

	@Override
	public void createFieldEditors()
	{
		// addField(new StringFieldEditor(Activator.PREF_WORKSPACE, "JACOB Workspace: ", getFieldEditorParent()));
		addField(new DirectoryFieldEditor(Activator.PREF_WORKSPACE, "JACOB Workspace: ", getFieldEditorParent()));
	}
	
	@Override
	public void init(IWorkbench workbench)
	{
		// setDescription("Preferences for JACOB plugins");
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}
}
