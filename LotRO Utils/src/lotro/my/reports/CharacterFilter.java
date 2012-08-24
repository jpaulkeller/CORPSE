package lotro.my.reports;

import lotro.models.Character;

public interface CharacterFilter
{
   boolean include (final Character ch);
}
