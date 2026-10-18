package org.apache.maven.plugin.surefire.report;

import org.apache.maven.plugin.surefire.log.api.ConsoleLogger;
import org.apache.maven.surefire.api.report.SafeThrowable;
import org.apache.maven.surefire.api.report.StackTraceWriter;
import org.apache.maven.surefire.shared.lang3.StringUtils;
import org.apache.maven.surefire.shared.utils.logging.MessageBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.maven.plugin.surefire.report.TextFormatter.abbreviateName;
import static org.apache.maven.surefire.shared.utils.StringUtils.isBlank;
import static org.apache.maven.surefire.shared.utils.logging.MessageUtils.buffer;

public class ActualTreePrinter {
    private final Theme theme;
    private final Node tree;
    private final ConsoleLogger consoleLogger;
    private final ReporterOptions options;

    public ActualTreePrinter(Node node, ConsoleLogger consoleLogger, ReporterOptions options) {
        this.tree = node;
        this.consoleLogger = consoleLogger;
        this.options = options;
        this.theme = options.getTheme();
    }

    public void printAndRemoveChild() {
        if (options.isPrintBlankLineBetweenTests()) println("");
        print();
        removeChild();
    }

    public void print() {
        print(new TreeView(options).of(tree));
    }

    public void removeChild() {
        tree.getParent().branches.remove(tree);
    }

    private void print(Node node) {
        printClass(node);
        printGroupedTests(node);
        node.branches.forEach(this::print);
    }

    private void printTestFormated(MessageBuilder prefix, String name, WrappedReportEntry testResult) {
        if (testResult.isErrorOrFailure()) {
            printFailure(prefix, name, testResult);
        } else if (testResult.isSkipped()) {
            printSkipped(prefix, name, testResult);
        } else {
            printSuccess(prefix, name, testResult);
        }
    }

    private boolean isVisible(WrappedReportEntry testResult) {
        return testResult.isErrorOrFailure()
                || testResult.isSkipped()
                || isSuccessPrintAllowed() && testResult.isSucceeded();
    }

    /**
     * Prints the tests of a class with the invocations of the same method under a node with the name of the method.
     * The connectors are calculated on the items that are printed: a test, or a method with its invocations.
     */
    private void printGroupedTests(Node node) {
        List<Item> items = new ArrayList<>();
        Map<WrappedReportEntry, Item> itemOf = new IdentityHashMap<>();
        Map<String, Item> methods = new HashMap<>();
        for (WrappedReportEntry testResult : node.wrappedReportEntries) {
            if (!isVisible(testResult)) {
                continue;
            }
            String method = ParameterizedInvocations.methodOf(testResult);
            Item item = method == null ? null : methods.get(method);
            if (item == null) {
                item = new Item(method);
                items.add(item);
                if (method != null) methods.put(method, item);
            }
            item.testResults.add(testResult);
            itemOf.put(testResult, item);
        }
        Set<Item> printed = Collections.newSetFromMap(new IdentityHashMap<>());
        for (WrappedReportEntry testResult : node.wrappedReportEntries) {
            Item item = itemOf.get(testResult);
            if (item == null) {
                printDetails(testResult); // a result that is not printed still has its output
            } else if (item.method == null) {
                printTestFormated(getTestPrefix(node, !isLast(items, item)), getTestName(testResult), testResult);
                printDetails(testResult);
            } else if (printed.add(item)) {
                printMethod(node, item, !isLast(items, item));
            }
        }
    }

    private static boolean isLast(List<Item> items, Item item) {
        return items.get(items.size() - 1) == item;
    }

    private void printMethod(Node node, Item item, boolean notLast) {
        println(getTestIndent(node)
                .a(notLast ? theme.entry() : theme.end())
                .a(theme.down())
                .strong(abbreviateName(item.method))
                .a(" - " + ParameterizedInvocations.elapsedTimeAsString(item.testResults))
                .toString());
        for (int i = 0; i < item.testResults.size(); i++) {
            WrappedReportEntry testResult = item.testResults.get(i);
            MessageBuilder prefix = getTestIndent(node)
                    .a(notLast ? theme.pipe() : theme.blank())
                    .a(i < item.testResults.size() - 1 ? theme.entry() : theme.end());
            String name = abbreviateName(ParameterizedInvocations.labelOf(testResult, testResult.getReportName()));
            printTestFormated(prefix, name, testResult);
            printDetails(testResult);
        }
    }

