package lotro.ww;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class GambitData
{
   private static SortedMap<String, Gambit> gambits = new TreeMap<String, Gambit>();
   
   public static Map<String, Gambit> load()
   {
      // name, builders, range, max targets, offense, defense, heal, threat, other
      addGambit("Adroit Maneuver", "RYGR", 58, 2.5f, 1, "5", null, null, null, "+Attack Speed");
      addGambit("Aggression", "YGRY", 62, 2.5f, 1, "4", null, null, "Fellowship Leech, Moderate", null);
      addGambit("Boar's Rush", "RYRY", 44, 2.5f, 1, "6 (+%Crit)", null, null, null, "25% Fear; ?% Daze");
      addGambit("Brink of Victory", "YGY", 22,2.5f, 1, "2 (DoT)", "+Evade", null, "Moderate", null);
      addGambit("Celebration of Skill", "GRGR", 40, 2.5f, 1, "4", "+Block", "HoT", "Moderate?", null);
      addGambit("Combination Strike", "RYR", 28, 2.5f, 1, "9", null, null, null, null);
      addGambit("Conviction", "GYGYG", 54, 30f, 0, null, null, "Fellowship HoT", "Fellowship Leech, Moderate", null);
      addGambit("Dance of War", "GYGY", 42, 0, 0, "4", "+Evade/CritDef/Mit", null, "Fellowship Leech, Moderate", null);
      addGambit("Defensive Strike", "GG", 2, 2.5f, 1, "4", "+Block", null, null, "Potency");
      addGambit("Deflection", "GYR", 50, 2.5f, 1, "1", null, null, "-Great", null);
      addGambit("Deft Strike", "RR", 1, 2.5f, 1, "5", null, null, null, "Potency");
      addGambit("Desolation", "YGYGY", 56, 6.2f, 3, "AoE DoT", null, null, null, "25% Fear");
      addGambit("Exultation of Battle", "YRGYG", 60, 6.2f, 10, "AoE DoT", null, "AoE Leech", "AoE ToT", null);
      addGambit("Fierce Resolve", "YRG", 26, 6.2f, 10, "AoE DoT", null, "AoE Leech", null, null);
      addGambit("Goad", "YY", 4, 6, 3, "2 (DoT)", null, null, "AoE", "Potency");
      addGambit("Impressive Flourish", "GY", 9, 2.5f, 0, "1 (DoT)", "+CritDef/Mit", "HoT", null, null);
      addGambit("Maddening Strike", "GYG", 16, 2.5f, 1, "4", "+CritDef/Mit", null, "Fellowship Leech, Slight", null);
      addGambit("Mighty Blow", "RGYR", 38, 2.5f, 1, "6 (-P/E)", null, null, null, null);
      addGambit("Offensive Strike", "RY", 10, 2.5f, 1, "8", null, null, "Moderate?", null);
      addGambit("Onslaught", "RGR", 32, 2.5f, 1, "X", null, null, null, "Interrupt");
      addGambit("Persevere", "GR", 6, 2.5f, 1, "4", "+Block", "HoT", "Moderate?", null);
      addGambit("Piercing Strike", "YRY", 30, 2.5f, 1, "5", null, null, "Moderate", null);
      addGambit("Power Attack", "RGY", 18, 2.5f, 1, "6 (-block?)", null, null, null, null);
      addGambit("Precise Blow", "YR", 12, 2.5f, 1, "4", null, null, "Moderate+", null);
      addGambit("Resolution", "YRGY", 72, -1, -1, "AoE DoT", null, "Aoe Leech", null, null);
      addGambit("Restoration", "GRGRG", 74, -1, -1, "4", null, "HoT", null, null);
      addGambit("Reversal", "RYG", 52, 2.5f, 1, "5", null, null, null, "Remove Corruption");
      addGambit("Safeguard", "GRG", 24, 2.5f, 1, "1", "+Block", "HoT", "Moderate?", null);
      addGambit("Shield Mastery", "GRYG", 34, 0, 0, null, "+Block/Evade", null, null, null);
      addGambit("Shield Tactics", "GYRG", 68, -1, -1, null, "+Mit", null, null, "-Stun");
      addGambit("Shield Up", "GRY", 20, 0, 0, null, "+Block/Evade", null, null, null);
      addGambit("Spear of Virtue", "YRYR", 46, 2.5f, 1, "7", null, null, "Great", null);
      addGambit("Surety of Death", "YGYG", 48, 2.5f, 1, "7 DoT", "+Evade", null, "Great", null);
      addGambit("The Boot", "RG", 3, 2.5f, 1, "2", null, null, null, "Interrupt; 25% Daze");
      addGambit("The Dark Before Dawn", "RGRYR", 64, 2.5f, 1, "X", null, null, null, "+Power");
      addGambit("Unerring Strike", "RGYRG", 50, 2.5f,1, "6 DoT (-avoid)", null, null, null, null);
      addGambit("Wall of Steel", "RGRG", 36, 2.5f, 1, "X", "+Parry", null, null, "Interrupt");
      addGambit("War-Cry", "YG", 13, 10.2f, 10, "AoE DoT", "+Evade", "HoT", "AoE", null);
      addGambit("Warden's Triumph", "RYGRY", 70, -1, -1, "?", "?", "?", "?", "+Stun Immunity");
      
      return gambits;
   }

   private static void addGambit(final String name, final String builders, final int level,
         final float range, final int maxTargets,
         final String offense, final String defense, final String heal,
         final String threat, final String other)
   {
      Gambit gambit = new Gambit(name, builders, level, range, maxTargets, offense, defense, heal, threat, other);
      gambits.put(name, gambit);
   }
   
   public static Gambit findByKeys(final String builders)
   {
      for (Gambit gambit : gambits.values())
         if (builders.equals(gambit.getBuilders()))
            return gambit;
      return null;
   }
}
