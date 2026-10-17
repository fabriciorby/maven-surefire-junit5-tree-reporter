package org.apache.maven.plugin.surefire.report;

import org.apache.maven.plugin.surefire.log.api.ConsoleLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * Passes everything to another logger and keeps the lines printed with {@link #info(String)}, which is how the tree
 * is printed, so the tree of a class can be put in the GitHub job summary.
 */
class RecordingConsoleLogger implements ConsoleLogger {
    private final ConsoleLogger delegate;
    final List<String> lines = new ArrayList<>();

    RecordingConsoleLogger(ConsoleLogger delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean isDebugEnabled() {
        return delegate.isDebugEnabled();
    }

    @Override
    public void debug(String message) {
        delegate.debug(message);
    }

    @Override
    public boolean isInfoEnabled() {
        return delegate.isInfoEnabled();
    }

    @Override
    public void info(String message) {
        lines.add(message);
        delegate.info(message);
    }

    @Override
    public boolean isWarnEnabled() {
        return delegate.isWarnEnabled();
    }

    @Override
    public void warning(String message) {
        delegate.warning(message);
    }

    @Override
    public boolean isErrorEnabled() {
        return delegate.isErrorEnabled();
    }

    @Override
    public void error(String message) {
        delegate.error(message);
    }

    @Override
    public void error(String message, Throwable t) {
        delegate.error(message, t);
    }

    @Override
    public void error(Throwable t) {
        delegate.error(t);
    }
}
