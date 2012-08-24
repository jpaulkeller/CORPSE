package lotro.raid;

import lotro.models.Character;

public interface SignupListener
{
   void characterAdded (final Character ch);
   void characterUpdated (final Character ch);
   void characterRemoved (final Character ch);
}
