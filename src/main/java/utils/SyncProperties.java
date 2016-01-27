package utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Properties;

public class SyncProperties {
    private static SyncProperties instance;
    private Properties syncProp = loadSyncProp();

    private LinkedList<String> parsedLocalTableNames;
    private LinkedList<String> parsedRemoteTableNames;
    private LinkedList<LinkedList<String>> parsedLocalColumnNames;
    private LinkedList<LinkedList<String>> parsedRemoteColumnNames;

    private SyncProperties() {
    }

    public static SyncProperties getInstance() {
        if (instance == null) {
            instance = new SyncProperties();
        }
        return instance;
    }

    private Properties loadSyncProp() {
        Properties prop = new Properties();

        try (InputStream input = new FileInputStream(Constants.SYNC_PROPERTIES)) {
            prop.load(input);
            //check the properties value
            if (prop.getProperty(Constants.SyncPropFile.REMOTE_DB_HOST) == null || prop.getProperty(Constants.SyncPropFile.REMOTE_DB_HOST).isEmpty() ||
                    prop.getProperty(Constants.SyncPropFile.REMOTE_DB_PORT) == null || prop.getProperty(Constants.SyncPropFile.REMOTE_DB_PORT).isEmpty() ||
                    prop.getProperty(Constants.SyncPropFile.REMOTE_DB_USER) == null || prop.getProperty(Constants.SyncPropFile.REMOTE_DB_USER).isEmpty() ||
                    prop.getProperty(Constants.SyncPropFile.REMOTE_DB_PASSWORD) == null || prop.getProperty(Constants.SyncPropFile.REMOTE_DB_PASSWORD).isEmpty() ||
                    prop.getProperty(Constants.SyncPropFile.REMOTE_DB_TYPE) == null || prop.getProperty(Constants.SyncPropFile.REMOTE_DB_TYPE).isEmpty() ||
                    prop.getProperty(Constants.SyncPropFile.REMOTE_DB_NAME) == null || prop.getProperty(Constants.SyncPropFile.REMOTE_DB_NAME).isEmpty() ||
                    prop.getProperty(Constants.SyncPropFile.REMOTE_DB_TABLE) == null || prop.getProperty(Constants.SyncPropFile.REMOTE_DB_TABLE).isEmpty() ||
                    prop.getProperty(Constants.SyncPropFile.REMOTE_DB_TABLE_COLUMNS) == null || prop.getProperty(Constants.SyncPropFile.REMOTE_DB_TABLE_COLUMNS).isEmpty() ||
                    prop.getProperty(Constants.SyncPropFile.LOCAL_DB_HOST) == null || prop.getProperty(Constants.SyncPropFile.LOCAL_DB_HOST).isEmpty() ||
                    prop.getProperty(Constants.SyncPropFile.LOCAL_DB_PORT) == null || prop.getProperty(Constants.SyncPropFile.LOCAL_DB_PORT).isEmpty() ||
                    prop.getProperty(Constants.SyncPropFile.LOCAL_DB_USER) == null || prop.getProperty(Constants.SyncPropFile.LOCAL_DB_USER).isEmpty() ||
                    prop.getProperty(Constants.SyncPropFile.LOCAL_DB_PASSWORD) == null || prop.getProperty(Constants.SyncPropFile.LOCAL_DB_PASSWORD).isEmpty() ||
                    prop.getProperty(Constants.SyncPropFile.LOCAL_DB_TYPE) == null || prop.getProperty(Constants.SyncPropFile.LOCAL_DB_TYPE).isEmpty() ||
                    prop.getProperty(Constants.SyncPropFile.LOCAL_DB_NAME) == null || prop.getProperty(Constants.SyncPropFile.LOCAL_DB_NAME).isEmpty() ||
                    prop.getProperty(Constants.SyncPropFile.LOCAL_DB_TABLE) == null || prop.getProperty(Constants.SyncPropFile.LOCAL_DB_TABLE).isEmpty() ||
                    prop.getProperty(Constants.SyncPropFile.LOCAL_DB_TABLE_COLUMNS) == null || prop.getProperty(Constants.SyncPropFile.LOCAL_DB_TABLE_COLUMNS).isEmpty() ||
                    prop.getProperty(Constants.SyncPropFile.SYNC_MINUTES) == null || prop.getProperty(Constants.SyncPropFile.SYNC_MINUTES).isEmpty() ||
                    prop.getProperty(Constants.SyncPropFile.FULL_SYNC) == null || prop.getProperty(Constants.SyncPropFile.FULL_SYNC).isEmpty() ||
                    !parseSyncProp(prop)) {
                LoggingJUL.getInstance().getLogger().throwing(SyncProperties.class.getName(), new Object() {
                }.getClass().getEnclosingMethod().getName(), new IOException(Constants.SYNC_PROPERTIES + " is incorrect"));
                return null;
            }
        } catch (IOException e) {
            LoggingJUL.getInstance().getLogger().throwing(SyncProperties.class.getName(), new Object() {
            }.getClass().getEnclosingMethod().getName(), e);
            e.printStackTrace();
            return null;
        }
        return prop;
    }

