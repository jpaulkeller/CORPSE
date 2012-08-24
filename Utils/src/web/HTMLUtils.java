package web;

import java.awt.Color;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ToolTipManager;
import javax.swing.UIManager;

public final class HTMLUtils
{
   private HTMLUtils() { }
   
   public static String encode (final String urlArg)
   {
      try
      {
         return URLEncoder.encode (urlArg, "UTF-8");
      }
      catch (UnsupportedEncodingException x)
      {
         System.out.println ("Failed to encode URL argument: " + urlArg);
         x.printStackTrace();
      }

      return null;
   }

   public static String decode (final String urlArg)
   {
      try
      {
         return URLDecoder.decode (urlArg, "UTF-8");
      }
      catch (UnsupportedEncodingException x)
      {
         System.out.println ("Failed to decode URL argument: " + urlArg);
         x.printStackTrace();
      }

      return null;
   }
   
   /** Returns a hidden (non-clickable, non-underlined) link to support simple tooltips. */
   
   public static String getTip (final String label, final String tip)
   {
      return "<a href=\"#\" onclick=\"return false;\" title=\"" + tip + 
             "\" style=\"text-decoration:none;\">" + label + "</a>";         
   }

   public static void appendArgument (final StringBuilder s, 
                                      final String arg, final String val)
   {
      s.append (s.indexOf ("?") > 0 ? "&" : "?");
      s.append (arg);
      s.append ("=");
      s.append (HTMLUtils.encode (val));
   }

   // copied from ComponentTools

   public static void setDefaults()
   {
      String plaf = UIManager.getSystemLookAndFeelClassName();
      try
      {
         UIManager.setLookAndFeel (plaf);
      }
      catch (Exception x)
      {
         System.out.println ("Error loading L&F: " + plaf + "\n" + x);
      }

      UIManager.put ("ToolTip.background", Color.yellow);
      UIManager.put ("ToolTip.foreground", Color.black);
      UIManager.put ("ToolTip.backgroundInactive", new Color (255, 255, 200));
      UIManager.put ("ToolTip.foregroundInactive", Color.gray);

      // leave tool tips up for 10 seconds (default is 4)
      ToolTipManager.sharedInstance().setDismissDelay (10000);
   }
   
   // Return the rest of a line in a multi-line text string that starts
   // with the given prefix
   public static String parseLineValue (final String prefix, final String text)
   {
      if (prefix != null && text != null)
      {
         int start = text.indexOf (prefix);
         if (start >= 0)
         {
            start += prefix.length();
            int end = text.indexOf ("\n", start);
            if (end < 0)
               return text.substring (start);
            return text.substring (start, end);
         }
      }
      return null;
   }
   
   private static final Pattern CHAR_PATTERN =
      Pattern.compile ("&#[a-z]+;", Pattern.CASE_INSENSITIVE);
   private static final Pattern UNICODE_PATTERN =
      Pattern.compile ("&#(\\d+);");

