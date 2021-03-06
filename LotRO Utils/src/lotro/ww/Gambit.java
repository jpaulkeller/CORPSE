package lotro.ww;

import java.util.ArrayList;
import java.util.List;

import model.CrossMap;

public class Gambit
{
   public static CrossMap<String, String> keyMapping = new CrossMap<String, String>();
   static
   {
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
   }
   
   private String name;
   private String builders;
   private String basic;
   private List<String> keys = new ArrayList<String>();
   
   private int level;
   private float range;
   private int maxTargets;
   private String offense;
   private String defense;
   private String heal;
   private String threat;
   private String other;   
   
   public Gambit(final String name, final String builders, final int level,
         final float range, final int maxTargets,
         final String offense, final String defense, final String heal,
         final String threat, final String other)
   {
      this.name = name;
      this.builders = builders;
      determineKeys();
      
      this.level = level;
      this.range = range;
      this.maxTargets = maxTargets;
      this.offense = offense;
      this.defense = defense;
      this.heal = heal;
      this.threat = threat;
      this.other = other;
   }
   
   public String getName()
   {
      return name;
   }
   
   public String getBuilders()
   {
      return builders;
   }
   
   public String getBasic()
   {
      return basic;
   }
   
   public List<String> getKeys()
   {
      return keys;
   }

   public int getLevel()
   {
      return level;
   }

   public float getRange()
   {
      return range;
   }

   public int getMaxTargets()
   {
      return maxTargets;
   }

   public String getOffense()
   {
      return offense;
   }

   public String getDefense()
   {
      return defense;
   }

   public String getHeal()
   {
      return heal;
   }

   public String getThreat()
   {
      return threat;
   }

   public String getOther()
   {
      return other;
   }

   private List<List<String>> getPermutations()
   {
      List<List<String>> permutations = new ArrayList<List<String>>();

      List<String> permutation;
      if (builders.length() == 2)
      {
         // 2
         permutation = new ArrayList<String>();
         permutation.add(builders);
         permutations.add(permutation);
      }
      else if (builders.length() == 3)
      {
         // 1-2
         permutation = new ArrayList<String>();
         permutation.add(builders.substring(0, 1));
         permutation.add(builders.substring(1));
         permutations.add(permutation);

         // 2-1
         permutation = new ArrayList<String>();
         permutation.add(builders.substring(0, 2));
         permutation.add(builders.substring(2));
         permutations.add(permutation);
      }
      else if (builders.length() == 4)
      {
         // 2-2
         permutation = new ArrayList<String>();
         permutation.add(builders.substring(0, 2));
         permutation.add(builders.substring(2));
         if (!permutation.get(0).equals(permutation.get(1)))
            permutations.add(permutation);
         
         // 1-1-2
         permutation = new ArrayList<String>();
         permutation.add(builders.substring(0, 1));
         permutation.add(builders.substring(1, 2));
         permutation.add(builders.substring(2));
         permutations.add(permutation);

         // 1-2-1
         permutation = new ArrayList<String>();
         permutation.add(builders.substring(0, 1));
         permutation.add(builders.substring(1, 3));
         permutation.add(builders.substring(3));
         permutations.add(permutation);

         // 2-1-1
         permutation = new ArrayList<String>();
         permutation.add(builders.substring(0, 2));
         permutation.add(builders.substring(2, 3));
         permutation.add(builders.substring(3));
         permutations.add(permutation);
      }
      else if (builders.length() == 5)
      {
         // 1-2-2
         permutation = new ArrayList<String>();
         permutation.add(builders.substring(0, 1));
         permutation.add(builders.substring(1, 3));
         permutation.add(builders.substring(3));
         if (!permutation.get(1).equals(permutation.get(2)))
            permutations.add(permutation);

         // 2-1-2
         permutation = new ArrayList<String>();
         permutation.add(builders.substring(0, 2));
         permutation.add(builders.substring(2, 3));
         permutation.add(builders.substring(3));
         if (!permutation.get(0).equals(permutation.get(2)))
            permutations.add(permutation);

         // 2-2-1
         permutation = new ArrayList<String>();
         permutation.add(builders.substring(0, 2));
         permutation.add(builders.substring(2, 4));
         permutation.add(builders.substring(4));
         if (!permutation.get(0).equals(permutation.get(1)))
            permutations.add(permutation);
         
         // 1-1-1-2
         permutation = new ArrayList<String>();
         permutation.add(builders.substring(0, 1));
         permutation.add(builders.substring(1, 2));
         permutation.add(builders.substring(2, 3));
         permutation.add(builders.substring(3));
         permutations.add(permutation);
         
         // 1-1-2-1
         permutation = new ArrayList<String>();
         permutation.add(builders.substring(0, 1));
         permutation.add(builders.substring(1, 2));
         permutation.add(builders.substring(2, 4));
         permutation.add(builders.substring(4));
         permutations.add(permutation);
         
         // 1-2-1-1
         permutation = new ArrayList<String>();
         permutation.add(builders.substring(0, 1));
         permutation.add(builders.substring(1, 3));
         permutation.add(builders.substring(3, 4));
         permutation.add(builders.substring(4));
         permutations.add(permutation);
         
         // 2-1-1-1
         permutation = new ArrayList<String>();
         permutation.add(builders.substring(0, 2));
         permutation.add(builders.substring(2, 3));
         permutation.add(builders.substring(3, 4));
         permutation.add(builders.substring(4));
         permutations.add(permutation);
      }

      // split into single keys for basic builders (except for Potency gambits)
      if (builders.charAt(0) != builders.charAt(1) && builders.length() < 5)
      {
         permutation = new ArrayList<String>();
         for (char c : builders.toCharArray())
            permutation.add(c + "");
         permutations.add(permutation);
      }
         
      return permutations;
   }

