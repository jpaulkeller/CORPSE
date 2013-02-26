package pr;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin
{
	// The plug-in ID
	public static final String PLUGIN_ID = "pr"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	// The identifiers for the preferences	
	public static final String PREF_WORKSPACE = "workspace";
	public static final String PREF_DIRT_PATTERN = "DIRT pattern";
	public static final String PREF_JIRA_PATTERN = "JIRA pattern";

	// The default values for the preferences
	
	// This is usually the Workspace folder, but might be different if JACOB projects are in a sub-folder
	public static final String DEFAULT_WORKSPACE = ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile().getPath();
	public static final String DEFAULT_DIRT_PATTERN = "(?:NONE|\\d+[.]\\d+) --> \\d+[.]\\d+\\s([^\\s]+)";
	// TODO - this will need to change once we figure out what it should be for JIRA
	public static final String DEFAULT_JIRA_PATTERN = "(?:NONE|\\d+[.]\\d+) --> \\d+[.]\\d+\\s([^\\s]+)";
	
	/**
	 * The constructor
	 */
	public Activator()
	{
		/*
		Preferences preferences = ConfigurationScope.INSTANCE.getNode("com.ng.jacob.preferences");

		Preferences p = preferences.node("node1");
		p.put("workspace", "");
		p.put("revision pattern", "");
		try
		{
			// Forces the application to save the preferences
			preferences.flush();
		}
		catch (BackingStoreException e)
		{
			e.printStackTrace();
		}
		*/
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception
	{
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception
	{
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault()
	{
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in relative path
	 * 
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path)
	{
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	/**
	 * Initializes a preference store with default preference values for this plug-in.
	 */
	protected void initializeDefaultPreferences(IPreferenceStore store)
	{
		store.setDefault(PREF_WORKSPACE, DEFAULT_WORKSPACE);
		store.setDefault(PREF_DIRT_PATTERN, DEFAULT_DIRT_PATTERN);
		store.setDefault(PREF_JIRA_PATTERN, DEFAULT_JIRA_PATTERN);
		// PreferenceConverter.setDefault(store, HIGHLIGHT_PREFERENCE, color.getRGB());
	}
}
