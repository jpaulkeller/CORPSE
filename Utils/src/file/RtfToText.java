package file;

/*
 * RtfToText.java - Returns the plain text version of RTF documents.
 * Copyright (C) 2008 Robert Futrell
 * robert_futrell at users.sourceforge.net
 * http://fifesoft.com/rsyntaxtextarea
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

/**
 * Gets the plain text version of RTF documents.
 * 
 * This is used by <code>RtfTransferable</code> to return the plain text version
 * of the transferable when the receiver does not support RTF.
 */
public final class RtfToText
{
   private Reader r;
   private StringBuffer sb;
   private StringBuffer controlWord;
   private int blockCount;
   private boolean inControlWord;

   private RtfToText (final Reader r)
   {
      this.r = r;
      sb = new StringBuffer();
      controlWord = new StringBuffer();
      blockCount = 0;
      inControlWord = false;
   }

   /**
    * Converts the RTF text read from this converter's <code>Reader</code> into
    * plain text. It is the caller's responsibility to close the reader after
    * this method is called.
    * 
    * @return The plain text.
    */
   private String convert() throws IOException
   {
      // Skip over first curly brace as the whole file is in '{' and '}'
      int i = r.read();
      if (i != '{')
         throw new IOException ("Invalid RTF file");

      while ((i = r.read()) != -1)
      {
         char ch = (char) i;
         switch (ch)
         {
         case '{':
            if (inControlWord && controlWord.length() == 0)
            {
               sb.append ('{');
               controlWord.setLength (0);
               inControlWord = false;
            }
            else
               blockCount++;
            break;
            
         case '}':
            if (inControlWord && controlWord.length() == 0)
            {
               sb.append ('}');
               controlWord.setLength (0);
               inControlWord = false;
            }
            else
               blockCount--;
            break;
            
         case '\\':
            if (blockCount == 0)
            {
               if (inControlWord)
               {
                  if (controlWord.length() == 0)
                  {
                     sb.append ('\\');
                     controlWord.setLength (0);
                     inControlWord = false;
                  }
                  else
                     endControlWord();
               }
               inControlWord = true;
            }
            break;
            
         case ' ':
            if (blockCount == 0)
            {
               if (inControlWord)
                  endControlWord();
               else
                  sb.append (' ');
            }
            break;
            
         case '\r':
         case '\n':
            if (blockCount == 0)
            {
               if (inControlWord)
                  endControlWord();
               // Otherwise, ignore
            }
            break;
            
         default:
            if (blockCount == 0)
            {
               if (inControlWord)
                  controlWord.append (ch);
               else
                  sb.append (ch);
            }
            break;
         }
      }

      return sb.toString();
   }

   /**
    * Ends a control word. Checks whether it is a common one that affects the
    * plain text output (such as "<code>par</code>" or "<code>tab</code>") and
    * updates the text buffer accordingly.
    */
   private void endControlWord()
   {
      String word = controlWord.toString();
      if ("par".equals (word))
         sb.append ('\n');
      else if ("tab".equals (word))
         sb.append ('\t');
      else if (sb.length() > 0 && sb.charAt (sb.length() - 1) != ' ')
         sb.append (' ');
      controlWord.setLength (0);
      inControlWord = false;
   }

   /**
    * Converts the contents of the specified byte array representing an RTF
    * document into plain text.
    */
   public static String getPlainText (final byte[] rtf) throws IOException
   {
      return getPlainText (new ByteArrayInputStream (rtf));
   }

   /**
    * Converts the contents of the specified RTF file to plain text.
    */
   public static String getPlainText (final File file) throws IOException
   {
      return getPlainText (new BufferedReader (new FileReader (file)));
   }

   /**
    * Converts the contents of the specified input stream to plain text. The
    * input stream will be closed when this method returns.
    */
   public static String getPlainText (final InputStream in) throws IOException
   {
      return getPlainText (new InputStreamReader (in, "US-ASCII"));
   }

   /**
    * Converts the contents of the specified <code>Reader</code> to plain text.
    */
   private static String getPlainText (final Reader r) throws IOException
   {
      try
      {
         RtfToText converter = new RtfToText (r);
         return converter.convert();
      }
      finally
      {
         r.close();
      }
   }

   /**
    * Converts the contents of the specified String to plain text.
    */
   public static String getPlainText (final String rtf) throws IOException
   {
      return getPlainText (new StringReader (rtf));
   }
}
