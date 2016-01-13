package utils;

public class Constants {
    public static final String HOME_DIR = System.getProperty("user.home");
    public static final String FILE_SEPARATOR = System.getProperty("file.separator");
    public static final String CONFIG_FOLDER = HOME_DIR + FILE_SEPARATOR + "UniversalDatabaseService";
    public static final String SYNC_PROPERTIES = Constants.CONFIG_FOLDER + Constants.FILE_SEPARATOR + "sync.properties";
    public static final String LOGGING_PROPERTIES = Constants.CONFIG_FOLDER + Constants.FILE_SEPARATOR + "logging.properties";
    public static final String LOGS_FOLDER = Constants.CONFIG_FOLDER + Constants.FILE_SEPARATOR + "Logs";

    public static class SyncPropFile {
        public static final String REMOTE_DB_HOST = "remote_db_host";
        public static final String REMOTE_DB_PORT = "remote_db_port";
        public static final String REMOTE_DB_USER = "remote_db_user";
        public static final String REMOTE_DB_PASSWORD = "remote_db_password";
        public static final String REMOTE_DB_NAME = "remote_db_name";
        public static final String REMOTE_DB_TABLE = "remote_db_table";
        public static final String REMOTE_DB_TYPE = "remote_db_type";
        public static final String LOCAL_DB_HOST = "local_db_host";
        public static final String LOCAL_DB_PORT = "local_db_port";
        public static final String LOCAL_DB_USER = "local_db_user";
        public static final String LOCAL_DB_PASSWORD = "local_db_password";
        public static final String LOCAL_DB_NAME = "local_db_name";
        public static final String LOCAL_DB_TABLE = "local_db_table";
        public static final String LOCAL_DB_TYPE = "local_db_type";
        public static final String LOCAL_DB_TABLE_COLUMNS = "local_db_table_columns";

        public static final String MYSQL = "mysql";
        public static final String ORACLE = "oracle";
        public static final String MSSQL = "mssql";
        public static final String DB2 = "db2";
    }
}
