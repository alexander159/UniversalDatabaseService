package database;

import utils.Constants;
import utils.LoggingJUL;
import utils.SyncProperties;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MySQLDatabase implements Database {
    private Database.DatabaseType dbType;

    public MySQLDatabase(Database.DatabaseType dbType) {
        this.dbType = dbType;
    }

    @Override
    public Connection getConnection() throws SQLException {
        // jdbc:mysql://<host>[:<port>]/<database_name>
        return DriverManager.getConnection(
                String.format("jdbc:mysql://%s:%s/%s",
                        ((dbType == Database.DatabaseType.LOCAL) ? SyncProperties.getSyncProp().getProperty(Constants.SyncPropFile.LOCAL_DB_HOST) : SyncProperties.getSyncProp().getProperty(Constants.SyncPropFile.REMOTE_DB_HOST)),
                        ((dbType == Database.DatabaseType.LOCAL) ? SyncProperties.getSyncProp().getProperty(Constants.SyncPropFile.LOCAL_DB_PORT) : SyncProperties.getSyncProp().getProperty(Constants.SyncPropFile.REMOTE_DB_PORT)),
                        ((dbType == Database.DatabaseType.LOCAL) ? SyncProperties.getSyncProp().getProperty(Constants.SyncPropFile.LOCAL_DB_NAME) : SyncProperties.getSyncProp().getProperty(Constants.SyncPropFile.REMOTE_DB_NAME))),
                ((dbType == Database.DatabaseType.LOCAL) ? SyncProperties.getSyncProp().getProperty(Constants.SyncPropFile.LOCAL_DB_USER) : SyncProperties.getSyncProp().getProperty(Constants.SyncPropFile.REMOTE_DB_USER)),
                ((dbType == Database.DatabaseType.LOCAL) ? SyncProperties.getSyncProp().getProperty(Constants.SyncPropFile.LOCAL_DB_PASSWORD) : SyncProperties.getSyncProp().getProperty(Constants.SyncPropFile.REMOTE_DB_PASSWORD)));
    }

    @Override
    public void insert(List<DatabaseData> synchronizedColumns) {
        String[] columns = SyncProperties.getSyncProp().getProperty(Constants.SyncPropFile.LOCAL_DB_TABLE_COLUMNS).replaceAll(" ", "").split(",");
        String columnsSql = "";
        for (int i = 0; i < columns.length; i++) {
            if (i != columns.length - 1) {
                columnsSql += columns[i] + ", ";
            } else {
                columnsSql += columns[i];
            }
        }

        String sql = String.format("INSERT INTO %s (%s) VALUES (%s);",
                ((dbType == Database.DatabaseType.LOCAL) ? SyncProperties.getSyncProp().getProperty(Constants.SyncPropFile.LOCAL_DB_TABLE) : SyncProperties.getSyncProp().getProperty(Constants.SyncPropFile.REMOTE_DB_TABLE)),
                columnsSql,
                columnsSql.replaceAll("\\w+", "?"));

        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            for (DatabaseData data : synchronizedColumns) {
                for (int i = 0; i < data.getRow().length; i++) {
                    ps.setString(i + 1, data.getRow()[i]);
                }
                LoggingJUL.getLogger().info(() -> ps.toString().substring(ps.toString().indexOf(": ") + 2));
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            LoggingJUL.getLogger().throwing(getClass().getName(), new Object() {
            }.getClass().getEnclosingMethod().getName(), e);
            e.printStackTrace();
        }
    }

    @Override
    public List<DatabaseData> select() {
        List<DatabaseData> result = new ArrayList<>();

        String[] columns = SyncProperties.getSyncProp().getProperty(Constants.SyncPropFile.LOCAL_DB_TABLE_COLUMNS).replaceAll(" ", "").split(",");
        String columnsSql = "";
        for (int i = 0; i < columns.length; i++) {
            if (i != columns.length - 1) {
                columnsSql += columns[i] + ", ";
            } else {
                columnsSql += columns[i];
            }
        }

        String sql = String.format("SELECT %s FROM %s",
                columnsSql,
                ((dbType == Database.DatabaseType.LOCAL) ? SyncProperties.getSyncProp().getProperty(Constants.SyncPropFile.LOCAL_DB_TABLE) : SyncProperties.getSyncProp().getProperty(Constants.SyncPropFile.REMOTE_DB_TABLE)));
        LoggingJUL.getLogger().info(() -> sql);

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                DatabaseData databaseData = new DatabaseData(new String[columns.length]);
                for (String column : columns) {
                    databaseData.add(rs.getString(column));
                }
                result.add(databaseData);
            }
        } catch (SQLException e) {
            LoggingJUL.getLogger().throwing(getClass().getName(), new Object() {
            }.getClass().getEnclosingMethod().getName(), e);
            e.printStackTrace();
        }

        return result;
    }
}
