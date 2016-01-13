package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class LoggingJUL {
    private static LoggingJUL instance;
    private static Logger logger;

    private LoggingJUL() {
    }

    public static LoggingJUL getInstance() {
        if (instance == null) {
            instance = new LoggingJUL();
        }
        return instance;
    }

    private static void init() {
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

            //init logger
            logger = Logger.getLogger(LoggingJUL.class.getName());

            Logger.getLogger("javax.management").setLevel(Level.WARNING);
            Logger.getLogger("javax.management.remote").setLevel(Level.WARNING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Logger getLogger() {
        if (logger == null) {
            init();
        }
        return logger;
    }
}
