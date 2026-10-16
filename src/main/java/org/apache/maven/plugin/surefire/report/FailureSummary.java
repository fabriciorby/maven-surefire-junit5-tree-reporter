package org.apache.maven.plugin.surefire.report;

import org.apache.maven.plugin.surefire.log.api.ConsoleLogger;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.apache.maven.surefire.shared.utils.logging.MessageUtils.buffer;

/**
 * What the reporter remembers from the classes of one surefire (or failsafe) execution to print the failure summary
 * and to write the GitHub job summary when the execution ends. Only strings and counters are kept, never the report
 * entries, so the stack traces and the output of the tests can be freed as soon as a class is printed.
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

    static void record(Node node, ReporterOptions options, ConsoleLogger logger, List<String> treeLines) {
        synchronized (LOCK) {
            if (state == null) {
                state = new State();
            }
            state.record(node, options, logger, treeLines);
        }
    }

    /**
     * Prints the summary (see the {@code printFailureSummary} option), appends to the file of the environment variable
     * {@code GITHUB_STEP_SUMMARY} (the {@code githubStepSummary} option) and forgets everything recorded.
     */
    public static void printAndClear() {
        printAndClear(System.getenv("GITHUB_STEP_SUMMARY"));
    }

    static void printAndClear(String githubStepSummaryFile) {
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
        if (recorded.options.isGithubStepSummary() && githubStepSummaryFile != null && !githubStepSummaryFile.isEmpty()) {
            recorded.appendMarkdown(githubStepSummaryFile);
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
        private int passed;
        private int failed;
        private int skipped;
        /** The failed tests, at most maxFailuresInSummary of them. */
        private final List<String> failures = new ArrayList<>();
        private int failureCount;
        /** The classes with a failure and their tree, at most maxFailuresInSummary of them. */
        private final List<FailedClass> failedClasses = new ArrayList<>();
        private int failedClassCount;
        private final List<String> passedClasses = new LinkedList<>();

        void record(Node node, ReporterOptions options, ConsoleLogger logger, List<String> treeLines) {
            this.options = options;
            this.logger = logger;
            int max = Math.max(0, options.getMaxFailuresInSummary());
            Counts counts = new Counts();
            collect(node, counts, max);
            passed += counts.passed;
            failed += counts.failed;
            skipped += counts.skipped;
            if (!options.isGithubStepSummary()) {
                return;
            }
            String className = ActualTreePrinter.cleanReportName(node);
            if (counts.failed == 0) {
                passedClasses.add(className);
            } else {
                failedClassCount++;
                if (failedClasses.size() < max) {
                    failedClasses.add(new FailedClass(className, counts.failed, treeLines));
                }
            }
        }

        private void collect(Node node, Counts counts, int max) {
            for (WrappedReportEntry entry : node.wrappedReportEntries) {
                if (entry.isErrorOrFailure()) {
                    counts.failed++;
                    failureCount++;
                    if (failures.size() < max) {
                        failures.add(path(node, entry));
                    }
                } else if (entry.isSkipped()) {
                    counts.skipped++;
                } else if (entry.isSucceeded()) {
                    counts.passed++;
                }
            }
            for (Node branch : node.branches) {
                collect(branch, counts, max);
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

        void appendMarkdown(String file) {
            StringBuilder md = new StringBuilder();
            md.append("### Test results\n\n");
            md.append(passed + failed + skipped).append(" tests: ")
                    .append(passed).append(" passed, ")
                    .append(failed).append(" failed, ")
                    .append(skipped).append(" skipped\n\n");
            for (FailedClass failedClass : failedClasses) {
                md.append("<details>\n<summary>").append(escapeHtml(failedClass.name)).append(": ")
                        .append(failedClass.failed).append(" failed</summary>\n\n```text\n");
                for (String line : failedClass.tree) {
                    md.append(stripAnsi(line).replaceAll("\\s+$", "")).append('\n');
                }
                md.append("```\n\n</details>\n\n");
            }
            if (failedClassCount > failedClasses.size()) {
                md.append("... and ").append(failedClassCount - failedClasses.size())
                        .append(" more classes with failures\n\n");
            }
            if (!passedClasses.isEmpty()) {
                md.append("Classes without failures: ");
                String separator = "";
                for (String passedClass : passedClasses) {
                    md.append(separator).append('`').append(passedClass).append('`');
                    separator = ", ";
                }
                md.append("\n\n");
            }
            // Append, never overwrite: other steps and other executions of this build write to the same file
            synchronized (LOCK) {
                try (Writer out = new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8)) {
                    out.write(md.toString());
                } catch (IOException e) {
                    logger.warning("Could not write the GitHub step summary to " + file + ": " + e);
                }
            }
        }

        private static String escapeHtml(String text) {
            return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        }

        private static String stripAnsi(String text) {
            return text.replaceAll("\u001B\\[[;\\d]*m", "");
        }
    }

    private static final class Counts {
        int passed;
        int failed;
        int skipped;
    }

    private static final class FailedClass {
        final String name;
        final int failed;
        final List<String> tree;

        FailedClass(String name, int failed, List<String> tree) {
            this.name = name;
            this.failed = failed;
            this.tree = new ArrayList<>(tree);
        }
    }
}