    private static final class Item {
        private final String method;
        private final List<WrappedReportEntry> testResults = new ArrayList<>();

        private Item(String method) {
            this.method = method;
        }
    }

    private boolean isSuccessPrintAllowed() {
        return !options.isHideResultsOnSuccess();
    }

    private boolean isShown(WrappedReportEntry testResult) {
        return isSuccessPrintAllowed() || !testResult.isSucceeded();
    }

    private void printSuccess(MessageBuilder prefix, String name, WrappedReportEntry testResult) {
        printTestResult(buffer().success(theme.successful() + name), prefix, testResult);
    }

    private void printTestResult(MessageBuilder builder, MessageBuilder prefix, WrappedReportEntry testResult) {
        println(prefix
                .a(builder)
                .a(getSlowMarker(testResult))
                .a(" - " + testResult.elapsedTimeAsString())
                .toString());
    }

    private MessageBuilder getSlowMarker(WrappedReportEntry testResult) {
        MessageBuilder marker = buffer();
        long threshold = options.getSlowTestThresholdMillis();
        Integer elapsed = testResult.getElapsed();
        if (threshold != ReporterOptions.SLOW_TEST_THRESHOLD_DISABLED && !testResult.isSkipped()
                && elapsed != null && elapsed >= threshold) {
            marker.warning(" " + theme.slow());
        }
        return marker;
    }

    private void println(String message) {
        consoleLogger.info(message);
    }

    /**
     * @return whether the node is the last branch of its parent. A top-level class has no siblings that matter here:
     * with parallel test classes ROOT also holds the classes still running, in any order.
     */
    private static boolean isLastBranch(Node node) {
        List<Node> siblings = node.getParent().branches;
        return siblings.get(siblings.size() - 1) == node;
    }

    /**
     * The columns of the nested classes the node is in, from the second level down to the node itself if asked. Each
     * is the line going down along that class while it has siblings still to be printed.
     */
    private String getBranchColumns(Node node, boolean includeNode) {
        if (node.getDepth() <= 1) return "";
        String columns = getBranchColumns(node.getParent(), true);
        return includeNode ? columns + (isLastBranch(node) ? theme.blank() : theme.pipe()) : columns;
    }

    private MessageBuilder getTestPrefix(Node node, boolean notLast) {
        return getTestIndent(node).a(notLast ? theme.entry() : theme.end());
    }

    private MessageBuilder getTestIndent(Node node) {
        MessageBuilder builder = buffer();
        builder.a(getBranchColumns(node, true));
        builder.a(node.hasBranches() ? theme.pipe() : theme.blank());
        return builder;
    }

    private void printClass(Node node) {
        MessageBuilder builder = buffer();
        if (node.getDepth() > 1) {
            builder.a(getBranchColumns(node, false));
            builder.a(isLastBranch(node) ? theme.end() : theme.entry());
        }
        if (node.hasBranches()) {
            builder.a(theme.down());
        } else {
            builder.a(theme.dash());
        }

        builder.strong(cleanReportName(node));
        if (node.counts != null && !node.counts.format().isEmpty()) {
            builder.a(" " + node.counts.format());
        }
        builder.a(" - " + node.getClassReportEntry().elapsedTimeAsString());

        println(builder.toString());
    }

    static String cleanReportName(Node node) {
        if (node.getParent().getClassReportEntry() != null &&
                node.getClassReportEntry().getReportNameWithGroup().startsWith(node.getParent().getClassReportEntry().getReportNameWithGroup())
        ) {
            int stringSizeToRemove = node.getParent().getClassReportEntry().getReportNameWithGroup().length() + 1;
            return node.getClassReportEntry().getReportNameWithGroup().substring(stringSizeToRemove);
        } else {
            return node.getClassReportEntry().getReportNameWithGroup();
        }
    }

