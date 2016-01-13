package database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface Database {
    public enum DatabaseType{LOCAL, REMOTE};

    public Connection getConnection() throws SQLException;

    public void insert(List<DatabaseData> synchronizedColumns);

    public List<DatabaseData> select();
}
