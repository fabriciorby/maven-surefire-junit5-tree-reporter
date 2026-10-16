package org.apache.maven.plugin.surefire.report;

import org.apache.maven.plugin.surefire.log.api.ConsoleLogger;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.apache.maven.surefire.shared.utils.logging.MessageUtils.buffer;

/**
 * What the reporter remembers from the classes of one surefire (or failsafe) execution to print the failure summary
 * when the execution ends. Only strings and counters are kept, never the report entries, so the stack traces and the
 * output of the tests can be freed as soon as a class is printed.
 * <p>
 * The state is static, like the tree: with forkCount above 1 every fork has its own reporter. It is filled by
 * {@link #record} when a class is printed, and printed and cleared by {@link #printAndClear()}, which
 * {@link org.apache.maven.plugin.surefire.extensions.junit5.JUnit5ConsoleOutputTreeReporter} calls when surefire closes
 * the execution. Surefire closes one listener per fork, one after the other, so only the first one finds something to
 * print, and the state is empty for the next execution of the build.
 */
public final class FailureSummary {
    private static final Object LOCK = new Object();
    private static State state;

    private FailureSummary() {
    }

    static void record(Node node, ReporterOptions options, ConsoleLogger logger) {
        synchronized (LOCK) {
            if (state == null) {
                state = new State();
            }
            state.record(node, options, logger);
        }
    }

    /**
     * Prints the summary (see the {@code printFailureSummary} option) and forgets everything recorded.
     */
    public static void printAndClear() {
        State recorded;
        synchronized (LOCK) {
            recorded = state;
            state = null;
        }
        if (recorded == null) {
            return;
        }
        if (recorded.options.isPrintFailureSummary() && recorded.failureCount > 0) {
            recorded.printFailures();
        }
    }

    /** Whether something is waiting to be printed, for the tests. */
    static boolean isEmpty() {
        synchronized (LOCK) {
            return state == null;
        }
    }

    private static final class State {
        private ReporterOptions options;
        private ConsoleLogger logger;
        /** The failed tests, at most maxFailuresInSummary of them. */
        private final List<String> failures = new ArrayList<>();
        private int failureCount;

        void record(Node node, ReporterOptions options, ConsoleLogger logger) {
            this.options = options;
            this.logger = logger;
            int max = Math.max(0, options.getMaxFailuresInSummary());
            collect(node, max);
        }

        private void collect(Node node, int max) {
            for (WrappedReportEntry entry : node.wrappedReportEntries) {
                if (entry.isErrorOrFailure()) {
                    failureCount++;
                    if (failures.size() < max) {
                        failures.add(path(node, entry));
                    }
                }
            }
            for (Node branch : node.branches) {
                collect(branch, max);
            }
        }

        /** The display names from the class down to the test, like in the tree. */
        private static String path(Node node, WrappedReportEntry entry) {
            LinkedList<String> names = new LinkedList<>();
            names.addFirst(ActualTreePrinter.getTestName(entry));
            for (Node n = node; n != null && n.getClassReportEntry() != null; n = n.getParent()) {
                names.addFirst(ActualTreePrinter.cleanReportName(n));
            }
            return String.join(" > ", names);
        }

        void printFailures() {
            // surefire prints an empty line after the totals
            logger.info(buffer().failure("Failed tests (" + failureCount + "):").toString());
            for (String failure : failures) {
                logger.info(buffer().failure("  " + failure).toString());
            }
            if (failureCount > failures.size()) {
                logger.info(buffer().failure("  ... and " + (failureCount - failures.size()) + " more").toString());
            }
            logger.info("");
        }
    }
}
