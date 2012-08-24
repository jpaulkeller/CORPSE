package gui.editors;

import java.io.File;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

public class FileFormat extends Format
{
   @Override
   public StringBuffer format (final Object obj, final StringBuffer toAppendTo,
                               final FieldPosition pos)
   {
      if (obj instanceof File)
         toAppendTo.append (((File) obj).getName());
      return toAppendTo;
   }

   @Override
   public Object parseObject (final String source, final ParsePosition pos)
   {
      pos.setIndex (source.length());
      return new File (source);
   }
}
