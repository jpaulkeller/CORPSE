package lotro.models;

public class Assignment
{
   private boolean organized;
   private boolean needed;
   private boolean assigned;
   
   public boolean isOrganized()
   {
      return organized;
   }

   public boolean isNeeded()
   {
      return needed;
   }

   public boolean isAssigned()
   {
      return assigned;
   }

   public void setOrganized (final boolean organized)
   {
      this.organized = organized;
   }

   public void setNeeded (final boolean needed)
   {
      this.needed = needed;
   }

   public void setAssigned (final boolean assigned)
   {
      this.assigned = assigned;
   }

   @Override
   public String toString()
   {
      return "N:" + needed + " A:" + assigned + " O:" + organized;
   }
}
