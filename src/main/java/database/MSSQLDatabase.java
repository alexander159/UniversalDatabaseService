package database;

import utils.Constants;
import utils.LoggingJUL;
import utils.SyncProperties;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;

public class MSSQLDatabase implements Database {
    private static final String[] timestampFormats = {
            "yyyy-MM-dd HH:mm:ss.SS",
            "yyyy-MM-dd'T'HH:mm:ss'Z'", "yyyy-MM-dd'T'HH:mm:ssZ",
            "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSSZ", "yyyy-MM-dd HH:mm:ss",
            "MM/dd/yyyy HH:mm:ss", "MM/dd/yyyy'T'HH:mm:ss.SSS'Z'",
            "MM/dd/yyyy'T'HH:mm:ss.SSSZ", "MM/dd/yyyy'T'HH:mm:ss.SSS",
            "MM/dd/yyyy'T'HH:mm:ssZ", "MM/dd/yyyy'T'HH:mm:ss",
            "yyyy:MM:dd HH:mm:ss", "yyyyMMdd",};
    private Database.DatabaseType dbType;

    public MSSQLDatabase(Database.DatabaseType dbType) {
        this.dbType = dbType;
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public String parseTimestamp(String d) {
        String date = null;
        if (d != null) {
            for (String parse : timestampFormats) {
                SimpleDateFormat sdf = new SimpleDateFormat(parse);
                try {
                    sdf.parse(d);
                    date = d.replace(" ", "T");
                    break;
                } catch (ParseException ignore) {
                }
            }
        }
        return date;
    }

    @Override
    public Connection getConnection() throws SQLException {
        // jdbc:sqlserver://<host>[:<port>];[Instance=<instance_name>;]databaseName=<database_name>
        return DriverManager.getConnection(
                String.format("jdbc:sqlserver://%s:%s;databaseName=%s",
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
                        String date = parseTimestamp(data.getRow()[i]);
                        if (date == null) {
                            ps.setString(i + 1, data.getRow()[i]);
                        } else {
                            ps.setString(i + 1, date);
                        }

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
