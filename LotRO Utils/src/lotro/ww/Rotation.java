package lotro.ww;

import java.util.ArrayList;
import java.util.List;

public class Rotation
{
   private String name;
   private List<Sequence> sequences = new ArrayList<Sequence>();
   
   public Rotation (final String name)
   {
      this.name = name;
   }
   
   public void addGambit(final String builder)
   {
      sequences.add(new Sequence(builder));
   }
   
   public String getName()
   {
      return name;
   }
   
   public List<Sequence> getSequences()
   {
      return sequences;
   }
}
