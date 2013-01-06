package lotro.ww;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import str.Token;

public class Sequence
{
   private List<String> builders = new ArrayList<String>(); 
   private Gambit gambit;

   public Sequence(final String sequence) // comma-separated list of builders
   {
      this.builders = Arrays.asList(Token.tokenize(sequence, ","));
      this.gambit = GambitData.findByKeys(sequence.replaceAll(",", ""));
      if (gambit == null)
         System.err.println("Invalid gambit sequence: " + sequence);
   }
   
   public List<String> getBuilders()
   {
      return builders;
   }
   
   public Gambit getGambit()
   {
      return gambit;
   }
}
