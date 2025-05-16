package server.utility;

import java.io.PrintWriter;
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
    public static void startServerLogger() {}

    /**
     * Creates a PrintWriter object
     */
    private static void createPrintWriter() {}

    /**
     * Updates the filename if different and creates a new PrintWriter object
     */
    private static void updateFilename() {}

    /**
     * Prints information to the console as well as logs in a text file
     * @param args arguments to be printed and logged
     */
    public synchronized static void log(Object... args) {}

    /**
     * Prints information to the console as an error as well as logs in a text file
     * @param args arguments to be printed and logged
     */
    public synchronized static void logError(Object... args) {}

    /**
     * Closes the PrintWriter
     */
    public synchronized static void closeServerLogger() {}

    /**
     * Get the DIR, used for testing purposes
     * @return the directory that the logs will be in
     */
    public static String getDIR() {
        return DIR;
    }
}
