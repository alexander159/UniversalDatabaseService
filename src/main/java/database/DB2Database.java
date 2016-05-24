package database;

import utils.Constants;
import utils.LoggingJUL;
import utils.SyncProperties;

import java.sql.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;

public class DB2Database implements Database {
    private Database.DatabaseType dbType;

    public DB2Database(Database.DatabaseType dbType) {
        this.dbType = dbType;
        try {
            Class.forName("com.ibm.db2.jcc.DB2Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        //jdbc:db2://<host>[:<port>]/<database_name>
        return DriverManager.getConnection(
                String.format("jdbc:db2://%s:%s/%s",
                        ((dbType == Database.DatabaseType.LOCAL) ? SyncProperties.getInstance().getSyncProp().getProperty(Constants.SyncPropFile.LOCAL_DB_HOST) : SyncProperties.getInstance().getSyncProp().getProperty(Constants.SyncPropFile.REMOTE_DB_HOST)),
                        ((dbType == Database.DatabaseType.LOCAL) ? SyncProperties.getInstance().getSyncProp().getProperty(Constants.SyncPropFile.LOCAL_DB_PORT) : SyncProperties.getInstance().getSyncProp().getProperty(Constants.SyncPropFile.REMOTE_DB_PORT)),
                        ((dbType == Database.DatabaseType.LOCAL) ? SyncProperties.getInstance().getSyncProp().getProperty(Constants.SyncPropFile.LOCAL_DB_NAME) : SyncProperties.getInstance().getSyncProp().getProperty(Constants.SyncPropFile.REMOTE_DB_NAME))),
                ((dbType == Database.DatabaseType.LOCAL) ? SyncProperties.getInstance().getSyncProp().getProperty(Constants.SyncPropFile.LOCAL_DB_USER) : SyncProperties.getInstance().getSyncProp().getProperty(Constants.SyncPropFile.REMOTE_DB_USER)),
                ((dbType == Database.DatabaseType.LOCAL) ? SyncProperties.getInstance().getSyncProp().getProperty(Constants.SyncPropFile.LOCAL_DB_PASSWORD) : SyncProperties.getInstance().getSyncProp().getProperty(Constants.SyncPropFile.REMOTE_DB_PASSWORD)));
    }

    @Override
    public void insert(HashSet<DatabaseData> synchronizedColumns, String tableName, LinkedList<String> columnNames) {
        String[] columns = Arrays.copyOf(columnNames.toArray(), columnNames.toArray().length, String[].class);
        String columnsSql = "";
        for (int i = 0; i < columns.length; i++) {
            if (i != columns.length - 1) {
                columnsSql += columns[i] + ", ";
            } else {
                columnsSql += columns[i];
            }
        }

        String sql = String.format("INSERT INTO %s (%s) VALUES (%s)",
                tableName,
                columnsSql,
                columnsSql.replaceAll("\\w+", "?"));

        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            for (DatabaseData data : synchronizedColumns) {
                String logsSql = sql.substring(0, sql.lastIndexOf("(") + 1);
                for (int i = 0; i < data.getRow().length; i++) {
                    if (data.getRow()[i] == null) {
                        ps.setObject(i + 1, null);
                        logsSql += "null, ";
                    } else {
                        ps.setString(i + 1, data.getRow()[i]);
                        logsSql += data.getRow()[i] + ", ";
                    }
                }
                LoggingJUL.getInstance().getLogger().info(logsSql.substring(0, logsSql.lastIndexOf(",")) + ")");
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            LoggingJUL.getInstance().getLogger().throwing(getClass().getName(), new Object() {
            }.getClass().getEnclosingMethod().getName(), e);
            e.printStackTrace();
        }
    }

    @Override
    public HashSet<DatabaseData> select(String tableName, LinkedList<String> columnNames) {
        HashSet<DatabaseData> result = new HashSet<>();

        String[] columns = Arrays.copyOf(columnNames.toArray(), columnNames.toArray().length, String[].class);
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
                tableName);
        LoggingJUL.getInstance().getLogger().info(() -> sql);

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
            LoggingJUL.getInstance().getLogger().throwing(getClass().getName(), new Object() {
            }.getClass().getEnclosingMethod().getName(), e);
            e.printStackTrace();
        }

        return result;
    }
}
