package pr;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class PreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage
{
	public PreferencePage()
	{
		super(GRID);
	}

	public void createFieldEditors()
	{
		addField(new StringFieldEditor(Activator.PREF_DIRT_PATTERN, "DIRT Revision Pattern: ", getFieldEditorParent()));
		addField(new StringFieldEditor(Activator.PREF_JIRA_PATTERN, "JIRA Revision Pattern: ", getFieldEditorParent()));
		/* other examples:
		addField(new DirectoryFieldEditor("PATH", "&Directory preference:", getFieldEditorParent()));
		addField(new BooleanFieldEditor("BOOLEAN_VALUE", "&An example of a boolean preference", getFieldEditorParent()));
		addField(new RadioGroupFieldEditor("CHOICE", "An example of a multiple-choice preference", 1,
											new String[][] { { "&Choice 1", "choice1" }, { "C&hoice 2", "choice2" } },
											getFieldEditorParent()));
		*/
	}

	@Override
	public void init(IWorkbench workbench)
	{
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		// setDescription("Preferences for the JACOB Peer Review plugin");
	}
}
