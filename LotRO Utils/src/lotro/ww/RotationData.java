package lotro.ww;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class RotationData
{
   private static Map<String, Gambit> gambits = GambitData.load();
   private static List<Rotation> rotations = new ArrayList<Rotation>();

   public static List<Rotation> load()
   {
      addRotation("Single Target DPS", "GR", "GY", "GG", "RG,R", "YG,Y,G", "RY", "YR"); 
      addRotation("Healing", "GR", "Y,RG", "GG", "GY,G,YG", "RY", "R,YR"); 
      addRotation("AoE DPS", "RY,GR", "Y,G", "Y,RG", "YY", "YR,G,YG", "Y,G,Y,GY"); 
      addRotation("Threat", "Y,G", "YY", "Y,RG,YG", "YR", "GY,G,Y", "GR"); 
      /*
      addRotation("Healing", "Persevere", "Fierce Resolve", "Defensive Strike", "Conviction", "Offensive Strike", "Combination Strike");
      addRotation("AoE DPS", "Adroit Maneuver", "War-Cry", "Fierce Resolve", "Goad", "Exultation of Battle", "Desolation");
      addRotation("Threat", "War-Cry", "Goad", "Exultation of Battle", "Precise Blow", "Dance of War", "Persevere");
      */
      return rotations;
   }
   
   public static void addRotation(final String name, final String... keys)
   {
      Rotation rotation = new Rotation(name);
      for (String builders : keys)
         rotation.addGambit(builders);
      rotations.add(rotation);
   }
   
   public static void show(final boolean details)
   {
      for (Rotation rotation : rotations)
      {
         System.out.print(rotation.getName() + ": ");
         for (Sequence sequence : rotation.getSequences())
         {
            for (String builder : sequence.getBuilders())
               System.out.print(Gambit.getKey(builder));
            System.out.print(", ");
         }
         System.out.println();
         
         if (details)
            for (Sequence sequence : rotation.getSequences())
               System.out.println(" > " + sequence.getGambit());
      }
   }
   
   public static void main(final String[] args)
   {
      RotationData.load();
      RotationData.show(false);
      RotationData.show(true);
   }
}
