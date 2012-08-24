package db;

import java.sql.SQLException;
import java.util.List;

public interface DAO
{
   int execute (final CharSequence sql) throws SQLException;
   List<String> getList  (final CharSequence sql) throws SQLException;
   Model getModel (final CharSequence sql) throws SQLException;
   void close();
}
