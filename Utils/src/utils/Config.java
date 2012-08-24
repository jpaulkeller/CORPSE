package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import str.AdvancedEncryptionStandard;

/**
 * Provides a means to store and retrieve persistent default configuration
 * settings.  Multiple sources are checked, including caller-added properties,
 * command-line argument list, Java system properties, system environment
 * variables, a configuration file (on the class-path, or in the user's local
 * application data), and the property cache.
 */
public final class Config
{
   /** The Constant serialVersionUID. */
   private static final long serialVersionUID = 1L;

   /** The Constant KEY_VALUE to match config file key/value assignments. */
   private static final Pattern KEY_VALUE = Pattern.compile("([^=]+)=(.+)");
   
   /** The text used for data encryption. */
   private static final String PASS_PHRASE = "782B426F-7BFF-731A-410D-8149FCC72DA1";
   
   /** The options file name (usually a relative path). */
   private String resource;

   /** A map of properties added via the put method; these take precedence. */
   private Map<String, String> putProps = new TreeMap<String, String>();

   /** A map of command-line argument properties. */
   private Map<String, String> argumentProps = new TreeMap<String, String>();

   /** A map of configuration file properties. */
   private Map<String, String> fileProps = new TreeMap<String, String>();

   /** A map of default properties. */
   private Map<String, String> defaultProps = new TreeMap<String, String>();

   /**
    * Instantiates a new configuration object.
    */
   public Config()
   {
   }

   /**
    * Instantiates a new configuration object with the given resource.
    *
    * @param resource the name (usually a relative path) to a file containing the configuration
    * properties. This may be null.
    */
   public Config(final String resource)
   {
      setResource(resource);
   }

   /**
    * Adds the given arguments as property/value pairs into the argumentProps
    * map. Assumes all argument pairs are in the form "key" "value", and that
    * the keys are unique.
    *
    * @param args the arguments, typically passed from the command-line to
    * a Java main method.
    */
   public void setArguments(final String[] args)
   {
      for (int i = 0, n = args.length; i < n; i += 2)
         if (i + 1 < n)
            argumentProps.put(args[i], args[i + 1]);
   }

   /**
    * Sets the configuration file.
    *
    * @param resource the name (usually a relative path) to a file containing the configuration
    * properties. This may be null.
    */
   public void setResource(final String resource)
   {
      this.resource = resource;
      read();
   }

   /**
    * Gets the configuration file.
    *
    * @return the directory name
    */
   public String getResource()
   {
      return resource;
   }

   /**
    * Gets the default path (in the user's home) to the configuration file.  This
    * is where the properties will be persisted if the write() method is called.
    *
    * @return the configuration file
    */
   public File getConfigFile()
   {
      String home = System.getProperty("user.home");
      String path = home + "/Local Settings/Application Data/" + resource;
      return new File(path);
   }

   /**
    * Load the config file, and store the properties in the fileProps map.
    */
   private void read()
   {
      if (resource != null)
      {
         // try to find the file in the classpath
         URL url = ClassLoader.getSystemResource(resource);
         if (url != null)
         {
            try
            {
               InputStream in = getClass().getResourceAsStream("/" + resource);
               read(in);
               in.close();
            }
            catch (IOException x)
            {
               System.err.println(x);
            }
         }
         
         // try to find the file in the user's local application data
         File file = getConfigFile();
         if (file.exists())
         {
            try
            {
               FileInputStream fis = new FileInputStream(file);
               read(fis);
               fis.close();
            }
            catch (IOException x)
            {
               System.err.println(x);
            }
         }
      }
   }
   
   /**
    * Read the configuration options from the given InputStream.
    *
    * @param is an InputStream containing the configuration options
    * @throws IOException Signals that an I/O exception has occurred.
    */
   private void read(final InputStream is) throws IOException
   {
      BufferedReader br = null;
      InputStreamReader isr = new InputStreamReader(is);
      br = new BufferedReader(isr);
      
      String line = null;
      while ((line = br.readLine()) != null)
      {
         Matcher m = KEY_VALUE.matcher(line);
         if (m.matches())
            fileProps.put(m.group(1), m.group(2)); 
      }
   }
   
   /**
    * Put a given key/value property into the putProps map.  Properties in this
    * map will be persisted when either of the write methods is called.
    *
    * @param key the property key
    * @param value the value associated with the key
    */
   public void put(final String key, final String value)
   {
      put(key, value, false);
   }

   /**
    * Put a given key/value property into the putProps map.  Properties in this
    * map will be persisted when either of the write methods is called. If encrypt
    * is true, the given value will be encrypted before it is stored.
    *
    * @param key the property key
    * @param value the value associated with the key
    * @param encrypt if true, the given value will be encrypted before it is stored
    */
   public void put(final String key, final String value, final boolean encrypt)
   {
      if (encrypt)
         putProps.put(key, encrypt(value));
      else
         putProps.put(key, value);
   }

