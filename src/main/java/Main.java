import database.*;
import utils.Constants;
import utils.LoggingJUL;
import utils.SyncProperties;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Main {
    public static void main(String[] a) {
        LoggingJUL.getLogger().info(() -> "Service started");

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (SyncProperties.reloadProperties() != null) {
                    synchronize();
                }
            }
        }, 0, 1 * 60 * 1000);
    }

    public static void synchronize() {
        Database remoteDb = null;
        if (SyncProperties.getSyncProp().getProperty(Constants.SyncPropFile.REMOTE_DB_TYPE).equals(Constants.SyncPropFile.MYSQL)) {
            remoteDb = new MySQLDatabase(Database.DatabaseType.REMOTE);
        } else if (SyncProperties.getSyncProp().getProperty(Constants.SyncPropFile.REMOTE_DB_TYPE).equals(Constants.SyncPropFile.ORACLE)) {
            remoteDb = new OracleDatabase(Database.DatabaseType.REMOTE);
        } else if (SyncProperties.getSyncProp().getProperty(Constants.SyncPropFile.REMOTE_DB_TYPE).equals(Constants.SyncPropFile.MSSQL)) {
            remoteDb = new MSSQLDatabase(Database.DatabaseType.REMOTE);
        } else if (SyncProperties.getSyncProp().getProperty(Constants.SyncPropFile.REMOTE_DB_TYPE).equals(Constants.SyncPropFile.DB2)) {
            remoteDb = new DB2Database(Database.DatabaseType.REMOTE);
        }

        Database localDb = null;
        if (SyncProperties.getSyncProp().getProperty(Constants.SyncPropFile.LOCAL_DB_TYPE).equals(Constants.SyncPropFile.MYSQL)) {
            localDb = new MySQLDatabase(Database.DatabaseType.LOCAL);
        } else if (SyncProperties.getSyncProp().getProperty(Constants.SyncPropFile.LOCAL_DB_TYPE).equals(Constants.SyncPropFile.ORACLE)) {
            localDb = new OracleDatabase(Database.DatabaseType.LOCAL);
        } else if (SyncProperties.getSyncProp().getProperty(Constants.SyncPropFile.LOCAL_DB_TYPE).equals(Constants.SyncPropFile.MSSQL)) {
            localDb = new MSSQLDatabase(Database.DatabaseType.LOCAL);
        } else if (SyncProperties.getSyncProp().getProperty(Constants.SyncPropFile.LOCAL_DB_TYPE).equals(Constants.SyncPropFile.DB2)) {
            localDb = new DB2Database(Database.DatabaseType.LOCAL);
        }

        //sync
        if (localDb != null && remoteDb != null) {
            List<DatabaseData> localData = localDb.select();
            LoggingJUL.getLogger().info("Local " + localDb.getClass().getName() + " SELECT. Returned " + localData.size() + " rows");
            List<DatabaseData> remoteData = remoteDb.select();
            LoggingJUL.getLogger().info("Remote " + remoteDb.getClass().getName() + " SELECT. Returned " + remoteData.size() + " rows");

            localData.removeAll(remoteData);
            LoggingJUL.getLogger().info(localData.size() + " new records found in local " + localDb.getClass().getName());
            if (localData.size() != 0) {
                remoteDb.insert(localData);
                LoggingJUL.getLogger().info(localData.size() + " new records inserted to remote" + remoteDb.getClass().getName());
            }
        } else {
            LoggingJUL.getLogger().throwing(Main.class.getName(), new Object() {
            }.getClass().getEnclosingMethod().getName(), new NullPointerException("Local or Remote databases are NULL"));
        }
    }
}
