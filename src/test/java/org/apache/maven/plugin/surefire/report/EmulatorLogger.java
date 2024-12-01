package org.apache.maven.plugin.surefire.report;

import org.codehaus.plexus.logging.AbstractLogger;
import org.codehaus.plexus.logging.Logger;

import java.util.ArrayList;
import java.util.List;

// Copy from org.codehaus.plexus.logging.console.ConsoleLogger so we can extend it

public class EmulatorLogger extends AbstractLogger {
    private static final String[] TAGS = {"[DEBUG] ", "[INFO] ", "[WARNING] ", "[ERROR] ", "[FATAL ERROR] "};
    private final ArrayList<String> logs = new ArrayList<>();

    public EmulatorLogger() {
        this(Logger.LEVEL_INFO, "console");
    }

    public EmulatorLogger(int threshold, String name) {
        super(threshold, name);
    }

    public void debug(final String message, final Throwable throwable) {
        if (isDebugEnabled()) log(LEVEL_DEBUG, message, throwable);
    }

    public void info(final String message, final Throwable throwable) {
        if (isInfoEnabled()) log(LEVEL_INFO, message, throwable);
    }

    public void warn(final String message, final Throwable throwable) {
        if (isWarnEnabled()) log(LEVEL_WARN, message, throwable);
    }

    public void error(final String message, final Throwable throwable) {
        if (isErrorEnabled()) log(LEVEL_ERROR, message, throwable);
    }

    public void fatalError(final String message, final Throwable throwable) {
        if (isFatalErrorEnabled()) log(LEVEL_FATAL, message, throwable);
    }

    public Logger getChildLogger(final String name) {
        return this;
    }

    public void clearLogs() {
        System.out.println("Cleaning logs...");
        logs.clear();
    }

    public List<String> getLogList() {
       return logs;
    }

    private void log(final int level, final String message, final Throwable throwable) {
        logs.add(message);
//        System.out.println(TAGS[level].concat(message));
        System.out.println(message);
        if (throwable != null) {
            throwable.printStackTrace(System.out);
        }
    }
}
