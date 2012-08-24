package str;

import java.util.ArrayList;

public class TokenizedString extends ArrayList<String>
{
   private static final long serialVersionUID = 0;

   private String separator;

   public TokenizedString (final String s, final String separator)
   {
      this.separator = separator;
      String[] tokens = Token.tokenize (s, separator);
      for (int i = 0; i < tokens.length; i++)
         add (tokens[i]);
   }

   @Override
   public Object clone()
   {
      TokenizedString clone = (TokenizedString) super.clone();
      clone.separator = separator;
      return clone;
   }

   @Override
   public String toString()
   {
      StringBuilder buf = new StringBuilder (getFirst());
      int len = size();
      for (int i = 1; i < len; i++)
         buf.append (separator + get (i));
      return buf.toString();
   }

   public String getFirst()
   {
      return get (0);
   }

   public String getLast()
   {
      return get (size() - 1);
   }

   public String getAllButLast()
   {
      int len = size() - 1;
      if (len == 0)
         return null;

      StringBuilder buf = new StringBuilder (getFirst());
      for (int i = 1; i < len; i++)
         buf.append (separator + get (i));
      return buf.toString();
   }

   public TokenizedString withoutFirst()
   {
      TokenizedString ts = ((TokenizedString) clone());
      ts.removeFirst();
      return (ts.size() > 0) ? ts : null;
   }

   public TokenizedString withoutLast()
   {
      TokenizedString ts = ((TokenizedString) clone());
      ts.removeLast();
      return (ts.size() > 0) ? ts : null;
   }

   public String removeFirst()    // pop
   {
      return remove (0);
   }

   public String removeLast()
   {
      return remove (size() - 1);
   }

   // static convenience methods

   public static String getAllButLast (final String s, final String separator)
   {
      TokenizedString ts = new TokenizedString (s, separator);
      return ts.getAllButLast();
   }

   public static void main (final String[] args)
   {
      TokenizedString ts = new TokenizedString ("OnlyOne", "/");
      System.out.println ("\nOne Token");
      System.out.println ("TokenizedString = " + ts);
      System.out.println ("getFirst        = " + ts.getFirst());
      System.out.println ("getLast         = " + ts.getLast());
      System.out.println ("getAllButLast   = " + ts.getAllButLast());

      ts = new TokenizedString ("First/2/3/Last", "/");
      System.out.println ("\nFour Tokens");
      System.out.println ("TokenizedString = " + ts);
      System.out.println ("getFirst        = " + ts.getFirst());
      System.out.println ("getLast         = " + ts.getLast());
      System.out.println ("getAllButLast   = " + ts.getAllButLast());

      TokenizedString t2 = ts.withoutFirst();
      System.out.println ("\nwithoutFirst");
      System.out.println ("TokenizedString = " + t2);
      System.out.println ("getFirst        = " + t2.getFirst());
      System.out.println ("getLast         = " + t2.getLast());

      t2 = ts.withoutLast();
      System.out.println ("\nwithoutLast");
      System.out.println ("TokenizedString = " + t2);
      System.out.println ("getFirst        = " + t2.getFirst());
      System.out.println ("getLast         = " + t2.getLast());
   }
}
