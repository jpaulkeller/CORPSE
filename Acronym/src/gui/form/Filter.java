package gui.form;

import java.io.Serializable;

/** 
 * This interface provides an API for form item filters. */

public interface Filter extends Serializable
{
   /**
    * Filters the data by returning a value (possibly an null character)
    * based on the input character.
    *
    * Usually, this method would either return the given character (if
    * the filter criteria were satisfied), or a null character (if
    * not), or possibly a different character (if the Filter was
    * acting as a transformer). */

   char process (char input);

   /**
    * Filters the data by returning a value (possibly an empty string)
    * based on the input string.
    *
    * Usually, this method would either return a subset of the string,
    * or possibly a transformed (e.g. uppercased) version of the input
    * data. */

   String process (String input);

   /**
    * Enable or disable audio feedback (a single beep) when filtering
    * data. */

   void enableAudio (boolean status);
}