   public static String replaceSpecialCharacters (final String html)
   {
      String s = html;
      
      // replace all the common tokens (use regex to be case-insensitive)
      Matcher m = CHAR_PATTERN.matcher (s);
      if (m.find())
      {
         s = s.replaceAll ("&amp;",  "&");
         s = s.replaceAll ("&gt;",   ">");
         s = s.replaceAll ("&lt;",   "<");
         s = s.replaceAll ("&quot;", "\"");
      }

      // replace all the case-sensitive tokens with their UNICODE equivalent
      m = CHAR_PATTERN.matcher (s);
      if (m.find())
      {
         s = s.replace ("&AElig;",  "&#198;");
         s = s.replace ("&Aacute;", "&#193;");
         s = s.replace ("&Acirc;",  "&#194;");
         s = s.replace ("&Agrave;", "&#192;");
         s = s.replace ("&Aring;",  "&#197;");
         s = s.replace ("&Atilde;", "&#195;");
         s = s.replace ("&Auml;",   "&#196;");
         s = s.replace ("&CCedil;", "&#199;");
         s = s.replace ("&ETH;",    "&#208;");
         s = s.replace ("&Eacute;", "&#201;");
         s = s.replace ("&Ecirc;",  "&#202;");
         s = s.replace ("&Egrave;", "&#200;");
         s = s.replace ("&Euml;",   "&#203;");
         s = s.replace ("&Iacute;", "&#205;");
         s = s.replace ("&Icirc;",  "&#206;");
         s = s.replace ("&Igrave;", "&#204;");
         s = s.replace ("&Iuml;",   "&#207;");
         s = s.replace ("&Ntilde;", "&#209;");
         s = s.replace ("&Oacute;", "&#211;");
         s = s.replace ("&Ocirc;",  "&#212;");
         s = s.replace ("&Ograve;", "&#210;");
         s = s.replace ("&Oslash;", "&#216;");
         s = s.replace ("&Otilde;", "&#213;");
         s = s.replace ("&Ouml;",   "&#214;");
         s = s.replace ("&THORN;",  "&#222;");
         s = s.replace ("&Uacute;", "&#218;");
         s = s.replace ("&Ucirc;",  "&#219;");
         s = s.replace ("&Ugrave;", "&#217;");
         s = s.replace ("&Uuml;",   "&#220;");
         s = s.replace ("&Yacute;", "&#221;");
      }

      // replace all the other tokens (ignore case) with UNICODE
      m = CHAR_PATTERN.matcher (s);
      if (m.find())
      {
         s = s.replaceAll ("&aacute;", "&#225;");
         s = s.replaceAll ("&acirc;",  "&#226;");
         s = s.replaceAll ("&acute;",  "&#180;");
         s = s.replaceAll ("&aelig;",  "&#230;");
         s = s.replaceAll ("&agrave;", "&#224;");
         s = s.replaceAll ("&aring;",  "&#229;");
         s = s.replaceAll ("&atilde;", "&#227;");
         s = s.replaceAll ("&auml;",   "&#228;");
         s = s.replaceAll ("&brvbar;", "&#166;");
         s = s.replaceAll ("&ccedil;", "&#231;");
         s = s.replaceAll ("&cedil;",  "&#184;");
         s = s.replaceAll ("&cent;",   "&#162;");
         s = s.replaceAll ("&copy;",   "&#169;");
         s = s.replaceAll ("&curren;", "&#164;");
         s = s.replaceAll ("&deg;",    "&#176;");
         s = s.replaceAll ("&divide;", "&#247;");
         s = s.replaceAll ("&eacute;", "&#233;");
         s = s.replaceAll ("&ecirc;",  "&#234;");
         s = s.replaceAll ("&egrave;", "&#232;");
         s = s.replaceAll ("&eth;",    "&#240;");
         s = s.replaceAll ("&euml;",   "&#235;");
         s = s.replaceAll ("&frac12;", "&#189;");
         s = s.replaceAll ("&frac14;", "&#188;");
         s = s.replaceAll ("&frac34;", "&#190;");
         s = s.replaceAll ("&iacute;", "&#237;");
         s = s.replaceAll ("&icirc;",  "&#238;");
         s = s.replaceAll ("&iexcl;",  "&#161;");
         s = s.replaceAll ("&igrave;", "&#236;");
         s = s.replaceAll ("&iquest;", "&#191;");
         s = s.replaceAll ("&iuml;",   "&#239;");
         s = s.replaceAll ("&laquo;",  "&#171;");
         s = s.replaceAll ("&macr;",   "&#175;");
         s = s.replaceAll ("&micro;",  "&#181;");
         s = s.replaceAll ("&middot;", "&#183;");
         s = s.replaceAll ("&nbsp;",   "&#160;");
         s = s.replaceAll ("&not;",    "&#172;");
         s = s.replaceAll ("&ntilde;", "&#241;");
         s = s.replaceAll ("&oacute;", "&#243;");
         s = s.replaceAll ("&ocirc;",  "&#244;");
         s = s.replaceAll ("&ograve;", "&#242;");
         s = s.replaceAll ("&ordf;",   "&#170;");
         s = s.replaceAll ("&ordm;",   "&#186;");
         s = s.replaceAll ("&oslash;", "&#248;");
         s = s.replaceAll ("&otilde;", "&#245;");
         s = s.replaceAll ("&ouml;",   "&#246;");
         s = s.replaceAll ("&para;",   "&#182;");
         s = s.replaceAll ("&plusmn;", "&#177;");
         s = s.replaceAll ("&pound;",  "&#163;");
         s = s.replaceAll ("&raquo;",  "&#187;");
         s = s.replaceAll ("&reg;",    "&#174;");
         s = s.replaceAll ("&sect;",   "&#167;");
         s = s.replaceAll ("&shy;",    "&#173;");
         s = s.replaceAll ("&sup1;",   "&#185;");
         s = s.replaceAll ("&sup2;",   "&#178;");
         s = s.replaceAll ("&sup3;",   "&#179;");
         s = s.replaceAll ("&szlig;",  "&#223;");
         s = s.replaceAll ("&thorn;",  "&#254;");
         s = s.replaceAll ("&times;",  "&#215;");
         s = s.replaceAll ("&uacute;", "&#250;");
         s = s.replaceAll ("&ucirc;",  "&#251;");
         s = s.replaceAll ("&ugrave;", "&#249;");
         s = s.replaceAll ("&uml;",    "&#168;");
         s = s.replaceAll ("&uuml;",   "&#252;");
         s = s.replaceAll ("&yacute;", "&#253;");
         s = s.replaceAll ("&yen;",    "&#165;");
         s = s.replaceAll ("&yuml;",   "&#255;");
      }

      // replace in others (just in case, there shouldn't be any)
      m = CHAR_PATTERN.matcher (s);
      if (m.find())
         s = s.replaceAll ("&[a-zA-Z]+;", "-");

      // replace all the UNICODE with the actual character
      while ((m = UNICODE_PATTERN.matcher (s)).find())
      {
         int uni = Integer.parseInt (m.group (1));
         s = s.replaceAll (m.group(), "" + ((char) uni));
      }

      return s;
   }
}