   /**
    * Put a given key/value property into the defaultProps map.  This map is
    * not persisted.
    *
    * @param key a key
    * @param value the value associated with the key
    */
   public void putDefault(final String key, final String value)
   {
      defaultProps.put(key, value);
   }

   /**
    * Retrieves the value associated with the given property. This method checks
    * the following sources (in order):
    * <ol>
    * <li>caller-added properties
    * <li>command-line argument list
    * <li>Java system properties
    * <li>system environment variables
    * <li>configuration file (in the user's local data)
    * <li>configuration file (in the classpath)
    * <li>caller-added default properties
    * </ol>
    *
    * @param key the property key-value to look up
    * @return the value associated with the given key, or null if the property isn't defined
    */
   public String get(final String key)
   {
      return get(key, false);
   }
   
   /**
    * Retrieves the value associated with the given property. If decrypt is true, the
    * value will be decrypted.
    *
    * @param key the property key-value to look up
    * @param decrypt if true, the value will be decrypted first
    * @return the value associated with the given key, or null if the property isn't defined
    */
   public String get(final String key, final boolean decrypt)
   {
      String val = putProps.get(key); // check developer settings first
      if (val == null)
         val = argumentProps.get(key); // check the command-line arguments
      if (val == null)
         val = System.getProperty(key); // try the Java system
      if (val == null)
         val = System.getenv(key); // try the environment variables
      if (val == null)
         val = fileProps.get(key); // check the configuration file(s)
      if (val == null)
         val = defaultProps.get(key); // check the defaults
      
      if (decrypt && val != null)
         val = decrypt(val);
      
      return val;
   }

   /**
    * Encrypt the given (non-null) string.
    *
    * @param plain any non-null string
    * @return the encrypted string
    */
   private String encrypt(final String plain)
   {
      String encrypted = null;
      try
      {
         encrypted = AdvancedEncryptionStandard.encryptWithStaticSalt(plain, PASS_PHRASE);
      } 
      catch (Exception x)
      {
         x.printStackTrace();
      }
      return encrypted;
   }

   /**
    * Decrypt the given (non-null) string.
    *
    * @param encrypted an encrypted string
    * @return the decrypted string
    */
   private String decrypt(final String encrypted)
   {
      String decrypted = null;
      try
      {
         decrypted = AdvancedEncryptionStandard.decryptWithStaticSalt(encrypted, PASS_PHRASE);
      } 
      catch (Exception x)
      {
         x.printStackTrace();
      }
      return decrypted;
   }

   /**
    * Gets the value associated with the given key; otherwise return the
    * given defaultValue.
    *
    * @param key the key
    * @param defaultValue the default value
    * @return the string
    */
   public String get(final String key, final String defaultValue)
   {
      String value = get(key, false);
      if (value == null)
         value = defaultValue;
      return value;
   }

   /**
    * Gets the value associated with the given key (as an int); otherwise return
    * the given defaultValue.
    *
    * @param key the key
    * @param defaultValue the default value
    * @return the int
    */
   public int getInt(final String key, final int defaultValue)
   {
      String value = get(key, false);
      return value != null ? Integer.parseInt(value) : defaultValue;
   }

   /**
    * Gets the value associated with the given key (as a file); otherwise return
    * the given defaultValue.
    *
    * @param key the key
    * @return the file
    */
   public File getFile(final String key)
   {
      String value = get(key, false);
      return value != null ? new File(value) : null;
   }

   /**
    * Save the putProps and fileProps to the config file.
    */
   public void write()
   {
      if (resource != null)
      {
         PrintStream out = null;
         try
         {
            File file = getConfigFile();
            File parent = file.getParentFile();
            if (!parent.exists())
               parent.mkdirs();
            out = new PrintStream(file);
            write(out);
         } 
         catch (IOException x)
         {
            x.printStackTrace();
         }
         finally
         {
            if (out != null)
               out.close();
         }
      }
   }

   /**
    * Write the putProps and fileProps to the given output stream.
    *
    * @param out any output stream, such as System.out.
    */
   public void write(final PrintStream out)
   {
      for (Map.Entry<String, String> entry : fileProps.entrySet())
         out.println(entry.getKey() + "=" + entry.getValue());
      for (Map.Entry<String, String> entry : putProps.entrySet())
         out.println(entry.getKey() + "=" + entry.getValue());
      out.flush();
   }
   
   /*
      Config config = new Config("server.properties");
      config.write();
      System.out.println("Configuration saved as: " + config.getConfigFile());
   }
   */

   public static void main (final String[] args)
   {
      Config config = new Config();
      config.setArguments (args);
      config.putDefault ("java.version", "Default Java Version");
      config.putDefault ("default", "DEFAULT");
      
      System.out.println ("arg: " + config.get ("arg"));
      System.out.println ("java.version: " + config.get ("java.version"));
      System.out.println ("PATH: " + config.get ("PATH"));
      System.out.println ("file: " + config.get ("file"));
      System.out.println ("default: " + config.get ("default"));
      System.out.println ("missing: " + config.get ("missing", "DEFAULT"));
   }
}
