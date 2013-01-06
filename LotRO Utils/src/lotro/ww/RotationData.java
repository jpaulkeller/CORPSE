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
      /*
      keyMapping.put("RG", "1");
      keyMapping.put("GY", "2");
      keyMapping.put("YG", "3");
      keyMapping.put("RY", "4");
      keyMapping.put("GR", "5");
      keyMapping.put("YR", "6");
      keyMapping.put("R", "7");
      keyMapping.put("G", "8");
      keyMapping.put("Y", "9");
      keyMapping.put("RR", "G10");
      keyMapping.put("GG", "G11");
      keyMapping.put("YY", "G12");

      keyMapping.put("RR", "1");
      keyMapping.put("GG", "2");
      keyMapping.put("YY", "3");
      keyMapping.put("R", "4");
      keyMapping.put("G", "5");
      keyMapping.put("Y", "6");
      keyMapping.put("RG", "7");
      keyMapping.put("GY", "8");
      keyMapping.put("YG", "9");
      keyMapping.put("RY", "A");
      keyMapping.put("GR", "B");
      keyMapping.put("YR", "C");
      
      */

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
