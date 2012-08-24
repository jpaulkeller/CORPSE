package lotro.models;

public enum Slot
{
   Head ("H", "#DFD9A6"),
   Chest ("Ch", "#DFD9A6"),
   Legs ("Lg", "#DFD9A6"),
   Gloves ("Gl", "#DFD9A6"),
   Boots ("B", "#DFD9A6"),
   Shoulder ("Sh", "#DFD9A6"),
   Back ("Bk", "#DFD9A6"),
   Bracelet1 ("B1", "#EEFFAA"),
   Bracelet2 ("B2", "#EEFFAA"),
   Necklace ("N", "#EEFFAA"),
   Ring1 ("R1", "#EEFFAA"),
   Ring2 ("R2", "#EEFFAA"),
   Earring1 ("E1", "#EEFFAA"),
   Earring2 ("E2", "#EEFFAA"),
   Pocket ("Pk", "#EEFFAA", "Pocket1"),
   MainHand ("W1", "#FFAAAA", "Weapon_Primary"),
   OffHand ("W2", "#FFAAAA", "Weapon_Secondary"),
   Ranged ("WR", "#FFAAAA", "Weapon_Ranged"),
   Tool ("T", "#FFAAAA", "CraftTool"),
   ClassItem ("CI", "#FFAAAA", "Last");

   private String abbrev;
   private String color;
   private String tag;
   
   private Slot (final String abbrev, final String color)
   {
      this.abbrev = abbrev;
      this.color = color;
      this.tag = toString();
   }
   
   private Slot (final String abbrev, final String color, final String tag)
   {
      this.abbrev = abbrev;
      this.color = color;
      this.tag = tag;
   }
   
   public String getAbbrev()
   {
      return abbrev;
   }
   
   public String getColor()
   {
      return color;
   }
   
   public String getTag()
   {
      return tag;
   }
   
   public static Slot find (final String s)
   {
      for (Slot slot : Slot.values())
         if (s.equalsIgnoreCase (slot.getTag()))
            return slot;
      return null;
   }
   
   public static Slot parse (final String name)
   {
      if (name != null)
         for (Slot slot : Slot.values())
            if (name.equalsIgnoreCase (slot.toString()))
               return slot;

      throw new IllegalArgumentException ("Invalid Slot: " + name); 
   }   
}
