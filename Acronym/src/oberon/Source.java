package oberon;

import java.awt.Color;

public enum Source
{
   User     (new Color (100, 255, 255)), // light cyan
   Explicit (new Color (225, 225, 255)), // light blue
   Database (new Color (225, 255, 225)), // light green
   Document (new Color (225, 245, 245)), // light blue-green
   WebGovt  (new Color (255, 255, 225)), // light yellow
   WebOther (new Color (255, 235, 225)), // light orange
   Unknown  (new Color (240, 240, 240)), // light grey
   Empty    (new Color (255, 255, 255)); // white
   
   private Color color;
   
   private Source (final Color color)
   {
      this.color = color;
   }
   
   public Color getColor()
   {
      return color;
   }
}
