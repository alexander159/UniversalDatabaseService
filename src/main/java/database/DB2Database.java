package database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class DB2Database implements Database {
    private Database.DatabaseType dbType;

    public DB2Database(Database.DatabaseType dbType) {
        this.dbType = dbType;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return null;
    }

    @Override
    public void insert(List<DatabaseData> synchronizedColumns) {

    }

    @Override
    public List<DatabaseData> select() {
        return null;
    }
}