    private void printDetails(WrappedReportEntry testResult) {
        boolean isSuccess = testResult.getReportEntryType() == ReportEntryType.SUCCESS;
        boolean isError = testResult.getReportEntryType() == ReportEntryType.ERROR;
        boolean isFailure = testResult.getReportEntryType() == ReportEntryType.FAILURE;

        boolean printStackTrace = options.isPrintStacktraceOnError() && isError
                || options.isPrintStacktraceOnFailure() && isFailure;
        boolean printStdOut = (options.isPrintStdoutOnSuccess() && isSuccess
                || options.isPrintStdoutOnError() && isError
                || options.isPrintStdoutOnFailure() && isFailure)
                && hasOutput(testResult.getStdout());
        boolean printStdErr = (options.isPrintStderrOnSuccess() && isSuccess
                || options.isPrintStderrOnError() && isError
                || options.isPrintStderrOnFailure() && isFailure)
                && hasOutput(testResult.getStdErr());

        if (printStackTrace || printStdOut || printStdErr) {
            printPreambleDetails(testResult);
            if (printStackTrace) printStackTrace(testResult);
            if (printStdOut) printStdOut(testResult);
            if (printStdErr) printStdErr(testResult);
        }
    }

    private static boolean hasOutput(Utf8RecodingDeferredFileOutputStream output) {
        return output != null && output.getByteCount() > 0;
    }

    private void printSkipped(MessageBuilder prefix, String name, WrappedReportEntry testResult) {
        printTestResult(buffer()
                .warning(theme.skipped() + name)
                .warning(getSkippedMessage(testResult)), prefix, testResult);
    }

    static String getTestName(WrappedReportEntry testResult) {
        if (!isBlank(testResult.getReportName())) {
            return abbreviateName(testResult.getReportName());
        }
        String reportSourceName = testResult.getReportSourceName();
        if (reportSourceName != null && reportSourceName.equals(SourceName.of(testResult))) {
            // A class that never started has no display name, from 3.6.0 on it comes as the binary class name,
            // so print the simple name as JUnit5 and maven-surefire-plugin 3.5.3 and older do.
            return reportSourceName.substring(Math.max(reportSourceName.lastIndexOf('$'), reportSourceName.lastIndexOf('.')) + 1);
        }
        return reportSourceName;
    }

    private String getSkippedMessage(WrappedReportEntry testResult) {
        String message = testResult.getMessage();
        if (isBlank(message)) {
            // An aborted test (failed assumption, TestAbortedException) is reported as skipped without a message,
            // the reason is only on its throwable.
            message = getThrowableMessage(testResult);
        }
        return isBlank(message) ? "" : " (" + message + ")";
    }

    private static String getThrowableMessage(WrappedReportEntry testResult) {
        StackTraceWriter stackTraceWriter = testResult.getStackTraceWriter();
        if (stackTraceWriter == null) {
            return null;
        }
        SafeThrowable throwable = stackTraceWriter.getThrowable();
        return throwable == null ? null : throwable.getLocalizedMessage();
    }

    private void printPreambleDetails(WrappedReportEntry testResult) {
        if (testResult.isSucceeded()) {
            println(buffer().success(theme.details()).success(getTestName(testResult)).toString());
        } else {
            println(buffer().failure(theme.details()).failure(getTestName(testResult)).toString());
        }
    }

    private void printStdOut(WrappedReportEntry testResult) {
        println("");
        println(buffer().strong("Standard out").toString());
        try {
            testResult.getStdout().writeTo(System.out);
        } catch (final IOException ignored) {
        }
    }

    private void printStdErr(WrappedReportEntry testResult) {
        println("");
        println(buffer().strong("Standard error").toString());
        try {
            testResult.getStdErr().writeTo(System.err);
        } catch (final IOException ignored) {
        }
    }

    private void printStackTrace(WrappedReportEntry testResult) {
        println("");
        println(buffer().strong("Stack trace").toString());
        String stackTrace = testResult.getStackTrace(false);
        if (stackTrace != null && !StringUtils.isBlank(stackTrace)) {
            println(testResult.getStackTrace(false));
        } else {
            println("[No stack trace available]");
        }
    }

    private void printFailure(MessageBuilder prefix, String name, WrappedReportEntry testResult) {
        printTestResult(buffer()
                .failure(theme.failed() + name), prefix, testResult);
    }
}
