package database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public interface Database {
    public Connection getConnection() throws SQLException;

    public void insert(List<DatabaseData> synchronizedColumns, String tableName, LinkedList<String> columnNames);

    public List<DatabaseData> select(String tableName, LinkedList<String> columnNames);

    public enum DatabaseType {LOCAL, REMOTE}
}
