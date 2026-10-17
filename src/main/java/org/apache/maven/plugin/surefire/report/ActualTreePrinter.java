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
import java.util.stream.LongStream;

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
        print(tree);
    }

    public void removeChild() {
        tree.getParent().branches.remove(tree);
    }

    private void print(Node node) {
        printClass(node);
        if (options.isGroupParameterizedTests()) {
            printGroupedTests(node);
        } else {
            node.wrappedReportEntries.forEach(i -> printTestFormated(node, i));
        }
        node.branches.forEach(this::print);
    }

    private void printTestFormated(Node node, WrappedReportEntry testResult) {
        if (isVisible(testResult)) {
            printTestFormated(getTestPrefix(node, testResult), getTestName(testResult), testResult);
        }
        printDetails(testResult);
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

    private void printSuccess(MessageBuilder prefix, String name, WrappedReportEntry testResult) {
        printTestResult(buffer().success(theme.successful() + name), prefix, testResult);
    }

    private void printTestResult(MessageBuilder builder, MessageBuilder prefix, WrappedReportEntry testResult) {
        println(prefix
                .a(builder)
                .a(" - " + testResult.elapsedTimeAsString())
                .toString());
    }

    private void println(String message) {
        consoleLogger.info(message);
    }

    private boolean isLastMissingBranch(Node node) {
        // The top-level class of this node, not ROOT's first branch: with parallel test classes ROOT also holds the
        // classes still running, in any order.
        Node rootChild = node.getTopLevelNode();
        if (rootChild.hasBranches()) {
            Node rootChildLastChild = getLastItem(rootChild.branches); // last branch in root child
            return node.getParent(rootChildLastChild.getName()).isPresent() || node == rootChildLastChild;
        } else {
            return true;
        }
    }

    private static <T> T getLastItem(List<T> list) {
        return list.get(list.size() - 1);
    }

    private MessageBuilder getTestPrefix(Node node, WrappedReportEntry testResult) {
        return getTestPrefix(node, isLastTestToBeEval(node, testResult));
    }

    private MessageBuilder getTestPrefix(Node node, boolean notLast) {
        return getTestIndent(node).a(notLast ? theme.entry() : theme.end());
    }

    private MessageBuilder getTestIndent(Node node) {
        MessageBuilder builder = buffer();
        if (isLastMissingBranch(node))
            builder.a(theme.blank());
        else
            builder.a(theme.pipe());
        if (node.getDepth() > 1) {
            LongStream.rangeClosed(0, node.getDepth() - 3)
                    .forEach(i -> builder.a(theme.blank()));
            if (node.getParent().hasBranches() && node.hasBranches()
            ) {
                builder.a(theme.pipe());
            } else {
                builder.a(theme.blank());
            }
        }
        return builder;
    }

    private static boolean isLastTestToBeEval(Node node, WrappedReportEntry testResult) {
        return node.wrappedReportEntries.indexOf(testResult) + 1 != node.wrappedReportEntries.size();
    }

    private void printClass(Node node) {
        MessageBuilder builder = buffer();
        if (node.getDepth() > 1) {
            if (node.getDepth() > 2) {
                if (isLastMissingBranch(node)) builder.a(theme.blank());
                else builder.a(theme.pipe());
                LongStream.rangeClosed(0, node.getDepth() - 4)
                        .forEach(i -> builder.a(theme.blank()));
                builder.a(theme.end());
            } else {
                if (isLastMissingBranch(node)) builder.a(theme.end());
                else builder.a(theme.entry());
            }
        }
        if (node.hasBranches()) {
            builder.a(theme.down());
        } else {
            builder.a(theme.dash());
        }

        builder.strong(cleanReportName(node));
        builder.a(" - " + node.getClassReportEntry().elapsedTimeAsString());

        println(builder.toString());
    }

    private String cleanReportName(Node node) {
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

    private String getTestName(WrappedReportEntry testResult) {
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
