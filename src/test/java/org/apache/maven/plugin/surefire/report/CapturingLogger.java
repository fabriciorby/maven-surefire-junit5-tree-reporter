package org.apache.maven.plugin.surefire.report;

import org.apache.maven.plugin.surefire.log.api.ConsoleLogger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Keeps every line the reporter prints, so tests can assert on the tree. Reporters on several threads can share it.
 */
class CapturingLogger implements ConsoleLogger {
    final List<String> lines = Collections.synchronizedList(new ArrayList<>());

    @Override
    public boolean isDebugEnabled() {
        return false;
    }

    @Override
    public void debug(String message) {
    }

    @Override
    public boolean isInfoEnabled() {
        return true;
    }

    @Override
    public void info(String message) {
        lines.add(message);
    }

    @Override
    public boolean isWarnEnabled() {
        return true;
    }

    @Override
    public void warning(String message) {
        lines.add(message);
    }

    @Override
    public boolean isErrorEnabled() {
        return true;
    }

    @Override
    public void error(String message) {
        lines.add(message);
    }

    @Override
    public void error(String message, Throwable t) {
        lines.add(message);
    }

    @Override
    public void error(Throwable t) {
        lines.add(String.valueOf(t));
    }
}
