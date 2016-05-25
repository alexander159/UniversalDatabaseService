package database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.LinkedList;

public interface Database {
    public Connection getConnection() throws SQLException;

    public void insert(HashSet<DatabaseData> synchronizedColumns, String tableName, LinkedList<String> columnNames);

    public void update(DatabaseData synchronizedColumns, String tableName, LinkedList<String> columnNames);

    public HashSet<DatabaseData> select(String tableName, LinkedList<String> columnNames);

    public enum DatabaseType {LOCAL, REMOTE}
}