    private boolean parseSyncProp(Properties prop) throws NumberFormatException {
        //# local_db_table = [table1, table2, table3, table4]
        //# local_db_table_columns = [column1Name, column2Name, column3Name][column1Name, column2Name][column1Name, column2Name][column1Name, column2Name, column3Name]

        Integer.parseInt(prop.getProperty(Constants.SyncPropFile.SYNC_MINUTES));
        Integer.parseInt(prop.getProperty(Constants.SyncPropFile.FULL_SYNC));

        if (Constants.supportDbTypes.contains(prop.getProperty(Constants.SyncPropFile.LOCAL_DB_TYPE)) &&
                Constants.supportDbTypes.contains(prop.getProperty(Constants.SyncPropFile.REMOTE_DB_TYPE))) {

            parsedLocalTableNames = new LinkedList<>(Arrays.asList(getArrayFromPropValue(prop.getProperty(Constants.SyncPropFile.LOCAL_DB_TABLE))));
            parsedRemoteTableNames = new LinkedList<>(Arrays.asList(getArrayFromPropValue(prop.getProperty(Constants.SyncPropFile.REMOTE_DB_TABLE))));

            parsedLocalColumnNames = new LinkedList<>();
            String localColumnNames = prop.getProperty(Constants.SyncPropFile.LOCAL_DB_TABLE_COLUMNS);
            while (localColumnNames.contains("[") && localColumnNames.contains("]")) {
                parsedLocalColumnNames.add(new LinkedList<>(Arrays.asList(getArrayFromPropValue(localColumnNames.substring(localColumnNames.indexOf("["), localColumnNames.indexOf("]"))))));
                localColumnNames = localColumnNames.substring(localColumnNames.indexOf("]") + 1);
            }

            parsedRemoteColumnNames = new LinkedList<>();
            String remoteColumnNames = prop.getProperty(Constants.SyncPropFile.REMOTE_DB_TABLE_COLUMNS);
            while (remoteColumnNames.contains("[") && remoteColumnNames.contains("]")) {
                parsedRemoteColumnNames.add(new LinkedList<>(Arrays.asList(getArrayFromPropValue(remoteColumnNames.substring(remoteColumnNames.indexOf("["), remoteColumnNames.indexOf("]"))))));
                remoteColumnNames = remoteColumnNames.substring(remoteColumnNames.indexOf("]") + 1);
            }

            return !(parsedLocalTableNames.size() != parsedRemoteTableNames.size() || parsedLocalColumnNames.size() != parsedRemoteColumnNames.size());
        } else {
            return false;
        }
    }

    /**
     * @return parsed string array from input value like '[value1, value2, value3, value4]'
     */
    private String[] getArrayFromPropValue(String input) {
        return input.replaceAll("[ \\[\\]]", "")
                .split(",");
    }

    public Properties getSyncProp() {
        return syncProp;
    }

    public LinkedList<String> getParsedLocalTableNames() {
        return parsedLocalTableNames;
    }

    public LinkedList<String> getParsedRemoteTableNames() {
        return parsedRemoteTableNames;
    }

    public LinkedList<LinkedList<String>> getParsedLocalColumnNames() {
        return parsedLocalColumnNames;
    }

    public LinkedList<LinkedList<String>> getParsedRemoteColumnNames() {
        return parsedRemoteColumnNames;
    }

    public Properties reloadProperties() {
        syncProp = loadSyncProp();
        return syncProp;
    }
}
