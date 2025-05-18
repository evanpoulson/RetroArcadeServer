package server.utility;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ServerLogger {
    private static final String DIR = "logs";
    private static PrintWriter writer;
    private static String filename = "logs_" +
            ZonedDateTime.now(java.time.ZoneId.systemDefault()).format(DateTimeFormatter.ISO_LOCAL_DATE);

    /**
     * Starts the ServerLogger
     */
    public static void startServerLogger() {
        updateFilename();
        createPrintWriter();
    }

    /**
     * Creates a PrintWriter object
     */
    private static void createPrintWriter() {
        try {
            // Make sure the log file has the necessary directory
            Files.createDirectories(Paths.get(DIR));
            // Create the writer object
            writer = new PrintWriter(new FileWriter(DIR + "\\" + filename, true));
            // Ensures the log file is saved properly if the server crashes or closed incorrectly
            Runtime.getRuntime().addShutdownHook(new Thread(ServerLogger::closeServerLogger));
        } catch (IOException e) {
            // Could be more robust?
            System.err.println("!!! CRITICAL ERROR: ServerLogger failed to start. Logging disabled. !!!");
        }
    }

    /**
     * Updates the filename if different and creates a new PrintWriter object
     */
    private static void updateFilename() {
        // Get the new filename
        String updatedFilename = "logs_" + ZonedDateTime.now(java.time.ZoneId.systemDefault()).format(DateTimeFormatter.ISO_LOCAL_DATE);
        // If the filenames do not match, update the filename, close the old log file and create a new one
        if (!filename.equals(updatedFilename)) {
            filename = updatedFilename;
            closeServerLogger();
            createPrintWriter();
        }
    }

    /**
     * Base command, prints information to the console as well as logs in a text file
     * @param level the severity or importance of the message
     * @param args arguments to be printed and logged
     */
    public synchronized static void log(LogLevel level, Object... args) {
        // Convert the Object arguments into Strings
        String[] stringArgs = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            stringArgs[i] = args[i].toString();
        }

        // Combine the arguments ino a String and add a timestamp and log level
        String timestamp = ZonedDateTime.now(java.time.ZoneId.systemDefault())
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String levelPadded = String.format("%-5s", level.name());
        String argsStr = String.join(" ", stringArgs);
        String formattedStr = level == LogLevel.ERROR ? ">>>>" + argsStr + "<<<<" : argsStr;
        String string = "[" + timestamp + "] [" + levelPadded + "] " + formattedStr;

        // Print the output to the console
        if (level == LogLevel.ERROR) {
            System.err.println(string);
        } else {
            System.out.println(string);
        }

        // Log the information in to a text file
        updateFilename();
        if (writer != null) {
            writer.println(string);
            // Ensure immediate writing
            writer.flush();
        }
    }

    /**
     * Prints information to the console as a regular message as well as logs in a text file
     * @param args arguments to be printed and logged
     */
    public synchronized static void logInfo(Object... args) {
        log(LogLevel.INFO, args);
    }

    /**
     * Prints information to the console as an error as well as logs in a text file
     * @param args arguments to be printed and logged
     */
    public synchronized static void logError(Object... args) {
        log(LogLevel.ERROR, args);
    }

    /**
     * Closes the PrintWriter
     */
    public synchronized static void closeServerLogger() {
        // Check that the writer is not null
        if (writer != null) {
            writer.close();
            writer = null;
        }
    }

    /**
     * Get the DIR, used for testing purposes
     * @return the directory that the logs will be in
     */
    public static String getDIR() {
        return DIR;
    }
}