   public static String getKey(final String builder)
   {
      return keyMapping.get(builder);
   }
   
   private void determineKeys()
   {
      basic = "";
      for (int i = 0; i < builders.length(); i++)
         basic += keyMapping.get(builders.subSequence(i, i + 1));
      
      List<List<String>> permutations = getPermutations();
      for (List<String> permutation : permutations)
      {
         String keySequence = "";
         for (String skill : permutation)
            keySequence += keyMapping.get(skill);
         System.out.println(permutation + " -- " + keySequence);
         keys.add(keySequence);
      }
   }
   
   public String getToolTip()
   {
      StringBuilder sb = new StringBuilder("<html>");
      
      sb.append("<center><b>" + name + " (" + level + ")</b></center><br/>");
      sb.append(" Range: " + range + "m ");
      if (maxTargets > 1)
         sb.append(", Targets: " + maxTargets);
      sb.append("<br/>");
      
      if (offense != null)
         sb.append(" Offense: " + offense + "<br/>");
      if (defense != null)
         sb.append(" Defense: " + defense + "<br/>");
      if (heal != null)
         sb.append(" Heal: " + heal + "<br/>");
      if (threat != null)
         sb.append(" Threat: " + threat + "<br/>");
      if (other != null)
         sb.append(" Other: " + other + "<br/>");
      
      sb.append("<html>");      
      
      return sb.toString();
   }
   
   public String toString()
   {
      StringBuilder sb = new StringBuilder();
      
      sb.append("[" + level + "] ");
      sb.append(name);
      sb.append(" (" + builders + "):");
      if (range > 2.5)
         sb.append(" " + range + "m ");
      if (maxTargets > 1)
         sb.append(" #T:" + maxTargets + " ");
      if (offense != null)
         sb.append(" Dmg:" + offense);
      if (defense != null)
         sb.append("; " + defense);
      if (heal != null)
         sb.append("; " + heal);
      if (threat != null)
         sb.append("; Thr: " + threat);
      if (other != null)
         sb.append("; " + other);
      
      return sb.toString();
   }
}
