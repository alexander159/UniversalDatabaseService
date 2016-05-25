package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class LoggingJUL {
    private static LoggingJUL instance;
    private Logger logger = init();

    private LoggingJUL() {
    }

    public static LoggingJUL getInstance() {
        if (instance == null) {
            instance = new LoggingJUL();
        }
        return instance;
    }

    private static Logger init() {
        try {
            //create logs directory
            File logsDir = new File(Constants.LOGS_FOLDER);
            if (!logsDir.exists()) {
                if (logsDir.mkdir()) {
                    System.out.println("Logs directory is created");
                } else {
                    System.out.println(Constants.LOGS_FOLDER + " Failed to create logs directory");
                    throw new IOException(Constants.LOGS_FOLDER + " Failed to create logs directory");
                }
            }

            //create log file 'dbservice-<current_timestamp>.log'
            File file = new File(Constants.LOGS_FOLDER + Constants.FILE_SEPARATOR + "dbservice-" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".log");
            if (file.createNewFile()) {
                System.out.println("Log file is created");
            } else {
                System.out.println("Log file already exists");
            }

            //create and init properties file for java.util.logging.Logger
            List<String> properties = Arrays.asList("# Setting the global logger",
                    "handlers = java.util.logging.FileHandler",
                    ".level = ALL",
                    "# File handler configuration",
                    "java.util.logging.FileHandler.level = ALL",
                    "java.util.logging.FileHandler.formatter = java.util.logging.SimpleFormatter",
                    "java.util.logging.FileHandler.pattern = " + file.getAbsolutePath().replace(Constants.FILE_SEPARATOR, Constants.FILE_SEPARATOR + Constants.FILE_SEPARATOR));
            Path loggingPropFile = Paths.get(Constants.LOGGING_PROPERTIES);
            Files.write(loggingPropFile, properties, Charset.forName("UTF-8"));

            //apply logger properties
            LogManager.getLogManager().readConfiguration(new FileInputStream(Constants.LOGGING_PROPERTIES));

            Logger.getLogger("javax.management").setLevel(Level.WARNING);
            Logger.getLogger("javax.management.remote").setLevel(Level.WARNING);
            Logger.getLogger("mysql").setLevel(Level.WARNING);
            Logger.getLogger("com.oracle").setLevel(Level.WARNING);
            Logger.getLogger("com.microsoft.sqlserver").setLevel(Level.WARNING);
            Logger.getLogger("com.ibm.db2").setLevel(Level.WARNING);

            //init logger after applying properties
            return Logger.getLogger(LoggingJUL.class.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Logger getLogger() {
        return logger;
    }

    public static String convertPlaceholdersListToStr(ArrayList<String> placeholdersList) {
        String result = "";

        for (int i = 0; i < placeholdersList.size(); i++) {
            if (i != placeholdersList.size() - 1) {
                result += String.format("%s, ", placeholdersList.get(i));
            } else {
                result += String.format("%s", placeholdersList.get(i));
            }
        }

        return result;
    }

    /**
     * Used to fix .replaceFirst() bug
     *
     * @return string like placeholderText_placeholderStartId, placeholderText_placeholderStartId+1, placeholderText_placeholderStartId+2, etc.
     */
    public static ArrayList<String> createTemporaryPlaceholdersArray(int columnsCount, String placeholderText, int placeholderStartId) {
        ArrayList<String> result = new ArrayList<>();

        for (int i = 0; i < columnsCount; i++) {
            result.add(String.format("%s_%s", placeholderText, placeholderStartId++));
        }

        return result;
    }

    public static String getInsertLogSql(String insertStatement, String tableName, String[] columns, ArrayList<String> columnPlaceholders) {
        String columnsLogSql = "";
        String columnsLogPlaceholderValues = "";
        for (int i = 0; i < columns.length; i++) {
            if (i != columns.length - 1) {
                columnsLogSql += columns[i] + ", ";
                columnsLogPlaceholderValues += columnPlaceholders + ", ";
            } else {
                columnsLogSql += columns[i];
                columnsLogPlaceholderValues += columnPlaceholders;
            }
        }

        return String.format("INSERT INTO %s (%s) VALUES (%s)",
                tableName,
                columnsLogSql,
                columnsLogPlaceholderValues);
    }

    public static String getUpdateLogSql(String updateStatement, String tableName, String[] columns, String whereColumnName, ArrayList<String> columnPlaceholders) {
        String columnsLogSql = "";
        for (int i = 0; i < columns.length; i++) {
            if (i != columns.length - 1) {
                columnsLogSql += String.format("%s = %s, ", columns[i], columnPlaceholders.get(i));
            } else {
                columnsLogSql += String.format("%s = %s", columns[i], columnPlaceholders.get(i));
            }
        }

        return String.format("UPDATE %s SET %s WHERE %s = %s;",
                tableName,
                columnsLogSql,
                whereColumnName,
                columnPlaceholders.get(columnPlaceholders.size() - 1));
    }

}
