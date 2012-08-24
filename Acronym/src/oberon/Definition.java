package oberon;

public class Definition implements Comparable<Definition>
{
   public static final String EMPTY = " ";
   
   private String text;
   private Source source;

   public Definition (final String text, final Source source)
   {
      this.text = text;
      this.source = source;
   }

   public String getText()
   {
      return text;
   }
   
   public Source getSource()
   {
      return source;
   }

   @Override
   public String toString()
   {
      return text;
   }

   @Override
   public boolean equals (final Object obj)
   {
      if (obj instanceof Definition)
         return getText().equals (((Definition) obj).getText());
      return false;
   }

   @Override
   public int hashCode()
   {
      return getText().hashCode();
   }

   public int compareTo (final Definition other)
   {
      if (getSource() != other.getSource())
         return getSource().ordinal() - other.getSource().ordinal();
      int len1 = getText().length();
      int len2 = other.getText().length();
      if (len1 != len2)
         return len1 - len2;
      return getText().compareTo (other.getText());
   }
}
