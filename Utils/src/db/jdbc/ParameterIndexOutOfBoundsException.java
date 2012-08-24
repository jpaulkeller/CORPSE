package db.jdbc;

import java.sql.SQLException;

public class ParameterIndexOutOfBoundsException extends SQLException
{
   private static final long serialVersionUID = 1L;

   public ParameterIndexOutOfBoundsException()
   {
      super();
   }

   public ParameterIndexOutOfBoundsException(final String s)
   {
      super(s);
   }
}
