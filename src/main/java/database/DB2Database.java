package database;

import utils.Constants;
import utils.LoggingJUL;
import utils.SyncProperties;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;

public class DB2Database implements Database {
    public static final String TAG = "DB2Database";
    private DatabaseType dbType;

    public DB2Database(DatabaseType dbType) {
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
                        ((dbType == DatabaseType.LOCAL) ? SyncProperties.getInstance().getSyncProp().getProperty(Constants.SyncPropFile.LOCAL_DB_HOST) : SyncProperties.getInstance().getSyncProp().getProperty(Constants.SyncPropFile.REMOTE_DB_HOST)),
                        ((dbType == DatabaseType.LOCAL) ? SyncProperties.getInstance().getSyncProp().getProperty(Constants.SyncPropFile.LOCAL_DB_PORT) : SyncProperties.getInstance().getSyncProp().getProperty(Constants.SyncPropFile.REMOTE_DB_PORT)),
                        ((dbType == DatabaseType.LOCAL) ? SyncProperties.getInstance().getSyncProp().getProperty(Constants.SyncPropFile.LOCAL_DB_NAME) : SyncProperties.getInstance().getSyncProp().getProperty(Constants.SyncPropFile.REMOTE_DB_NAME))),
                ((dbType == DatabaseType.LOCAL) ? SyncProperties.getInstance().getSyncProp().getProperty(Constants.SyncPropFile.LOCAL_DB_USER) : SyncProperties.getInstance().getSyncProp().getProperty(Constants.SyncPropFile.REMOTE_DB_USER)),
                ((dbType == DatabaseType.LOCAL) ? SyncProperties.getInstance().getSyncProp().getProperty(Constants.SyncPropFile.LOCAL_DB_PASSWORD) : SyncProperties.getInstance().getSyncProp().getProperty(Constants.SyncPropFile.REMOTE_DB_PASSWORD)));
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
                ArrayList<String> columnPlaceholders = LoggingJUL.createTemporaryPlaceholdersArray(data.getRow().length, TAG, 0);
                String logsSql = LoggingJUL.getInsertLogSql("INSERT INTO %s (%s) VALUES (%s)", tableName, columns, columnPlaceholders);
                for (int i = 0; i < data.getRow().length; i++) {
                    if (data.getRow()[i] == null) {
                        ps.setObject(i + 1, null);
                        logsSql = logsSql.replace(columnPlaceholders.get(i), "null");
                    } else {
                        ps.setString(i + 1, data.getRow()[i]);
                        logsSql = logsSql.replace(columnPlaceholders.get(i), data.getRow()[i]);
                    }
                }
                LoggingJUL.getInstance().getLogger().info(logsSql);

                try {
                    ps.executeUpdate();
                } catch (SQLException e) {
                    update(data, tableName, columnNames);
                }
            }
        } catch (SQLException e) {
            LoggingJUL.getInstance().getLogger().throwing(getClass().getName(), new Object() {
            }.getClass().getEnclosingMethod().getName(), e);
            e.printStackTrace();
        }
    }

    @Override
    public void update(DatabaseData synchronizedColumns, String tableName, LinkedList<String> columnNames) {
        String[] columns = Arrays.copyOf(columnNames.toArray(), columnNames.toArray().length, String[].class);
        String columnsSql = "";
        for (int i = 0; i < columns.length; i++) {
            if (i != columns.length - 1) {
                columnsSql += columns[i] + " = ?, ";
            } else {
                columnsSql += columns[i] + " = ?";
            }
        }

        String sql = String.format("UPDATE %s SET %s WHERE %s = ?;",
                tableName,
                columnsSql,
                columnNames.getFirst());

        ArrayList<String> columnPlaceholders = LoggingJUL.createTemporaryPlaceholdersArray(synchronizedColumns.getRow().length + 1, TAG, 0);    //+1  for WHERE case
        String logsSql = LoggingJUL.getUpdateLogSql("UPDATE %s SET %s WHERE %s = ?;", tableName, columns, columnNames.getFirst(), columnPlaceholders);

        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < synchronizedColumns.getRow().length; i++) {
                if (synchronizedColumns.getRow()[i] == null) {
                    ps.setObject(i + 1, null);
                    logsSql = logsSql.replace(columnPlaceholders.get(i), "null");
                } else {
                    ps.setString(i + 1, synchronizedColumns.getRow()[i]);
                    logsSql = logsSql.replace(columnPlaceholders.get(i), synchronizedColumns.getRow()[i]);
                }

                if (i == synchronizedColumns.getRow().length - 1) {
                    ps.setString(i + 2, synchronizedColumns.getRow()[0]);
                    logsSql = logsSql.replace(columnPlaceholders.get(columnPlaceholders.size() - 1), synchronizedColumns.getRow()[0]);
                }
            }
            LoggingJUL.getInstance().getLogger().info(logsSql);
            ps.executeUpdate();

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
