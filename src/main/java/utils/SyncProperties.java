package utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class SyncProperties {
    private static SyncProperties instance;
    private static Properties syncProp;

    private SyncProperties() {
    }

    public static SyncProperties getInstance() {
        if (instance == null) {
            instance = new SyncProperties();
        }
        return instance;
    }

    public static Properties getSyncProp() {
        if (syncProp == null) {
            syncProp = loadSyncProp();
        }
        return syncProp;
    }

    private static Properties loadSyncProp() {
        Properties prop = new Properties();

        try (InputStream input = new FileInputStream(Constants.SYNC_PROPERTIES)) {
            prop.load(input);
            //check the properties value
            if (prop.getProperty(Constants.SyncPropFile.REMOTE_DB_HOST) == null || prop.getProperty(Constants.SyncPropFile.REMOTE_DB_HOST).isEmpty() ||
                    prop.getProperty(Constants.SyncPropFile.REMOTE_DB_PORT) == null || prop.getProperty(Constants.SyncPropFile.REMOTE_DB_PORT).isEmpty() ||
                    prop.getProperty(Constants.SyncPropFile.REMOTE_DB_USER) == null || prop.getProperty(Constants.SyncPropFile.REMOTE_DB_USER).isEmpty() ||
                    prop.getProperty(Constants.SyncPropFile.REMOTE_DB_PASSWORD) == null || prop.getProperty(Constants.SyncPropFile.REMOTE_DB_PASSWORD).isEmpty() ||
                    prop.getProperty(Constants.SyncPropFile.REMOTE_DB_NAME) == null || prop.getProperty(Constants.SyncPropFile.REMOTE_DB_NAME).isEmpty() ||
                    prop.getProperty(Constants.SyncPropFile.REMOTE_DB_TABLE) == null || prop.getProperty(Constants.SyncPropFile.REMOTE_DB_TABLE).isEmpty() ||
                    prop.getProperty(Constants.SyncPropFile.REMOTE_DB_TYPE) == null || prop.getProperty(Constants.SyncPropFile.REMOTE_DB_TYPE).isEmpty() ||
                    prop.getProperty(Constants.SyncPropFile.LOCAL_DB_HOST) == null || prop.getProperty(Constants.SyncPropFile.LOCAL_DB_HOST).isEmpty() ||
                    prop.getProperty(Constants.SyncPropFile.LOCAL_DB_PORT) == null || prop.getProperty(Constants.SyncPropFile.LOCAL_DB_PORT).isEmpty() ||
                    prop.getProperty(Constants.SyncPropFile.LOCAL_DB_USER) == null || prop.getProperty(Constants.SyncPropFile.LOCAL_DB_USER).isEmpty() ||
                    prop.getProperty(Constants.SyncPropFile.LOCAL_DB_PASSWORD) == null || prop.getProperty(Constants.SyncPropFile.LOCAL_DB_PASSWORD).isEmpty() ||
                    prop.getProperty(Constants.SyncPropFile.LOCAL_DB_NAME) == null || prop.getProperty(Constants.SyncPropFile.LOCAL_DB_NAME).isEmpty() ||
                    prop.getProperty(Constants.SyncPropFile.LOCAL_DB_TABLE) == null || prop.getProperty(Constants.SyncPropFile.LOCAL_DB_TABLE).isEmpty() ||
                    prop.getProperty(Constants.SyncPropFile.LOCAL_DB_TYPE) == null || prop.getProperty(Constants.SyncPropFile.LOCAL_DB_TYPE).isEmpty() ||
                    prop.getProperty(Constants.SyncPropFile.LOCAL_DB_TABLE_COLUMNS) == null || prop.getProperty(Constants.SyncPropFile.LOCAL_DB_TABLE_COLUMNS).isEmpty() ||
                    prop.getProperty(Constants.SyncPropFile.SYNC_MINUTES) == null || prop.getProperty(Constants.SyncPropFile.SYNC_MINUTES).isEmpty()) {
                LoggingJUL.getLogger().throwing(SyncProperties.class.getName(), new Object() {
                }.getClass().getEnclosingMethod().getName(), new IOException(Constants.SYNC_PROPERTIES + " is incorrect"));
                return null;
            }
        } catch (IOException e) {
            LoggingJUL.getLogger().throwing(SyncProperties.class.getName(), new Object() {
            }.getClass().getEnclosingMethod().getName(), e);
            e.printStackTrace();
            return null;
        }
        return prop;
    }

    public static Properties reloadProperties() {
        syncProp = loadSyncProp();
        return syncProp;
    }
}
