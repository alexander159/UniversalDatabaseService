package universal.database.service;

import database.*;
import utils.Constants;
import utils.LoggingJUL;
import utils.SyncProperties;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Main {
    private static Timer timer;

    public static void main(String[] a) {
        LoggingJUL.getInstance().getLogger().info(() -> "Service started");

        try {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (SyncProperties.getInstance().reloadProperties() != null) {
                        synchronize();
                    }
                }
            }, 0, Integer.parseInt(SyncProperties.getInstance().getSyncProp().getProperty(Constants.SyncPropFile.SYNC_MINUTES)) * 60 * 1000);
        } catch (NumberFormatException ex) {
            LoggingJUL.getInstance().getLogger().throwing(Main.class.getName(), new Object() {
            }.getClass().getEnclosingMethod().getName(), new IOException(Constants.SYNC_PROPERTIES + " is incorrect"));
            ex.printStackTrace();
            timer.cancel();
            LoggingJUL.getInstance().getLogger().info(() -> "Service stopped");
        }
    }

    private static void synchronize() {
        Database remoteDb = null;
        if (SyncProperties.getInstance().getSyncProp().getProperty(Constants.SyncPropFile.REMOTE_DB_TYPE).equals(Constants.SyncPropFile.MYSQL)) {
            remoteDb = new MySQLDatabase(Database.DatabaseType.REMOTE);
        } else if (SyncProperties.getInstance().getSyncProp().getProperty(Constants.SyncPropFile.REMOTE_DB_TYPE).equals(Constants.SyncPropFile.ORACLE)) {
            remoteDb = new OracleDatabase(Database.DatabaseType.REMOTE);
        } else if (SyncProperties.getInstance().getSyncProp().getProperty(Constants.SyncPropFile.REMOTE_DB_TYPE).equals(Constants.SyncPropFile.MSSQL)) {
            remoteDb = new MSSQLDatabase(Database.DatabaseType.REMOTE);
        } else if (SyncProperties.getInstance().getSyncProp().getProperty(Constants.SyncPropFile.REMOTE_DB_TYPE).equals(Constants.SyncPropFile.DB2)) {
            remoteDb = new DB2Database(Database.DatabaseType.REMOTE);
        }

        Database localDb = null;
        if (SyncProperties.getInstance().getSyncProp().getProperty(Constants.SyncPropFile.LOCAL_DB_TYPE).equals(Constants.SyncPropFile.MYSQL)) {
            localDb = new MySQLDatabase(Database.DatabaseType.LOCAL);
        } else if (SyncProperties.getInstance().getSyncProp().getProperty(Constants.SyncPropFile.LOCAL_DB_TYPE).equals(Constants.SyncPropFile.ORACLE)) {
            localDb = new OracleDatabase(Database.DatabaseType.LOCAL);
        } else if (SyncProperties.getInstance().getSyncProp().getProperty(Constants.SyncPropFile.LOCAL_DB_TYPE).equals(Constants.SyncPropFile.MSSQL)) {
            localDb = new MSSQLDatabase(Database.DatabaseType.LOCAL);
        } else if (SyncProperties.getInstance().getSyncProp().getProperty(Constants.SyncPropFile.LOCAL_DB_TYPE).equals(Constants.SyncPropFile.DB2)) {
            localDb = new DB2Database(Database.DatabaseType.LOCAL);
        }

        //sync
        if (localDb != null && remoteDb != null) {
            List<DatabaseData> localData = localDb.select();
            LoggingJUL.getInstance().getLogger().info("Local " + localDb.getClass().getName() + " SELECT. Returned " + localData.size() + " rows");
            List<DatabaseData> remoteData = remoteDb.select();
            LoggingJUL.getInstance().getLogger().info("Remote " + remoteDb.getClass().getName() + " SELECT. Returned " + remoteData.size() + " rows");

            localData.removeAll(remoteData);
            LoggingJUL.getInstance().getLogger().info(localData.size() + " new records found in local " + localDb.getClass().getName());
            if (localData.size() != 0) {
                remoteDb.insert(localData);
                LoggingJUL.getInstance().getLogger().info(localData.size() + " new records inserted to remote" + remoteDb.getClass().getName());
            }
        } else {
            LoggingJUL.getInstance().getLogger().throwing(Main.class.getName(), new Object() {
            }.getClass().getEnclosingMethod().getName(), new NullPointerException("Local or Remote databases are NULL"));
        }
    }
}
